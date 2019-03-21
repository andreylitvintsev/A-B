package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val fragments = arrayOf(FragmentA(), FragmentB())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragments[0]).commit()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.fragment1 -> {
//                openFragment(fragments[0])
                true
            }
            R.id.fragment2 -> {
//                openFragment(fragments[1])

                FragmentComposer(supportFragmentManager)
                    .remove(fragments[0])
                    .add(R.id.fragmentContainer, fragments[1])
                    .animate { view, baseFragment ->
                        return@animate ObjectAnimator.ofFloat(view, "rotation", 45f)
                            .setDuration(1000L)
                    }
                    .remove(fragments[1])
                    .add(R.id.fragmentContainer, FragmentA())
                    .transform { view, baseFragment ->
                        (view as SupportedFractionFrameLayout).yFraction = 0f
                    }
                    .animate { view, baseFragment ->
                        return@animate AnimatorInflater.loadAnimator(this, R.animator.show_up).apply {
                            setTarget(view)
                        }
                    }
                .letsGo()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFragment(fragment: BaseFragment) {
        val oldFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        // уводим назад (с чем работаем)
        // добавляем с анимацией новый фрагмент (куда)
        // анимируем (с чем работаем)
        // удаляем задний (куда)

        // FragmentManipulation(

        // в какой момент мы узнаем что вьюха готова?
        // как управляем задержкой вызовов "инструкций"?

        (oldFragment as? BaseFragment)?.setAnimatorListener(onEnd = { a, b ->
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.show_up, R.animator.move_to_back)
                .add(R.id.fragmentContainer, fragment)
                .commit()
        })

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.animator.show_up, R.animator.move_to_back)
            .apply { if (oldFragment != null) remove(oldFragment) }
            .commit()

    }

}
