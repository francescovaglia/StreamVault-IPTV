package com.streamvault.core.ui.interaction

import android.view.KeyEvent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TvComponentsTest {

    @Test
    fun enterDownWithoutLongClickIsConsumed() {
        assertThat(
            remoteActivationHandling(
                enabled = true,
                keyCode = KeyEvent.KEYCODE_ENTER,
                action = KeyEvent.ACTION_DOWN,
                hasLongClick = false,
                pressedHere = true,
            )
        ).isEqualTo(RemoteActivationHandling.Consume)
    }

    @Test
    fun enterUpWithoutLongClickActivates() {
        assertThat(
            remoteActivationHandling(
                enabled = true,
                keyCode = KeyEvent.KEYCODE_ENTER,
                action = KeyEvent.ACTION_UP,
                hasLongClick = false,
                pressedHere = true,
            )
        ).isEqualTo(RemoteActivationHandling.Activate)
    }

    @Test
    fun enterDownWithLongClickIsLeftForNativeTvSurface() {
        assertThat(
            remoteActivationHandling(
                enabled = true,
                keyCode = KeyEvent.KEYCODE_ENTER,
                action = KeyEvent.ACTION_DOWN,
                hasLongClick = true,
                pressedHere = true,
            )
        ).isEqualTo(RemoteActivationHandling.Ignore)
    }

    @Test
    fun disabledOrUnrelatedKeysAreIgnored() {
        assertThat(
            remoteActivationHandling(
                enabled = false,
                keyCode = KeyEvent.KEYCODE_ENTER,
                action = KeyEvent.ACTION_UP,
                hasLongClick = false,
                pressedHere = true,
            )
        ).isEqualTo(RemoteActivationHandling.Ignore)
        assertThat(
            remoteActivationHandling(
                enabled = true,
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
                action = KeyEvent.ACTION_UP,
                hasLongClick = false,
                pressedHere = true,
            )
        ).isEqualTo(RemoteActivationHandling.Ignore)
    }

    @Test
    fun releaseWithoutPressOnThisElementIsSwallowed() {
        assertThat(
            remoteActivationHandling(
                enabled = true,
                keyCode = KeyEvent.KEYCODE_DPAD_CENTER,
                action = KeyEvent.ACTION_UP,
                hasLongClick = false,
                pressedHere = false,
            )
        ).isEqualTo(RemoteActivationHandling.Consume)
    }
}
