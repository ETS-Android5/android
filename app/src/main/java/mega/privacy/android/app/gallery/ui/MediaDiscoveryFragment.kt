package mega.privacy.android.app.gallery.ui

import android.os.Bundle
import android.view.*
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.R
import mega.privacy.android.app.databinding.FragmentMediaDecoveryBinding
import mega.privacy.android.app.fragments.homepage.*
import mega.privacy.android.app.gallery.data.GalleryCard
import mega.privacy.android.app.gallery.data.GalleryItem
import mega.privacy.android.app.gallery.fragment.BaseZoomFragment
import mega.privacy.android.app.utils.*
import mega.privacy.android.app.utils.Constants.*
import java.util.*

@AndroidEntryPoint
class MediaDiscoveryFragment : BaseZoomFragment() {

    override val viewModel by viewModels<MediaViewModel>()

    private lateinit var binding: FragmentMediaDecoveryBinding

    /**
     * Current order.
     */
    private var order = 0

    companion object {
        private var instance: MediaDiscoveryFragment? = null

        fun getInstance(): MediaDiscoveryFragment {
            if (instance == null) {
                instance = MediaDiscoveryFragment()
            }
            return instance!!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        order = viewModel.getOrder()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediaDecoveryBinding.inflate(inflater, container, false)
        adapterType = MEDIA_BROWSE_ADAPTER
        setupBinding()
        setupParentActivityUI()
        return binding.root
    }

    private fun setupParentActivityUI() {
        mManagerActivity.setToolbarTitle()
        mManagerActivity.invalidateOptionsMenu()
        mManagerActivity.hideFabButton()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewCreated()
        subscribeObservers()
    }

    private fun setupBinding() {
        binding.apply {
            viewModel = this@MediaDiscoveryFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
            viewTypePanel = photosViewType.root
        }

        listView = binding.photoList
        scroller = binding.scroller
        viewTypePanel = binding.photosViewType.root
        yearsButton = binding.photosViewType.yearsButton
        monthsButton = binding.photosViewType.monthsButton
        daysButton = binding.photosViewType.daysButton
        allButton = binding.photosViewType.allButton
    }

    private fun initViewCreated() {
        val currentZoom = ZoomUtil.MEDIA_ZOOM_LEVEL
        zoomViewModel.setCurrentZoom(currentZoom)
        zoomViewModel.setZoom(currentZoom)
        viewModel.mZoom = currentZoom
        setupEmptyHint()
        setupListView()
        setupTimePanel()
        setupListAdapter(currentZoom, viewModel.items.value)
    }

    override fun handleZoomChange(zoom: Int, needReload: Boolean) {
        ZoomUtil.MEDIA_ZOOM_LEVEL = zoom
        handleZoomAdapterLayoutChange(zoom)
        if (needReload) {
            loadPhotos()
        }
    }

    override fun handleOnCreateOptionsMenu() {
        val hasImages = gridAdapterHasData()
        handleOptionsMenuUpdate(hasImages && shouldShowZoomMenuItem())
    }

    private fun subscribeObservers() {
        viewModel.items.observe(viewLifecycleOwner) {
            // Order changed.
            if (order != viewModel.getOrder()) {
                setupListAdapter(getCurrentZoom(), it)
                order = viewModel.getOrder()
            }

            actionModeViewModel.setNodesData(it.filter { nodeItem -> nodeItem.type != GalleryItem.TYPE_HEADER })
            if (it.isEmpty()) {
                handleOptionsMenuUpdate(false)
                viewTypePanel.visibility = View.GONE
            } else {
                handleOptionsMenuUpdate(shouldShowZoomMenuItem())
                viewTypePanel.visibility = View.VISIBLE
            }
        }

        viewModel.dateCards.observe(viewLifecycleOwner, ::showCards)

        viewModel.refreshCards.observe(viewLifecycleOwner) {
            if (it && selectedView != ALL_VIEW) {
                showCards(viewModel.dateCards.value)
                viewModel.refreshCompleted()
            }
        }
    }

    override fun onCardClicked(position: Int, @NonNull card: GalleryCard) {
        when (selectedView) {
            DAYS_VIEW -> {
                zoomViewModel.restoreDefaultZoom()
                handleZoomMenuItemStatus()
                newViewClicked(ALL_VIEW)
                val photoPosition = gridAdapter.getNodePosition(card.node.handle)
                layoutManager.scrollToPosition(photoPosition)

                val node = gridAdapter.getNodeAtPosition(photoPosition)
                node?.let {
                    RunOnUIThreadUtils.post {
                        openPhoto(getOrder(), it)
                    }
                }
            }
            MONTHS_VIEW -> {
                newViewClicked(DAYS_VIEW)
                layoutManager.scrollToPosition(viewModel.monthClicked(position, card))
            }
            YEARS_VIEW -> {
                newViewClicked(MONTHS_VIEW)
                layoutManager.scrollToPosition(viewModel.yearClicked(position, card))
            }
        }

        showViewTypePanel()
    }

    private fun setupEmptyHint() {
        binding.emptyHint.emptyHintImage.isVisible = false
        binding.emptyHint.emptyHintText.isVisible = false
        binding.emptyHint.emptyHintText.text =
            getString(R.string.homepage_empty_hint_photos).toUpperCase(Locale.ROOT)
    }

    /**
     * Show the selected card view after corresponding button is clicked.
     *
     * @param selectedView The selected view.
     */
    override fun newViewClicked(selectedView: Int) {
        if(this.selectedView == selectedView) return

        super.newViewClicked(selectedView)
        setHideBottomViewScrollBehaviour()
    }

    /**
     * Only refresh the list items of uiDirty = true
     */
    override fun updateUiWhenAnimationEnd() {
        viewModel.items.value?.let {
            val newList = ArrayList(it)
            gridAdapter.submitList(newList)
        }
    }

    private fun handleZoomAdapterLayoutChange(zoom: Int) {
        val state = listView.layoutManager?.onSaveInstanceState()
        setupListAdapter(zoom, viewModel.items.value)
        viewModel.mZoom = zoom
        listView.layoutManager?.onRestoreInstanceState(state)
    }

    /**
     * Display the view type buttons panel with animation effect, after a card is clicked.
     */
    private fun showViewTypePanel() {
        val params = viewTypePanel.layoutParams as CoordinatorLayout.LayoutParams
        params.setMargins(
            0, 0, 0,
            resources.getDimensionPixelSize(R.dimen.cu_view_type_button_vertical_margin)
        )
        viewTypePanel.animate().translationY(0f).setDuration(175)
            .withStartAction { viewTypePanel.visibility = View.VISIBLE }
            .withEndAction { viewTypePanel.layoutParams = params }.start()
    }

    fun loadPhotos() {
        if (isAdded) viewModel.loadPhotos(true)
    }

    override fun getNodeCount() = viewModel.getRealPhotoCount()

    override fun getOrder() = viewModel.getOrder()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!isInThisPage()) {
            return
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (!isInThisPage()) {
            true
        } else super.onOptionsItemSelected(item)
    }

    fun isInThisPage(): Boolean {
        return mManagerActivity.isInMDPage
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}