package com.wezom.kiviremote.views

import android.content.Context
import android.content.res.Resources
import android.support.annotation.IntegerRes
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.net.model.AspectMessage
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.util.*


class HorizontalSwitchView : LinearLayout {
    lateinit var name: TextView
    lateinit var variant: TextView
    lateinit var arrow: ImageView

    private var listener: OnSwitchListener? = null
    private var varargs: LinkedList<Int> = LinkedList()
    private var aspectValueType: AspectMessage.ASPECT_VALUE ?  = null


    constructor(context: Context, aspectValueType: AspectMessage.ASPECT_VALUE, listener: OnSwitchListener) : this(context, null) {
        this.listener = listener
        this.aspectValueType = aspectValueType
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }


    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_horizontal_switch, this)
        name = findViewById(R.id.name)
        variant = findViewById(R.id.variant)
        arrow = findViewById(R.id.arrow)

        val attributes = context.obtainStyledAttributes(
                attrs, R.styleable.HorizontalSwitchView, defStyle, 0)

        try {
            name.text = attributes.getString(R.styleable.HorizontalSwitchView_name)
            variant.text = attributes.getString(R.styleable.HorizontalSwitchView_variant)
            Timber.i("attrs ${name.text} ${variant.text}")
            arrow.setOnClickListener { click -> doOnclick() }
            variant.setOnClickListener { click -> doOnclick() }
        } finally {
            attributes.recycle()
        }
    }

    private fun doOnclick() {
        var position = varargs.indexOf(variant.tag)
        Timber.i(" position of " + variant.text + " pos =" + position)
//        if(variant.tag == null && varargs.isNotEmpty()) position = 0
        when (position) {
            -1 -> position = 0
            varargs.size - 1 -> position = 0
            varargs.size -> position = 0
            else -> {
                position++
            }
        }
        variant.text =  resources.getString(varargs[position].toInt())
        variant.tag = varargs[position]
        listener?.onSwitch(aspectValueType,  varargs[position])
    }

    fun setOnSwitchListener(l: OnSwitchListener) {
        listener = l
    }

    fun setVariants(mode: AspectMessage.ASPECT_VALUE, @NotNull vars: List<Int>) {
        varargs = LinkedList(vars.distinct())
        aspectValueType = mode
        variant.text =  resources.getString(varargs[0])
        variant.tag = varargs[0]
    }

    interface OnSwitchListener {
        fun onSwitch(aspectValueType : AspectMessage.ASPECT_VALUE ? , resId : Int)
    }

}