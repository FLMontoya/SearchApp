package com.plexus.searchapp.data.cache

import java.util.concurrent.TimeUnit

/** Monotonic time source, injectable so cache expiration can be tested. */
fun interface Clock {
    fun nowMillis(): Long

    companion object {
        val System = Clock { TimeUnit.NANOSECONDS.toMillis(java.lang.System.nanoTime()) }
    }
}
