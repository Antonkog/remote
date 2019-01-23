package com.wezom.kiviremote.presentation.home.ports

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.Constants.ASPECT_GET_TRY
import com.wezom.kiviremote.databinding.PortsFragmentBinding
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import javax.inject.Inject

class PortsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: PortsViewModel
    private lateinit var binding: PortsFragmentBinding
    private var lastPortId = InputSourceHelper.INPUT_PORT.INPUT_SOURCE_NONE.id
    private var aspectTryCounter = ASPECT_GET_TRY
    private val portsAdapter: PortsAdapter by lazy {
        PortsAdapter(object : PortsAdapter.CheckListener {
            override fun onPortChecked(id: Int) {
                lastPortId = id
                setPort(id)
                viewModel.requestAspect()
            }
        })
    }

    override fun injectDependencies() = fragmentComponent.inject(this)


    override fun onResume() {
        aspectTryCounter = ASPECT_GET_TRY
        if (AspectHolder.message != null && AspectHolder.availableSettings != null) {
            refreshData()
        } else {
            viewModel.requestAspect()
        }
        super.onResume()
    }

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PortsViewModel::class.java)

        (activity as HomeActivity).run {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
        }

        initPortsContainer()

        viewModel.ports.observe(this, showPortsObserver)
    }

    private fun initPortsContainer() {
        binding.portsContainer.run {
            adapter = portsAdapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }
    }

    private val showPortsObserver = Observer<List<Port>> {
        it?.let {
            for (port in it) {
                if (port.active) {
                    if (port.portNum == lastPortId || aspectTryCounter == 0) {
                        binding.portsRefreshBar.visibility = View.GONE
                        portsAdapter.setData(it)
                    } else {
                        Handler().postDelayed({ viewModel.requestAspect() }, 1000)
                        aspectTryCounter--
                    }
                }
            }
        }
    }

    private fun refreshData() {
        binding.portsRefreshBar.visibility = View.GONE
        AspectHolder.availableSettings?.portsSettings?.let {
            portsAdapter.setData(InputSourceHelper.getPortsList(it, AspectHolder.message?.currentPort
                    ?: Constants.NO_VALUE).distinct())
        }
    }

    private fun setPort(portId: Int) {
        binding.portsRefreshBar.visibility = View.VISIBLE
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, portId)
    }
}