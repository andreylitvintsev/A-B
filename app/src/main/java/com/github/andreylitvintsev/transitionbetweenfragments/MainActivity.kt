package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer.BaseFragment
import com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer.FragmentComposer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val fragments = arrayOf(FragmentA(), FragmentB())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(
            R.id.fragmentContainer,
            fragments[0]
        ).commit()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.fragment1 -> {
                openFragmentWithAnimation(fragments[0])
                true
            }
            R.id.fragment2 -> {
                openFragmentWithAnimation(fragments[1])
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFragmentWithAnimation(newFragment: BaseFragment) {
        Log.d("NON COMPOSER", "----------------------------")
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as BaseFragment

        FragmentComposer(supportFragmentManager)
//            .setTargetFragment(currentFragment)
//            .animate { view, _ ->
//                return@animate AnimatorInflater.loadAnimator(this, R.animator.move_to_back).apply {
//                    setTarget(view)
//                }
//            }
            .add(R.id.fragmentContainer, newFragment)
            .waitForViewLayoutChanged() // вьюха еще не готова для анимирования, проблема в том что не сбрасывается флаг у все время существующего фрагмента + вообще по идее не нужно вызывать
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
            .letsGo()
    }

}
