package com.wezom.kiviremote.presentation.home.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.wezom.kiviremote.common.Constants.*
import com.wezom.kiviremote.databinding.MediaFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.upnp.ContentCallback
import com.wezom.kiviremote.upnp.org.droidupnp.view.DIDLObjectDisplay
import timber.log.Timber
import javax.inject.Inject


class MediaFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: MediaViewModel

    private lateinit var binding: MediaFragmentBinding

    private var currentTitle: String? = null

    private val contentAdapter: MainContentAdapter
            by lazy {
                MainContentAdapter(
                    activity,
                    activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                    this::browseTo
                )
            }

    private val contentCallBack: ContentCallback = object : ContentCallback {
        private var content: ArrayList<DIDLObjectDisplay>? = null

        override fun setContent(content: ArrayList<DIDLObjectDisplay>) {
            this.content = content
        }

        override fun call(): Void? {
            Timber.d("Content size: ${content?.size}")
            activity?.runOnUiThread {
                processContent()
            }

            return null
        }

        private fun processContent() {
            if (content?.size != 0)
                if (content?.get(0)?.didlObject?.id == 1.toString()) {
                    contentAdapter.apply {
                        val newContent = content?.filter { it.title != AUDIO }
                        clear()
                        addAll(newContent)
                        binding.mediaProgress.visibility = View.GONE
                    }
                } else {
                    viewModel.run {
                        content?.forEach {
                            // as ClingDIDLContainer
                            Timber.d("Content title: ${it.title}")
                        }

                        when (currentTitle) {
                            IMAGE -> manager.currentImageContentDirectories = content ?: ArrayList()

                            VIDEO -> manager.currentVideoContentDirectories = content ?: ArrayList()
                        }

                        if (isVisible && content != null) {
                            navigateToDirectories()
                        }
                    }
                }

            if (content?.size == 0 && contentAdapter.count != 0) {
                contentAdapter.notifyDataSetChanged()
//                toast(R.string.empty_directory)
            }
        }
    }

//    private val contentCallback =

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MediaFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
                ViewModelProviders.of(activity!!, viewModelFactory).get(MediaViewModel::class.java)
        viewModel.manager.contentCallback = contentCallBack
        binding.mediaContainer.adapter = contentAdapter
    }

    override fun onStart() {
        super.onStart()
        viewModel.addObservers()
    }

    override fun onStop() {
        viewModel.removeObservers()
        super.onStop()
    }

    private lateinit var requestPermissionCallback: () -> Unit

    private fun checkPermission(browse: () -> Unit): Boolean {
        requestPermissionCallback = browse
        return if (!(activity as HomeActivity).hasReadPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
            false
        } else {
            browse()
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                //https://stackoverflow.com/questions/50770955/in-onrequestpermissionsresult-grantresults-on-some-device-return-empty-when-user
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        (activity as HomeActivity).showOpenSettingsDialog()
                    }
                } else if (::requestPermissionCallback.isInitialized){
                    requestPermissionCallback()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun injectDependencies() = fragmentComponent.inject(this)

    private fun browseTo(id: String, title: String?): Boolean = checkPermission {
        currentTitle = title
        viewModel.initContent()
        viewModel.browseTo(id, title)
    }

    companion object {
        const val POSITION = 3
        const val REQUEST_PERMISSION_CODE = 12233
    }
}