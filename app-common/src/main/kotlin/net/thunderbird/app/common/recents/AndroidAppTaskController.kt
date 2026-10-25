package net.thunderbird.app.common.recents

import android.app.ActivityManager
import android.content.Context
import net.thunderbird.components.core.logging.Logger

private const val TAG = "AndroidAppTaskController"

/**
 * [AppTaskController] implementation that uses [ActivityManager.getAppTasks] and
 * [ActivityManager.AppTask.setExcludeFromRecents].
 */
class AndroidAppTaskController(
    private val context: Context,
    private val logger: Logger,
) : AppTaskController {

    // The platform can throw unchecked exceptions (e.g. IllegalArgumentException for a task that is no longer
    // running or a SecurityException) and failing to update the recents visibility shouldn't crash the app.
    @Suppress("TooGenericExceptionCaught")
    override fun setExcludeFromRecents(excludeFromRecents: Boolean) {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        if (activityManager == null) {
            logger.warn(TAG) { "ActivityManager is not available; can't update recents visibility" }
            return
        }

        activityManager.appTasks.forEach { appTask ->
            try {
                appTask.setExcludeFromRecents(excludeFromRecents)
            } catch (e: RuntimeException) {
                logger.error(tag = TAG, throwable = e) { "Failed to update recents visibility of an app task" }
            }
        }
    }
}
