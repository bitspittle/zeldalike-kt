package bitspittle.game2d.extras.memory

import bitspittle.game2d.core.math.Pt2
import bitspittle.game2d.core.math.Vec2
import bitspittle.game2d.core.time.Duration
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaType

/**
 * An interface for an object that can reset Kotlin classes and their properties.
 *
 * Implementations should respect the presence of [Resetter.Ignore] annotations.
 */
interface Resetter {
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    annotation class Ignore

    fun <T : Any> reset(target: T)
}

/**
 * A useful [Resetter] implementation that uses reflection to reset a class and as many fields as
 * it can.
 *
 * Out of the box, it can reset primitive values and relevant game2d types. A user may need to add
 * additional support for custom values, which they can do so via [addDefaultProvider] and
 * [addTypeResetter] methods.
 *
 * A default provider provides a default value used when encountering a `var` property which should
 * be overwritten, while a type resetter provides the logic to reset a `val` property whose
 * contents should be reset. Note that nullable vars will always be set to null.
 *
 * A resetter follows an intentional order - it evaluates default providers for all vars first,
 * then it tries to reset all vals, and then it tries to reset the class itself. Although
 * ideally default providers should cover most cases, the user has an option to re-overwrite
 * those values with a custom type resetter later, e.g. a count that should be reset to 1 and
 * not 0, or a String that should be reset to "(Your text here)" and not "", etc.
 */
class ReflectionResetter : Resetter {
    // Use Class instead of KClass under the hood because reflection was easier to get working...
    private val defaultProviders = mutableMapOf<Class<out Any>, () -> Any>()
    private val typeResetters = mutableMapOf<Class<out Any>, (Any) -> Unit>()

    init {
        addDefaultProvider(Boolean::class) { false }
        addDefaultProvider(Short::class) { 0 }
        addDefaultProvider(Int::class) { 0 }
        addDefaultProvider(Long::class) { 0 }
        addDefaultProvider(Float::class) { 0f }
        addDefaultProvider(Double::class) { 0.0 }
        addDefaultProvider(String::class) { "" }

        addTypeResetter(Pt2::class) { pt -> pt.set(Pt2.ZERO) }
        addTypeResetter(Vec2::class) { vec -> vec.set(Vec2.ZERO) }
        addTypeResetter(Duration::class) { d -> d.setFrom(Duration.ZERO) }
    }

    fun <T: Any> addDefaultProvider(kClass: KClass<T>, provider: () -> T) {
        val jClass = kClass.java
        defaultProviders.putIfAbsent(jClass, provider).let { prevProvider ->
            if (provider == prevProvider) {
                throw IllegalArgumentException(
                    "Attempting to register a second default provider for type: ${kClass.simpleName}")
            }
        }
    }

    fun <T: Any> addTypeResetter(kClass: KClass<T>, resetter: (T) -> Unit) {
        val jClass = kClass.java
        @Suppress("UNCHECKED_CAST") // Map key verifies correct resetter type later
        typeResetters.putIfAbsent(jClass, resetter as (Any) -> Unit).let { prevResetter ->
            if (resetter == prevResetter) {
                throw IllegalArgumentException(
                    "Attempting to register a second default provider for type: ${kClass.simpleName}")
            }
        }
    }

    override fun <T : Any> reset(target: T) {
        resetAndReport(target)
    }

    /**
     * Reset what we can, and anything we skip over will be returned in a list that the caller can
     * sanity check.
     */
    fun <T: Any> resetAndReport(target: T): List<KProperty<*>> {
        val ignored = mutableListOf<KProperty<*>>()
        val properties = target::class.memberProperties
            .filterIsInstance<KProperty1<T, *>>()
            .partition { it is KMutableProperty1<T, *> }

        // First, set all 'var' properties to any registered default values. (These changes may get
        // overwritten by custom resetters in followup steps)
        properties.first.forEach { prop ->
            if (prop.findAnnotation<Resetter.Ignore>() != null) {
                ignored.add(prop)
                return@forEach
            }

            prop as KMutableProperty1<T, *>
            val setterType = prop.setter.parameters[1].type
            if (setterType.isMarkedNullable) {
                prop.setter.call(target, null)
            } else {
                val provider = defaultProviders[setterType.javaType]
                if (provider != null) {
                    prop.setter.isAccessible = true
                    prop.setter.call(target, provider())
                } else {
                    ignored.add(prop)
                }
            }
        }

        // Next, reset any 'val' we have resetters for
        properties.second.forEach { prop ->
            if (prop.findAnnotation<Resetter.Ignore>() != null) {
                ignored.add(prop)
                return@forEach
            }

            // One or the other should be non-null
            val getterType = (prop.javaGetter?.returnType ?: prop.javaField?.type)!!
            prop.getter.isAccessible = true
            val value = prop.getter.invoke(target)!!
            val matches = typeResetters.entries
                .filter { it.key.isAssignableFrom(getterType) }
                .map { it.value }
                .toList()

            if (!matches.isEmpty()) {
                matches.forEach { reset -> reset(value) }
            } else {
                ignored.add(prop)
            }
        }

        // Finally, reset this class itself, if we have a relevant resetter for it
        kotlin.run {
            val matches = typeResetters.entries
                .filter { it.key.isAssignableFrom(target.javaClass) }
                .map { it.value }
                .toList()

            if (!matches.isEmpty()) {
                matches.forEach { reset -> reset(target) }
            }
        }

        return ignored
    }
}
