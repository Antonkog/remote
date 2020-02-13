package com.kivi.remote.presentation.home.recommendations.deep

import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.home.HomeActivity


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