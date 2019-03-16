package com.github.andreylitvintsev.transitionbetweenfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class FragmentA : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_a

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            viewTreeObserver.addOnGlobalLayoutListener {
            }
        }
    }
}
