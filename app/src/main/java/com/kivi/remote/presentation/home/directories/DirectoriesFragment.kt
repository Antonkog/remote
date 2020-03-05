package com.kivi.remote.presentation.home.directories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kivi.remote.App
import com.kivi.remote.R
import com.kivi.remote.common.Constants.*
import com.kivi.remote.common.extensions.toPx
import com.kivi.remote.databinding.DirectoriesFragmentBinding
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.home.HomeActivity
import com.kivi.remote.upnp.ContentCallback
import com.kivi.remote.upnp.org.droidupnp.view.DIDLObjectDisplay
import timber.log.Timber
import javax.inject.Inject


class DirectoriesFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: DirectoriesViewModel
    private lateinit var binding: DirectoriesFragmentBinding

    private val panelObserver = Observer<Boolean>(this::modifyPadding)

//    override fun injectDependencies() = fragmentComponent.inject(this)

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

        if (App.isDarkMode())
            binding.container.background = ResourcesCompat.getDrawable (resources, R.drawable.shape_gradient_black, null)
        else
            binding.container.background = ResourcesCompat.getDrawable (resources, R.drawable.shape_gradient_white, null )

        viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(DirectoriesViewModel::class.java)
        viewModel.uPnPManager.currentDirType?.let {
            when (it) {
                VIDEO -> binding.title.text = view.context.getString(R.string.video)
                IMAGE -> binding.title.text = view.context.getString(R.string.photo)
                AUDIO -> binding.title.text = view.context.getString(R.string.audio)
            }
            adapter = DirectoriesAdapter(it, ::navigateToDirectory)
        }

        (activity as HomeActivity).run {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
            modifyPadding(isTouchPadCollapsed.value)
            isTouchPadCollapsed.observe(this, panelObserver)
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
