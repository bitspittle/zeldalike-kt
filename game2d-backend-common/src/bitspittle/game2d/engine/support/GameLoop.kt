package bitspittle.game2d.engine.support

import bitspittle.game2d.engine.api.Game
import bitspittle.game2d.engine.api.app.ApplicationSystem
import bitspittle.game2d.engine.api.context.DrawContext
import bitspittle.game2d.engine.api.context.UpdateContext
import bitspittle.game2d.engine.api.graphics.DrawSurface
import bitspittle.game2d.engine.api.input.Keyboard

class GameLoop(private val backend: Backend, private val game: Game) {
    class Backend(
        val app: ApplicationSystem,
        val screen: DrawSurface,
        val keyboard: Keyboard
    )

    private val updateContext = object : UpdateContext {
        override val app = backend.app
        override val screen = backend.screen
        override val keyboard = backend.keyboard
    }
    private val drawContext = object : DrawContext {
        override val screen = backend.screen
    }

    fun step() {
        game.update(updateContext)
        game.draw(drawContext)
    }
}