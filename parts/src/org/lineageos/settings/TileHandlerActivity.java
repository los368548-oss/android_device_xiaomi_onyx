/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.lineageos.settings.dolbyvision.DolbyVisionActivity;
import org.lineageos.settings.dolbyvision.DolbyVisionTileService;
import org.lineageos.settings.turbocharging.TurboChargingActivity;
import org.lineageos.settings.turbocharging.TurboChargingTile;

import java.util.HashMap;
import java.util.Map;

public class TileHandlerActivity extends Activity {

    private static final String TAG = "TileHandlerActivity";

    private static final Map<String, Class<?>> TILE_ACTIVITY_MAP = new HashMap<>();

    static {
        TILE_ACTIVITY_MAP.put(DolbyVisionTileService.class.getName(), DolbyVisionActivity.class);
        TILE_ACTIVITY_MAP.put(TurboChargingTile.class.getName(), TurboChargingActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            String tileServiceClassName = intent.getStringExtra("android.service.quicksettings.ACTUAL_TILE_SERVICE_KEY");
            if (tileServiceClassName != null) {
                Class<?> activityClass = TILE_ACTIVITY_MAP.get(tileServiceClassName);
                if (activityClass != null) {
                    startActivity(new Intent(this, activityClass));
                } else {
                    Log.w(TAG, "Unknown tile service: " + tileServiceClassName);
                }
            }
        }
        finish();
    }
}