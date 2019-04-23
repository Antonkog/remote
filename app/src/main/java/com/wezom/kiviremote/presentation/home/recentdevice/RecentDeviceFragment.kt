package com.wezom.kiviremote.presentation.home.recentdevice

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.bus.NewNameEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.remove032Space
import com.wezom.kiviremote.common.hideKeyboard
import com.wezom.kiviremote.common.restartApp
import com.wezom.kiviremote.databinding.RecentDeviceFragmentBinding
import com.wezom.kiviremote.nsd.LastNsdHolder
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.recentdevice.item_info.TvInfoUnit
import com.wezom.kiviremote.presentation.home.recentdevices.TvDeviceInfo
import com.wezom.kiviremote.presentation.home.recentdevices.item.TvInfoAdapter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.support.v4.toast
import java.io.Serializable
import javax.inject.Inject

class RecentDeviceFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private var viewModel: RecentDeviceViewModel? = null

    private lateinit var data: TvDeviceInfo

    private lateinit var binding: RecentDeviceFragmentBinding

    private lateinit var tvRename: TextView
    private lateinit var switchAutoConnect: Switch
    private lateinit var tvDeviceName: TextView
    private lateinit var rvInfoContainer: RecyclerView
    private lateinit var ivForgetDevice: ImageView
    private lateinit var tvForgetDevice: TextView

    private lateinit var dialog: AlertDialog
    private lateinit var dialogEditText: EditText

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecentDeviceFragmentBinding.inflate(inflater, container!!, false)

        tvRename = binding.tvRename
        switchAutoConnect = binding.switchAutoConnect
        tvDeviceName = binding.tvDeviceName
        rvInfoContainer = binding.rvInfoContainer
        ivForgetDevice = binding.ivForgetDevice
        tvForgetDevice = binding.tvForgetDevice

        dialog = AlertDialog.Builder(binding.root.context).create()

        val dialogView = inflater.inflate(R.layout.alert_dialog_with_edit_text, null)

        dialogEditText = dialogView.findViewById(R.id.et_name)
        dialogView.findViewById<TextView>(R.id.tv_cancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<TextView>(R.id.tv_rename).setOnClickListener {
            renameDevice(dialogEditText.text.toString())
            dialog.dismiss()
        }

        dialog.setView(dialogView)
        dialog.setCancelable(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecentDeviceViewModel::class.java)
        (activity as HomeActivity).showBackButton()

        val adapter = TvInfoAdapter()
        data = arguments!!.getSerializable("data") as TvDeviceInfo

        tvDeviceName.text = "УСТРОЙСТВО ${(data.recentDevice.userDefinedName ?: data.recentDevice.actualName).remove032Space()}"
        dialogEditText.setText((data.recentDevice.userDefinedName ?: "").remove032Space())

        switchAutoConnect.setOnCheckedChangeListener(null)
        switchAutoConnect.isEnabled = data.nsdServiceInfoWrapper != null

        if (switchAutoConnect.isEnabled) {
            switchAutoConnect.isChecked = LastNsdHolder.nsdServiceWrapper?.equals(data.nsdServiceInfoWrapper!!) ?: false
            switchAutoConnect.setOnCheckedChangeListener { _, checked -> LastNsdHolder.nsdServiceWrapper = if (checked) data.nsdServiceInfoWrapper else null }
        }

        rvInfoContainer.apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            this.setHasFixedSize(true)
        }

        tvForgetDevice.setOnClickListener {
            confirmDeletion()
        }

        tvRename.setOnClickListener {
            dialog.show()
        }

        // Test data
        adapter.swapData(listOf(
                TvInfoUnit("Диагональ", "32"),
                TvInfoUnit("Тип матрицы", "IPS"),
                TvInfoUnit("Bluetooth модуль", "4.2"),
                TvInfoUnit("Статус сети:", "DIR-320"),
                TvInfoUnit("Разрешение экрана", "1920х1080"),
                TvInfoUnit("Автоподключение", "Да")
        ))

        (activity as HomeActivity).run {
            setSupportActionBar(binding.recentDeviceToolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    private fun confirmDeletion() {
        launch(CommonPool) {
            viewModel?.database?.recentDeviceDao()?.deleteDevices(data.recentDevice)

            launch(UI) {
                if (data.indexInRecentList == 0)
                    restartApp(getContext()!!)
                else
                    viewModel?.goBack()
            }
        }
    }

    private fun renameDevice(newName: String) {
        if (newName.isEmpty()) {
            toast(R.string.device_name_cannot_be_empty)
        } else {
            val value = RecentDevice(data.recentDevice.id, data.recentDevice.actualName, newName)
            hideKeyboard(activity as Activity)
            launch(UI) {
                viewModel?.saveChanges(value)
                RxBus.publish(NewNameEvent(newName))
                tvDeviceName.text = "УСТРОЙСТВО ${newName.remove032Space()}"
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(value: Serializable): RecentDeviceFragment {
            val fragment = RecentDeviceFragment()
            val args = Bundle()
            args.putSerializable("data", value)
            fragment.arguments = args
            return fragment
        }
    }

}

//    private fun popBackStack() {
//        toast(R.string.save_success)
//        fragmentManager?.popBackStack()
//    }

//    private fun displayUserDefinedName() = model.run {
//        binding.recentDeviceName.text = userDefinedName
//        //binding.recentDeviceToChange.text = userDefinedName
//        //binding.recentDeviceEditText.setText(userDefinedName, TextView.BufferType.EDITABLE)
//    }


//    private fun displayActualName() {
//        val actualName = model.actualName.removeMasks()
//
//        binding.recentDeviceName.text = actualName
//        //binding.recentDeviceToChange.text = actualName
//        //binding.recentDeviceEditText.setText(actualName, TextView.BufferType.EDITABLE)
//    }

//    private fun setupListeners() {
//        //binding.recentDeviceToChange.setOnClickListener(editNameClickListener)
//        //binding.recentDeviceEditIcon.setOnClickListener(editNameClickListener)
//        //binding.recentDeviceEditText.setOnEditorActionListener(editNameEditorListener)
//        //binding.recentDeviceSave.setOnClickListener(saveButtonClickListener)
//    }
//
//    private fun saveChanges() {
////        if (binding.recentDeviceEditText.text.toString().isEmpty()) {
////            toast(R.string.device_name_cannot_be_empty)
////        } else {
////            val value = RecentDevice(model.id, model.actualName, binding.recentDeviceEditText.text.toString())
////            hideKeyboard(activity as Activity)
////            launch(UI) {
////                viewModel?.saveChanges(value)
////                RxBus.publish(NewNameEvent(binding.recentDeviceEditText.text.toString()));
////            }
////            popBackStack()
////        }
//    }