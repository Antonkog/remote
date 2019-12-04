package com.wezom.kiviremote.presentation.home.kivi_catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.wezom.kiviremote.App
import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.KiviCatalogSeriesFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import com.wezom.kiviremote.presentation.base.recycler.initWithLinLay
import com.wezom.kiviremote.presentation.home.kivi_catalog.adapters.CatalogEpisodeAdapter
import com.wezom.kiviremote.presentation.home.kivi_catalog.adapters.MovieData
import com.wezom.kiviremote.presentation.home.kivi_catalog.adapters.pagination.UserPagination
import java.io.Serializable
import javax.inject.Inject

class KiviCatalogSeriesFragment : BaseFragment(), LazyAdapter.OnItemClickListener<MovieData> {

    private lateinit var data: MovieData

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory
    private val viewModel: KiviCatalogSeriesViewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(KiviCatalogSeriesViewModel::class.java) }

    private lateinit var binding: KiviCatalogSeriesFragmentBinding
    private val episodesAdapter = CatalogEpisodeAdapter(this)

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        data = arguments!!.getSerializable("data") as MovieData
        binding = KiviCatalogSeriesFragmentBinding.inflate(inflater, container!!, false)

        initTabLayout()
        initTabs()
        binding.rvSeries.initWithLinLay(LinearLayoutManager.VERTICAL, episodesAdapter, listOf())

        binding.rvSeries.addOnScrollListener(object: UserPagination(binding.rvSeries.layoutManager) {
            override fun onLoadMore(currentPage: Int, totalItemCount: Int, view: View?) {
                if (!viewModel.catalogRequestOnWay) {
                    val seasonNumber = if (binding.tlSeries.selectedTabPosition < 0) {
                        1
                    } else {
                        data.seasons!![binding.tlSeries.selectedTabPosition].number
                    }

                    paginationFetchEpisodesData(data.id, seasonNumber, episodesAdapter.getLastVisibleItemId() + 1)
                }
            }
        })

        paginationFetchEpisodesData(data.id, data.seasons?.firstOrNull()?.number ?: 1, 0)
        return binding.root
    }

    private fun initTabLayout() {
        binding.tlSeries.apply {
            setBackgroundColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.colorBlack else R.color.colorWhite, null))
            setSelectedTabIndicatorColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.tab_indicator_dark else R.color.tab_indicator_light, null))
            setTabTextColors(
                    ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.dove_gray else R.color.silver, null),
                    ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.colorWhite else R.color.mine_shaft_33, null)
            )

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    binding.rvSeries.scrollToPosition(0)
                    episodesAdapter.clearData()
                    paginationFetchEpisodesData(data.id, data.seasons!![selectedTabPosition].number, 0)
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    private fun initTabs() {
        binding.tlSeries.apply {
            if (data.seasons?.size ?: 0 > 1) {
                data.seasons?.forEach { addTab(newTab().setText("${it.number} сезон")) }
            } else {
                visibility = View.GONE
            }
        }
    }

    override fun onLazyItemClick(data: MovieData) {
        //TODO show content with data.id on TV
    }

    private fun paginationFetchEpisodesData(id: Int, season: Int, from: Int) {
        viewModel.seasonId = id
        viewModel.seasonNumber = season

        viewModel.paginationFetchEpisodesData(context!!, from) { items ->
            if (episodesAdapter.data.isEmpty() && items.isNullOrEmpty()) {
                binding.llNotFoundPlaceholder.visibility = View.VISIBLE
                return@paginationFetchEpisodesData
            } else {
                binding.llNotFoundPlaceholder.visibility = View.GONE
            }

            if (episodesAdapter.getLastVisibleItemId() == 0) {
                episodesAdapter.swapData(items)
            } else {
                if (episodesAdapter.data.firstOrNull()?.equals(items.firstOrNull()) != false) {
                    return@paginationFetchEpisodesData
                }

                episodesAdapter.addData(items.filter { !episodesAdapter.data.contains(it) })
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(value: Serializable): KiviCatalogSeriesFragment {
            val fragment = KiviCatalogSeriesFragment()
            val args = Bundle()
            args.putSerializable("data", value)
            fragment.arguments = args
            return fragment
        }
    }

}