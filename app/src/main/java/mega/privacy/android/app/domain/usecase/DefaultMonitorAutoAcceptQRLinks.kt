package mega.privacy.android.app.domain.usecase

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import mega.privacy.android.app.domain.entity.user.UserChanges
import mega.privacy.android.app.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Default monitor auto accept QR links us case
 *
 * @property fetchAutoAcceptQRLinks
 * @property getAccountDetails
 * @property accountRepository
 */
class DefaultMonitorAutoAcceptQRLinks @Inject constructor(
    private val fetchAutoAcceptQRLinks: FetchAutoAcceptQRLinks,
    private val getAccountDetails: GetAccountDetails,
    private val accountRepository: AccountRepository,
) : MonitorAutoAcceptQRLinks {
    override fun invoke(): Flow<Boolean> {
        return flow {
            val loggedInUser = getAccountDetails(false).userId

            emit(fetchAutoAcceptQRLinks())

            emitAll(
                accountRepository.monitorUserUpdates()
                    .map { (changes) ->
                        changes
                            .filter { (key, value) ->
                                key == loggedInUser && value.contains(
                                    UserChanges.ContactLinkVerification
                                )
                            }
                    }
                    .filter { it.isNotEmpty() }
                    .map { fetchAutoAcceptQRLinks() }
            )

            awaitCancellation()
        }
    }

}
