package com.wanjf.wxr.views.hoverListView;

import java.io.Serializable;

public class HoverData implements Serializable {
    protected int hover;
    protected int data;

    public int getHover() {
        return hover;
    }

    public void setHover(int hover) {
        this.hover = hover;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}
