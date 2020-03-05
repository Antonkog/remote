package com.kivi.remote.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kivi.remote.common.dpToPx


@Suppress("unused")
class LockableBottomSheetBehavior<V : View> : BottomSheetBehavior<V> {
    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var scrollHinDp = 60


    companion object {
        /**
         * A utility function to get the [LockableBottomSheetBehavior] associated with the `view`.
         *
         * @param view The [View] with [LockableBottomSheetBehavior].
         * @return The [LockableBottomSheetBehavior] associated with the `view`.
         */
        fun <V : View> from(view: V): LockableBottomSheetBehavior<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                    ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            val behavior = params
                    .behavior as? BottomSheetBehavior<*> ?: throw IllegalArgumentException(
                    "The view is not associated with BottomSheetBehavior")
            return behavior as LockableBottomSheetBehavior<V>
        }


    }

    var swipeEnabled = true


    var shouldProceed = false

    override fun onInterceptTouchEvent(
            parent: CoordinatorLayout,
            child: V,
            event: MotionEvent
    ): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            val maxH = child.top + dpToPx(parent.context, scrollHinDp)
            shouldProceed = (event.y < maxH && event.y > child.top)
//            Timber.e("qwqqe" + event.action + " evnt y" + event.y + " child top + " + child.top + " maxH  = " + maxH + " topdrag? " + shouldProceed)
        }
//        if(event.action == MotionEvent.ACTION_UP) shouldProceed = false
        if (shouldProceed) return super.onInterceptTouchEvent(parent, child, event)
        return if (swipeEnabled) {
            super.onInterceptTouchEvent(parent, child, event)
        } else {
            false
        }
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (shouldProceed) return super.onTouchEvent(parent, child, event)
        return if (swipeEnabled) {
            super.onTouchEvent(parent, child, event)
        } else {
            false
        }
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int
    ): Boolean {
        return if (swipeEnabled) {
            super.onStartNestedScroll(
                    coordinatorLayout,
                    child,
                    directTargetChild,
                    target,
                    axes,
                    type
            )
        } else {
            false
        }
    }

    override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            dx: Int,
            dy: Int,
            consumed: IntArray,
            type: Int
    ) {
        if (swipeEnabled) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            type: Int
    ) {
        if (swipeEnabled) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }
    }

    override fun onNestedPreFling(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            velocityX: Float,
            velocityY: Float
    ): Boolean {
        return if (swipeEnabled) {
            super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        } else {
            false
        }
    }
}
