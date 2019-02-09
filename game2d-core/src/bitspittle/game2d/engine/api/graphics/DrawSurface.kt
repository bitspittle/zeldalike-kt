package bitspittle.game2d.engine.api.graphics

import bitspittle.game2d.core.graphics.Color
import bitspittle.game2d.core.math.ImmutablePt2
import bitspittle.game2d.core.math.ImmutableVec2
import bitspittle.game2d.core.math.Pt2

interface ImmutableDrawSurface {
    val size: ImmutableVec2
}

interface DrawSurface : ImmutableDrawSurface {
    class DrawParams(
        val dest: ImmutablePt2
        // TODO: Expand the API in a later tutorial...
//        val src: ImmutablePt2 = Pt2.ZERO,
//        val destSize: ImmutableVec2? = null,
//        val srcSize: ImmutableVec2? = null
    )

    fun clear(color: Color)
    fun draw(image: Image, params: DrawParams)
}