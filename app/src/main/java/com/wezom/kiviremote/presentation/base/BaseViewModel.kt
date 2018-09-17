package com.wezom.kiviremote.presentation.base

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


abstract class BaseViewModel : ViewModel() {
    protected operator fun CompositeDisposable.plusAssign(value: Disposable) {
        this.add(value)
    }

    protected val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCleared() {
        disposables.takeIf { !it.isDisposed }?.dispose()
        super.onCleared()
    }
}