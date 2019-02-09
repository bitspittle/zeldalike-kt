package bitspittle.game2d.engine.api.assets

interface Loader<T> {
    fun load(bytes: ByteArray): T
}

fun <T> loader(load: (ByteArray) -> T): Loader<T> = object : Loader<T> {
    override fun load(bytes: ByteArray): T = load(bytes)
}

class Loaders {
    @PublishedApi
    internal val loaders = mutableMapOf<Class<out Any>, Loader<out Any>>()

    inline fun <reified T : Any> registerLoader(loader: Loader<T>) {
        val loaderType = T::class.java
        if (loaders.containsKey(loaderType)) {
            throw IllegalStateException("Attempted to register a second Loader for $loaderType")
        }

        loaders[loaderType] = loader
    }

    inline fun <reified T : Any> load(bytes: ByteArray): T {
        val loaderType = T::class.java
        return loaders[loaderType]?.load(bytes) as T?
            ?: throw IllegalArgumentException("No loader registered for $loaderType")
    }
}