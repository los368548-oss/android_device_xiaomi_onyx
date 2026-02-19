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

package org.lineageos.settings.dolbyvision;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.Constants;
import org.lineageos.settings.R;

public class DolbyVisionTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean currentState = prefs.getBoolean(Constants.KEY_DOLBY_VISION_ENABLED, true);
        boolean newState = !currentState;

        prefs.edit().putBoolean(Constants.KEY_DOLBY_VISION_ENABLED, newState).apply();
        DolbyVisionUtil.applyDolbyVisionSettings(this);
        updateTileState();

        Toast.makeText(this,
                newState ? getString(R.string.toast_dv_on) : getString(R.string.toast_dv_off),
                Toast.LENGTH_SHORT).show();
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = prefs.getBoolean(Constants.KEY_DOLBY_VISION_ENABLED, true);

        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
