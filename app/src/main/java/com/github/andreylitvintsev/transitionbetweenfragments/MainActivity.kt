package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.system.Os.remove
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.AnimatorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val fragments = arrayOf(FragmentA(), FragmentB())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragments[0]).commit()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.fragment1 -> {
                openFragment(fragments[0])
                true
            }
            R.id.fragment2 -> {
                openFragment(fragments[1])
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
