package com.github.andreylitvintsev.transitionbetweenfragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer.PlayerFragment


abstract class ClickableFragment : PlayerFragment() {

    interface OnClickListener {
        fun onClick(fragment: Fragment, view: View)
    }

    private lateinit var clickListener: OnClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        clickListener = (context as? OnClickListener)
            ?: throw ClassCastException("${context::class.java.name} must implement ClickableFragment.OnClickListener")
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false).apply {
            setOnClickListener { clickListener.onClick(this@ClickableFragment, this) }
        }
    }

}
