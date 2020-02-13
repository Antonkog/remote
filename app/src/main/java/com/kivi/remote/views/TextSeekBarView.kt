package com.kivi.remote.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kivi.remote.R


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

        seekBar.progressDrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.MULTIPLY)


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


}
