package com.kivi.remote.presentation.home.gallery

import com.kivi.remote.common.ImageInfo
import com.kivi.remote.common.VideoInfo
import com.kivi.remote.presentation.base.BaseViewModel
import com.kivi.remote.upnp.UPnPManager
import com.kivi.remote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem


class GalleryViewModel(val manager: UPnPManager) : BaseViewModel() {
    fun renderItem(item: IDIDLItem,
                   position: Int,
                   imageThumbnails: Set<ImageInfo>?,
                   videoThumbnails: Set<VideoInfo>?,
                   type: GalleryFragment.MediaType) =
            manager.launchItem(item,
                    position,
                    type,
                    imageThumbnails,
                    videoThumbnails,
                    true)
}