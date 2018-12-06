package com.wezom.kiviremote.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wezom.kiviremote.R
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.util.*


class AspectHeaderView : LinearLayout {
    lateinit var header: TextView
    lateinit var row: TextView
    lateinit var arrowl: ImageView
    lateinit var arrowr: ImageView

    private var listener: OnSwitchListener? = null
    private var varargs: LinkedList<String> = LinkedList()


    constructor(context: Context, listener: OnSwitchListener) : this(context, null) {
        this.listener = listener
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
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
            arrowl.setOnClickListener { click -> doOnLeftclick() }
            arrowr.setOnClickListener { click -> doOnRightclick() }
        } finally {
            attributes.recycle()
        }
    }

    private fun doOnRightclick() {
        var position = varargs.indexOf(row.text)
        Timber.i(" position of " + row.text + " pos =" + position)
        when (position) {
            -1 -> return
            varargs.size - 1 -> position = 0
            varargs.size -> position = 0
            else -> {
                position++
            }
        }
        row.text = varargs[position]
        listener?.onSwitch(varargs[position])    }

    private fun doOnLeftclick() {
        var position = varargs.indexOf(row.text)
        Timber.i(" position of " + row.text + " pos =" + position)
        when (position) {
            -1 -> return
            0 -> position = varargs.size - 1
            else -> {
                position--
            }
        }
        row.text = varargs[position]
        listener?.onSwitch(varargs[position])
    }


    fun setOnSwitchListener(l: OnSwitchListener) {
        listener = l
    }

    fun setVariants(@NotNull vars: LinkedList<String>) {
        varargs = vars
    }

    interface OnSwitchListener {
        fun onSwitch(currentEntry: String)
    }

}
