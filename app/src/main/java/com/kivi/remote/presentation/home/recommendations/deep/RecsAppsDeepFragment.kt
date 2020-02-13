package com.kivi.remote.presentation.home.recommendations.deep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kivi.remote.databinding.RecsDeepFragmentBinding
import com.kivi.remote.net.model.ServerAppInfo
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import com.kivi.remote.presentation.base.recycler.initWithManager
import com.kivi.remote.presentation.home.HomeActivity
import timber.log.Timber
import javax.inject.Inject

class RecsAppsDeepFragment : DeepFragment(), LazyAdapter.OnItemClickListener<ServerAppInfo> {


    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: RecsDeepFragmentBinding
    private lateinit var viewModel: AppsDeepViewModel
    private lateinit var adapter: AppsDeepAdapter

    override fun injectDependencies() = fragmentComponent.inject(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecsDeepFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AppsDeepViewModel::class.java)
        adapter = AppsDeepAdapter(this, viewModel.cache)
        viewModel.populateApps();
        viewModel.apps.observe(this@RecsAppsDeepFragment, recommendationsObserver)

        val manager = GridLayoutManager(this.context, 2)

        manager.initialPrefetchItemCount = 5
        binding.recommendationsRecicler.initWithManager(manager, adapter, listOf())
    }


    private val recommendationsObserver = Observer<List<ServerAppInfo>> {
        it?.let {
            adapter.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    override fun onLazyItemClick(data: ServerAppInfo) {
        viewModel.launchApp(data.packageName)
        (activity as HomeActivity).run {
            showTouchPad()
            hideSlidingPanel()
        }
    }
}