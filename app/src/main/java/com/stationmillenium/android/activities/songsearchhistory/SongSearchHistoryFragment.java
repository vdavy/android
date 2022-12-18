/**
 *
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;

import com.stationmillenium.android.R;
import com.stationmillenium.android.databinding.SongSearchHistoryFragmentBinding;
import com.stationmillenium.android.providers.SongHistoryContract;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init widgets
        binding = SongSearchHistoryFragmentBinding.inflate(inflater, container, false);
        binding.setActivity((SongSearchHistoryActivity) getActivity());
        binding.songHistorySwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);

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
