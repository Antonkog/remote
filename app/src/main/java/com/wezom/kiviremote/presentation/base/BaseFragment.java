package com.wezom.kiviremote.presentation.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wezom.kiviremote.di.components.ActivityComponent;
import com.wezom.kiviremote.di.components.FragmentComponent;
import com.wezom.kiviremote.di.modules.FragmentModule;

import io.reactivex.disposables.CompositeDisposable;


public abstract class BaseFragment extends Fragment {

    private FragmentComponent fragmentComponent;

    protected CompositeDisposable disposables;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentComponent = getActivityComponent()
                .providesFragmentComponent(new FragmentModule(this));
        injectDependencies();
        disposables = new CompositeDisposable();
    }

    public abstract void injectDependencies();

    @Override
    public void onDestroyView() {
        if (disposables != null && disposables.isDisposed())
            disposables.dispose();
        super.onDestroyView();
    }

    private ActivityComponent getActivityComponent() {
        return ((BaseActivity) getActivity()).getActivityComponent();
    }

    public FragmentComponent getFragmentComponent() {
        return fragmentComponent;
    }
}
