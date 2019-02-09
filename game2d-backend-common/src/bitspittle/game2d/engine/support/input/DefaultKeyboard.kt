package bitspittle.game2d.engine.support.input

import bitspittle.game2d.engine.api.input.Keyboard

class DefaultKeyboard : Keyboard {
    private val keysPrev = mutableSetOf<Keyboard.Key>()
    private val keysCurr = mutableSetOf<Keyboard.Key>()

    fun handleKey(key: Keyboard.Key, isDown: Boolean) {
        if (isDown) keysCurr.add(key) else keysCurr.remove(key)
    }

    fun step() {
        keysPrev.clear()
        keysPrev.addAll(keysCurr)
    }

    override fun isJustPressed(key: Keyboard.Key) = !keysPrev.contains(key) && keysCurr.contains(key)
    override fun isDown(key: Keyboard.Key) = keysCurr.contains(key)
    override fun isJustReleased(key: Keyboard.Key) = keysPrev.contains(key) && !keysCurr.contains(key)
    override fun isUp(key: Keyboard.Key) = !keysCurr.contains(key)

}