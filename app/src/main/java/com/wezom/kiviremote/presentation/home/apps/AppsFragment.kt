package com.wezom.kiviremote.presentation.home.apps

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.extensions.toPx
import com.wezom.kiviremote.databinding.AppsFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import javax.inject.Inject


class AppsFragment : BaseFragment() {

    @Inject
    lateinit var factory: BaseViewModelFactory

    @Inject
    lateinit var cache: KiviCache

    private var viewModel: AppsViewModel? = null

    private lateinit var binding: AppsFragmentBinding

    private val appsAdapter: AppsAdapter by lazy {
        AppsAdapter(cache, { viewModel?.launchApp(it) })
    }

    private val noResultRunnable = Runnable {
        Timber.d("Error while updating apps list")
        if (isVisible) {
            binding.appsRefreshProgress.visibility = View.GONE
            binding.appsSwipeContainer.isRefreshing = false

            if (appsAdapter.itemCount == 0)
                binding.appsRefreshLabel.visibility = View.VISIBLE
            else
                binding.appsRefreshLabel.visibility = View.GONE
        }

        toast(R.string.error)
    }

    private val handler = Handler()

    private val appsObserver = Observer<List<AppModel>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            updateApps(true, it)
        } ?: updateApps(false, null)
    }

    private fun updateApps(visible: Boolean, apps: List<AppModel>?) {
        if (visible) {
            binding.appsRefreshLabel.visibility = View.GONE
        } else {
            binding.appsRefreshLabel.visibility = View.VISIBLE
        }

        binding.appsRefreshProgress.visibility = View.INVISIBLE
        binding.appsSwipeContainer.isRefreshing = false
        removeRefreshingRunnable()
        appsAdapter.setNewApps(apps)
    }

    private val panelObserver = Observer<Boolean>(this::modifyPadding)

    private fun modifyPadding(modify: Boolean?) {
        modify?.let {
            if (it) {
                binding.appsContainer.setPadding(0, 0, 0, 64.toPx)
            } else {
                binding.appsContainer.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AppsFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, factory).get(AppsViewModel::class.java)
        binding.viewModel = viewModel

        initAppContainer()
        initListeners()

        viewModel?.run {
            populateApps()
            apps.observe(this@AppsFragment, appsObserver)
        }

        (activity as HomeActivity).run {
            modifyPadding(isTouchPadCollapsed.value)
            isTouchPadCollapsed.observe(this, panelObserver)
        }
    }

    private fun initListeners() {
        binding.appsSwipeContainer.setOnRefreshListener {
            viewModel?.requestApps()
            handler.postDelayed(noResultRunnable, DELAY)
        }

        binding.appsRefreshLabel.setOnClickListener {
            viewModel?.requestApps()
            binding.appsRefreshLabel.visibility = View.GONE
            binding.appsRefreshProgress.visibility = View.VISIBLE
            handler.postDelayed(noResultRunnable, DELAY)
        }
    }

    private fun initAppContainer() {
        binding.appsContainer.run {
            adapter = appsAdapter
            layoutManager = GridLayoutManager(activity, 4)
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        }
    }

    override fun injectDependencies() = fragmentComponent.inject(this)

    private fun removeRefreshingRunnable() = handler.removeCallbacks(noResultRunnable)

    override fun onDestroyView() {
        removeRefreshingRunnable()
        super.onDestroyView()
    }

    companion object {
        private const val DELAY: Long = Constants.DELAY_APPS_GET
        const val POSITION = 2
    }
}