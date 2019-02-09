package bitspittle.zeldalike

import bitspittle.game2d.core.math.Vec2
import bitspittle.game2d.engine.desktop.GameApplication

fun main() {
    val appParams = GameApplication.AppParams(
        title = "Zeldalike.kt: A Gamedev Tutorial for Kotlin",
        size = Vec2(640, 480)
    )
    val app = GameApplication(appParams)
    app.run(ZeldalikeGame())
}
