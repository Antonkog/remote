package com.kivi.remote.presentation.home.subscriptions.subs_price_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivi.remote.databinding.SubsPriceListFragmentBinding
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import com.kivi.remote.presentation.base.recycler.initWithLinLay
import javax.inject.Inject

class SubsPriceListFragment : BaseFragment(), LazyAdapter.OnItemClickListener<PricePerTime> {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: SubsPriceListFragmentBinding
    private lateinit var viewModel: SubsPriceListViewModel

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SubsPriceListFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SubsPriceListViewModel::class.java)

        val dataList = listOf(
                PricePerTime(1, "месяцев", 177, "грн."),
                PricePerTime(3, "месяцев", 531, "грн."),
                PricePerTime(6, "месяцев", 942, "грн."),
                PricePerTime(12, "месяцев", 1764, "грн."))

        binding.rvPrices.initWithLinLay(LinearLayoutManager.VERTICAL, PriceAdapter(this), dataList)
    }

    override fun onLazyItemClick(data: PricePerTime) {
        viewModel.navigateToSubsInfo(data)
    }

}