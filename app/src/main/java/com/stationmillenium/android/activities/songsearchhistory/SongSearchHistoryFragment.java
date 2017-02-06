/**
 *
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.annotation.SuppressLint;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.stationmillenium.android.R;
import com.stationmillenium.android.contentproviders.SongHistoryContract;
import com.stationmillenium.android.databinding.SongSearchHistoryFragmentBinding;

import static com.stationmillenium.android.libutils.R.color;

/**
 * Activity to display the song search history
 *
 * @author vincent
 */
public class SongSearchHistoryFragment extends Fragment {

    //static parts
    public enum LoadingState {
        LOADING,
        LOADED,
        ERROR
    }

    private SongSearchHistoryFragmentBinding binding;

    //instance vars
    private SimpleCursorAdapter cursorAdapter;

    @SuppressLint("InlinedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init widgets
        binding = DataBindingUtil.inflate(inflater, R.layout.song_search_history_fragment, container, false);
        binding.setActivity((SongSearchHistoryActivity) getActivity());
        binding.setFragment(this);
        binding.songHistorySwipeRefreshLayout.setColorSchemeResources(color.primary, color.accent);

        //cursor adapter
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.song_search_history_list_item, null,
                new String[]{
                        SongHistoryContract.Columns.DATE,
                        SongHistoryContract.Columns.ARTIST,
                        SongHistoryContract.Columns.TITLE
                },
                new int[]{
                        R.id.song_history_item_date_text,
                        R.id.song_history_item_artist_text,
                        R.id.song_history_item_title_text
                }, 0);
        binding.songHistoryList.setAdapter(cursorAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.songHistoryList.setNestedScrollingEnabled(true);
        }
        return binding.getRoot();
    }


    public SimpleCursorAdapter getCursorAdapter() {
        return cursorAdapter;
    }

    public void setLoadingState(LoadingState loadingState) {
        binding.setLoadingState(loadingState);
    }

    @BindingAdapter("itemClick")
    public static void setItemClick(ListView listView, OnItemClickListener onItemClickListener) {
        if (listView != null) {
            listView.setOnItemClickListener(onItemClickListener);
        }
    }
}
