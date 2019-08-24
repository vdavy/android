/**
 *
 */
package com.stationmillenium.android.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.stationmillenium.android.R;
import com.stationmillenium.android.databinding.WebviewFragmentBinding;

import timber.log.Timber;

import static com.stationmillenium.android.libutils.R.color.accent;
import static com.stationmillenium.android.libutils.R.color.primary;

/**
 * Abstract webview fragment
 *
 * @author vincent
 */
public class AntennaGridWebViewFragement extends Fragment {

    private WebviewFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.webview_fragment, container, false);
        binding.webviewSwipeRefreshLayout.setColorSchemeResources(primary, accent);
        binding.setFragment(this);
        binding.setProgress(0);
        return binding.getRoot();
    }

    public WebView getWebView() {
        return binding.webview;
    }


    /**
     * Called from data binding
     */
    public void onRefresh() {
        Timber.d("Reload the web view");

        if (binding.webview != null) {
            binding.webview.reload();
        }
    }

    public void setProgress(int progress) {
        binding.setProgress(progress);
    }

}
