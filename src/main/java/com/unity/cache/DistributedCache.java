package com.unity.cache;

import com.unity.cache.exceptions.InternalException;
import com.unity.cache.node.NodeManager;

import java.io.Serializable;
import java.util.Optional;

/**
 * DistributedCache is a distributed caching mechanism using a consistent hashing algorithm.
 * It uses a NodeManager to get the node where the key is stored.
 * Note:  DistributedCache should be initialized after all nodes are added to NodeManager.
 * Distributed node(s) is(are) invisible to the client. The client only interacts with the DistributedCache.
 * e.g.
 * NodeManager nodeManager = new NodeManager();
 * nodeManager.addNode(new Node("node1"...));
 * nodeManager.addNode(new Node("node2"...));
 * nodeManager.init();
 * DistributedCache distributedCache = new DistributedCache(nodeManager);
 * distributedCache.put("key1", "value1");
 */
public class DistributedCache implements Cacheable {

    private final NodeManager nodeManager;

    public DistributedCache(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    /**
     * Get the value of the key from the node where the key is stored.
     *
     * @param key key of the value. It should be serializable.
     * @return value value of the key. It can be null.
     * @throws IllegalArgumentException if key is null or key is not in the cache
     * @throws InternalException        if cache is not initialized for the node
     * @see NodeManager#nodeGet(Serializable)
     */
    @Override
    public Optional<Object> get(Serializable key) {
        validate(key);
        return nodeManager.nodeGet(key).getCache().getFromCache(key);
    }

    /**
     * Put the key-value pair to contributed cache
     *
     * @param key   key of the value. It should be serializable.
     * @param value value of the key. It can not be null
     * @throws IllegalArgumentException if key is null or key is not in the cache or value is null
     * @throws InternalException        if cache is not initialized for the node
     * @see NodeManager#nodeGet(Serializable)
     */
    @Override
    public void put(Serializable key, Object value) {
        validate(key);
        if (value == null) {
            throw new IllegalArgumentException("Value can't be null.");
        }
        nodeManager.nodeGet(key).getCache().putToCache(key, value);
    }

    /**
     * Remove the key from the cache
     *
     * @param key key of the value. It should be serializable.
     * @throws IllegalArgumentException if key is null or key is not in the cache
     * @throws InternalException        if cache is not initialized for the node
     * @see NodeManager#nodeGet(Serializable)
     */
    @Override
    public void remove(Serializable key) {
        validate(key);
        nodeManager.nodeGet(key).getCache().removeFromCache(key);
    }

    private void validate(Serializable key) {
        if (key == null) {
            throw new IllegalArgumentException("Key can't be null.");
        }
        if (nodeManager.nodeGet(key) == null) {
            throw new IllegalArgumentException("Key is not in the cache. Key: " + key);
        }
        if (nodeManager.nodeGet(key).getCache() == null) {
            throw new InternalException("Cache is not initialized for node " + nodeManager.nodeGet(key).getNodeId());
        }
    }
}
