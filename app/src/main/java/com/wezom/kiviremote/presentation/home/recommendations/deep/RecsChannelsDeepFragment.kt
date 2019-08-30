package com.wezom.kiviremote.presentation.home.recommendations.deep

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.databinding.RecsDeepFragmentBinding
import com.wezom.kiviremote.net.model.Channel
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import com.wezom.kiviremote.presentation.base.recycler.initWithManager
import timber.log.Timber
import javax.inject.Inject

class RecsChannelsDeepFragment : DeepFragment(), LazyAdapter.OnItemClickListener<Channel> {


    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: RecsDeepFragmentBinding
    private lateinit var viewModel: ChannelsDeepViewModel
    private var adapter = ChannelsDeepAdapter(this)

    override fun injectDependencies() = fragmentComponent.inject(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecsDeepFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChannelsDeepViewModel::class.java)

        viewModel.populateChannels()
        viewModel.channels.observe(this@RecsChannelsDeepFragment, channelsObserver)
        val manager = GridLayoutManager(this.context, 4)
        manager.initialPrefetchItemCount = 9
        binding.recommendationsRecicler.initWithManager(manager, adapter, listOf())


    }


    private val channelsObserver = Observer<List<Channel>> {
        it?.let {
            adapter.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    override fun onLazyItemClick(data: Channel) {
        viewModel.launchChannel(data)
    }
}