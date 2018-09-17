package com.wezom.kiviremote.presentation.home.gallery

import com.wezom.kiviremote.common.ImageInfo
import com.wezom.kiviremote.common.VideoInfo
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.upnp.UPnPManager
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem


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