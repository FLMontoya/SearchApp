package com.plexus.searchapp.data

import com.plexus.searchapp.data.cache.Clock
import com.plexus.searchapp.data.cache.InMemoryTtlCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryTtlCacheTest {
    private var now = 0L
    private val clock = Clock { now }
    private val cache = InMemoryTtlCache<String, Int>(ttlMillis = 1_000, clock = clock)

    @Test
    fun `missing key returns null`() {
        assertNull(cache.get("a"))
    }

    @Test
    fun `value is returned until the TTL elapses`() {
        cache.put("a", 1)

        now = 999
        assertEquals(1, cache.get("a"))

        now = 1_000
        assertNull(cache.get("a"))
    }

    @Test
    fun `putting again restarts the TTL`() {
        cache.put("a", 1)
        now = 900
        cache.put("a", 2)

        now = 1_800
        assertEquals(2, cache.get("a"))
    }

    @Test
    fun `keys expire independently`() {
        cache.put("a", 1)
        now = 600
        cache.put("b", 2)

        now = 1_100
        assertNull(cache.get("a"))
        assertEquals(2, cache.get("b"))
    }
}
