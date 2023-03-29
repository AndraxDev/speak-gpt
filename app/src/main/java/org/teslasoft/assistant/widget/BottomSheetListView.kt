package org.teslasoft.assistant.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.AbsListView
import android.widget.ListView

class BottomSheetListView(context: Context?, p_attrs: AttributeSet?) :
    ListView(context, p_attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (canScrollVertically(this)) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        performClick()

        return super.onTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun canScrollVertically(view: AbsListView?): Boolean {
        var canScroll = false
        if (view != null && view.childCount > 0) {
            val isOnTop = view.firstVisiblePosition != 0 || view.getChildAt(0).top != 0
            val isAllItemsVisible = isOnTop && view.lastVisiblePosition == view.childCount
            if (isOnTop || isAllItemsVisible) {
                canScroll = true
            }
        }

        return canScroll
    }
}