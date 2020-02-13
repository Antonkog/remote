package com.kivi.remote.presentation.base;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.kivi.remote.di.components.ActivityComponent;
import com.kivi.remote.di.components.FragmentComponent;
import com.kivi.remote.di.modules.FragmentModule;


public abstract class BaseFragment extends Fragment {

    private FragmentComponent fragmentComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentComponent = getActivityComponent()
                .providesFragmentComponent(new FragmentModule(this));
        injectDependencies();
    }

    public abstract void injectDependencies();

    private ActivityComponent getActivityComponent() {
        return ((BaseActivity) getActivity()).getActivityComponent();
    }

    public FragmentComponent getFragmentComponent() {
        return fragmentComponent;
    }
}
