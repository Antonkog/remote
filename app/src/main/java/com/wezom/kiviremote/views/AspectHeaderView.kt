package com.wezom.kiviremote.views

import android.content.Context
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.net.model.AspectMessage
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.util.*


class AspectHeaderView : LinearLayout {


    lateinit var header: TextView
    lateinit var row: TextView
    lateinit var arrowl: ImageView
    lateinit var arrowr: ImageView

    private var listener: OnSwitchListener? = null
    private var varargs: LinkedList<Int> = LinkedList()
    private var aspectValueType: AspectMessage.ASPECT_VALUE? = null


    constructor(context: Context, listener: OnSwitchListener) : this(context, null) {
        this.listener = listener
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
        this.aspectValueType = aspectValueType

    }


    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.aspect_header, this)
        header = findViewById(R.id.header)
        row = findViewById(R.id.row)
        arrowl = findViewById(R.id.arrow_l)
        arrowr = findViewById(R.id.arrow_r)

        val attributes = context.obtainStyledAttributes(
                attrs, R.styleable.AspectHeaderView, defStyle, 0)

        try {
            header.text = attributes.getString(R.styleable.HorizontalSwitchView_name)
            row.text = attributes.getString(R.styleable.HorizontalSwitchView_variant)
            Timber.i("attrs ${header.text} ${row.text}")
            arrowl.setOnClickListener { click -> doOnLeftClick() }
            arrowr.setOnClickListener { click -> doOnRightclick() }
        } finally {
            attributes.recycle()
        }
    }


    private fun doOnRightclick() {
        var position = varargs.indexOf(row.tag)
        Timber.i(" position of " + row.text + " pos =" + position)
//        if(row.tag == null && varargs.isNotEmpty()) position = 0
        when (position) {
            -1 -> return
            varargs.size - 1 -> position = 0
            varargs.size -> position = 0
            else -> {
                position++
            }
        }
        row.text = resources.getString(varargs[position])
        row.tag = varargs[position]
        listener?.onSwitch(aspectValueType, varargs[position])
    }

    private fun doOnLeftClick() {
        var position = varargs.indexOf(row.tag)
        Timber.i(" position of " + row.text + " pos =" + position)
//        if(row.tag == null && varargs.isNotEmpty()) position = varargs.size - 1
        when (position) {
            -1 -> return
            0 -> position = varargs.size - 1
            else -> {
                position--
            }
        }
        row.text = resources.getString(varargs[position])
        row.tag = varargs[position]
        listener?.onSwitch(aspectValueType, varargs[position])
    }


    fun setOnSwitchListener(l: OnSwitchListener) {
        listener = l
    }


    fun setVariants( mode: AspectMessage.ASPECT_VALUE, @NotNull vars: List<Int>) {
        varargs = LinkedList(vars.distinct())
        aspectValueType = mode
        row.text =  resources.getString(varargs[0])
        row.tag = varargs[0]
    }


    interface OnSwitchListener {
        fun onSwitch(aspectValueType: AspectMessage.ASPECT_VALUE?, resId: Int)
    }

}
