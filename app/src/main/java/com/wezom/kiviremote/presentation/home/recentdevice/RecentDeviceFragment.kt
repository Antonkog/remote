package com.wezom.kiviremote.presentation.home.recentdevice

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.wezom.kiviremote.R
import com.wezom.kiviremote.bus.NewNameEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.remove032Space
import com.wezom.kiviremote.common.hideKeyboard
import com.wezom.kiviremote.databinding.RecentDeviceFragmentBinding
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.Serializable
import javax.inject.Inject

class RecentDeviceFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: RecentDeviceFragmentBinding
    lateinit var viewModel: RecentDeviceViewModel

    private lateinit var data: RecentDevice

    private lateinit var dialog: AlertDialog
    private lateinit var dialogEditText: EditText

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecentDeviceFragmentBinding.inflate(inflater, container!!, false)

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

        val adapter = TvInfoAdapter()
        data = arguments!!.getSerializable("data") as RecentDevice

        dialogEditText.setText((data.userDefinedName ?: "").remove032Space())

        binding.rvInfoContainer.apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            this.setHasFixedSize(true)
        }

        binding.ivForgetDevice.setOnClickListener {
            confirmDeletion()
        }

        binding.tvForgetDevice.setOnClickListener {
            confirmDeletion()
        }

        binding.tvRename.setOnClickListener {
            dialog.show()
        }

        binding.ivRename.setOnClickListener {
            dialog.show()
        }
//
//        // Test data
//        adapter.swapData(listOf(
//                TvInfoUnit("Диагональ", "32"),
//                TvInfoUnit("Тип матрицы", "IPS"),
//                TvInfoUnit("Bluetooth модуль", "4.2"),
//                TvInfoUnit("Статус сети:", "DIR-320"),
//                TvInfoUnit("Разрешение экрана", "1920х1080"),
//                TvInfoUnit("Автоподключение", "Да")
//        ))
    }

    @SuppressLint("CheckResult")
    private fun confirmDeletion() {
        Completable.fromAction { viewModel.database.recentDeviceDao()?.removeByName(data.actualName) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                        viewModel.goBack(data)
                }
    }

    private fun renameDevice(newName: String) {
        if (newName.isEmpty()) {
//            toast(R.string.device_name_cannot_be_empty)
        } else {
            val value = RecentDevice(data.actualName).apply {
                userDefinedName = newName
                isOnline = true
                wasConnected = System.currentTimeMillis()
            }
            hideKeyboard(activity as Activity)
            launch(UI) {
                viewModel.saveChanges(value)
                RxBus.publish(NewNameEvent(newName))
                binding.tvDeviceName.text = "УСТРОЙСТВО ${newName.remove032Space()}"
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