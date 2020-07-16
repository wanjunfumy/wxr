package com.wanjf.wxr.views.hoverListView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanjf.wxr.R;

import java.util.ArrayList;

/**
 * 数据需要extends HoverData
 */
public class HoverListView<T extends HoverData> extends FrameLayout implements AdapterView.OnItemClickListener {
    //region views
    private ListView list;
    private TextView hover_text;
    private TextView view;
    private View empty;
    //endregion

    //region 数据
    private OnSwipeListener mOnSwipeListener;
    private OnViewListener mOnViewListener;
    private ArrayList<T> lists;
    private Hoveradapter adapter;
    private OnItemClickListener itemClickListener;
    //endregion

    public HoverListView(@NonNull Context context) {
        super(context);
        init();
    }

    public HoverListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoverListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoverListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.hover_list, this);
        list = findViewById(R.id.no_body_list);
        hover_text = findViewById(R.id.hover_text);
        hover_text.setVisibility(GONE);
        empty = findViewById(R.id.no_body_empty);
        list.setEmptyView(empty);
        lists = new ArrayList<>();
        adapter = new Hoveradapter(lists);
        list.setAdapter(adapter);
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if (list.getChildCount() == 0) {
                    return;
                }
                View _view = list.getChildAt(0);
                if (i == 0 && _view.getTop() == 0) {
                    // 滚动到了顶部
                    if (mOnSwipeListener != null) {
                        mOnSwipeListener.onSwipe(true);
                    }
                } else {
                    if (mOnSwipeListener != null) {
                        mOnSwipeListener.onSwipe(false);
                    }
                }

                if (mOnSwipeListener != null) {
                    mOnSwipeListener.OnScroll(absListView, i, i1, i2, adapter);
                }
            }
        });
    }

    public void addFootView(OnClickListener l) {
        view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.view_footer, null);
        if (list.getFooterViewsCount() == 0) {
            list.addFooterView(view);
            view.setOnClickListener(l);
        }
    }

    /**
     * @param lists
     */
    public void setAdapterList(@NonNull ArrayList<T> lists) {
        this.lists = lists;
        if (this.lists.size() == 0) {
            this.list.removeFooterView(view);
        }
        if (adapter != null) {
            adapter.notifyData(this.lists);
        }
    }

    public void setHoverText(String text) {
        if (hover_text != null) {
            hover_text.setText(text);
            hover_text.setVisibility(VISIBLE);
        }
    }

    public void hideHoverText() {
        hover_text.setVisibility(GONE);
    }

    public void setFootViewText(String footViewText) {
        if (view != null) {
            view.setText(footViewText);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.itemClickListener != null) {
            this.itemClickListener.onItemClick(adapter.getItem(position), position);
        }
    }

    /**
     * 适配器
     */
    public class Hoveradapter extends BaseAdapter {

        private ArrayList<T> lists;

        public Hoveradapter(ArrayList<T> lists) {
            this.lists = (ArrayList<T>) lists.clone();
        }

        public void notifyData(ArrayList<T> lists) {
            this.lists = (ArrayList<T>) lists.clone();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return this.lists == null ? 0 : this.lists.size();
        }

        @Override
        public T getItem(int position) {
            return this.lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T t = getItem(position);
            if (convertView == null) {
                if (t.getHover() != 0) {// hover
                    if (mOnViewListener != null) {
                        convertView = mOnViewListener.initHover();
                    }
                } else if (t.getData() != 0) {// 数据
                    if (mOnViewListener != null) {
                        convertView = mOnViewListener.initBody();
                    }
                }
            } else {// 如何界面重用view的类型适配问题
                if (t.getHover() != 0) {// 悬浮
                    if (convertView.getTag() instanceof Body) {
                        if (mOnViewListener != null) {
                            convertView = mOnViewListener.initHover();
                        }
                    }
                } else if (t.getData() != 0) {
                    if (convertView.getTag() instanceof Hover) {
                        if (mOnViewListener != null) {
                            convertView = mOnViewListener.initBody();
                        }
                    }
                }
            }
            if (convertView != null) {
                if (t.getHover() != 0) {
                    if (mOnViewListener != null) {
                        assert convertView.getTag() != null: "may be initBody() never setTag()";
                        mOnViewListener.bindHover((Hover) convertView.getTag(), t);
                    }
                } else if (t.getData() != 0) {
                    if (mOnViewListener != null) {
                        assert convertView.getTag() != null: "may be initBody() never setTag()";
                        mOnViewListener.bindBody((Body) convertView.getTag(), t);
                    }
                }
            }
            return convertView;
        }

    }

    public void setOnSwipeListener(OnSwipeListener mOnSwipeListener) {
        this.mOnSwipeListener = mOnSwipeListener;
    }

    public void setOnViewListener(OnViewListener mOnViewListener) {
        this.mOnViewListener = mOnViewListener;
    }

    public interface OnViewListener {
        View initHover();
        View initBody();

        <T extends Hover, J extends HoverData> void bindHover(@Nullable T hover, J data);
        <T extends Body, J extends HoverData> void bindBody(@Nullable T body, J data);
    }

    public interface OnItemClickListener {
        <T extends HoverData> void onItemClick(T data, int position);
    }
}
