package net.thunderbird.app.common.recents

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import net.thunderbird.components.core.logging.Logger
import net.thunderbird.core.preference.GeneralSettingsManager

private const val TAG = "RecentsVisibilityManager"

/**
 * Keeps the "exclude from recents" flag of the app's tasks in sync with the "hide from recents" privacy setting.
 *
 * The setting is applied whenever the app comes to the foreground (a new task could have been created in the
 * meantime) and whenever the value of the setting changes (so toggling it takes effect immediately).
 */
class RecentsVisibilityManager(
    private val application: Application,
    private val appTaskController: AppTaskController,
    private val generalSettingsManager: GeneralSettingsManager,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger,
) : Application.ActivityLifecycleCallbacks {

    /**
     * Starts observing the app's tasks and the privacy setting.
     *
     * Has to be called on the main thread after the settings have been loaded.
     */
    fun init() {
        application.registerActivityLifecycleCallbacks(this)

        generalSettingsManager.getConfigFlow()
            .map { settings -> settings.privacy.isHideFromRecents }
            .distinctUntilChanged()
            .onEach(::updateRecentsVisibility)
            .launchIn(coroutineScope)
    }

    override fun onActivityResumed(activity: Activity) {
        updateRecentsVisibility(generalSettingsManager.getConfig().privacy.isHideFromRecents)
    }

    private fun updateRecentsVisibility(excludeFromRecents: Boolean) {
        logger.debug(TAG) { "Updating recents visibility (excludeFromRecents=$excludeFromRecents)" }
        appTaskController.setExcludeFromRecents(excludeFromRecents)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
