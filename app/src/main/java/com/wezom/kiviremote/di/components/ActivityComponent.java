package com.wezom.kiviremote.di.components;

import com.wezom.kiviremote.di.modules.ActivityModule;
import com.wezom.kiviremote.di.modules.FragmentModule;
import com.wezom.kiviremote.di.scopes.ActivityScope;
import com.wezom.kiviremote.presentation.home.HomeActivity;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {
    FragmentComponent providesFragmentComponent(FragmentModule fragmentModule);

    void inject(HomeActivity homeActivity);
}
