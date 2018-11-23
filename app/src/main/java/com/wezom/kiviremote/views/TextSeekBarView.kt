package com.wezom.kiviremote.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.wezom.kiviremote.R


class TextSeekBarView : LinearLayout   {
    lateinit var textView: TextView
    lateinit var seekBar: SeekBar

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_seekbar, this)
        textView = findViewById(R.id.txt)
        seekBar = findViewById(R.id.seek)

        val attributes = context.obtainStyledAttributes(
                attrs, R.styleable.TextSeekBarView, defStyle, 0)

       try {
           textView.text = attributes.getString(R.styleable.TextSeekBarView_text)
           seekBar.progress = attributes.getInt(R.styleable.TextSeekBarView_progress, 50)
        }
        finally {
            attributes.recycle()
        }
    }


//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        if (atrs.hasValue(R.styleable.TextSeekBarView_text)) {
//            var txt = atrs.getString(
//                    R.styleable.TextSeekBarView_text)
//
//
//            textView.setText(txt)
//        }
//    }
}
