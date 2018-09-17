package com.wezom.kiviremote.presentation.home.directories

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.common.Constants.*
import com.wezom.kiviremote.common.extensions.toPx
import com.wezom.kiviremote.databinding.DirectoriesFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.upnp.ContentCallback
import com.wezom.kiviremote.upnp.org.droidupnp.view.DIDLObjectDisplay
import timber.log.Timber
import javax.inject.Inject


class DirectoriesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: DirectoriesViewModel
    private lateinit var binding: DirectoriesFragmentBinding

    private val panelObserver = Observer<Boolean>(this::modifyPadding)

    override fun injectDependencies() = fragmentComponent.inject(this)

    private val contentCallBack: ContentCallback = object : ContentCallback {
        private var content: ArrayList<DIDLObjectDisplay>? = null

        override fun setContent(content: ArrayList<DIDLObjectDisplay>) {
            this.content = content
        }

        override fun call(): Void? {
            Timber.d("Content size: ${content?.size}")
            activity?.runOnUiThread {
                if (content?.size != 0)
                    if (content?.get(0)?.didlObject?.id != 1.toString()) {
                        when (viewModel.uPnPManager.currentDirType) {
                            IMAGE -> {
                                viewModel.uPnPManager.potentialImageContent = content!!
                            }

                            VIDEO -> {
                                viewModel.uPnPManager.potentialVideoContent = content!!
                            }
                        }
                        content?.forEach {
                            // as ClingDIDLContainer
                            Timber.d("Content title: ${it.title}")
                        }

                        navigateToGallery()
                    }
            }
            return null
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DirectoriesFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    private fun navigateToDirectory(directory: DIDLObjectDisplay, directoryTitle: String) {
        viewModel.uPnPManager.run {
            browseTo(directory.didlObject.id, null)
            currentDir = directoryTitle
        }
    }

    private lateinit var adapter: DirectoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(DirectoriesViewModel::class.java)
        viewModel.uPnPManager.currentDirType?.let {
            binding.title.text = it
            adapter = DirectoriesAdapter(it, ::navigateToDirectory)
        }

        (activity as HomeActivity).run {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
            modifyPadding(isPanelCollapsed.value)
            isPanelCollapsed.observe(this, panelObserver)
        }

        when (viewModel.uPnPManager.currentDirType) {
            IMAGE -> {
                binding.container.run {
                    adapter = this@DirectoriesFragment.adapter
                    layoutManager =
                            GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
                    setHasFixedSize(true)
                    this@DirectoriesFragment.adapter.directories =
                            viewModel.uPnPManager.currentImageContentDirectories
                }
            }

            VIDEO -> {
                binding.container.run {
                    adapter = this@DirectoriesFragment.adapter
                    layoutManager =
                            GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
                    setHasFixedSize(true)
                    this@DirectoriesFragment.adapter.directories =
                            viewModel.uPnPManager.currentVideoContentDirectories
                }
            }

            AUDIO -> {

            }
        }

        viewModel.uPnPManager.contentCallback = this@DirectoriesFragment.contentCallBack
    }

    private fun modifyPadding(modify: Boolean?) {
        modify?.let {
            if (it) {
                binding.container.setPadding(0, 0, 0, 64.toPx)
            } else {
                binding.container.setPadding(0, 0, 0, 0)
            }
        }
    }

    private fun navigateToGallery() = viewModel.navigateToGallery()
}
