package com.yugy.qingbo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.yugy.qingbo.R;

/**
 * Created by yugy on 13-12-30.
 */
public class LoadMoreView extends FrameLayout{
    public LoadMoreView(Context context) {
        super(context);
        init();
    }

    public LoadMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public static final int TYPE_LOADING = 0;
    public static final int TYPE_CLICK_TO_LOAD_MORE = 1;

    private int mType = TYPE_LOADING;

    private ViewSwitcher mViewSwitcher;
    private TextView mLoadMore;

    private void init(){
        inflate(getContext(), R.layout.widget_loadmore_footer, this);
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.loadmore_view_switcher);
        mLoadMore = (TextView) findViewById(R.id.loadmore_loadmore_text);
    }

    public void setType(int type){
        switch (type){
            case TYPE_LOADING:
                if(mType == TYPE_CLICK_TO_LOAD_MORE){
                    mViewSwitcher.showPrevious();
                }
                break;
            case TYPE_CLICK_TO_LOAD_MORE:
                if(mType == TYPE_LOADING){
                    mViewSwitcher.showNext();
                }
                break;
        }
        mType = type;
    }

    public void setLoadMoreOnClickListener(OnClickListener listener){
        mLoadMore.setOnClickListener(listener);
    }
}
