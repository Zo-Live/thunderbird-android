package net.thunderbird.app.common.recents

/**
 * Controls whether the app's tasks are listed in the system's recent tasks screen.
 */
interface AppTaskController {

    /**
     * Updates the "exclude from recents" flag of all tasks belonging to this app.
     *
     * When the flag is set the tasks are no longer shown in the recent tasks screen. This only affects the entry in
     * the recent tasks screen; the app keeps running in the background, including all foreground services and their
     * notifications.
     *
     * @param excludeFromRecents `true` to hide the app's tasks from the recent tasks screen, `false` to show them.
     */
    fun setExcludeFromRecents(excludeFromRecents: Boolean)
}
