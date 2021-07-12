package mega.privacy.android.app.myAccount.editProfile

import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jeremyliao.liveeventbus.LiveEventBus
import mega.privacy.android.app.R
import mega.privacy.android.app.SMSVerificationActivity
import mega.privacy.android.app.activities.PasscodeActivity
import mega.privacy.android.app.components.AppBarStateChangeListener
import mega.privacy.android.app.components.twemoji.EmojiEditText
import mega.privacy.android.app.databinding.ActivityEditProfileBinding
import mega.privacy.android.app.databinding.DialogChangeNameBinding
import mega.privacy.android.app.lollipop.ChangePasswordActivityLollipop
import mega.privacy.android.app.modalbottomsheet.ModalBottomSheetUtil.isBottomSheetDialogShown
import mega.privacy.android.app.modalbottomsheet.PhotoBottomSheetDialogFragment
import mega.privacy.android.app.modalbottomsheet.phoneNumber.PhoneNumberBottomSheetDialogFragment
import mega.privacy.android.app.modalbottomsheet.phoneNumber.PhoneNumberCallback
import mega.privacy.android.app.myAccount.MyAccountViewModel
import mega.privacy.android.app.myAccount.MyAccountViewModel.Companion.PROCESSING_FILE
import mega.privacy.android.app.utils.AlertDialogUtil.isAlertDialogShown
import mega.privacy.android.app.utils.AlertDialogUtil.quitEditTextError
import mega.privacy.android.app.utils.AlertDialogUtil.setEditTextError
import mega.privacy.android.app.utils.AlertsAndWarnings.showRemoveOrModifyPhoneNumberConfirmDialog
import mega.privacy.android.app.utils.AvatarUtil.getColorAvatar
import mega.privacy.android.app.utils.AvatarUtil.getDominantColor
import mega.privacy.android.app.utils.CacheFolderManager.buildAvatarFile
import mega.privacy.android.app.utils.ChatUtil
import mega.privacy.android.app.utils.ChatUtil.StatusIconLocation
import mega.privacy.android.app.utils.ColorUtils.getColorForElevation
import mega.privacy.android.app.utils.Constants.*
import mega.privacy.android.app.utils.FileUtil
import mega.privacy.android.app.utils.StringResourcesUtils
import mega.privacy.android.app.utils.Util
import mega.privacy.android.app.utils.Util.canVoluntaryVerifyPhoneNumber
import mega.privacy.android.app.utils.Util.dp2px
import nz.mega.sdk.MegaChatApi
import nz.mega.sdk.MegaError
import java.util.*

