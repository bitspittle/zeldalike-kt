package bitspittle.game2d.engine.api.context

import bitspittle.game2d.engine.api.graphics.DrawSurface
import bitspittle.game2d.engine.api.time.Timer

interface DrawContext {
    val screen: DrawSurface
    val timer: Timer
}