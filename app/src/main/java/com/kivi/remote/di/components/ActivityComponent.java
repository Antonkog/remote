package com.kivi.remote.di.components;

import com.kivi.remote.di.modules.ActivityModule;
import com.kivi.remote.di.modules.FragmentModule;
import com.kivi.remote.di.scopes.ActivityScope;
import com.kivi.remote.presentation.home.HomeActivity;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {
    FragmentComponent providesFragmentComponent(FragmentModule fragmentModule);

    void inject(HomeActivity homeActivity);
}
