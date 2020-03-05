package com.kivi.remote.presentation.home.gallery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivi.remote.R
import com.kivi.remote.common.Constants.*
import com.kivi.remote.common.ImageInfo
import com.kivi.remote.common.VideoInfo
import com.kivi.remote.common.extensions.toPx
import com.kivi.remote.common.getAllImages
import com.kivi.remote.common.getAllVideos
import com.kivi.remote.databinding.GalleryFragmentBinding
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.home.HomeActivity
import com.kivi.remote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem
import javax.inject.Inject


class GalleryFragment : Fragment() {
    enum class MediaType {
        IMAGE, VIDEO
    }

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: GalleryViewModel

    private lateinit var binding: GalleryFragmentBinding

    private val panelObserver = Observer<Boolean>(this::modifyPadding)

//    override fun injectDependencies() = fragmentComponent.inject(this)

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
            showFullPreviewPanel()
        }
    }
}