package com.androidagent.tools.core

/**
 * Android KeyEvent constants for use with press_key tool.
 * These values correspond to Android's KeyEvent.KEYCODE_* constants.
 */
object KeyEventConstants {
    // Navigation keys
    const val BACK = 4
    const val HOME = 3
    const val MENU = 82
    const val SEARCH = 84

    // Directional keys
    const val DPAD_UP = 19
    const val DPAD_DOWN = 20
    const val DPAD_LEFT = 21
    const val DPAD_RIGHT = 22
    const val DPAD_CENTER = 23

    // Input keys
    const val ENTER = 66
    const val TAB = 61
    const val ESCAPE = 111
    const val DELETE = 67
    const val DEL = 67  // Alias for DELETE
    const val SPACE = 62

    // Media keys
    const val MEDIA_PLAY = 126
    const val MEDIA_PAUSE = 127
    const val MEDIA_PLAY_PAUSE = 85
    const val MEDIA_STOP = 86
    const val MEDIA_NEXT = 87
    const val MEDIA_PREVIOUS = 88

    // Volume keys
    const val VOLUME_UP = 24
    const val VOLUME_DOWN = 25
    const val VOLUME_MUTE = 164

    // Power
    const val POWER = 26

    // Camera
    const val CAMERA = 27
    const val FOCUS = 80

    // Call keys
    const val CALL = 5
    const val ENDCALL = 6

    /**
     * Maps string key names to their corresponding key codes.
     */
    fun fromName(name: String): Int? = when (name.lowercase()) {
        "back" -> BACK
        "home" -> HOME
        "menu" -> MENU
        "search" -> SEARCH
        "up", "dpad_up" -> DPAD_UP
        "down", "dpad_down" -> DPAD_DOWN
        "left", "dpad_left" -> DPAD_LEFT
        "right", "dpad_right" -> DPAD_RIGHT
        "center", "dpad_center", "enter" -> ENTER
        "tab" -> TAB
        "escape", "esc" -> ESCAPE
        "delete", "del", "backspace" -> DELETE
        "space" -> SPACE
        "volume_up", "vol_up" -> VOLUME_UP
        "volume_down", "vol_down" -> VOLUME_DOWN
        "volume_mute", "mute" -> VOLUME_MUTE
        "power" -> POWER
        "camera" -> CAMERA
        "play" -> MEDIA_PLAY
        "pause" -> MEDIA_PAUSE
        "play_pause" -> MEDIA_PLAY_PAUSE
        "stop" -> MEDIA_STOP
        "next" -> MEDIA_NEXT
        "previous", "prev" -> MEDIA_PREVIOUS
        "call" -> CALL
        "endcall" -> ENDCALL
        else -> null
    }
}