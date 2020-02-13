package com.kivi.remote.presentation.home.subscriptions.subs_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivi.remote.databinding.SubsInfoFragmentBinding
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.base.recycler.initWithLinLay
import com.kivi.remote.presentation.home.subscriptions.subs_price_list.PricePerTime
import java.io.Serializable
import javax.inject.Inject

class SubsInfoFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: SubsInfoFragmentBinding
    private lateinit var viewModel: SubsInfoViewModel

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SubsInfoFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SubsInfoViewModel::class.java)

        binding.cvConnectSubscription.setOnClickListener {
            viewModel.navigateToSubsTariffs()
        }

        val data = arguments!!.getSerializable("data") as PricePerTime
        val dataList = listOf(
                TvProgramsChunk("Познавательные", listOf("", "", "", "", "", "", "", "")),
                TvProgramsChunk("Фильмовые", listOf("", "", "", "", "", "", "", "")),
                TvProgramsChunk("Фильмовые", listOf("", "", "", "", "", "", "", "")))

        binding.rvTvprograms.initWithLinLay(LinearLayoutManager.VERTICAL, TvProgramListAdapter(), dataList)
        binding.rvTvprograms.isNestedScrollingEnabled = false
    }

    companion object {
        @JvmStatic
        fun newInstance(value: Serializable): SubsInfoFragment {
            val fragment = SubsInfoFragment()
            val args = Bundle()
            args.putSerializable("data", value)
            fragment.arguments = args
            return fragment
        }
    }

}