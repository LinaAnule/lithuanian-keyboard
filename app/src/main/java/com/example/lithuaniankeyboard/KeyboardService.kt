package com.example.lithuaniankeyboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowId
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.contentValuesOf

class KeyboardService : InputMethodService() {
    private var isShifted = false
    private var isCapsLock = false
    private var keyboardView: View? = null
    private var popupWindow: PopupWindow? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isDeleting = false
    private var isLongPressed = false

    private val deleteRunnable = object : Runnable {
        override fun run() {
            val ic = currentInputConnection
            ic.deleteSurroundingText(1, 0)
            handler.postDelayed(this, 100) // Adjust the delay for desired speed
        }
    }

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        setupKeyboard(keyboardView!!)
        return keyboardView!!
    }

    @SuppressLint("ClickableViewAccessibility")
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

            if (keyId == R.id.key_dot) {
                var longPressHandler: Handler? = null
                var longPressRunnable: Runnable? = null

                key.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isLongPressed = false
                            longPressHandler = Handler(Looper.getMainLooper())
                            longPressRunnable = Runnable {
                                showPopupWindow(key)
                                isLongPressed = true
                            }
                            longPressHandler?.postDelayed(
                                longPressRunnable!!,
                                ViewConfiguration.getLongPressTimeout().toLong()
                            )
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            longPressHandler?.removeCallbacks(longPressRunnable!!)
                            if (!isLongPressed) {
                                onKeyPress(v as Button)
                            }
                            isLongPressed = false
                        }
                    }
                    true
                }
            } else if (keyId == R.id.key_delete) {
                key.setOnLongClickListener {
                    isDeleting = true
                    handler.post(deleteRunnable)
                    true
                }
                key.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (isDeleting) {
                                handler.removeCallbacks(deleteRunnable)
                                isDeleting = false
                            }
                        }
                    }
                    false
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopupWindow(view: View) {
        // Inflate your custom layout
        val customView: View = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_symbols, FrameLayout(view.context))

        // Create a PopupWindow instance
        val popupWindow = PopupWindow(view.context)
        popupWindow.contentView = customView

        // Set the width and height of the PopupWindow
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // Optional: Set background drawable to dismiss when touching outside
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        // Show the PopupWindow below the anchor view
        popupWindow.showAsDropDown(view, 0, -view.height)

        // Set onTouchListeners for the buttons in the popup
        customView.findViewById<Button>(R.id.symbol_question).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                handleSymbolSelection("?")
                popupWindow.dismiss()
            }
            true
        }

        customView.findViewById<Button>(R.id.symbol_exclamation).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                handleSymbolSelection("!")
                popupWindow.dismiss()
            }
            true
        }
    }

    private fun handleSymbolSelection(symbol: String) {
        val ic = currentInputConnection
        ic.commitText(symbol, 1)
    }


    private fun onKeyPress(key: Button) {
        val ic = currentInputConnection

        // Show key preview popup
        showKeyPreview(key)

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
                    isShifted = false
                    updateKeyCaps()
                }
            }
        }

        // Hide key preview popup after a delay
        key.postDelayed({ hideKeyPreview() }, 500)
    }

    private fun showKeyPreview(key: Button) {
        val inflater = LayoutInflater.from(key.context)
        val view = inflater.inflate(R.layout.popup_key_preview, null)
        val textView = view.findViewById<TextView>(R.id.popup_text)
        textView.text = key.text

        popupWindow = PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow?.isFocusable = false
        popupWindow?.isOutsideTouchable = false

        val location = IntArray(2)
        key.getLocationInWindow(location)
        val x = location[0] + view.measuredWidth / 4
        val y = location[1] - view.measuredHeight- key.height / 2 - 40

        popupWindow?.showAtLocation(key.rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun hideKeyPreview() {
        popupWindow?.dismiss()
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
        if (shiftButton != null) {
            shiftButton.isActivated = isCapsLock
            shiftButton.isSelected = isShifted
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