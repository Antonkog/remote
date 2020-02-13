package com.kivi.remote.common

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable


object RxBus {
    private val publisher = PublishRelay.create<Any>()

    fun publish(event: Any) = publisher.accept(event)

    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}