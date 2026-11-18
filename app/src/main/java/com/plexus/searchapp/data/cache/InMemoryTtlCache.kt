package com.plexus.searchapp.data.cache

class InMemoryTtlCache<K : Any, V : Any>(
    private val ttlMillis: Long,
    private val clock: Clock = Clock.System,
) {
    private class Entry<V>(val value: V, val storedAt: Long)

    private val entries = HashMap<K, Entry<V>>()

    /** Returns the cached value, or null if absent or older than the TTL. */
    @Synchronized
    fun get(key: K): V? {
        val entry = entries[key] ?: return null
        if (clock.nowMillis() - entry.storedAt >= ttlMillis) {
            entries.remove(key)
            return null
        }
        return entry.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        entries[key] = Entry(value, clock.nowMillis())
    }
}
