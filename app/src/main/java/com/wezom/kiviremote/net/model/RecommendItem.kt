package com.wezom.kiviremote.net.model


data class RecommendItem(var type: Int,
                         var serverId: Int,
                         var title: String,
                         var packageName: String,
                         var imageId: Int,
                         var url: String){

    constructor( type: Int, serverId: Int, title: String) :
            this(type, serverId, title, "", -1, "")

    constructor(type: Int,  serverId: Int,  title: String, url: String) :
            this(type, serverId, title,"",-1,url)

}



