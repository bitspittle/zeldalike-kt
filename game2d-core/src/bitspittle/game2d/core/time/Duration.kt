package bitspittle.game2d.core.time

import java.lang.Double.max
import kotlin.math.min

interface ImmutableDuration {
    val nanos: Double
    val micros: Double
        get() = nanos / 1000.0
    val millis: Double
        get() = micros / 1000.0
    val secs: Double
        get() = millis / 1000.0
    val mins: Double
        get() = secs / 60.0
    fun isZero(): Boolean = nanos == 0.0

    fun copy() = Duration().apply { setFrom(this@ImmutableDuration) }

    operator fun plus(rhs: ImmutableDuration) = Duration(nanos + rhs.nanos)
    operator fun minus(rhs: ImmutableDuration) = Duration(nanos - rhs.nanos)
    operator fun compareTo(rhs: ImmutableDuration) = nanos.compareTo(rhs.nanos)
}

fun max(a: ImmutableDuration, b: ImmutableDuration) = if (a.nanos > b.nanos) a else b
fun min(a: ImmutableDuration, b: ImmutableDuration) = if (a.nanos < b.nanos) a else b

/**
 * An class which represents a time duration.
 */
data class Duration
/**
 * Don't construct directly. Use [ofSeconds], [ofMinutes], [ofMillis], [ofMicros], [ofNanos] or
 * [copy] instead.
 */
internal constructor(override var nanos: Double = 0.0) : ImmutableDuration {
    companion object {
        val ZERO: ImmutableDuration = Duration()

        /**
         * A duration that essentially represents forever. This could be useful for parameters
         * that expect infinite timeouts, for example.
         */
        val MAX = object : ImmutableDuration {
            override val nanos: Double = Double.POSITIVE_INFINITY
            override val micros: Double = Double.POSITIVE_INFINITY
            override val millis: Double = Double.POSITIVE_INFINITY
            override val secs: Double = Double.POSITIVE_INFINITY
            override val mins: Double = Double.POSITIVE_INFINITY
        }

        /**
         * A duration that represents -MAX. This could be useful for when you are trying to
         * find the minimum duration value in a list, and want an initial value to compare
         * against.
         */
        val MIN = object : ImmutableDuration {
            override val nanos: Double = Double.NEGATIVE_INFINITY
            override val micros: Double = Double.NEGATIVE_INFINITY
            override val millis: Double = Double.NEGATIVE_INFINITY
            override val secs: Double = Double.NEGATIVE_INFINITY
            override val mins: Double = Double.NEGATIVE_INFINITY
        }

        fun zero(): Duration {
            return Duration()
        }

        fun ofNanos(nanos: Double) = Duration(nanos)
        fun ofMicros(micros: Double) = Duration().apply { this.micros = micros }
        fun ofMillis(millis: Double) = Duration().apply { this.millis = millis }
        fun ofSeconds(secs: Double) = Duration().apply { this.secs = secs }
        fun ofMinutes(mins: Double) = Duration().apply { this.mins = mins }

        fun ofNanos(nanos: Long) = ofNanos(nanos.toDouble())
        fun ofMicros(micros: Long) = ofMicros(micros.toDouble())
        fun ofMillis(millis: Long) = ofMillis(millis.toDouble())
        fun ofSeconds(secs: Long) = ofSeconds(secs.toDouble())
        fun ofMinutes(mins: Long) = ofMinutes(mins.toDouble())
    }

    override var micros: Double
        get() = super.micros
        set(value) {
            nanos = value * 1000.0
        }

    override var millis: Double
        get() = super.millis
        set(value) {
            micros = value * 1000.0
        }

    override var secs: Double
        get() = super.secs
        set(value) {
            millis = value * 1000.0
        }

    override var mins: Double
        get() = super.mins
        set(value) {
            secs = value * 60.0
        }

    fun setFrom(rhs: ImmutableDuration) {
        nanos = rhs.nanos
    }

    fun clampToMax(other: ImmutableDuration) {
        nanos = max(nanos, other.nanos)
    }

    fun clampToMin(other: ImmutableDuration) {
        nanos = min(nanos, other.nanos)
    }

    operator fun plusAssign(rhs: ImmutableDuration) {
        nanos += rhs.nanos
    }

    operator fun minusAssign(rhs: ImmutableDuration) {
        nanos -= rhs.nanos
    }

    override fun toString(): String {
        return "${secs}s"
    }
}
