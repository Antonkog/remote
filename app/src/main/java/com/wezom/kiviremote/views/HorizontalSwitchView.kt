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


class HorizontalSwitchView : LinearLayout {
    lateinit var name: TextView
    lateinit var variant: TextView
    lateinit var arrow: ImageView

    //    private var varargs: HashMap<String, Any>? = hashMapOf()
    private var listener: OnSwitchListener? = null
//    private var currentList: LinkedList<String> = LinkedList()


    constructor(context: Context, listener: OnSwitchListener) : this(context, null) {
        this.listener = listener
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
            Timber.e("attrs ${name.text} ${variant.text}")

            arrow.setOnClickListener { click -> doOnclick() }
            variant.setOnClickListener { click -> doOnclick() }
        } finally {
            attributes.recycle()
        }
    }

    private fun doOnclick() {
        Timber.i("click")
    }

    fun setOnSwitchListener(l: OnSwitchListener) {
        listener = l
    }

    fun setVariants(@NotNull vars: HashMap<String, Any>) {
    }

    interface OnSwitchListener {
        fun onSwitch(currentEntry: Map.Entry<String, Any>?)
    }

}
