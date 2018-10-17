package com.stationmillenium.android.libutils.activities;

import com.stationmillenium.android.libutils.intents.LocalIntents;

/**
 * Available player state, with associated {@link LocalIntents}
 *
 * @author vincent
 */
public enum PlayerState {
    PLAYING(LocalIntents.ON_PLAYER_PLAY),
    PAUSED(LocalIntents.ON_PLAYER_PAUSE),
    STOPPED(LocalIntents.ON_PLAYER_STOP),
    BUFFERING(LocalIntents.ON_PLAYER_BUFFERING);

    private LocalIntents associatedIntent;

    PlayerState(LocalIntents associatedIntent) {
        this.associatedIntent = associatedIntent;
    }

    /**
     * Get the associated {@link LocalIntents}
     *
     * @return the {@link LocalIntents}
     */
    public LocalIntents getAssociatedIntent() {
        return associatedIntent;
    }
}