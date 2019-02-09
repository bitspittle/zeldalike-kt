package bitspittle.game2d.engine.desktop

import bitspittle.game2d.core.graphics.Color
import bitspittle.game2d.core.math.ImmutableVec2
import bitspittle.game2d.core.time.Duration
import bitspittle.game2d.core.time.Instant
import bitspittle.game2d.engine.api.Game
import bitspittle.game2d.engine.api.app.ApplicationSystem
import bitspittle.game2d.engine.api.graphics.DrawSurface
import bitspittle.game2d.engine.api.input.Keyboard.Key
import bitspittle.game2d.engine.support.GameLoop
import bitspittle.game2d.engine.support.input.DefaultKeyboard
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import javax.swing.WindowConstants
import kotlin.math.roundToInt
import java.awt.Color as AwtColor

fun Color.toAwtColor() = AwtColor(r, g, b, a)

fun ImmutableVec2.toDimension() = Dimension(x.roundToInt(), y.roundToInt())

class GameApplication(private val params: AppParams) {
    class AppParams(
        val title: String,
        val size: ImmutableVec2,
        val frameTarget: Duration = Duration.ofSeconds(1.0 / 60.0)
    )

    private class Screen(override val size: ImmutableVec2) : JPanel(), DrawSurface {
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

        override fun paintComponent(g: Graphics) {
            val g2d = g as Graphics2D
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

        val canvas = Screen(params.size)
        frame.contentPane.add(canvas)
        frame.isResizable = false
        frame.pack()
        frame.setLocationRelativeTo(null) // This centers the frame on the Window

        val keyboard = DefaultKeyboard()
        frame.addKeyListener(object : KeyAdapter() {
            fun handleAwtKey(awtKey: Int, isDown: Boolean) {
                when (awtKey) {
                    KeyEvent.VK_ESCAPE -> Key.ESC
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
            GameLoop.Backend(appSystem, canvas, keyboard),
            game)

        val fpsTimer = Timer(params.frameTarget.millis.toInt(), null)
        fpsTimer.isRepeats = false

        // Run the game loop on the UI thread a step at a time by using Platform.runLater.
        // If we had used a background thread instead, then we'd have to worry about that
        // thread and JavaFX trying to interact with the same UI components at the same time.
        val gameStep = object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                val timeStart = Instant.now()
                gameLoop.step()
                keyboard.step()

                if (!shouldQuit) {
                    val timeRemaining = params.frameTarget - (Instant.now() - timeStart)
                    if (!timeRemaining.isZero()) {
                        fpsTimer.delay = timeRemaining.millis.toInt()
                        fpsTimer.start()
                    } else {
                        this.actionPerformed(e)
                    }
                } else {
                    System.exit(0)
                }
            }
        }
        fpsTimer.addActionListener(gameStep)
        fpsTimer.start()
        frame.isVisible = true
    }
}