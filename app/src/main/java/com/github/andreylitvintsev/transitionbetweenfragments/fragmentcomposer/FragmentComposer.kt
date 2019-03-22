package com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer

import android.animation.Animator
import android.view.View
import androidx.annotation.IdRes
import androidx.core.animation.addListener
import androidx.fragment.app.FragmentManager


private enum class CommandType {
    TRANSACTION, ANIMATION, TRANSFORM, WAIT
}

private class CommandDescriptor(
    val commandType: CommandType,
    var nextCommand: CommandDescriptor? = null,
    val command: () -> Unit
)

class FragmentComposer(
    private val fragmentManager: FragmentManager
) {
    private val commands = arrayListOf<CommandDescriptor>()

    private var currentCommandIndex = -1

    private var currentFragment: BaseFragment? = null

    fun add(@IdRes containerViewId: Int, baseFragment: BaseFragment): FragmentComposer {
        newCommand(CommandType.TRANSACTION) {
            currentFragment = baseFragment
            fragmentManager.beginTransaction().add(containerViewId, baseFragment).commit()
            launchNextCommandForTransaction(baseFragment)
        }
        return this@FragmentComposer
    }

    fun remove(baseFragment: BaseFragment): FragmentComposer {
        newCommand(CommandType.TRANSACTION) {
            currentFragment = baseFragment
            fragmentManager.beginTransaction().remove(baseFragment).commit()
            launchNextCommandForTransaction(baseFragment)
        }
        return this@FragmentComposer
    }

    fun animate(animationCreating: (view: View, baseFragment: BaseFragment) -> Animator): FragmentComposer {
        newCommand(CommandType.ANIMATION) {
            val nonNullableCurrentFragment = currentFragment
                ?: throw IllegalStateException("Must be fragment added before 'animate' method!")

            val nonNullableFragmentView = nonNullableCurrentFragment.view
                ?: throw IllegalStateException("Fragment must be have the view!")

            with(animationCreating.invoke(nonNullableFragmentView, nonNullableCurrentFragment)) {
                start()
                launchNextCommandForAnimation(this)
            }
        }
        return this@FragmentComposer
    }

    fun transform(viewTransformation: (view: View, baseFragment: BaseFragment) -> Unit): FragmentComposer {
        newCommand(CommandType.TRANSFORM) {
            currentFragment?.setOnViewCreatedListener(needInvokeAfterEvent = true) {
                val nonNullableCurrentFragment = currentFragment
                    ?: throw IllegalStateException("Must be fragment added before 'transform' method!")

                val nonNullableFragmentView = nonNullableCurrentFragment.view
                    ?: throw IllegalStateException("Fragment must be have the view!")

                viewTransformation.invoke(nonNullableFragmentView, nonNullableCurrentFragment)

                nextCommandDescriptor()?.command?.invoke()
            }
        }
        return this@FragmentComposer
    }

    fun waitForViewLayoutChanged(): FragmentComposer {
        newCommand(CommandType.WAIT) {
            currentFragment?.setOnViewLayoutChanged(needInvokeAfterEvent = true) {
                nextCommandDescriptor()?.command?.invoke()
                currentFragment?.setOnViewLayoutChanged(listener = null)
            }
        }
        return this@FragmentComposer
    }

    private inline fun newCommand(commandType: CommandType, crossinline commandBody: () -> Unit) {
        commands.add(
            CommandDescriptor(
                commandType
            ) {
                ++currentCommandIndex

                commandBody.invoke()

                if (commands.size > 1) {
                    commands[commands.size - 2].nextCommand = commands[commands.size - 1]
                }
            })
    }

    private fun launchNextCommandForTransaction(baseFragment: BaseFragment) {
        if (checkNextCommandForType(CommandType.ANIMATION)) {
            baseFragment.setOnResumeListener(needInvokeAfterEvent = true) {
                nextCommandDescriptor()?.command?.invoke()
            }
        } else {
            nextCommandDescriptor()?.command?.invoke()
        }
    }

    private fun launchNextCommandForAnimation(fragmentAnimator: Animator) {
        if (hasNextCommand()) {
            fragmentAnimator.addListener(onEnd = {
                nextCommandDescriptor()?.command?.invoke()
            })
        }
    }

    private fun nextCommandDescriptor(): CommandDescriptor? = commands.getOrNull(currentCommandIndex + 1)

    private fun hasNextCommand(): Boolean = (commands.size - 1) != currentCommandIndex

    private fun checkNextCommandForType(commandType: CommandType): Boolean {
        return (hasNextCommand()) && commands[currentCommandIndex + 1].commandType == commandType
    }

    fun letsGo() {
        if (commands.size > 0) {
            commands[0].command.invoke()
        }
    }

}
