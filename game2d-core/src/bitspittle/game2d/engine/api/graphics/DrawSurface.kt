package bitspittle.game2d.engine.api.graphics

import bitspittle.game2d.core.graphics.Color
import bitspittle.game2d.core.math.ImmutableVec2

interface ImmutableDrawSurface {
    val size: ImmutableVec2
}

interface DrawSurface : ImmutableDrawSurface {
    fun clear(color: Color)
}