package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer.FragmentComposer
import com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer.PlayerFragment


class MainActivity : AppCompatActivity(), ClickableFragment.OnClickListener {

    private val fragments = arrayOf(FragmentA(), FragmentB())
    private var currentFragmentIndex = 0
    private var canClick = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) openFragment(currentFragmentIndex)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onClick(fragment: Fragment, view: View) {
        if (canClick) {
            canClick = false
            currentFragmentIndex = if (currentFragmentIndex == 0) 1 else 0
            openFragmentWithAnimation(currentFragmentIndex)
        }
    }

    private fun openFragment(fragmentIndex: Int) {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragments[0])
            .commit()
    }

    private fun openFragmentWithAnimation(fragmentIndex: Int) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as PlayerFragment

        FragmentComposer(supportFragmentManager)
            .setTargetFragment(currentFragment)
            .animate { view, _ ->
                return@animate AnimatorInflater.loadAnimator(this, R.animator.move_to_back).apply {
                    setTarget(view)
                }
            }
            .add(R.id.fragmentContainer, fragments[fragmentIndex])
            .waitForViewLayoutChanged()
            .transform { view, baseFragment ->
                (view as SupportedFractionFrameLayout).yFraction = 1f
            }
            .waitForFragmentResume()
            .animate { view, _ ->
                return@animate AnimatorInflater.loadAnimator(this, R.animator.show_up).apply {
                    setTarget(view)
                }
            }
            .remove(currentFragment)
            .notify {
                canClick = true
            }
            .letsGo()
    }

}
