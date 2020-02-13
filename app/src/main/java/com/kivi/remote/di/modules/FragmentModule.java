package com.kivi.remote.di.modules;


import androidx.fragment.app.Fragment;

import dagger.Module;

@Module
public class FragmentModule {
    private Fragment fragment;
    public FragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }
}
