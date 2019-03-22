package com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment(), View.OnLayoutChangeListener {

    private var resumed = false
    private var onResumeListener: (() -> Unit)? = null

    private var viewCreated = false
    private var onViewCreatedListener: (() -> Unit)? = null

    private var viewLayoutChanged = false
    private var onViewLayoutChangeListener: (() -> Unit)? = null

    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.addOnLayoutChangeListener(this)

        if (onViewCreatedListener != null) {
            viewCreated = false
            onViewCreatedListener?.invoke()
        } else {
            viewCreated = true
        }
    }

    override fun onResume() {
        super.onResume()

        if (onResumeListener != null) {
            resumed = false
            onResumeListener?.invoke()
        } else {
            resumed = true
        }
    }

    override fun onDestroyView() {
        view!!.removeOnLayoutChangeListener(this)
        super.onDestroyView()
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (onViewLayoutChangeListener != null) {
            viewLayoutChanged = false
            onViewLayoutChangeListener?.invoke()
        } else {
            viewLayoutChanged = true
        }
    }

    fun setOnResumeListener(needInvokeAfterEvent: Boolean = false, listener: (() -> Unit)?) {
        onResumeListener = listener
        if (needInvokeAfterEvent && resumed) { // когда фрагмент создается обычным способом, у нас не сбрасывается флаг если мы не подписывались
            onResumeListener?.invoke()
            resumed = false
        }
    }
    // создали -> запустили A -> !!!установились флаги но не сбросились!!! -> запустили B -> ...
    // идеи: может сбрасывать при удалении?

    fun setOnViewCreatedListener(needInvokeAfterEvent: Boolean = false, listener: (() -> Unit)?) {
        onViewCreatedListener = listener
        if (needInvokeAfterEvent && viewCreated) {
            onViewCreatedListener?.invoke()
            viewCreated = false
        }
    }

    fun setOnViewLayoutChanged(needInvokeAfterEvent: Boolean = false, listener: (() -> Unit)?) {
        onViewLayoutChangeListener = listener
        if (needInvokeAfterEvent && viewLayoutChanged) {
            onViewLayoutChangeListener?.invoke()
            viewLayoutChanged = false
        }
    }

    internal fun cleanEventFlags() { // TODO: подумать над очисткой (количество флагов будет расти)
        resumed = false
        viewCreated = false
        viewLayoutChanged = false
    }

}
