package com.wezom.kiviremote.presentation.home.recommendations.deep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.wezom.kiviremote.databinding.RecsDeepFragmentBinding
import com.wezom.kiviremote.net.model.Recommendation
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import com.wezom.kiviremote.presentation.base.recycler.initWithManager
import timber.log.Timber
import javax.inject.Inject

class RecsMovieDeepFragment : DeepFragment(), LazyAdapter.OnItemClickListener<Recommendation> {
//
//    companion object {
//        @JvmStatic
//        fun newInstance(value: Serializable): RecsMovieDeepFragment {
//            val fragment = RecsMovieDeepFragment()
//            val args = Bundle()
//            args.putSerializable("data", value)
//            fragment.arguments = args
//            return fragment
//        }
//    }

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: RecsDeepFragmentBinding
    private lateinit var viewModel: RecsDeepViewModel
    private var adapter = RecsDeepAdapter(this)

    override fun injectDependencies() = fragmentComponent.inject(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecsDeepFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecsDeepViewModel::class.java)
        viewModel.populateRecommendations()
        viewModel.recommendations.observe(this@RecsMovieDeepFragment, recommendationsObserver)
        val manager = GridLayoutManager(this.context, 3)
        manager.initialPrefetchItemCount = 5

//        val recs = arguments?.getSerializable("data") as RecommendationsHolder
//
//        if (recs?.data != null && recs.data.isNotEmpty())
//            binding.recommendationsRecicler.initWithManager(manager, adapter, recs.data)
//        else
            binding.recommendationsRecicler.initWithManager(manager, adapter, listOf())
    }

    private val recommendationsObserver = Observer<List<Recommendation>> {
        it?.let {
            adapter.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    override fun onLazyItemClick(data: Recommendation) {
        viewModel.launchRecommendation(data)
    }
}