package bitspittle.game2d.engine.api.assets

import java.io.InputStream
import java.nio.charset.Charset

class Asset(@PublishedApi internal val loaders: Loaders, inputStream: InputStream) {
    val bytes = inputStream.use { it.readBytes() }
    fun intoText(charset: Charset = Charsets.UTF_8) = String(bytes, charset)
    inline fun <reified T : Any> into() = loaders.load<T>(bytes)
}

interface AssetFinder {
    fun find(path: String): InputStream?
}

fun assetFinder(find: (String) -> InputStream?): AssetFinder {
    return object : AssetFinder {
        override fun find(path: String) = find(path)
    }
}

class Assets(val loaders: Loaders, private val finders: List<AssetFinder>) {
    fun findAsset(path: String): Asset? =
        finders
            .mapNotNull { finder -> finder.find(path) }
            .firstOrNull()
            ?.let { inputStream -> Asset(loaders, inputStream) }
}