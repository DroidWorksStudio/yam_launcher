package eu.ottop.yamlauncher.utils

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.core.view.children
import com.google.android.material.textfield.TextInputEditText
import eu.ottop.yamlauncher.settings.SharedPreferenceManager

class UIUtils(private val context: Context) {

    private val sharedPreferenceManager = SharedPreferenceManager(context)

    // Colors
    fun setBackground(window: Window) {
        window.decorView.setBackgroundColor(
            sharedPreferenceManager.getBgColor()
        )
    }

    fun setTextColors(view: View) {
        val color = sharedPreferenceManager.getTextColor()
        when {
            view is ViewGroup -> {
                view.children.forEach { child ->
                    setTextColors(child)
                }
            }
            hasMethod(view, "setTextColor") -> {
                (view as TextView).setTextColor(color)
                view.compoundDrawables[0]?.colorFilter =
                    BlendModeColorFilter(sharedPreferenceManager.getTextColor(), BlendMode.SRC_ATOP)
                view.compoundDrawables[2]?.colorFilter =
                    BlendModeColorFilter(sharedPreferenceManager.getTextColor(), BlendMode.SRC_ATOP)

            }
            view is TextView && view.compoundDrawables[0] != null -> {
                println(view.text)
            }
            else -> {
                view.setBackgroundColor(color)
            }
        }
    }

    private fun hasMethod(view: View, methodName: String): Boolean {
        return try {
            view.javaClass.getMethod(methodName, Int::class.java)
            true
        } catch (e: NoSuchMethodException) {
            false
        }
    }

