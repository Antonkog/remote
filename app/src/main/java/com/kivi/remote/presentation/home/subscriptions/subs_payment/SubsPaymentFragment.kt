package com.kivi.remote.presentation.home.subscriptions.subs_payment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.kivi.remote.R
import com.kivi.remote.databinding.SubsPaymentFragmentBinding
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.base.view.InputFilterMinMax
import javax.inject.Inject

class SubsPaymentFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: SubsPaymentFragmentBinding
    private lateinit var viewModel: SubsPaymentViewModel

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SubsPaymentFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SubsPaymentViewModel::class.java)

        binding.etDayNumber.filters = arrayOf(InputFilterMinMax(0, 31))
        binding.etMonthNumber.filters = arrayOf(InputFilterMinMax(0, 12))

        setTextWatcher(binding.etDayNumber, 10.0f, 18.0f)
        setTextWatcher(binding.etMonthNumber, 10.0f, 18.0f)
        setTextWatcher(binding.etCardNumber)

        initUserRulesTextView()
    }

    private fun setTextWatcher(editText: EditText, beforeInputTextSize: Float = 0.0f, afterInputTextSize: Float = 0.0f) {
        editText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(editable: Editable?) {}

            override fun beforeTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (charSequence == null) { return }

                when (editText.id) {
                    R.id.et_card_number -> {
                        if (!charSequence.contains("•") && p3 != 0) {
                            binding.etDayNumber.requestFocus()
                        }
                    }
                    R.id.et_day_number -> {
                        editText.textSize = if (charSequence.isNotEmpty()) { afterInputTextSize } else { beforeInputTextSize }
                        if (charSequence.length == 2) { binding.etMonthNumber.requestFocus() }
                    }
                    R.id.et_month_number -> {
                        editText.textSize = if (charSequence.isNotEmpty()) { afterInputTextSize } else { beforeInputTextSize }
                        if (charSequence.length == 2) { binding.etCardCvv.requestFocus() }
                    }
                }
            }
        })
    }

    private fun initUserRulesTextView() {
        binding.tvUserRules.apply {
            val text = "Совершая покупку, вы принимаете\nусловия Пользовательского Соглашения.\nС текстом соглашения можно ознакомиться в профиле."
            val firstIndex = text.indexOf("Пользовательского Соглашения.")
            val lastIndex = firstIndex + "Пользовательского Соглашения.".length

            val spannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.setSpan(ForegroundColorSpan(Color.BLUE), firstIndex, lastIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            spannableStringBuilder.setSpan(UnderlineSpan(), firstIndex, lastIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            spannableStringBuilder.setSpan(object: ClickableSpan() {
                override fun onClick(view: View) { onUserRulesClick() }
            }, firstIndex, lastIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            this.text = spannableStringBuilder
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun onUserRulesClick() {
        Toast.makeText(context!!, "TestToast", Toast.LENGTH_LONG).show()
    }

}