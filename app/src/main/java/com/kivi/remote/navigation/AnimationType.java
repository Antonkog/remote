package com.kivi.remote.navigation;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {AnimationType.NO_ANIM, AnimationType.FADE_ANIM})
public @interface AnimationType {
    int NO_ANIM = 0;
    int FADE_ANIM = 1;
}
