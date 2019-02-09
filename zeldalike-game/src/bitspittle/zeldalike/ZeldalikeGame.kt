package bitspittle.zeldalike

import bitspittle.game2d.core.graphics.Color
import bitspittle.game2d.engine.api.Game
import bitspittle.game2d.engine.api.context.DrawContext
import bitspittle.game2d.engine.api.context.UpdateContext
import bitspittle.game2d.engine.api.input.Keyboard.Key

private val CLEAR_COLOR = Color(0x77, 0x77, 0x77)

class ZeldalikeGame : Game {
    override fun update(ctx: UpdateContext) {
        if (ctx.keyboard.isDown(Key.ESC)) {
            ctx.app.quit()
        }
    }

    override fun draw(ctx: DrawContext) {
        ctx.screen.clear(CLEAR_COLOR)
    }
}