package com.wezom.kiviremote.presentation.home.ports

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.databinding.PortsFragmentBinding
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.TvKeysFragment
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import timber.log.Timber
import javax.inject.Inject

class PortsFragment : TvKeysFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: PortsViewModel
    private lateinit var binding: PortsFragmentBinding


    private val portsAdapter: PortsAdapter by lazy {
        PortsAdapter(object : PortsAdapter.CheckListener {
            override fun onPortChecked(position: Int) {
                setPort(position)
                refreshData()
            }
        })
    }


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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PortsViewModel::class.java)

        initPortsContainer()

        viewModel.ports.observe(this, showAspectObserver)

        refreshData()

        setTvButtons(viewModel, binding.menu, binding.back, binding.home)

        (activity as HomeActivity).run {
            setSupportActionBar(binding.portsFragmentToolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    private fun initPortsContainer() {
        binding.portsContainer.run {
            adapter = portsAdapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }
    }

    private val showAspectObserver = Observer<List<Port>> {
        it?.let { refreshData() }
    }

    private fun refreshData() {
//        Timber.e(" ports ${AspectHolder.availableSettings?.porsSettings?.size} ${AspectHolder.message.toString()}" )
        AspectHolder.availableSettings?.porsSettings.let {
            portsAdapter.setData(InputSourceHelper.getPortsList(it, AspectHolder.message?.currentPort
                    ?: Constants.NO_VALUE))
        }
    }

    private fun setPort(portId: Int) {
        Timber.e(" setPort:  $portId  " )
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, portId) // sendAspectChangeEvent(msg)
    }


}