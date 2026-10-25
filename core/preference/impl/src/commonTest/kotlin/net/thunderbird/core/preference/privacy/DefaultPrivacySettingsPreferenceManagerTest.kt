package net.thunderbird.core.preference.privacy

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import net.thunderbird.components.core.logging.testing.TestLogger
import net.thunderbird.core.preference.DefaultPreferenceChangeBroker
import net.thunderbird.core.preference.storage.InMemoryStorage
import net.thunderbird.core.preference.storage.Storage
import net.thunderbird.core.preference.storage.StorageEditor
import net.thunderbird.core.preference.storage.StorageProvider

class DefaultPrivacySettingsPreferenceManagerTest {

    private val logger = TestLogger()
    private val storageValues = mutableMapOf<String, String>()
    private val storageProvider = FakeStorageProvider(storageValues, logger)

    @Test
    fun `isHideFromRecents should default to false`() {
        // Arrange
        val testSubject = createPreferenceManager()

        // Assert
        assertThat(testSubject.getConfig().isHideFromRecents).isEqualTo(false)
    }

    @Test
    fun `should load isHideFromRecents from storage`() {
        // Arrange
        storageValues[PrivacySettingKey.HideFromRecents.value] = "true"

        // Act
        val testSubject = createPreferenceManager()

        // Assert
        assertThat(testSubject.getConfig().isHideFromRecents).isTrue()
    }

    @Test
    fun `should save isHideFromRecents to storage`() {
        // Arrange
        val testSubject = createPreferenceManager()

        // Act
        testSubject.save(testSubject.getConfig().copy(isHideFromRecents = true))

        // Assert
        assertThat(storageValues[PrivacySettingKey.HideFromRecents.value]).isEqualTo("true")
        assertThat(testSubject.getConfig().isHideFromRecents).isTrue()
    }

    private fun createPreferenceManager(): DefaultPrivacySettingsPreferenceManager {
        return DefaultPrivacySettingsPreferenceManager(
            logger = logger,
            storageProvider = storageProvider,
            ioDispatcher = UnconfinedTestDispatcher(),
            scope = CoroutineScope(UnconfinedTestDispatcher()),
            preferenceChangeBroker = DefaultPreferenceChangeBroker(),
        )
    }
}

private class FakeStorageProvider(
    private val values: MutableMap<String, String>,
    private val logger: TestLogger,
) : StorageProvider {

    override val storage: Storage
        get() = InMemoryStorage(values.toMap(), logger)

    override fun createStorageEditor(): StorageEditor = object : StorageEditor {
        private val updatedValues = values.toMutableMap()

        override fun commit(): Boolean {
            values.clear()
            values.putAll(updatedValues)
            return true
        }

        override fun putBoolean(key: String, value: Boolean): StorageEditor {
            updatedValues[key] = value.toString()
            return this
        }

        override fun putInt(key: String, value: Int): StorageEditor {
            updatedValues[key] = value.toString()
            return this
        }

        override fun putLong(key: String, value: Long): StorageEditor {
            updatedValues[key] = value.toString()
            return this
        }

        override fun putString(key: String, value: String?): StorageEditor {
            if (value == null) {
                updatedValues.remove(key)
            } else {
                updatedValues[key] = value
            }
            return this
        }

        override fun remove(key: String): StorageEditor {
            updatedValues.remove(key)
            return this
        }
    }
}
