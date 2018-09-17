package com.wezom.kiviremote.presentation.home.recentdevice

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.common.extensions.vanish
import com.wezom.kiviremote.common.hideKeyboard
import com.wezom.kiviremote.databinding.RecentDeviceFragmentBinding
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class RecentDeviceFragment : BaseFragment() {

    private lateinit var model: RecentDevice

    private val saveButtonClickListener: View.OnClickListener = View.OnClickListener {
        saveChanges()
    }

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private var viewModel: RecentDeviceViewModel? = null

    private lateinit var binding: RecentDeviceFragmentBinding

    private val editNameClickListener: View.OnClickListener = View.OnClickListener {
        binding.recentDeviceEditIcon.vanish()
        binding.recentDeviceToChange.vanish()
        binding.recentDeviceEditText.visibility = View.VISIBLE
    }

    private val editNameEditorListener = TextView.OnEditorActionListener { _: TextView?,
                                                                           actionId: Int?,
                                                                           _: KeyEvent? ->
        var handled = false
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            saveChanges()
            handled = true
        }
        handled
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecentDeviceFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecentDeviceViewModel::class.java)

        (activity as HomeActivity).showBackButton()

        model = arguments!!.getParcelable("recent_device")
        if (model.userDefinedName != null) {
            displayUserDefinedName()
        } else {
            displayActualName()
        }
        setupListeners()

        (activity as HomeActivity).run {
            setSupportActionBar(binding.recentDeviceToolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun injectDependencies() = fragmentComponent.inject(this)

    private fun popBackStack() {
        toast(R.string.save_success)
        fragmentManager?.popBackStack()
    }

    private fun displayUserDefinedName() = model.run {
        binding.recentDeviceName.text = userDefinedName
        binding.recentDeviceToChange.text = userDefinedName
        binding.recentDeviceEditText.setText(userDefinedName, TextView.BufferType.EDITABLE)
    }


    private fun displayActualName() {
        val actualName = model.actualName.removeMasks()

        binding.recentDeviceName.text = actualName
        binding.recentDeviceToChange.text = actualName
        binding.recentDeviceEditText.setText(actualName, TextView.BufferType.EDITABLE)
    }

    private fun setupListeners() {
        binding.recentDeviceToChange.setOnClickListener(editNameClickListener)
        binding.recentDeviceEditIcon.setOnClickListener(editNameClickListener)
        binding.recentDeviceEditText.setOnEditorActionListener(editNameEditorListener)
        binding.recentDeviceSave.setOnClickListener(saveButtonClickListener)
    }

    private fun saveChanges() {
        if (binding.recentDeviceEditText.text.toString().isEmpty()) {
            toast(R.string.device_name_cannot_be_empty)
        } else {
            val value = RecentDevice(model.id, model.actualName, binding.recentDeviceEditText.text.toString())
            hideKeyboard(activity as Activity)
            launch(UI) {
                viewModel?.saveChanges(value)
            }
            popBackStack()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(value: Parcelable): RecentDeviceFragment {
            val fragment = RecentDeviceFragment()
            val args = Bundle()
            args.putParcelable("recent_device", value)
            fragment.arguments = args
            return fragment
        }
    }
}
