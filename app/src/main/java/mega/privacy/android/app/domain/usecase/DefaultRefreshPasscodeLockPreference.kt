package mega.privacy.android.app.domain.usecase

import mega.privacy.android.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Default refresh passcode lock preference
 *
 * @property settingsRepository
 */
class DefaultRefreshPasscodeLockPreference @Inject constructor(private val settingsRepository: SettingsRepository): RefreshPasscodeLockPreference {
    override fun invoke(): Boolean {
        if (!settingsRepository.isPasscodeLockPreferenceEnabled()) {
            settingsRepository.setPasscodeLockEnabled(false)
        }
        return settingsRepository.isPasscodeLockPreferenceEnabled()
    }
}