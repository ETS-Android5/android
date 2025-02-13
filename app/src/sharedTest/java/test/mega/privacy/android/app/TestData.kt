package test.mega.privacy.android.app

import mega.privacy.android.app.domain.entity.UserAccount
import mega.privacy.android.app.domain.entity.user.UserId
import mega.privacy.android.app.utils.Constants

internal val TEST_USER_ACCOUNT = UserAccount(
    userId = UserId(1),
    email = "email@email.com",
    isBusinessAccount = false,
    isMasterBusinessAccount = false,
    accountTypeIdentifier = Constants.FREE,
)