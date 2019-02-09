package bitspittle.game2d.engine.api

import bitspittle.game2d.engine.api.context.DrawContext
import bitspittle.game2d.engine.api.context.InitContext
import bitspittle.game2d.engine.api.context.UpdateContext

interface Game {
    fun init(ctx: InitContext)
    fun update(ctx: UpdateContext)
    fun draw(ctx: DrawContext)
}