class EditProfileActivity : PasscodeActivity(), PhotoBottomSheetDialogFragment.PhotoCallback,
    PhoneNumberCallback {

    companion object {
        private const val PADDING_BOTTOM_APP_BAR = 19F
        private const val PADDING_COLLAPSED_BOTTOM_APP_BAR = 9F
        private const val NAME_SIZE = 16F
        private const val EMAIL_SIZE = 12F
        private const val PADDING_LEFT_STATE = 8F

        private const val CHANGE_NAME_SHOWN = "CHANGE_NAME_SHOWN"
        private const val FIRST_NAME_TYPED = "FIRST_NAME_TYPED"
        private const val LAST_NAME_TYPED = "LAST_NAME_TYPED"
        private const val CHANGE_EMAIL_SHOWN = "CHANGE_EMAIL_SHOWN"
        private const val EMAIL_TYPED = "EMAIL_TYPED"
        private const val REMOVE_OR_MODIFY_PHONE_SHOWN = "REMOVE_OR_MODIFY_PHONE_SHOWN"
        private const val IS_MODIFY = "IS_MODIFY"
    }

    private val viewModel by viewModels<MyAccountViewModel>()

    private lateinit var binding: ActivityEditProfileBinding

    private var maxWidth = 0
    private var firstLineTextMaxWidthExpanded = 0
    private var stateToolbar = AppBarStateChangeListener.State.IDLE

    private var photoBottomSheet: PhotoBottomSheetDialogFragment? = null

    private var phoneNumberBottomSheetOld: PhoneNumberBottomSheetDialogFragment? = null

    private var isModify = false
    private var removeOrModifyPhoneNumberDialog: AlertDialog? = null
    private var changeNameDialog: AlertDialog? = null
    private var changeEmailDialog: AlertDialog? = null

    private val profileAvatarUpdatedObserver = Observer<Boolean> { setUpAvatar() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupObservers()

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(CHANGE_NAME_SHOWN, false)) {
                showChangeNameDialog(
                    savedInstanceState.getString(FIRST_NAME_TYPED),
                    savedInstanceState.getString(LAST_NAME_TYPED)
                )
            }

            if (savedInstanceState.getBoolean(CHANGE_EMAIL_SHOWN, false)) {
                showChangeEmailDialog(savedInstanceState.getString(EMAIL_TYPED))
            }

            if (savedInstanceState.getBoolean(REMOVE_OR_MODIFY_PHONE_SHOWN, false)) {
                showConfirmation(savedInstanceState.getBoolean(IS_MODIFY, false))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isAlertDialogShown(changeNameDialog)) {
            outState.putBoolean(CHANGE_NAME_SHOWN, true)
            outState.putString(
                FIRST_NAME_TYPED,
                changeNameDialog?.findViewById<EmojiEditText>(R.id.first_name_field)?.text.toString()
            )

            outState.putString(
                LAST_NAME_TYPED,
                changeNameDialog?.findViewById<EmojiEditText>(R.id.last_name_field)?.text.toString()
            )
        }

        if (isAlertDialogShown(changeEmailDialog)) {
            outState.putBoolean(CHANGE_EMAIL_SHOWN, true)

            outState.putString(
                EMAIL_TYPED,
                changeEmailDialog?.findViewById<EmojiEditText>(R.id.email_field)?.text.toString()
            )
        }

        if (isAlertDialogShown(removeOrModifyPhoneNumberDialog)) {
            outState.putBoolean(REMOVE_OR_MODIFY_PHONE_SHOWN, true)
            outState.putBoolean(IS_MODIFY, isModify)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        LiveEventBus.get(EVENT_AVATAR_CHANGE, Boolean::class.java)
            .removeObserver(profileAvatarUpdatedObserver)

        changeNameDialog?.dismiss()
        changeEmailDialog?.dismiss()
        removeOrModifyPhoneNumberDialog?.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val error = viewModel.manageActivityResult(this, requestCode, resultCode, data)

        if (!error.isNullOrEmpty()) {
            if (error == PROCESSING_FILE) binding.progressBar.isVisible = true
            else showSnackbar(error)
        }
    }

    private fun setupView() {
        setUpActionBar()
        setUpAvatar()
        setUpHeader()

        binding.changeName.setOnClickListener { showChangeNameDialog(null, null) }

        binding.addPhoto.setOnClickListener {
            if (isBottomSheetDialogShown(photoBottomSheet))
                return@setOnClickListener

            photoBottomSheet = PhotoBottomSheetDialogFragment()
            photoBottomSheet?.show(supportFragmentManager, photoBottomSheet?.tag)
        }

        binding.changeEmail.setOnClickListener { showChangeEmailDialog(null) }

        binding.changePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivityLollipop::class.java))
        }

        binding.addPhoneNumberLayout.apply {
            isVisible = canVoluntaryVerifyPhoneNumber()
            setOnClickListener {
                if (canVoluntaryVerifyPhoneNumber()) {
                    startActivity(
                        Intent(
                            this@EditProfileActivity,
                            SMSVerificationActivity::class.java
                        )
                    )
                } else if (!isBottomSheetDialogShown(phoneNumberBottomSheetOld)) {
                    phoneNumberBottomSheetOld = PhoneNumberBottomSheetDialogFragment()
                    phoneNumberBottomSheetOld!!.show(
                        supportFragmentManager,
                        phoneNumberBottomSheetOld!!.tag
                    )
                }
            }
        }

        binding.recoveryKeyButton.setOnClickListener { viewModel.exportMK(this) }

        binding.logoutButton.setOnClickListener { viewModel.logout(this@EditProfileActivity) }
    }

    private fun setupObservers() {
        LiveEventBus.get(EVENT_AVATAR_CHANGE, Boolean::class.java)
            .observeForever(profileAvatarUpdatedObserver)

        viewModel.onSetProfileAvatar().observe(this, ::profileAvatarSet)
        viewModel.onNameUpdated().observe(this, ::updateName)
    }

    /**
     * Shows the result of an avatar change or deletion.
     *
     * @param result Pair<Boolean, Boolean> containing the result of the request.
     *      If first value is true, indicates is an avatar change, otherwise is a deletion.
     *      If second value is true, means the action finished with success, otherwise not.
     */
    private fun profileAvatarSet(result: Pair<Boolean, Boolean>) {
        binding.progressBar.isVisible = false

        val avatarChange = result.first
        val success = result.second

        val stringId = if (success) {
            if (avatarChange) R.string.success_changing_user_avatar
            else R.string.success_deleting_user_avatar
        } else if (avatarChange) R.string.error_changing_user_avatar
        else R.string.error_deleting_user_avatar

        showSnackbar(StringResourcesUtils.getString(stringId))
    }

    /**
     * Shows the result of a name change.
     *
     * @param success True if the name was changed successfully, false otherwise.
     */
    private fun updateName(success: Boolean) {
        binding.progressBar.isVisible = false
        binding.headerLayout.firstLineToolbar.text =
            viewModel.getName().toUpperCase(Locale.getDefault())

        showSnackbar(
            StringResourcesUtils.getString(
                if (success) R.string.success_changing_user_attributes
                else R.string.error_changing_user_attributes
            )
        )
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.headerLayout.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = null

        var drawableArrow = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.ic_arrow_back_white
        )

        drawableArrow = drawableArrow?.mutate()

        val statusBarColor =
            getColorForElevation(this, resources.getDimension(R.dimen.toolbar_elevation))

        binding.headerLayout.collapseToolbar.apply {
            if (Util.isDarkMode(this@EditProfileActivity)) {
                setContentScrimColor(statusBarColor)
            }

            setStatusBarScrimColor(statusBarColor)
        }

        val white = ContextCompat.getColor(this, R.color.white_alpha_087)
        val black = ContextCompat.getColor(this, R.color.grey_087_white_087)

        binding.headerLayout.appBar.addOnOffsetChangedListener(object :
            AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                stateToolbar = state

                if (state == State.EXPANDED || state == State.COLLAPSED) {
                    val color: Int = if (state == State.EXPANDED) white else black

                    binding.headerLayout.firstLineToolbar.setTextColor(color)
                    binding.headerLayout.secondLineToolbar.setTextColor(color)

                    drawableArrow?.colorFilter =
                        PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

                    supportActionBar?.setHomeAsUpIndicator(drawableArrow)
                    visibilityStateIcon()
                }

                binding.headerLayout.secondLineToolbar.setPadding(
                    0, 0, 0,
                    dp2px(if (state == State.COLLAPSED) PADDING_COLLAPSED_BOTTOM_APP_BAR else PADDING_BOTTOM_APP_BAR)
                )
            }
        })
    }

    private fun visibilityStateIcon() {
        val status = megaChatApi.onlineStatus
        val showStatus = stateToolbar == AppBarStateChangeListener.State.EXPANDED
                && (status == MegaChatApi.STATUS_ONLINE || status == MegaChatApi.STATUS_AWAY
                || status == MegaChatApi.STATUS_BUSY || status == MegaChatApi.STATUS_OFFLINE)

        binding.headerLayout.firstLineToolbar.apply {
            maxLines =
                if (showStatus || stateToolbar == AppBarStateChangeListener.State.EXPANDED) 2
                else 1

            if (showStatus) {
                setTrailingIcon(
                    ChatUtil.getIconResourceIdByLocation(
                        this@EditProfileActivity,
                        status,
                        StatusIconLocation.STANDARD
                    ), dp2px(PADDING_LEFT_STATE)
                )
            }

            updateMaxWidthAndIconVisibility(
                if (stateToolbar == AppBarStateChangeListener.State.EXPANDED) firstLineTextMaxWidthExpanded else maxWidth,
                showStatus
            )
        }
    }

    private fun setUpAvatar() {
        val avatar = buildAvatarFile(this, megaApi.myEmail + FileUtil.JPG_EXTENSION)
        var avatarBitmap: Bitmap? = null

        if (avatar != null) {
            avatarBitmap = BitmapFactory.decodeFile(avatar.absolutePath, BitmapFactory.Options())

            if (avatarBitmap != null) {
                binding.headerLayout.toolbarImage.setImageBitmap(avatarBitmap)

                if (!avatarBitmap.isRecycled) {
                    binding.headerLayout.imageLayout.setBackgroundColor(
                        getDominantColor(
                            avatarBitmap
                        )
                    )
                }
            }
        }

        if (avatarBitmap == null) {
            binding.headerLayout.toolbarImage.setImageDrawable(null)
            binding.headerLayout.imageLayout.setBackgroundColor(getColorAvatar(megaApi.myUser))
        }
    }

    private fun setUpHeader() {
        firstLineTextMaxWidthExpanded = outMetrics.widthPixels - dp2px(108f, outMetrics)
        binding.headerLayout.firstLineToolbar.apply {
            setMaxWidthEmojis(firstLineTextMaxWidthExpanded)
            text = viewModel.getName().toUpperCase(Locale.getDefault())
            textSize = NAME_SIZE
        }

        maxWidth = dp2px(
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MAX_WIDTH_APPBAR_PORT
            else MAX_WIDTH_APPBAR_LAND,
            outMetrics
        )

        binding.headerLayout.secondLineToolbar.apply {
            maxWidth = maxWidth
            text = viewModel.getEmail()
            textSize = EMAIL_SIZE
        }
    }

    private fun showChangeNameDialog(firstName: String?, lastName: String?) {
        val dialogBinding = DialogChangeNameBinding.inflate(layoutInflater)

        changeNameDialog = MaterialAlertDialogBuilder(this)
            .setTitle(StringResourcesUtils.getString(R.string.change_name_action))
            .setView(dialogBinding.root)
            .setPositiveButton(StringResourcesUtils.getString(R.string.save_action), null)
            .setNegativeButton(StringResourcesUtils.getString(R.string.button_cancel), null)
            .create()

        changeNameDialog?.apply {
            setOnShowListener {
                quitEditTextError(dialogBinding.firstNameLayout, dialogBinding.firstNameErrorIcon)
                quitEditTextError(dialogBinding.lastNameLayout, dialogBinding.lastNameErrorIcon)

                dialogBinding.firstNameField.apply {
                    setText(firstName ?: viewModel.getFirstName())
                    setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            getButton(AlertDialog.BUTTON_POSITIVE).performClick()
                        }

                        false
                    }

                    doAfterTextChanged {
                        quitEditTextError(
                            dialogBinding.firstNameLayout,
                            dialogBinding.firstNameErrorIcon
                        )
                    }
                }

                dialogBinding.lastNameField.apply {
                    setText(lastName ?: viewModel.getLastName())
                    setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            getButton(AlertDialog.BUTTON_POSITIVE).performClick()
                        }

                        false
                    }

                    doAfterTextChanged {
                        quitEditTextError(
                            dialogBinding.lastNameLayout,
                            dialogBinding.lastNameErrorIcon
                        )
                    }
                }

                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    var errorShown = false

                    if (dialogBinding.firstNameField.text.isNullOrEmpty()) {
                        errorShown = true
                        setEditTextError(
                            StringResourcesUtils.getString(R.string.error_enter_username),
                            dialogBinding.firstNameLayout, dialogBinding.firstNameErrorIcon
                        )
                    }

                    if (dialogBinding.lastNameField.text.isNullOrEmpty()) {
                        errorShown = true
                        setEditTextError(
                            StringResourcesUtils.getString(R.string.error_enter_userlastname),
                            dialogBinding.lastNameLayout, dialogBinding.lastNameErrorIcon
                        )
                    }

                    if (!errorShown) {
                        binding.progressBar.isVisible =
                            viewModel.changeName(
                                dialogBinding.firstNameField.text.toString(),
                                dialogBinding.lastNameField.text.toString()
                            )

                        dismiss()
                    }
                }
            }

            show()
        }
    }

    private fun showChangeEmailDialog(email: String?) {

    }

    override fun capturePhoto() {
        viewModel.capturePhoto(this)
    }

    override fun choosePhoto() {
        viewModel.launchChoosePhotoIntent(this)
    }

    override fun deletePhoto() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setMessage(R.string.confirmation_delete_avatar)
            .setPositiveButton(R.string.context_delete) { _, _ -> viewModel.deleteProfileAvatar(this) }
            .setNegativeButton(R.string.general_cancel, null).show()
    }

    override fun showConfirmation(isModify: Boolean) {
        this.isModify = isModify
        removeOrModifyPhoneNumberDialog =
            showRemoveOrModifyPhoneNumberConfirmDialog(this, isModify, this)
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun onReset(error: MegaError) {
        TODO("Not yet implemented")
    }

    override fun onUserDataUpdate(error: MegaError) {
        TODO("Not yet implemented")
    }

    private fun showSnackbar(message: String) {
        showSnackbar(binding.root, message)
    }
}