package bitspittle.game2d.core.time

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.TimeUnit

private val NS_IN_ONE_DAY = TimeUnit.DAYS.toNanos(1).toDouble()
private val US_IN_ONE_DAY = TimeUnit.DAYS.toMicros(1).toDouble()
private val MS_IN_ONE_DAY = TimeUnit.DAYS.toMillis(1).toDouble()
private val SECS_IN_ONE_DAY = TimeUnit.DAYS.toSeconds(1).toDouble()
private val MINS_IN_ONE_DAY = TimeUnit.DAYS.toMinutes(1).toDouble()

/**
 * Test [Duration] and [Instant] together, since they're relatively interdependent.
 */
class DurationInstantTest {
    @Test
    fun `can construct a duration`() {
        run {
            val d = Duration.zero()
            assertThat(d.millis).isEqualTo(0.0)
            assertThat(d.secs).isEqualTo(0.0)
            assertThat(d.mins).isEqualTo(0.0)
            assertThat(d.isZero()).isTrue()
        }

        run {
            val d = Duration.ofNanos(NS_IN_ONE_DAY)
            assertThat(d.nanos).isEqualTo(NS_IN_ONE_DAY)
            assertThat(d.micros).isEqualTo(US_IN_ONE_DAY)
            assertThat(d.millis).isEqualTo(MS_IN_ONE_DAY)
            assertThat(d.secs).isEqualTo(SECS_IN_ONE_DAY)
            assertThat(d.mins).isEqualTo(MINS_IN_ONE_DAY)
            assertThat(d.isZero()).isFalse()
        }

        run {
            val d = Duration.ofMicros(US_IN_ONE_DAY)
            assertThat(d.nanos).isEqualTo(NS_IN_ONE_DAY)
            assertThat(d.micros).isEqualTo(US_IN_ONE_DAY)
            assertThat(d.millis).isEqualTo(MS_IN_ONE_DAY)
            assertThat(d.secs).isEqualTo(SECS_IN_ONE_DAY)
            assertThat(d.mins).isEqualTo(MINS_IN_ONE_DAY)
            assertThat(d.isZero()).isFalse()
        }

        run {
            val d = Duration.ofMillis(MS_IN_ONE_DAY)
            assertThat(d.nanos).isEqualTo(NS_IN_ONE_DAY)
            assertThat(d.micros).isEqualTo(US_IN_ONE_DAY)
            assertThat(d.millis).isEqualTo(MS_IN_ONE_DAY)
            assertThat(d.secs).isEqualTo(SECS_IN_ONE_DAY)
            assertThat(d.mins).isEqualTo(MINS_IN_ONE_DAY)
            assertThat(d.isZero()).isFalse()
        }

        run {
            val d = Duration.ofSeconds(SECS_IN_ONE_DAY)
            assertThat(d.nanos).isEqualTo(NS_IN_ONE_DAY)
            assertThat(d.micros).isEqualTo(US_IN_ONE_DAY)
            assertThat(d.millis).isEqualTo(MS_IN_ONE_DAY)
            assertThat(d.secs).isEqualTo(SECS_IN_ONE_DAY)
            assertThat(d.mins).isEqualTo(MINS_IN_ONE_DAY)
            assertThat(d.isZero()).isFalse()
        }

        run {
            val d = Duration.ofMinutes(MINS_IN_ONE_DAY)
            assertThat(d.nanos).isEqualTo(NS_IN_ONE_DAY)
            assertThat(d.micros).isEqualTo(US_IN_ONE_DAY)
            assertThat(d.millis).isEqualTo(MS_IN_ONE_DAY)
            assertThat(d.secs).isEqualTo(SECS_IN_ONE_DAY)
            assertThat(d.mins).isEqualTo(MINS_IN_ONE_DAY)
            assertThat(d.isZero()).isFalse()
        }

        run {
            val dSrc = Duration.ofNanos(NS_IN_ONE_DAY)
            val d = dSrc.copy()
            assertThat(d.nanos).isEqualTo(NS_IN_ONE_DAY)
            assertThat(d.micros).isEqualTo(US_IN_ONE_DAY)
            assertThat(d.millis).isEqualTo(MS_IN_ONE_DAY)
            assertThat(d.secs).isEqualTo(SECS_IN_ONE_DAY)
            assertThat(d.mins).isEqualTo(MINS_IN_ONE_DAY)
            assertThat(d.isZero()).isFalse()
        }
    }

    @Test
    fun `min and max work`() {
        val d1: ImmutableDuration = Duration.ofSeconds(1)
        val d5: ImmutableDuration = Duration.ofSeconds(5)
        val d9: ImmutableDuration = Duration.ofSeconds(9)

        assertThat(max(d1, d9)).isEqualTo(d9)
        assertThat(min(d1, d9)).isEqualTo(d1)

        assertThat(max(Duration.ZERO, Duration.MAX)).isEqualTo(Duration.MAX)
        assertThat(min(Duration.ZERO, Duration.MAX)).isEqualTo(Duration.ZERO)

        run {
            val d = d5.copy()
            d.clampToMax(d1)
            assertThat(d).isEqualTo(d5)
            d.clampToMin(d1)
            assertThat(d).isEqualTo(d1)
        }
        run {
            val d = d5.copy()
            d.clampToMin(d9)
            assertThat(d).isEqualTo(d5)
            d.clampToMax(d9)
            assertThat(d).isEqualTo(d9)
        }
    }

    @Test
    fun `add and subtract durations`() {
        val d1: ImmutableDuration = Duration.ofSeconds(1)
        val d5: ImmutableDuration = Duration.ofSeconds(5)
        val d9: ImmutableDuration = Duration.ofSeconds(9)

        assertThat(d1 + d9).isEqualTo(Duration.ofSeconds(10))
        assertThat(d9 - d5).isEqualTo(Duration.ofSeconds(4))

        run {
            val d = d1.copy()
            d += d9
            assertThat(d.secs).isEqualTo(10.0)
        }
        run {
            val d = d5.copy()
            d -= d1
            assertThat(d.secs).isEqualTo(4.0)
        }
    }

    @Test
    fun `durations can be compared`() {
        val d1: ImmutableDuration = Duration.ofSeconds(1)
        val d5: ImmutableDuration = Duration.ofSeconds(5)
        val d9: ImmutableDuration = Duration.ofSeconds(9)

        assertThat(d1 < d5).isTrue()
        assertThat(d1 > d5).isFalse()
        assertThat(d1 > Duration.ZERO).isTrue()
        assertThat(d9 < Duration.MAX).isTrue()
        assertThat(Duration.ZERO > Duration.MIN).isTrue()

        assertThat(Duration.zero() == Duration.ZERO).isTrue()
    }

    @Test
    fun `durations can be negative`() {
        val d1: ImmutableDuration = Duration.ofSeconds(1)
        val d5: ImmutableDuration = Duration.ofSeconds(5)

        assertThat(d1 - d5).isEqualTo(Duration.ofSeconds(-4))
    }

    @Test
    fun `instant minus instant equals duration`() {
        val instantA = Instant(TimeUnit.SECONDS.toNanos(1))
        val instantB = Instant(TimeUnit.SECONDS.toNanos(5))

        assertThat(instantB - instantA).isEqualTo(Duration.ofSeconds(4))
    }

    @Test
    fun `duration overrides equals and hashcode`() {
        val durationSet = mutableSetOf(Duration.ofSeconds(1))
        assertThat(durationSet.contains(Duration.ofSeconds(1)))
    }

}