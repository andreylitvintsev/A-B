package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.Animator
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent


class LifecycleSafetyWrapperForAnimator(lifecycle: Lifecycle, val animator: Animator?) : LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pauseListener() {
        animator?.cancel()
        Log.d("TAG", "pause")
    }

}

fun Animator.observeLifecycle(lifecycle: Lifecycle): Animator {
    LifecycleSafetyWrapperForAnimator(lifecycle, this)
    return this
}
