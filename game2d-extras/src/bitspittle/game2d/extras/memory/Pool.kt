package bitspittle.game2d.extras.memory

import bitspittle.game2d.extras.memory.Pool.Handle

private const val ID_FREE = 0 // Reserved ID for an entry that's free for using

/**
 * A class which manages a pool of pre-allocated objects so you can avoid thrashing the garbage
 * collector when you want to make lots of allocations which may live for a while, e.g. particles
 * in a particle system.
 *
 * Pools are constructed with a callback one which allocates a new instance of a class, which
 * immediately is used to allocation a bunch of initial instance. After that, just call [newItem]
 * and [freeItem] to get access to them.
 *
 * Pools don't return your item directly, but instead they give you a handle with which to access
 * the item. This roundabout API helps encourage you to only access an item temporarily and not
 * hold onto it. Handles will stop working as soon as the item they were originally associated with
 * was freed, and you can check [Handle.isValid] to see if this is the case.
 *
 * ```
 * val handle = pool.newItem()
 * pool.getItem(handle) { item -> /*...*/ }
 * assert(handle.isValid())
 * pool.freeItem(handle)
 * assert(!handle.isValid())
 * ```
 *
 * Finally, a pool is backed by a [Resetter] which uses reflection to clear each instance when
 * [grabNew] is called. See the [Resetter] class for more details, but if you need more powerful
 * support than what the default resetter provides, you should consider creating your own and
 * passing it in.
 */
class Pool<T: Any>(
    allocate: () -> T,
    val capacity: Int = DEFAULT_CAPACITY,
    @PublishedApi internal val resetter: Resetter = DEFAULT_RESETTER
) : Iterable<T> {
    class Entry<T: Any>(val item: T, var nextFree: Int) {
        var id = ID_FREE
    }
    class Handle<T: Any>(internal val pool: Pool<T>, internal var index: Int) {
        internal val entry: Entry<T>?
            get() = if (isValid()) pool.entries[index] else null

        private val id = pool.entries[index].id
        fun isValid() = pool.entries[index].id == id
    }

    companion object {
        const val DEFAULT_CAPACITY = 10
        private val DEFAULT_RESETTER by lazy { ReflectionResetter() }
    }

    @PublishedApi
    internal val entries: MutableList<Entry<T>>

    val handles: List<Handle<T>>
        get() =
            entries
                .mapIndexedNotNull { i, entry -> if (entry.id != ID_FREE) Handle(this, i) else null }
                .toList()

    var size: Int = 0
        private set

    val isEmpty: Boolean
        get() = size == 0

    private var nextFree = 0
    private var nextId = 1

    init {
        if (capacity <= 0) {
            throw IllegalArgumentException("Invalid pool capacity: $capacity")
        }

        entries = ArrayList(capacity)
        for (i in 0 until capacity) {
            entries.add(Entry(allocate(), i + 1))
        }
    }

    inline fun newItem(init: T.() -> Unit = {}): Handle<T> {
        val itemIndex = reserveItem()
        val entry = entries[itemIndex]
        entry.item.apply {
            resetter.reset(this)
            init(this)
        }
        return Handle(this, itemIndex)
    }

    fun freeItem(handle: Handle<T>) {
        verifyCorrectPool(handle)
        handle.entry?.let { entry ->
            size--
            entry.id = ID_FREE
            entry.nextFree = nextFree
            nextFree = handle.index
        }
    }

    inline fun getItem(handle: Handle<T>, itemCallback: T.() -> Unit) {
        getItem(handle)?.let { item -> itemCallback(item) }
    }

    override fun iterator() = handles.map { it.entry!!.item }.iterator()

    @PublishedApi
    internal fun reserveItem(): Int {
        if (size == capacity) {
            throw IllegalStateException(
                "Requested too many entries from this pool (capacity: $capacity) - are you forgetting to free some?")
        }

        size++
        val reservedIndex = nextFree
        nextFree = entries[reservedIndex].nextFree
        entries[reservedIndex].id = nextId++
        return reservedIndex
    }

    @PublishedApi
    internal fun getItem(handle: Handle<T>): T? {
        verifyCorrectPool(handle)
        return handle.entry?.item
    }

    private fun verifyCorrectPool(handle: Handle<T>) {
        if (handle.pool != this) {
            throw java.lang.IllegalArgumentException("Using a Pool handle with the wrong Pool")
        }
    }
}