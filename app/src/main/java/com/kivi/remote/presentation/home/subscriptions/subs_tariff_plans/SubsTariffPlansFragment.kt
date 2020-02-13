package com.kivi.remote.presentation.home.subscriptions.subs_tariff_plans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivi.remote.databinding.SubsTariffPlansFragmentBinding
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.base.recycler.initWithLinLay
import javax.inject.Inject

class SubsTariffPlansFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: SubsTariffPlansFragmentBinding
    private lateinit var viewModel: SubsTariffPlansViewModel

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SubsTariffPlansFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SubsTariffPlansViewModel::class.java)

        binding.textView.setOnClickListener {
            viewModel.navigateToSubsPayment()
        }

        val dataList = listOf(
                TariffChunk("Стартовая", 47, listOf("", "", "", "", "", "", "", "")),
                TariffChunk("Расширенная", 177, listOf("", "", "", "", "", "", "", "")),
                TariffChunk("Стартовая", 47, listOf("", "", "", "", "", "", "", "")))

        binding.rvTariffs.initWithLinLay(LinearLayoutManager.VERTICAL, TariffListAdapter(), dataList)
        binding.rvTariffs.isNestedScrollingEnabled = false
    }

}