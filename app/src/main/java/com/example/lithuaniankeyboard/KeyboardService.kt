package com.example.lithuaniankeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class KeyboardService : InputMethodService() {
    private var isShifted = false
    private var isCapsLock = false
    private var keyboardView: View? = null

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        setupKeyboard(keyboardView!!)
        return keyboardView!!
    }

    private fun setupKeyboard(keyboardView: View) {

        val keys = listOf(
            R.id.key_shift,
            R.id.key_ą,
            R.id.key_č,
            R.id.key_ę,
            R.id.key_ė,
            R.id.key_į,
            R.id.key_š,
            R.id.key_ų,
            R.id.key_ū,
            R.id.key_ž,
            R.id.key_q,
            R.id.key_w,
            R.id.key_e,
            R.id.key_r,
            R.id.key_t,
            R.id.key_y,
            R.id.key_u,
            R.id.key_i,
            R.id.key_o,
            R.id.key_p,
            R.id.key_a,
            R.id.key_s,
            R.id.key_d,
            R.id.key_f,
            R.id.key_g,
            R.id.key_h,
            R.id.key_j,
            R.id.key_k,
            R.id.key_l,
            R.id.key_z,
            R.id.key_x,
            R.id.key_c,
            R.id.key_v,
            R.id.key_b,
            R.id.key_n,
            R.id.key_m,
            R.id.key_delete,
            R.id.key_comma,
            R.id.key_space,
            R.id.key_dot,
            R.id.key_enter
        )

        keys.forEach { keyId ->
            val key = keyboardView.findViewById<Button>(keyId)
            key.setOnClickListener { onKeyPress(key) }
        }
    }

    private fun onKeyPress(key: Button) {
        val ic = currentInputConnection
        when (key.id) {
            R.id.key_delete -> ic.deleteSurroundingText(1, 0)
            R.id.key_enter -> ic.sendKeyEvent(
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN,
                    android.view.KeyEvent.KEYCODE_ENTER
                )
            )

            R.id.key_space -> ic.commitText(" ", 1)
            R.id.key_shift -> toggleShift()

            else -> {
                var text = key.text.toString()
                if (isShifted || isCapsLock) {
                    text = text.uppercase()
                }
                ic.commitText(text, 1)
                if (isShifted && !isCapsLock) {
                    isShifted = false;
                    updateKeyCaps() // turn off shift after one key press
                }
            }
        }
    }

    private fun toggleShift() {
        if (isShifted) {
            if (isCapsLock) {
                isCapsLock = false
            } else {
                isCapsLock = true
                isShifted = false
            }
        } else if (isCapsLock) {
            isCapsLock = false

        } else {
            isShifted = true
        }
        updateShiftKey()
        updateKeyCaps()
    }

    private fun updateShiftKey() {
        val shiftButton = keyboardView?.findViewById<Button>(R.id.key_shift)
        shiftButton?.text = when {
            isCapsLock -> "⇧⇧"
            isShifted -> "⇧"
            else -> "⇧"
        }
    }

    private fun updateKeyCaps() {
        val keys = listOf(
            R.id.key_ą,
            R.id.key_č,
            R.id.key_ę,
            R.id.key_ė,
            R.id.key_į,
            R.id.key_š,
            R.id.key_ų,
            R.id.key_ū,
            R.id.key_ž,
            R.id.key_q,
            R.id.key_w,
            R.id.key_e,
            R.id.key_r,
            R.id.key_t,
            R.id.key_y,
            R.id.key_u,
            R.id.key_i,
            R.id.key_o,
            R.id.key_p,
            R.id.key_a,
            R.id.key_s,
            R.id.key_d,
            R.id.key_f,
            R.id.key_g,
            R.id.key_h,
            R.id.key_j,
            R.id.key_k,
            R.id.key_l,
            R.id.key_z,
            R.id.key_x,
            R.id.key_c,
            R.id.key_v,
            R.id.key_b,
            R.id.key_n,
            R.id.key_m
        )

        keys.forEach { keyId ->
            val button = keyboardView?.findViewById<Button>(keyId)
            if (button != null) {
                button.text = if (isShifted || isCapsLock) {
                    button.text.toString().uppercase()
                } else {
                    button.text.toString().lowercase()
                }
            }
        }
    }
}