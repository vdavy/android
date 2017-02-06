package com.stationmillenium.android.libutils.binding;

import android.databinding.BindingAdapter;
import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Binding class for {@link SwipeRefreshLayout}
 * Created by vincent on 05/02/17.
 */
public class SwipeRefreshLayoutBindingAdapter {

    /**
     * Bind a refreshing attribute
     * @param swipeRefreshLayout the {@link SwipeRefreshLayout} to setup
     * @param refreshing the refreshing state
     */
    @BindingAdapter("refreshing")
    public static void setRefreshing(SwipeRefreshLayout swipeRefreshLayout, boolean refreshing) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    /**
     * Bind a onRefresh attribute
     * @param swipeRefreshLayout the {@link SwipeRefreshLayout} to setup
     * @param onRefresh the refreshing action
     */
    @BindingAdapter("onRefresh")
    public static void setRefreshing(SwipeRefreshLayout swipeRefreshLayout, final OnRefresh onRefresh) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefresh.onRefresh();
                }
            });
        }
    }

    public interface OnRefresh {
        void onRefresh();
    }
}
