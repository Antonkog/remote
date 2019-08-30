package com.wezom.kiviremote.presentation.home.recommendations.deep

import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.home.HomeActivity


abstract class DeepFragment : BaseFragment(){
    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).setHomeAsUp(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as HomeActivity).setHomeAsUp(false)
    }
}