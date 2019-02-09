package bitspittle.game2d.engine.desktop

import bitspittle.game2d.core.graphics.Color
import bitspittle.game2d.core.math.ImmutableVec2
import bitspittle.game2d.core.math.Vec2
import bitspittle.game2d.core.time.Duration
import bitspittle.game2d.core.time.Instant
import bitspittle.game2d.core.time.max
import bitspittle.game2d.engine.api.Game
import bitspittle.game2d.engine.api.app.ApplicationSystem
import bitspittle.game2d.engine.api.assets.AssetFinder
import bitspittle.game2d.engine.api.assets.Assets
import bitspittle.game2d.engine.api.assets.Loaders
import bitspittle.game2d.engine.api.assets.loader
import bitspittle.game2d.engine.api.graphics.DrawSurface
import bitspittle.game2d.engine.api.graphics.Image
import bitspittle.game2d.engine.api.input.Keyboard.Key
import bitspittle.game2d.engine.support.GameLoop
import bitspittle.game2d.engine.support.input.DefaultKeyboard
import bitspittle.game2d.engine.support.time.DefaultTimer
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer as SwingTimer
import javax.swing.WindowConstants
import kotlin.math.max
import kotlin.math.roundToInt
import java.awt.Color as AwtColor
import java.awt.Image as AwtImage

fun Color.toAwtColor() = AwtColor(r, g, b, a)

fun ImmutableVec2.toDimension() = Dimension(x.roundToInt(), y.roundToInt())

class GameApplication(private val params: AppParams, private val assetFinders: List<AssetFinder>) {
    class AppParams(
        val title: String,
        val winSize: ImmutableVec2,
        val resolution: ImmutableVec2? = null,
        val frameTarget: Duration = Duration.ofSeconds(1.0 / 60.0)
    )

    private class ImageImpl(val awtImage: AwtImage) : Image {
        override val size = Vec2(awtImage.getWidth(null), awtImage.getHeight(null))
    }

    private class Screen(size: ImmutableVec2, resolution: ImmutableVec2) : JPanel(), DrawSurface {
        override val size = resolution
        private val scale = size / resolution

        /**
         * Buffer of draw commands. We receive requests to draw to the screen outside of a time we
         * can actually draw them, so we save them up until the next paint happens.
         */
        private val drawCommands = mutableListOf<(Graphics2D) -> Unit>()

        init {
            preferredSize = size.toDimension()
        }

        private fun enqueueCommand(command: (Graphics2D) -> Unit) {
            drawCommands.add(command)
            if (drawCommands.size == 1) {
                repaint() // Schedules this component for repainting
            }
        }

        override fun clear(color: Color) {
            enqueueCommand { g ->
                g.background = color.toAwtColor()
                g.clearRect(0, 0, width, height)
            }
        }

        override fun draw(image: Image, params: DrawSurface.DrawParams) {
            @Suppress("NAME_SHADOWING") // Intentional shadowing for readability
            val image = image as ImageImpl // Safe cast as we're the code that registers the Image loader
            enqueueCommand { g ->
                g.drawImage(
                    image.awtImage,
                    params.dest.x.roundToInt(),
                    params.dest.y.roundToInt(),
                    null
                )
            }
        }

        override fun paintComponent(g: Graphics) {
            val g2d = g as Graphics2D
            g2d.scale(scale.x.toDouble(), scale.y.toDouble())
            drawCommands.forEach { command -> command(g2d) }
            drawCommands.clear()
        }
    }

    fun run(game: Game) {
        System.setProperty("sun.java2d.opengl", "true") // For hardware accelerated rendering
        var shouldQuit = false

        val frame = JFrame(params.title)
        frame.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE // Handle ourselves via listener
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                shouldQuit = true
            }
        })

        val appSystem = object : ApplicationSystem {
            override fun quit() {
                shouldQuit = true
            }
        }

        val resolution = params.resolution ?: params.winSize
        val screen = Screen(params.winSize, resolution)
        frame.contentPane.add(screen)
        frame.isResizable = false
        frame.pack()
        frame.setLocationRelativeTo(null) // This centers the frame on the Window

        val timer = DefaultTimer(Duration.zero())

        val loaders = Loaders()
        loaders.registerLoader(loader<Image> { bytes -> ImageImpl(ImageIO.read(bytes.inputStream())) })

        val keyboard = DefaultKeyboard()
        frame.addKeyListener(object : KeyAdapter() {
            fun handleAwtKey(awtKey: Int, isDown: Boolean) {
                when (awtKey) {
                    KeyEvent.VK_DOWN -> Key.DOWN
                    KeyEvent.VK_ESCAPE -> Key.ESC
                    KeyEvent.VK_LEFT -> Key.LEFT
                    KeyEvent.VK_RIGHT -> Key.RIGHT
                    KeyEvent.VK_UP -> Key.UP

                    else -> null
                }?.let { key -> keyboard.handleKey(key, isDown) }
            }

            override fun keyPressed(e: KeyEvent) {
                handleAwtKey(e.keyCode, true)
            }

            override fun keyReleased(e: KeyEvent) {
                handleAwtKey(e.keyCode, false)
            }
        })

        val gameLoop = GameLoop(
            GameLoop.Backend(appSystem, Assets(loaders, assetFinders), screen, timer, keyboard),
            game)

        val fpsTimer = SwingTimer(getSwingTimerDelay(params.frameTarget), null)
        fpsTimer.isRepeats = false

        // Run the game loop on the UI thread a step at a time by using Platform.runLater.
        // If we had used a background thread instead, then we'd have to worry about that
        // thread and JavaFX trying to interact with the same UI components at the same time.
        var timeStart = Instant.now()
        val gameStep = ActionListener {
            val lastStart = timeStart
            timeStart = Instant.now()
            timer.lastFrameDuration.setFrom(timeStart - lastStart)
            gameLoop.step()
            keyboard.step()

            if (!shouldQuit) {
                val timeRemaining = params.frameTarget - (Instant.now() - timeStart)
                // If our last frame ran long, we may not want to sleep as long
                val overshoot = max(timer.lastFrameDuration - params.frameTarget, Duration.ZERO)
                fpsTimer.delay = getSwingTimerDelay(timeRemaining - overshoot)
                fpsTimer.start()
            } else {
                System.exit(0)
            }
        }
        fpsTimer.addActionListener(gameStep)
        fpsTimer.start()
        frame.isVisible = true
    }

    /**
     * The Swing timer tends to run a bit slow, so just subtract by a few millis to make sure we
     * aren't slower than our target.
     */
    private fun getSwingTimerDelay(target: Duration) = max(target.millis.toInt() - 1, 0)
}