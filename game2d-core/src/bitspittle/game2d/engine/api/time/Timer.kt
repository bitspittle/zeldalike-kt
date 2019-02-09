package bitspittle.game2d.engine.api.time

import bitspittle.game2d.core.time.Duration
import bitspittle.game2d.core.time.ImmutableDuration
import bitspittle.game2d.core.time.Instant

interface Timer {
    val lastFrameDuration: ImmutableDuration
}

/**
 * Shortcut for [Instant.now] for simplicity, allowing users of this API to find every method they
 * need from the current context, and not having to remember one-off static methods here and there.
 */
fun Timer.now() = Instant.now()

inline fun Timer.measure(block: () -> Unit): Duration {
    val start = Instant.now()
    block()
    return Instant.now() - start
}