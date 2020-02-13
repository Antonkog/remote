package com.kivi.remote.presentation.base.view;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {

    private int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence newChar, int start, int end, Spanned oldChar, int dstart, int dend) {
        try {

            int input = -1;
            if (dstart == 1) {
                input = Integer.parseInt(oldChar.toString() + newChar.toString());
            } else if (dstart == 0) {
                input = Integer.parseInt(newChar.toString() + oldChar.toString());
            }

            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }


        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}