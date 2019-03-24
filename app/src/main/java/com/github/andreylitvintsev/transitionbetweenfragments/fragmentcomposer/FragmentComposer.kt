package com.github.andreylitvintsev.transitionbetweenfragments.fragmentcomposer

import android.animation.Animator
import android.view.View
import androidx.annotation.IdRes
import androidx.core.animation.addListener
import androidx.fragment.app.FragmentManager


private enum class CommandType {
    TRANSACTION, ANIMATION, TRANSFORM, WAIT, CALLBACK
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

    private var currentFragment: PlayerFragment? = null

    fun add(@IdRes containerViewId: Int, playerFragment: PlayerFragment): FragmentComposer {
        newCommand(CommandType.TRANSACTION) {
            currentFragment = playerFragment
            fragmentManager.beginTransaction().add(containerViewId, playerFragment).commitAllowingStateLoss()
            nextCommandDescriptor()?.command?.invoke()
        }
        return this@FragmentComposer
    }

    fun setTargetFragment(playerFragment: PlayerFragment): FragmentComposer {
        newCommand(CommandType.TRANSACTION) {
            currentFragment = playerFragment
            nextCommandDescriptor()?.command?.invoke()
        }
        return this@FragmentComposer
    }

    fun remove(playerFragment: PlayerFragment): FragmentComposer {
        newCommand(CommandType.TRANSACTION) {
            currentFragment = playerFragment
            playerFragment.cleanEventFlags()
            fragmentManager.beginTransaction().remove(playerFragment).commitAllowingStateLoss()
            nextCommandDescriptor()?.command?.invoke()
        }
        return this@FragmentComposer
    }

    fun animate(animationCreating: (view: View, playerFragment: PlayerFragment) -> Animator): FragmentComposer {
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

    fun transform(viewTransformation: (view: View, playerFragment: PlayerFragment) -> Unit): FragmentComposer {
        newCommand(CommandType.TRANSFORM) {
            viewTransformation.invoke(
                getSafetyCurrentFragmentView(),
                getSafetyCurrentFragment()
            )
            nextCommandDescriptor()?.command?.invoke()
        }
        return this@FragmentComposer
    }

    fun waitForViewLayoutChanged(): FragmentComposer {
        newCommand(CommandType.WAIT) {
            getSafetyCurrentFragment().setOnViewLayoutChanged(needInvokeAfterEvent = true) {
                // TODO: попробовать сделать обертку для (подписки -> отклика -> отписки)
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

    fun notify(id: Int = 0, callback: (id: Int) -> Unit): FragmentComposer  {
        newCommand(CommandType.CALLBACK) {
            callback.invoke(id)
            nextCommandDescriptor()?.command?.invoke()
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

    private inline fun getSafetyCurrentFragment(): PlayerFragment {
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
