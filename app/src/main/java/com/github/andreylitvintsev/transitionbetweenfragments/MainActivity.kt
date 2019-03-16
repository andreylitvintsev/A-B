package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.AnimatorInflater
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private companion object {
        const val KEY_CURRENT_INDEX = "currentIndex"
    }

    private val fragments = arrayOf(FragmentA(), FragmentB())
    private var currentFragmentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState != null) currentFragmentIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX)

        openFragment(fragments[currentFragmentIndex])
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.fragment1 -> {
                currentFragmentIndex = 0
                openFragmentWithAnimation(fragments[0])
                true
            }
            R.id.fragment2 -> {
                currentFragmentIndex = 1
                openFragmentWithAnimation(fragments[1])
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFragmentWithAnimation(fragment: BaseFragment) {
        val oldFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? BaseFragment

        if (fragment.isAdded) return

        fragment.setAnimatorListener(onEnd = {
            if (oldFragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(oldFragment)
                    .commit()
            }
        })

        val moveToBackAnimator = AnimatorInflater.loadAnimator(applicationContext, R.animator.move_to_back)
        moveToBackAnimator.setTarget(oldFragment?.view)
        moveToBackAnimator.addListener(onEnd = {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.show_up, 0)
                .add(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        })
        moveToBackAnimator.start()
    }

    private fun openFragment(fragment: BaseFragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_INDEX, currentFragmentIndex)
    }
}
