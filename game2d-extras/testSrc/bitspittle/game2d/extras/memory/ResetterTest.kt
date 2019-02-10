package bitspittle.game2d.extras.memory

import bitspittle.game2d.core.math.ImmutablePt2
import bitspittle.game2d.core.math.ImmutableVec2
import bitspittle.game2d.core.math.Pt2
import bitspittle.game2d.core.math.Vec2
import bitspittle.game2d.core.time.Duration
import bitspittle.game2d.core.time.ImmutableDuration
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

@Suppress("unused") // Many fields accessed by reflection
class ResetterTest {
    class PrimitiveValues {
        class NotAPrimitive

        // Writable
        var boolVar: Boolean = true
        var shortVar: Short = 4
        var intVar: Int = 9
        var longVar: Long = 320
        var floatVar: Float = -4f
        var doubleVar: Double = 3.2
        var strVar: String = "Dummy Value"
        var boolVarNullable: Boolean? = true
        var shortVarNullable: Short? = 4
        var intVarNullable: Int? = 9
        var longVarNullable: Long? = 320
        var floatVarNullable: Float? = -4f
        var doubleVarNullable: Double? = 3.2
        var strVarNullable: String? = "Dummy Value"

        // Read-only
        val boolVal: Boolean = true
        val shortVal: Short = 4
        val intVal: Int = 9
        val longVal: Long = 320
        val floatVal: Float = -4f
        val doubleVal: Double = 3.2
        val strVal: String = "Dummy Value"

        // Not a primitive! Should be ignored
        var notAPrimitive = NotAPrimitive()
    }

    class Privates {
        private var _x = 1234
        private var _y = 9876

        private val duration = Duration.ofNanos(123456)

        val x
            get() = _x
        val y
            get() = _y

        val nanos
            get() = duration.nanos
    }

    class ResettableTypes {
        val mutablePt = Pt2(123, 456)
        val mutableVec = Vec2(987, 654)
        val mutableDuration = Duration.ofMillis(12345)

        val immutablePt: ImmutablePt2 = Pt2(123, 456)
        val immutableVec: ImmutableVec2 = Vec2(987, 654)
        val immutableDuration: ImmutableDuration = Duration.ofMillis(12345)
    }

    interface Counter {
        var count: Int
        fun increment()
        fun resetCounter()
    }

    abstract class CounterIncrementer : Counter {
        override fun increment() {
            count++
        }
    }

    class CountFrom1 : CounterIncrementer() {
        override var count: Int = 1
        override fun resetCounter() {
            count = 1
        }
    }

    class CountFrom1000 : CounterIncrementer() {
        override var count: Int = 1000
        override fun resetCounter() {
            count = 1000
        }
    }

    class CounterOwner {
        val countFrom1 = CountFrom1()
        val countFrom1000 = CountFrom1000()
    }

    class CompanionableVariables {
        companion object {
            var X = 123
            var Y = 789
        }
    }

    open class Parent {
        var parentA = 123
        var parentB = 456
    }

    class Child : Parent() {
        var childA = 789
        var childB = 1011
    }

    class WithConstructorVariables(var x: Int = 123, var y: Int = 456) {
        var z: Int = 789
        var w: Int = 9999
    }

    class WithIgnoredProperties(@Resetter.Ignore var x: Int = 123, var y: Int = 456) {
        @Resetter.Ignore
        var z: Int = 789
        var w: Int = 9999
    }

    @Test
    fun `default resetter can reset primitive values`() {
        val r = ReflectionResetter()
        val primitives = PrimitiveValues()
        val ignored = r.resetAndReport(primitives)

        // Settable vars, which can be reset
        assertThat(primitives.boolVar).isEqualTo(false)
        assertThat(primitives.shortVar).isEqualTo(0)
        assertThat(primitives.intVar).isEqualTo(0)
        assertThat(primitives.longVar).isEqualTo(0)
        assertThat(primitives.floatVar).isEqualTo(0f)
        assertThat(primitives.doubleVar).isEqualTo(0.0)
        assertThat(primitives.strVar).isEqualTo("")
        assertThat(primitives.boolVarNullable).isNull()
        assertThat(primitives.shortVarNullable).isNull()
        assertThat(primitives.intVarNullable).isNull()
        assertThat(primitives.longVarNullable).isNull()
        assertThat(primitives.floatVarNullable).isNull()
        assertThat(primitives.doubleVarNullable).isNull()
        assertThat(primitives.strVarNullable).isNull()

        // Read-only vals, which cannot be reset
        assertThat(primitives.boolVal).isNotEqualTo(false)
        assertThat(primitives.shortVal).isNotEqualTo(0)
        assertThat(primitives.intVal).isNotEqualTo(0)
        assertThat(primitives.longVal).isNotEqualTo(0)
        assertThat(primitives.floatVal).isNotEqualTo(0f)
        assertThat(primitives.doubleVal).isNotEqualTo(0.0)
        assertThat(primitives.strVal).isNotEqualTo("")

        assertThat(ignored.map { it.name })
            .containsExactly(
                "boolVal",
                "shortVal",
                "intVal",
                "longVal",
                "floatVal",
                "doubleVal",
                "strVal",
                "notAPrimitive"
            )
    }

