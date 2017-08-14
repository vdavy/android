package com.stationmillenium.android.activities;

import android.view.Menu;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;
import com.stationmillenium.android.R;

/**
 * Expanded controller activity instance class
 * Created by vincent on 14/08/17.
 */

public class CastExpandedControlsActivity extends ExpandedControllerActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.cast_menu);
        return true;
    }

}
