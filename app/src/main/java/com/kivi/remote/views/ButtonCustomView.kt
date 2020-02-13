package com.kivi.remote.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.kivi.remote.App
import com.kivi.remote.R
import com.kivi.remote.common.Constants


class ButtonCustomView : AppCompatImageView {


    private var listener: View.OnClickListener? = null

    constructor(context: Context, listener: OnClickListener) : this(context, null) {
        setListener(listener)
    }

    constructor(context: Context) : this(context, null)


    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {


        val attributes = context.obtainStyledAttributes(
                attrs, R.styleable.ButtonCustomView, defStyleAttr, 0)
        try {

            val strokeWidth = 5
            var strokeColor = ContextCompat.getColor(context, R.color.shadow_outline)
            var fillColor =  ContextCompat.getColor(context, R.color.colorWhite)

            if (App.isDarkMode()) {
                strokeColor =  ContextCompat.getColor(context, R.color.kiviDark)
                fillColor =  ContextCompat.getColor(context, R.color.btnDark)
            }
            val gD = GradientDrawable()
            gD.setColor(fillColor)
            gD.shape = GradientDrawable.OVAL
            gD.setStroke(strokeWidth, strokeColor)
            this.background = gD


//            if (App.isDarkMode()) this.background = ContextCompat.getDrawable(context, R.drawable.ic_btn_back)
//            else this.background = ContextCompat.getDrawable(context, R.drawable.ic_btn)
            val drRes = attributes.getResourceId(R.styleable.ButtonCustomView_central_dr, Constants.NO_VALUE)
            if (drRes != Constants.NO_VALUE) setCentralDr(drRes)
        } finally {
            attributes.recycle()
        }


        this.isClickable = true
        this.scaleType = ScaleType.CENTER_INSIDE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.focusable = View.FOCUSABLE
        }


    }


fun setCentralDr(dr: Int) {
    this.setImageResource(dr)
}

fun setListener(listener: View.OnClickListener) {
    this.listener = listener
}


override fun performClick(): Boolean {
    listener?.onClick(this)
    return super.performClick()
}
}
