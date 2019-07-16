package com.wezom.kiviremote.presentation.home.ports

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.App
import com.wezom.kiviremote.R
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.Constants.ASPECT_GET_TRY
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.Run
import com.wezom.kiviremote.databinding.PortsFragmentBinding
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.TvKeysFragment
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import java.util.*
import javax.inject.Inject

class PortsFragment : TvKeysFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: PortsViewModel
    private lateinit var binding: PortsFragmentBinding
    private var lastPortId = Constants.INPUT_HOME_ID
    private var aspectTryCounter = ASPECT_GET_TRY
    private val portsAdapter: PortsAdapter by lazy {

        PortsAdapter(object : PortsAdapter.CheckListener {
            override fun onPortChecked(portId: Int) {
                aspectTryCounter = ASPECT_GET_TRY
                lastPortId = portId
                if (lastPortId == Constants.INPUT_HOME_ID) {
                    setPort(lastPortId)
                    RxBus.publish(SendActionEvent(Action.HOME_DOWN))
                    RxBus.publish(SendActionEvent(Action.HOME_UP))
                    fragmentManager?.popBackStack()
                } else {
                    if (AspectHolder.message?.serverVersionCode ?: 0 < Constants.VER_ASPECT_XIX) {
                        setPort(lastPortId)
                    } else {
                        setPortServerCheck(lastPortId)
                    }
                }
            }
        })
    }

    private fun setPortServerCheck(id: Int) {
        binding.portsRefreshBar.visibility = View.VISIBLE
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, id)
        viewModel.requestAspect()
    }

    override fun injectDependencies() = fragmentComponent.inject(this)


    override fun onResume() {
        AspectHolder.getInputsList().let {
            portsAdapter.setData(it)
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


        if (App.isDarkMode())
            binding.portsContainer.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_gradient_black, null)
        else
            binding.portsContainer.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_gradient_white, null)

        (activity as HomeActivity).run {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
        }

        initPortsContainer()

        viewModel.aspectEvent.observe(this, showPortsObserver)
    }

    private fun initPortsContainer() {
        binding.portsContainer.run {
            adapter = portsAdapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }
    }

    private val showPortsObserver = Observer<GotAspectEvent> {
        var inputPorts = it?.getInputsList() ?: LinkedList()
        if (!inputPorts.isEmpty()) {
            for (port in inputPorts) {
                if (port.isActive) {
                    if (port.intID == lastPortId || aspectTryCounter == 0) {
                        binding.portsRefreshBar.visibility = View.GONE
                        portsAdapter.setData(inputPorts)
                    } else {
                        Run.after(1000){
                            viewModel.requestAspect()
                        }
                        aspectTryCounter--
                    }
                }
            }
        } else {
            viewModel.requestAspect()
        }
    }

    private fun setPort(portId: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, portId)
        portsAdapter.setInputActiveById(portId)
    }

}