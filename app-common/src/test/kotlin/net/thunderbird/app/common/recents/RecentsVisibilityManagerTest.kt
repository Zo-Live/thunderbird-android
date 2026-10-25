package net.thunderbird.app.common.recents

import android.app.Activity
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import net.thunderbird.components.core.logging.testing.TestLogger
import net.thunderbird.core.android.testing.RobolectricTest
import net.thunderbird.core.common.appConfig.PlatformConfigProvider
import net.thunderbird.core.preference.GeneralSettings
import net.thunderbird.core.preference.GeneralSettingsManager
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment

class RecentsVisibilityManagerTest : RobolectricTest() {

    private val application = RuntimeEnvironment.getApplication()
    private val logger = TestLogger()
    private val appTaskController = FakeAppTaskController()
    private val generalSettingsManager = FakeGeneralSettingsManager()

    @Test
    fun `excludes app tasks when hide from recents is enabled`() {
        // Arrange
        generalSettingsManager.setHideFromRecents(true)

        // Act
        createRecentsVisibilityManager()

        // Assert
        assertThat(appTaskController.excludeFromRecentsValues).containsExactly(true)
    }

    @Test
    fun `does not exclude app tasks when hide from recents is disabled`() {
        // Arrange
        generalSettingsManager.setHideFromRecents(false)

        // Act
        createRecentsVisibilityManager()

        // Assert
        assertThat(appTaskController.excludeFromRecentsValues).containsExactly(false)
    }

    @Test
    fun `updates app tasks when the hide from recents setting changes`() {
        // Arrange
        createRecentsVisibilityManager()
        appTaskController.clear()

        // Act
        generalSettingsManager.setHideFromRecents(true)

        // Assert
        assertThat(appTaskController.excludeFromRecentsValues).containsExactly(true)
    }

    @Test
    fun `applies current setting when an activity is resumed`() {
        // Arrange
        generalSettingsManager.setHideFromRecents(true)
        val testSubject = createRecentsVisibilityManager()
        appTaskController.clear()
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()

        // Act
        testSubject.onActivityResumed(activity)

        // Assert
        assertThat(appTaskController.excludeFromRecentsValues).containsExactly(true)
    }

    private fun createRecentsVisibilityManager(): RecentsVisibilityManager {
        return RecentsVisibilityManager(
            application = application,
            appTaskController = appTaskController,
            generalSettingsManager = generalSettingsManager,
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
            logger = logger,
        ).apply { init() }
    }
}

private class FakeAppTaskController : AppTaskController {
    val excludeFromRecentsValues = mutableListOf<Boolean>()

    override fun setExcludeFromRecents(excludeFromRecents: Boolean) {
        excludeFromRecentsValues += excludeFromRecents
    }

    fun clear() {
        excludeFromRecentsValues.clear()
    }
}

private class FakeGeneralSettingsManager : GeneralSettingsManager {
    private val generalSettings = MutableStateFlow(
        GeneralSettings(platformConfigProvider = FakePlatformConfigProvider),
    )

    override fun save(config: GeneralSettings) {
        generalSettings.value = config
    }

    override fun getConfig(): GeneralSettings = generalSettings.value

    override fun getConfigFlow(): Flow<GeneralSettings> = generalSettings

    @Deprecated(
        message = "Use PreferenceManager<GeneralSettings>.getConfig() instead",
        replaceWith = ReplaceWith("getConfig()"),
    )
    override fun getSettings(): GeneralSettings = generalSettings.value

    @Deprecated(
        message = "Use PreferenceManager<GeneralSettings>.getConfigFlow() instead",
        replaceWith = ReplaceWith("getConfigFlow()"),
    )
    override fun getSettingsFlow(): Flow<GeneralSettings> = generalSettings

    fun setHideFromRecents(isHideFromRecents: Boolean) {
        generalSettings.update { settings ->
            settings.copy(privacy = settings.privacy.copy(isHideFromRecents = isHideFromRecents))
        }
    }
}

private object FakePlatformConfigProvider : PlatformConfigProvider {
    override val isDebug: Boolean = true
}
