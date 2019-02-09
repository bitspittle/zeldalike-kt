package bitspittle.game2d.engine.support

import bitspittle.game2d.engine.api.Game
import bitspittle.game2d.engine.api.app.ApplicationSystem
import bitspittle.game2d.engine.api.assets.Assets
import bitspittle.game2d.engine.api.context.DrawContext
import bitspittle.game2d.engine.api.context.InitContext
import bitspittle.game2d.engine.api.context.UpdateContext
import bitspittle.game2d.engine.api.graphics.DrawSurface
import bitspittle.game2d.engine.api.input.Keyboard
import bitspittle.game2d.engine.api.time.Timer

class GameLoop(private val backend: Backend, private val game: Game) {
    class Backend(
        val app: ApplicationSystem,
        val assets: Assets,
        val screen: DrawSurface,
        val timer: Timer,
        val keyboard: Keyboard
    )

    private val initContext = object : InitContext {
        override val app = backend.app
        override val assets = backend.assets
        override val screen = backend.screen
        override val timer = backend.timer
    }

    private val updateContext = object : UpdateContext {
        override val app = backend.app
        override val assets = backend.assets
        override val screen = backend.screen
        override val timer = backend.timer
        override val keyboard = backend.keyboard
    }
    private val drawContext = object : DrawContext {
        override val screen = backend.screen
        override val timer = backend.timer
    }

    init {
        game.init(initContext)
    }

    fun step() {
        game.update(updateContext)
        game.draw(drawContext)
    }
}