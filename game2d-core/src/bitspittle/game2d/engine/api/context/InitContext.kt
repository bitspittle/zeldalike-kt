package bitspittle.game2d.engine.api.context

import bitspittle.game2d.engine.api.app.ApplicationSystem
import bitspittle.game2d.engine.api.assets.Assets
import bitspittle.game2d.engine.api.graphics.ImmutableDrawSurface
import bitspittle.game2d.engine.api.time.Timer

interface InitContext {
    val app: ApplicationSystem
    val assets: Assets
    val screen: ImmutableDrawSurface
    val timer: Timer
}