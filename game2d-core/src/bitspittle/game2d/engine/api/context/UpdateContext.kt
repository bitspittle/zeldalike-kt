package bitspittle.game2d.engine.api.context

import bitspittle.game2d.engine.api.app.ApplicationSystem
import bitspittle.game2d.engine.api.graphics.ImmutableDrawSurface
import bitspittle.game2d.engine.api.input.Keyboard

interface UpdateContext {
    val app: ApplicationSystem
    val screen: ImmutableDrawSurface
    val keyboard: Keyboard
}