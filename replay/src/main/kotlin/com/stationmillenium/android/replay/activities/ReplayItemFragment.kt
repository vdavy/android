package com.stationmillenium.android.replay.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment

import com.stationmillenium.android.replay.R
import com.stationmillenium.android.replay.databinding.ReplayItemFragmentBinding
import com.stationmillenium.android.replay.dto.TrackDTO

/**
 * Replay item fragment
 * Created by vincent on 28/11/16.
 */
class ReplayItemFragment : Fragment() {

    private lateinit var binding: ReplayItemFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_item_fragment, container, false)
        binding.activity = activity as ReplayItemActivity
        return binding.root
    }

    /**
     * Bind the replay data
     * @param replay replay to display
     */
    fun setReplay(replay: TrackDTO) {
        binding.replayItem = replay
        setPlayedTimeAndDuration(0, 0)
    }

    /**
     * Set the progress bar visibility
     * @param visible `true` : visible, `false` : hidden
     */
    fun setProgressBarVisible(visible: Boolean) {
        binding.replayItemProgressbar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Set the played time and the duration for progress display
     * @param playedTime the played time in second
     * @param duration the duration in second
     */
    fun setPlayedTimeAndDuration(playedTime: Int, duration: Int) {
        binding.playedTime = playedTime
        binding.duration = duration
    }

    fun setPlayingOnChromecast(playingOnChromecast: Boolean) {
        if (playingOnChromecast) {
            setProgressBarVisible(false)
            setPlayedTimeAndDuration(0, 0)
        }
    }
}
