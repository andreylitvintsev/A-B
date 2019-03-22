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
            nextCommandDescriptor()?.command?.invoke()
        }
        return this@FragmentComposer
    }

    fun setTargetFragment(baseFragment: BaseFragment): FragmentComposer {
        newCommand(CommandType.TRANSACTION) {
            currentFragment = baseFragment
            nextCommandDescriptor()?.command?.invoke()
        }
        return this@FragmentComposer
    }

    fun remove(baseFragment: BaseFragment): FragmentComposer {
        newCommand(CommandType.TRANSACTION) {
            currentFragment = baseFragment
            fragmentManager.beginTransaction().remove(baseFragment).commit()
            nextCommandDescriptor()?.command?.invoke()
        }
        return this@FragmentComposer
    }

    fun animate(animationCreating: (view: View, baseFragment: BaseFragment) -> Animator): FragmentComposer {
        newCommand(CommandType.ANIMATION) {
            animationCreating.invoke(
                getSafetyCurrentFragmentView(),
                getSafetyCurrentFragment()
            ).run {
                start()
                launchNextCommandForAnimation(this)
            }
        }
        return this@FragmentComposer
    }

    fun transform(viewTransformation: (view: View, baseFragment: BaseFragment) -> Unit): FragmentComposer {
        newCommand(CommandType.TRANSFORM) {
            currentFragment?.setOnViewCreatedListener(needInvokeAfterEvent = true) {
                viewTransformation.invoke(
                    getSafetyCurrentFragmentView(),
                    getSafetyCurrentFragment()
                )
                nextCommandDescriptor()?.command?.invoke()
            }
        }
        return this@FragmentComposer
    }

    fun waitForViewLayoutChanged(): FragmentComposer {
        newCommand(CommandType.WAIT) {
            getSafetyCurrentFragment().setOnViewLayoutChanged(needInvokeAfterEvent = true) {
                currentFragment!!.setOnViewLayoutChanged(listener = null)
                nextCommandDescriptor()?.command?.invoke()
            }
        }
        return this@FragmentComposer
    }

    fun waitForViewCreated(): FragmentComposer {
        newCommand(CommandType.WAIT) {
            getSafetyCurrentFragment().setOnViewCreatedListener(needInvokeAfterEvent = true) {
                currentFragment!!.setOnViewCreatedListener(listener = null)
                nextCommandDescriptor()?.command?.invoke()
            }
        }
        return this@FragmentComposer
    }

    fun waitForFragmentResume(): FragmentComposer {
        newCommand(CommandType.WAIT) {
            getSafetyCurrentFragment().setOnResumeListener(needInvokeAfterEvent = true) {
                currentFragment!!.setOnResumeListener(listener = null)
                nextCommandDescriptor()?.command?.invoke()
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

    private fun launchNextCommandForAnimation(fragmentAnimator: Animator) { // TODO: заменить
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

    private inline fun getSafetyCurrentFragment(): BaseFragment {
        return nonNullable(currentFragment, "Must be fragment added before 'transform' method!")
    }

    private inline fun getSafetyCurrentFragmentView(): View {
        return nonNullable(currentFragment?.view, "Fragment must be have the view!")
    }

    private inline fun <T> nonNullable(nullableValue: T?, exceptionMessage: String): T {
        return nullableValue ?: throw IllegalStateException(exceptionMessage)
    }

    fun letsGo() {
        if (commands.size > 0) {
            commands[0].command.invoke()
        }
    }

}
