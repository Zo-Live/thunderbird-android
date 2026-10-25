package net.thunderbird.app.common.recents

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appCommonRecentsModule: Module = module {
    single<AppTaskController> {
        AndroidAppTaskController(
            context = androidApplication(),
            logger = get(),
        )
    }

    single {
        RecentsVisibilityManager(
            application = androidApplication(),
            appTaskController = get(),
            generalSettingsManager = get(),
            coroutineScope = get(named("AppCoroutineScope")),
            logger = get(),
        )
    }
}
