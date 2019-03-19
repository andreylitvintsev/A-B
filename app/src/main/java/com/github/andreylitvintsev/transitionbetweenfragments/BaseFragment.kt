package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment() {

    private var animator: Animator? = null

    private var animatorListener: Animator.AnimatorListener? = null
    private var onResumeListener: (() -> Unit)? = null


    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onResume() {
        super.onResume()
        onResumeListener?.invoke()
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        animator = super.onCreateAnimator(transit, enter, nextAnim)

        if (animator == null && nextAnim != 0) {
            animator = AnimatorInflater.loadAnimator(context, nextAnim)
        }

        if (animator != null && nextAnim != 0) {
//            animator.observeLifecycle(lifecycle)
        }

        if (animatorListener != null) {
            animator?.addListener(animatorListener)
        }


        return animator
    }

    fun setAnimatorListener(
        onEnd: (animator: Animator?, isCanceled: Boolean) -> Unit = { _, _ -> },
        onStart: (animator: Animator?) -> Unit = {},
        onCancel: (animator: Animator?) -> Unit = {},
        onRepeat: (animator: Animator?) -> Unit = {}
    ) {
        animatorListener = object : Animator.AnimatorListener {
            private var isCanceled = false

            override fun onAnimationEnd(animation: Animator?) {
                onEnd(animation, isCanceled)
                isCanceled = false
            }

            override fun onAnimationStart(animation: Animator?) {
                onStart(animation)
            }

            override fun onAnimationCancel(animation: Animator?) {
                isCanceled = true
                onCancel(animation)
            }

            override fun onAnimationRepeat(animation: Animator?) {
                onRepeat(animation)
            }
        }
    }

    fun setOnResumeListener(listener: (() -> Unit)) {
        onResumeListener = listener
    }

    override fun onStop() {
        animator?.cancel()
        super.onStop()
    }

}


// move to back
// show


