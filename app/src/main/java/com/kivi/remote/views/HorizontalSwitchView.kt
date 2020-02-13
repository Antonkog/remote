package com.kivi.remote.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.kivi.remote.R
import com.kivi.remote.net.model.AspectMessage
import org.jetbrains.annotations.NotNull
import java.util.*


class HorizontalSwitchView : LinearLayout {
    lateinit var name: TextView
    lateinit var variant: TextView
    lateinit var arrow: ImageView

    private var listener: OnSwitchListener? = null
    private var varargs: LinkedList<Int> = LinkedList()
    private var aspectValueType: AspectMessage.ASPECT_VALUE? = null


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
            arrow.setOnClickListener { _-> doOnclick() }
            variant.setOnClickListener { _-> doOnclick() }
        } finally {
            attributes.recycle()
        }
    }

    private fun doOnclick() {
        var position = varargs.indexOf(variant.tag)
        if (position == -1) return
        when (position) {
            varargs.size - 1 -> position = 0
            varargs.size -> position = 0
            else -> {
                position++
            }
        }
        variant.text = resources.getString(varargs[position])
        variant.tag = varargs[position]
        listener?.onSwitch(aspectValueType, varargs[position])
    }

    fun setOnSwitchListener(l: OnSwitchListener) {
        listener = l
    }

    fun setVariants(mode: AspectMessage.ASPECT_VALUE, @NotNull vars: List<Int>) {
        varargs = LinkedList(vars.distinct())
        aspectValueType = mode
        if (varargs.size > 0) {
            if (varargs.size == 1) this.visibility = View.GONE
            var position = varargs.indexOf(variant.tag)
            if (position == -1) {
                variant.text = resources.getString(varargs[0])
                variant.tag = varargs[0]
            } else {
                variant.text = resources.getString(varargs[position])
                variant.tag = varargs[position]
            }
        }
        this.invalidate()
    }

    interface OnSwitchListener {
        fun onSwitch(aspectValueType: AspectMessage.ASPECT_VALUE?, resId: Int)
    }

}
