package com.wezom.kiviremote.presentation.home.gallery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants.*
import com.wezom.kiviremote.common.ImageInfo
import com.wezom.kiviremote.common.VideoInfo
import com.wezom.kiviremote.common.extensions.toPx
import com.wezom.kiviremote.common.getAllImages
import com.wezom.kiviremote.common.getAllVideos
import com.wezom.kiviremote.databinding.GalleryFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem
import javax.inject.Inject


class GalleryFragment : BaseFragment() {
    enum class MediaType {
        IMAGE, VIDEO
    }

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: GalleryViewModel

    private lateinit var binding: GalleryFragmentBinding

    private val panelObserver = Observer<Boolean>(this::modifyPadding)

    override fun injectDependencies() = fragmentComponent.inject(this)

    private fun modifyPadding(modify: Boolean?) {
        modify?.let {
            if (it) {
                binding.galleryContainer.setPadding(0, 0, 0, 64.toPx)
            } else {
                binding.galleryContainer.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = GalleryFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GalleryViewModel::class.java)

        (activity as HomeActivity).run {
            isTouchPadCollapsed.observe(this, panelObserver)
        }

        binding.galleryTitle.text = viewModel.manager.currentDir

        (activity as HomeActivity).run {
            setSupportActionBar(binding.galleryToolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        }

        binding.galleryContainer.run {
            when (viewModel.manager.currentDirType) {
                IMAGE -> {
                    layoutManager = GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)
                    adapter = GalleryImageAdapter((activity as HomeActivity).applicationContext,
                            getAllImages(activity as Context),
                            viewModel.manager.potentialImageContent,
                            R.layout.gallery_image_item, this@GalleryFragment::render)
                    setHasFixedSize(true)
                }

                VIDEO -> {
                    layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                    adapter = GalleryVideoAdapter(getAllVideos(activity as Context),
                            viewModel.manager.potentialVideoContent,
                            R.layout.gallery_video_item, this@GalleryFragment::render)
                    setHasFixedSize(true)
                }

                AUDIO -> {

                }
            }
        }
    }

    override fun onDestroyView() {
        (activity as HomeActivity).isTouchPadCollapsed.removeObserver(panelObserver)
        super.onDestroyView()
    }

    /**
     * Renders an image or thumbnail for video items
     * @param item IDIDLItem object to render
     * @param image URL for an Image
     */
    private fun render(item: IDIDLItem, image: String?, position: Int, imageThumbnails: Set<ImageInfo>?, videoThumbnails: Set<VideoInfo>?, type: MediaType) {
        viewModel.renderItem(item, position, imageThumbnails, videoThumbnails, type)
        (activity as HomeActivity).run {
            setUpnpContent(item.title, image, position, type)
            expandSlidingPanel()
        }
    }
}