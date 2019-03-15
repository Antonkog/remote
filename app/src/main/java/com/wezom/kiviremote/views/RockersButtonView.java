package com.wezom.kiviremote.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.wezom.kiviremote.App;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.databinding.ViewRockersButtonBinding;
import com.wezom.kiviremote.interfaces.RockersButtonClickListener;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.wezom.kiviremote.common.Constants.INITIAL_DELAY;
import static com.wezom.kiviremote.common.Constants.VOLUME_EVENT_FREQUENCY;


/**
 * Created by andre on 29.05.2017.
 */

public class RockersButtonView extends LinearLayout {
    Disposable disposable;

    RockersButtonClickListener<Integer> clickListener;

    ViewRockersButtonBinding binding;

    public RockersButtonView(Context context) {
        super(context);
        init(context);
    }

    public RockersButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setUp(context, attrs);
    }

    public RockersButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setUp(context, attrs);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = ViewRockersButtonBinding.inflate(inflater, this, true);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void setUp(Context context, AttributeSet attr) {
        TypedArray typedArr = context.obtainStyledAttributes(attr, R.styleable.RockersButtonView);

        final CharSequence description = typedArr.getText(R.styleable.RockersButtonView_rb_textDescription);

        binding.container.setBackground(ResourcesCompat.getDrawable(getResources(),App.isDarkMode() ? R.drawable.bg_rockets_black : R.drawable.bg_rockets, null ));
        binding.topImage.setImageResource(typedArr.getResourceId(R.styleable.RockersButtonView_rb_top_image, 0));
        binding.bottomImage.setImageResource(typedArr.getResourceId(R.styleable.RockersButtonView_rb_bottom_image, 0));
        binding.description.setText(description);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            binding.description.setTextColor(getResources().getColor(R.color.colorSecondaryText, null));
        else
            binding.description.setTextColor(getResources().getColor(R.color.colorSecondaryText));

        if (TextUtils.equals(binding.description.getText().toString(), getContext().getString(R.string.volume_string))) {
            binding.topImage.setOnTouchListener((v, event) -> onTouch(v, event, KeyEvent.KEYCODE_VOLUME_UP));
            binding.bottomImage.setOnTouchListener((v, event) -> onTouch(v, event, KeyEvent.KEYCODE_VOLUME_DOWN));
        } else {
            binding.topImage.setOnTouchListener((v, event) -> onTouch(v, event, KeyEvent.KEYCODE_CHANNEL_UP));
            binding.bottomImage.setOnTouchListener((v, event) -> onTouch(v, event, KeyEvent.KEYCODE_CHANNEL_DOWN));
        }

        typedArr.recycle();
    }

    public void setClickListener(RockersButtonClickListener<Integer> clickListener) {
        this.clickListener = clickListener;
    }

    private boolean onTouch(View v, MotionEvent event, int type) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dispose();
                disposable = Observable.interval(INITIAL_DELAY, VOLUME_EVENT_FREQUENCY, TimeUnit.MILLISECONDS)
                        .doOnSubscribe(t -> clickListener.onButtonClick(type))
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnError(e -> Timber.e(e, e.getMessage()))
                        .subscribe(t -> clickListener.onButtonClick(type));
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                dispose();
                break;
        }

        return false;
    }

    private void dispose() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }
}
