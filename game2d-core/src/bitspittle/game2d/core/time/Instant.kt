package bitspittle.game2d.core.time

/**
 * An immutable class which represents a moment in time.
 *
 * `Instant`s on their own are not useful, but you can subtract one from another to get
 * a `Duration`.
 *
 * @param nanos A value on the timescale of nanos. The absolute value has no meaning; the only
 * requirement is that two `Instant`s created over some elapsed time can be subtracted to
 * return a duration of that elapsed time.
 *
 * TODO: Convert to an inline class once that feature becomes standardized
 */
class Instant(private var nanos: Long) {
    companion object {
        fun now() = Instant(System.nanoTime())
    }
    operator fun minus(rhs: Instant) = Duration((nanos - rhs.nanos).toDouble())
    operator fun compareTo(rhs: ImmutableDuration) = nanos.compareTo(rhs.nanos)
}
