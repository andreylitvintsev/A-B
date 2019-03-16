package com.github.andreylitvintsev.transitionbetweenfragments

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout


class SupportedFractionFrameLayout : FrameLayout {

    constructor(context: Context) : super(
        context
    )

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    val displaySize: Point
        get() {
            val displayMetrics = DisplayMetrics()
            val windowManager = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }

    var xFraction: Float
        get() {
            return x / displaySize.x
        }
        set(value) {
            if (value !in -1f..1f) throw IllegalArgumentException("Argument of xFraction parameter must be in range [-1;1]")
            x = value * displaySize.x
        }

    var yFraction: Float
        get() {
            return y / displaySize.y
        }
        set(value) {
            if (value !in -1f..1f) throw IllegalArgumentException("Argument of yFraction parameter must be in range [-1;1]")
            y = value * displaySize.y
        }

    var pivotFractionX: Float
        get() {
            return pivotX / displaySize.x
        }
        set(value) {
            if (value !in -1f..1f) throw IllegalArgumentException("Argument of pivotFractionX parameter must be in range [-1;1]")
            pivotX = value * displaySize.x
        }

    var pivotFractionY: Float
        get() {
            return pivotY / displaySize.y
        }
        set(value) {
            Log.d("TAG", value.toString())
            if (value !in -1f..1f) throw IllegalArgumentException("Argument of pivotFractionY parameter must be in range [-1;1]")
            pivotY = value * displaySize.y
        }

}
