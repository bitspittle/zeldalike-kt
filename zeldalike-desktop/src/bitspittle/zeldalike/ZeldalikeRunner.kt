package bitspittle.zeldalike

import bitspittle.game2d.core.math.Vec2
import bitspittle.game2d.core.time.Duration
import bitspittle.game2d.engine.api.assets.assetFinder
import bitspittle.game2d.engine.desktop.GameApplication

fun main() {
    val screenSize = Vec2(160, 144)
    val appParams = GameApplication.AppParams(
        title = "Zeldalike.kt: A Gamedev Tutorial for Kotlin",
        winSize = screenSize * 4f,
        resolution = screenSize
    )
    val assetFinders = listOf(
        assetFinder { path -> ZeldalikeGame::class.java.getResourceAsStream(path) }
    )

    val app = GameApplication(appParams, assetFinders)
    app.run(ZeldalikeGame())
}
