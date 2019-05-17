package com.wezom.kiviremote.common

import java.util.*

class LowCostLRUCache<K, V>(private val capacity: Int = 5) {
    private val cache = HashMap<K, V>()
    private val insertionOrder = LinkedList<K>()

    /**
     * [HashMap] put and remove is O(1).
     * More info: https://stackoverflow.com/a/4578039/2085356
     */
    fun put(key: K, value: V): K? {
        var evictedKey: K? = null
        if (cache.size >= capacity) {
            evictedKey = getKeyToEvict()
            cache.remove(evictedKey)
        }
        cache[key] = value
        insertionOrder.addLast(key)
        return evictedKey
    }

    /**
     * [HashMap] get is O(1).
     * More info: https://stackoverflow.com/a/4578039/2085356
     */
    fun get(key: K): V? = cache[key]

    /**
     * The head of the [insertionOrder] is removed, which is O(1), since this
     * is a linked list, and it's inexpensive to remove an item from head.
     * More info: https://stackoverflow.com/a/42849573/2085356
     */
    private fun getKeyToEvict(): K? = insertionOrder.removeFirst()

    override fun toString() = cache.toString()
}