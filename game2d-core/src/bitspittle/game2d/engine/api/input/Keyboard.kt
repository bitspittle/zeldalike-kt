package bitspittle.game2d.engine.api.input

interface Keyboard {
    enum class Key {
        ESC
        // TODO: Add way more keys!
    }

    fun isJustPressed(key: Key): Boolean
    fun isDown(key: Key): Boolean
    fun isJustReleased(key: Key): Boolean
    fun isUp(key: Key): Boolean
}