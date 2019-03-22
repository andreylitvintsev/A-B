package com.github.andreylitvintsev.transitionbetweenfragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer.FragmentComposer
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
                true
            }
            R.id.fragment2 -> {
                FragmentComposer(supportFragmentManager)
                    .add(R.id.fragmentContainer, FragmentA())
                    .waitForViewLayoutChanged()
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

}
