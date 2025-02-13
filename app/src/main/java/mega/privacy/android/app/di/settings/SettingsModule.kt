package mega.privacy.android.app.di.settings

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mega.privacy.android.app.data.repository.DefaultSettingsRepository
import mega.privacy.android.app.domain.repository.SettingsRepository
import mega.privacy.android.app.domain.usecase.*
import javax.inject.Singleton
import kotlin.contracts.ExperimentalContracts

/**
 * Settings module
 *
 * Provides dependencies used by multiple screens in the settings package
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    abstract fun bindSetChatLogsEnabled(useCase: DefaultSetChatLogsEnabled): SetChatLogsEnabled

    @Binds
    abstract fun bindSetSdkLogsEnabled(useCase: DefaultSetSdkLogsEnabled): SetSdkLogsEnabled

    @Binds
    abstract fun bindAreChatLogsEnabled(useCase: DefaultAreChatLogsEnabled): AreChatLogsEnabled

    @Binds
    abstract fun bindAreSdkLogsEnabled(useCase: DefaultAreSdkLogsEnabled): AreSdkLogsEnabled
}
