/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LRUEviction.java — O(1) LRU eviction using HashMap + DoublyLinkedList

import java.util.*;

/**
 * O(1) LRU eviction: HashMap for O(1) node lookup + DoublyLinkedList for O(1) move-to-head/remove-tail.
 * This is the classic LRU implementation used in production systems.
 */
public class LRUEviction<K> implements EvictionStrategy<K> { // implements = fulfills eviction strategy contract
    private Map<K, Node<K>> nodeMap;             // HashMap = O(1) node lookup by key
    private Node<K> head; // Most recently used — DoublyLinkedList gives O(1) move-to-head
    private Node<K> tail; // Least recently used — O(1) removal from tail

    public LRUEviction() {
        this.nodeMap = new HashMap<>();
        this.head = new Node<>(null);
        this.tail = new Node<>(null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void onAccess(K key) {
        Node<K> node = nodeMap.get(key);
        if (node != null) {
            removeNode(node);
            addToHead(node);
        }
    }

    @Override
    public void onInsert(K key) {
        Node<K> node = new Node<>(key);
        nodeMap.put(key, node);
        addToHead(node);
    }

    @Override
    public K evict() {
        if (tail.prev == head) return null; // Empty
        Node<K> lru = tail.prev;
        removeNode(lru);
        nodeMap.remove(lru.key);
        return lru.key;
    }

    @Override
    public void onRemove(K key) {
        Node<K> node = nodeMap.remove(key);
        if (node != null) removeNode(node);
    }

    @Override
    public void clear() {
        nodeMap.clear();
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public String getName() { return "LRU (O(1) HashMap+DLL)"; }

    private void addToHead(Node<K> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private static class Node<K> {               // static inner class = DoublyLinkedList node; no outer ref needed
        K key;
        Node<K> prev, next;                      // prev/next = doubly linked for O(1) insert/remove
        Node(K key) { this.key = key; }
    }
}
