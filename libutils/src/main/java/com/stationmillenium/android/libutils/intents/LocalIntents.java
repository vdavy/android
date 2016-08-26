/**
 *
 */
package com.stationmillenium.android.libutils.intents;

/**
 * Intents used in the app
 *
 * @author vincent
 */
public enum LocalIntents {

    CURRENT_TITLE_UPDATED("CURRENT_TITLE_UPDATED"),
    PLAYER_PLAY("PLAYER_PLAY"),
    PLAYER_PLAY_PAUSE("PLAYER_PLAY_PAUSE"),
    PLAYER_PAUSE("PLAYER_PAUSE"),
    PLAYER_STOP("PLAYER_STOP"),
    PLAYER_OPEN("PLAYER_OPEN"),
    PLAYER_ACTIVITY_RESUME("PLAYER_ACTIVITY_RESUME"),
    PLAYER_ACTIVITY_PAUSE("PLAYER_ACTIVITY_PAUSE"),
    ON_PLAYER_PLAY("ON_PLAYER_PLAY"),
    ON_PLAYER_PAUSE("ON_PLAYER_PAUSE"),
    ON_PLAYER_STOP("ON_PLAYER_STOP"),
    ON_PLAYER_BUFFERING("ON_PLAYER_BUFFERING"),
    ON_PLAYER_OPEN("ON_PLAYER_OPEN"),
    SET_ALARM_TIME("SET_ALARM_TIME"),
    ON_ALARM_TIME_ELAPSED("ON_ALARM_TIME_ELAPSED"),
    ON_TIME_PICKED_UP("ON_TIME_PICKED_UP"),
    ON_DATE_PICKED_UP("ON_DATE_PICKED_UP");

    private String intentValue;

    private LocalIntents(String intentValue) {
        this.intentValue = "com.stationmillenium.android.intents." + intentValue;
    }

    @Override
    public String toString() {
        return intentValue;
    }
}
