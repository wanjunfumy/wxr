package com.wanjf.wxr.views.hoverListView;

import android.widget.AbsListView;

public interface OnSwipeListener {
    /**
     * 是否回到了listView顶部
     * @param hasTop 是否到了顶部
     */
    void onSwipe(boolean hasTop);

    /**
     * 同步滚动listView的滚动
     * @param absListView
     * @param i
     * @param i1
     * @param i2
     */
    void OnScroll(AbsListView absListView, int i, int i1, int i2, HoverListView.Hoveradapter adapter);
}
