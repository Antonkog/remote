package com.kivi.remote.presentation.base;

import android.os.Bundle;

import com.kivi.remote.App;
import com.kivi.remote.common.Utils;
import com.kivi.remote.di.components.ActivityComponent;
import com.kivi.remote.di.components.ApplicationComponent;
import com.kivi.remote.di.modules.ActivityModule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ActivityComponent activityComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activityComponent = getApplicationComponent().provideActivityComponent(new ActivityModule(this));
        super.onCreate(savedInstanceState);
//        injectDependency();
    }

    ApplicationComponent getApplicationComponent() {
        return ((App) getApplication()).getApplicationComponent();
    }


    public abstract void injectDependency();

    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void onBackPressed() {
        Utils.hideKeyboard(this);
        super.onBackPressed();
    }
}
