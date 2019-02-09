package bitspittle.zeldalike

import bitspittle.game2d.core.graphics.Color
import bitspittle.game2d.core.math.Pt2
import bitspittle.game2d.core.math.Vec2
import bitspittle.game2d.core.time.Duration
import bitspittle.game2d.core.time.ImmutableDuration
import bitspittle.game2d.core.time.min
import bitspittle.game2d.engine.api.Game
import bitspittle.game2d.engine.api.context.DrawContext
import bitspittle.game2d.engine.api.context.InitContext
import bitspittle.game2d.engine.api.context.UpdateContext
import bitspittle.game2d.engine.api.graphics.DrawSurface
import bitspittle.game2d.engine.api.graphics.DrawSurface.DrawParams
import bitspittle.game2d.engine.api.graphics.Image
import bitspittle.game2d.engine.api.input.Keyboard.Key

private val CLEAR_COLOR = Color(0x77, 0x77, 0x77)

/**
 * In case our update loop REALLY starts to fall behind, say a render takes 5 seconds or you, the
 * dev, set a breakpoint -- it's useful to clamp the update logic to some max duration, to ensure
 * that there isn't a single frame where things go INSANE.
 */
private val MAX_UPDATE_DURATION: ImmutableDuration = Duration.ofMillis(100.0)

class ZeldalikeGame : Game {
    private class Entity(val pos: Pt2, val image: Image) {
        fun drawTo(surface: DrawSurface) {
            surface.draw(image, DrawParams(pos))
        }
    }

    private lateinit var player: Entity
    private val walls = mutableListOf<Entity>()

    override fun init(ctx: InitContext) {
        run { // init player
            val playerImage: Image = ctx.assets.findAsset("/images/player.png")!!.into<Image>()
            val center = Pt2((ctx.screen.size - playerImage.size) / 2f)
            player = Entity(center, playerImage)
        }

        run { // init walls
            val wallImage = ctx.assets.findAsset("/images/wall.png")!!.into<Image>()

            val tileCounts = (ctx.screen.size / wallImage.size)
            val numWallsX = tileCounts.x.toInt()
            val numWallsY = tileCounts.y.toInt()

            for (x in 0 until numWallsX) {
                walls.add(Entity(Pt2(x * wallImage.size.x, 0f), wallImage))
                walls.add(Entity(Pt2(x * wallImage.size.x, (numWallsY - 1) * wallImage.size.y), wallImage))
            }

            for (y in 1 until numWallsY - 1) {
                walls.add(Entity(Pt2(0f, y * wallImage.size.y), wallImage))
                walls.add(Entity(Pt2((numWallsX - 1) * wallImage.size.x, y * wallImage.size.y), wallImage))
            }
        }
    }

    override fun update(ctx: UpdateContext) {
        if (ctx.keyboard.isDown(Key.ESC)) {
            ctx.app.quit()
            return
        }

        val elapsed = min(ctx.timer.lastFrameDuration, MAX_UPDATE_DURATION)
        run { // handle keyboard input
            fun Boolean.toFloat() = if (this) 1f else 0f
            val vel = Vec2(
                ctx.keyboard.isDown(Key.RIGHT).toFloat() - ctx.keyboard.isDown(Key.LEFT).toFloat(),
                ctx.keyboard.isDown(Key.DOWN).toFloat() - ctx.keyboard.isDown(Key.UP).toFloat()
            )
            // The player should move around 60 pixels / second, a speed which which gets them
            // across the screen in 2-3 seconds.
            player.pos += (vel * (elapsed.secs * 60).toFloat())
        }
    }

    override fun draw(ctx: DrawContext) {
        ctx.screen.clear(CLEAR_COLOR)
        walls.forEach { wall -> wall.drawTo(ctx.screen) }
        player.drawTo(ctx.screen)
    }
}