    fun setMenuItemColors(view: TextView, alphaHex: String = "FF") {
        val viewTreeObserver = view.viewTreeObserver

        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val color = sharedPreferenceManager.getTextColor()
                view.setTextColor(setAlpha(color, alphaHex))
                view.setHintTextColor(setAlpha(color, "A9"))

                view.compoundDrawables[0]?.mutate()?.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)

            }
        }

        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    private fun setAlpha(color: Int, alphaHex: String): Int {
        val newAlpha = Integer.parseInt(alphaHex, 16)

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        return Color.argb(newAlpha, r, g, b)
    }

    // Visibility
    fun setClockVisibility(clock: TextClock) {
        val layoutParams = clock.layoutParams

        if (sharedPreferenceManager.isClockEnabled()) {
            layoutParams.height = WRAP_CONTENT
        } else {
            layoutParams.height = 1
        }
        clock.layoutParams = layoutParams
    }

    fun setDateVisibility(dateText: TextClock) {
        dateText.visibility = when (sharedPreferenceManager.isDateEnabled()) {
            true -> {
                View.VISIBLE
            }
            false -> {
                View.GONE
            }
        }
    }

    fun setSearchVisibility(searchView: TextInputEditText, replacementView: View) {
        if (sharedPreferenceManager.isSearchEnabled()) {
            searchView.visibility = View.VISIBLE
            replacementView.visibility = View.GONE
        } else {
            searchView.visibility = View.GONE
            replacementView.visibility = View.VISIBLE
        }
    }

    // Alignment
    fun setClockAlignment(clock: TextClock, dateText: TextClock) {
        val alignment = sharedPreferenceManager.getClockAlignment()
        setTextAlignment(clock, alignment)
        setTextAlignment(dateText, alignment)
    }

    fun setShortcutsAlignment(shortcuts: LinearLayout) {
        val alignment = sharedPreferenceManager.getShortcutAlignment()
        shortcuts.children.forEach {
            if (it is TextView) {
                setTextGravity(it, alignment)
                setDrawables(it, alignment)
            }
        }
    }


    fun setDrawables(shortcut: TextView, alignment: String?) {
        try {
            when (alignment) {
                "left" -> {
                    shortcut.setCompoundDrawablesWithIntrinsicBounds(
                        shortcut.compoundDrawables.filterNotNull().first(),
                        null,
                        null,
                        null
                    )
                }

                "center" -> {
                    shortcut.setCompoundDrawablesWithIntrinsicBounds(
                        shortcut.compoundDrawables.filterNotNull().first(),
                        null,
                        shortcut.compoundDrawables.filterNotNull().first(),
                        null
                    )
                }

                "right" -> {
                    shortcut.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        shortcut.compoundDrawables.filterNotNull().first(),
                        null
                    )
                }
            }
        } catch (_: Exception) {}
    }

    fun setAppAlignment(
        textView: TextView,
        editText: TextView? = null,
        regionText: TextView? = null
    ) {
        val alignment = sharedPreferenceManager.getAppAlignment()
        setTextGravity(textView, alignment)

        if (regionText != null) {
            setTextGravity(regionText, alignment)
            return
        }

        if (editText != null) {
            setDrawables(textView, alignment)
            setTextGravity(editText, alignment)
        }

    }

    fun setSearchAlignment(searchView: TextInputEditText) {
        setTextAlignment(searchView, sharedPreferenceManager.getSearchAlignment())
    }

    fun setMenuTitleAlignment(menuTitle: TextView) {
        setTextGravity(menuTitle, sharedPreferenceManager.getAppAlignment())

    }

    private fun setTextAlignment(view: TextView, alignment: String?) {
        try {
            view.textAlignment = when (alignment) {
            "left" -> View.TEXT_ALIGNMENT_VIEW_START

            "center" -> View.TEXT_ALIGNMENT_CENTER

            "right" -> View.TEXT_ALIGNMENT_VIEW_END

            else -> View.TEXT_ALIGNMENT_VIEW_START
            }
        } catch (_: Exception) {}
    }

    private fun setTextGravity(view: TextView, alignment: String?) {
        try {
            view.gravity = when (alignment) {
                "left" -> Gravity.CENTER_VERTICAL or Gravity.START

                "center" -> Gravity.CENTER

                "right" -> Gravity.CENTER_VERTICAL or Gravity.END

                else -> Gravity.CENTER_VERTICAL or Gravity.START
            }
        } catch (_: Exception) {}
    }

    // Size
    fun setClockSize(clock: TextClock) {
        setTextSize(clock, sharedPreferenceManager.getClockSize(), 48F, 58F, 70F, 78F, 82F, 84F)
    }

    fun setDateSize(dateText: TextClock) {
        setTextSize(dateText, sharedPreferenceManager.getDateSize(), 14F, 17F, 20F, 23F, 26F, 29F)
    }

    fun setShortcutsSize(shortcuts: LinearLayout) {

        val size = sharedPreferenceManager.getShortcutSize()

        shortcuts.children.forEach {
            if (it is TextView) {
                setShortcutSize(it, size)
            }
        }
    }

    private fun setShortcutSize(shortcut: TextView, size: String?) {
        try {
            when (size) {
                "tiny" -> {
                    shortcut.setAutoSizeTextTypeUniformWithConfiguration(
                        5,   // Min text size in SP
                        20,   // Max text size in SP
                        2,    // Step granularity in SP
                        TypedValue.COMPLEX_UNIT_SP // Unit of measurement
                    )
                }

                "small" -> {
                    shortcut.setAutoSizeTextTypeUniformWithConfiguration(
                        5,   // Min text size in SP
                        24,   // Max text size in SP
                        2,    // Step granularity in SP
                        TypedValue.COMPLEX_UNIT_SP // Unit of measurement
                    )
                }

                "medium" -> {
                    shortcut.setAutoSizeTextTypeUniformWithConfiguration(
                        5,   // Min text size in SP
                        28,   // Max text size in SP
                        2,    // Step granularity in SP
                        TypedValue.COMPLEX_UNIT_SP // Unit of measurement
                    )
                }

                "large" -> {
                    shortcut.setAutoSizeTextTypeUniformWithConfiguration(
                        5,   // Min text size in SP
                        32,   // Max text size in SP
                        2,    // Step granularity in SP
                        TypedValue.COMPLEX_UNIT_SP // Unit of measurement
                    )
                }

                "extra" -> {
                    shortcut.setAutoSizeTextTypeUniformWithConfiguration(
                        5,   // Min text size in SP
                        36,   // Max text size in SP
                        2,    // Step granularity in SP
                        TypedValue.COMPLEX_UNIT_SP // Unit of measurement
                    )
                }

                "huge" -> {
                    shortcut.setAutoSizeTextTypeUniformWithConfiguration(
                        5,   // Min text size in SP
                        40,   // Max text size in SP
                        2,    // Step granularity in SP
                        TypedValue.COMPLEX_UNIT_SP // Unit of measurement
                    )
                }
            }
        } catch(_: Exception) {}
    }

    fun setAppSize(
        textView: TextView,
        editText: TextInputEditText? = null,
        regionText: TextView? = null
    ) {
        val size = sharedPreferenceManager.getAppSize()
        setTextSize(textView, size, 21F, 24F, 27F, 30F, 33F, 36F)
        if (editText != null) {
            setTextSize(editText, size, 21F, 24F, 27F, 30F, 33F, 36F)
        }
        if (regionText != null) {
            setTextSize(regionText, size, 11F, 14F, 17F, 20F, 23F, 26F)
        }
    }

    fun setSearchSize(searchView: TextInputEditText) {
        setTextSize(searchView, sharedPreferenceManager.getSearchSize(), 18F, 21F, 25F, 27F, 30F, 33F)
    }

    private fun setTextSize(view: TextView, size: String?, t: Float, s: Float, m: Float, l: Float, x: Float, h: Float) {
        try {
            view.textSize = when (size) {
                "tiny" -> t

                "small" -> s

                "medium" -> m

                "large" -> l

                "extra" -> x

                "huge" -> h

                else -> {
                    0F
                }
            }
        } catch (_: Exception) {}
    }

    // Status bar visibility
    fun setStatusBar(window: Window) {
        val windowInsetsController = window.insetsController

        windowInsetsController?.let {
            if (sharedPreferenceManager.isBarVisible()) {
                it.show(WindowInsets.Type.statusBars())
            }
            else {
                it.hide(WindowInsets.Type.statusBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    fun setShortcutsSpacing(shortcuts: LinearLayout) {
        val shortcutWeight = sharedPreferenceManager.getShortcutWeight()
        shortcuts.children.forEach {
            if (it is TextView) {
                setShortcutSpacing(it, shortcutWeight)
            }
        }
    }

    private fun setShortcutSpacing(shortcut: TextView, shortcutWeight: Float?) {
        val layoutParams = shortcut.layoutParams as LinearLayout.LayoutParams

        if (shortcutWeight != null) {
            layoutParams.weight = shortcutWeight
        }

        shortcut.layoutParams = layoutParams
    }

    fun setAppSpacing(app: TextView) {
        val spacing = sharedPreferenceManager.getAppSpacing()
        if (spacing != null) {
            val spacingPx = dpToPx(spacing)
            app.setPadding(app.paddingLeft, spacingPx, app.paddingRight, spacingPx)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}