    @Test
    fun `default resetter can reset some val types`() {
        val r = ReflectionResetter()
        val types = ResettableTypes()
        val ignored = r.resetAndReport(types)

        assertThat(types.mutablePt).isEqualTo(Pt2.ZERO)
        assertThat(types.mutableVec).isEqualTo(Vec2.ZERO)
        assertThat(types.mutableDuration).isEqualTo(Duration.ZERO)

        assertThat(ignored.map { it.name })
            .containsExactly(
                "immutablePt",
                "immutableVec",
                "immutableDuration"
            )
    }

    @Test
    fun `default resetter resets private variables and values`() {
        val r = ReflectionResetter()
        val privates = Privates()
        val ignored = r.resetAndReport(privates)

        assertThat(privates.x).isEqualTo(0)
        assertThat(privates.y).isEqualTo(0)
        assertThat(privates.nanos).isEqualTo(0.0)

        assertThat(ignored.map { it.name }).containsExactly("x", "y", "nanos")
    }

    @Test
    fun `resetters can handle resetters for base classes and interfaces`() {
        val r = ReflectionResetter()
        r.addTypeResetter(Counter::class) { counter -> counter.resetCounter() }

        val countFrom1 = CountFrom1().apply { increment(); increment() }
        val countFrom1000 = CountFrom1000().apply { increment(); increment(); increment() }
        val counterOwner = CounterOwner()
        counterOwner.countFrom1.increment()
        counterOwner.countFrom1000.increment()

        assertThat(countFrom1.count).isEqualTo(3)
        assertThat(countFrom1000.count).isEqualTo(1003)
        assertThat(counterOwner.countFrom1.count).isEqualTo(2)
        assertThat(counterOwner.countFrom1000.count).isEqualTo(1001)

        r.resetAndReport(countFrom1).also { ignored -> assertThat(ignored).isEmpty() }
        r.resetAndReport(countFrom1000).also { ignored -> assertThat(ignored).isEmpty() }
        r.resetAndReport(counterOwner).also { ignored -> assertThat(ignored).isEmpty() }

        assertThat(countFrom1.count).isEqualTo(1)
        assertThat(countFrom1000.count).isEqualTo(1000)
        assertThat(counterOwner.countFrom1.count).isEqualTo(1)
        assertThat(counterOwner.countFrom1000.count).isEqualTo(1000)
    }

    @Test
    fun `resetters ignore companion properties`() {
        val r = ReflectionResetter()
        val companionable = CompanionableVariables()
        r.resetAndReport(companionable).also { ignored -> assertThat(ignored).isEmpty() }

        assertThat(CompanionableVariables.X).isNotEqualTo(0)
        assertThat(CompanionableVariables.Y).isNotEqualTo(0)
    }

    @Test
    fun `resetters set all variables in a class hierarchy`() {
        val r = ReflectionResetter()
        val child = Child()

        r.resetAndReport(child).also { ignored -> assertThat(ignored).isEmpty() }

        assertThat(child.childA).isEqualTo(0)
        assertThat(child.childB).isEqualTo(0)
        assertThat(child.parentA).isEqualTo(0)
        assertThat(child.parentB).isEqualTo(0)
    }

    @Test
    fun `resetters reset properties defined in the constructor`() {
        val r = ReflectionResetter()
        val withConsVars = WithConstructorVariables()
        r.resetAndReport(withConsVars).also { ignored -> assertThat(ignored).isEmpty() }

        assertThat(withConsVars.x).isEqualTo(0)
        assertThat(withConsVars.y).isEqualTo(0)
        assertThat(withConsVars.z).isEqualTo(0)
        assertThat(withConsVars.w).isEqualTo(0)
    }

    @Test
    fun `resetters ignore properties marked with the ignore annotation`() {
        val r = ReflectionResetter()
        val withIgnoredProperties = WithIgnoredProperties()
        val ignored = r.resetAndReport(withIgnoredProperties)

        assertThat(withIgnoredProperties.x).isNotEqualTo(0)
        assertThat(withIgnoredProperties.y).isEqualTo(0)
        assertThat(withIgnoredProperties.z).isNotEqualTo(0)
        assertThat(withIgnoredProperties.w).isEqualTo(0)

        assertThat(ignored.map { it.name }).containsExactly("x", "z")
    }
}