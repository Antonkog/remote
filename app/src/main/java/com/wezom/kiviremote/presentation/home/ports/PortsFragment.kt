package com.wezom.kiviremote.presentation.home.ports

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.databinding.PortsFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import javax.inject.Inject


class PortsFragment : BaseFragment() {

    private lateinit var adapter: PortsAdapter

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: PortsViewModel
    private lateinit var binding: PortsFragmentBinding


    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = PortsFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(PortsViewModel::class.java)
//        viewModel.PortManager.currentport?.let {
//            binding.title.text = it
//                    adapter = PortsAdapter("currentPort", :: setPort)
//        }

        adapter = PortsAdapter("currentPort", :: setPort)

        (activity as HomeActivity).run {
            setSupportActionBar(binding.recentDeviceToolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
        }


    }


    private fun setPort(port : Port) {

    }

    private fun navigateHome() = viewModel.navigateToHome()
    private fun navigateBack() = viewModel.navigateBack()


}