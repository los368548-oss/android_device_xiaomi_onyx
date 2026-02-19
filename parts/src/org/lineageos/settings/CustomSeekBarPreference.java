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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.lineageos.settings.R;

public class CustomSeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private int mMin = 0;
    private int mMax = 100;
    private int mValue = 0;
    private String mUnits = "";
    private TextView mValueText;
    private SeekBar mSeekBar;
    private boolean mTrackingTouch = false;

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.custom_seekbar_preference);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBarPreference);
        mMin = a.getInt(R.styleable.CustomSeekBarPreference_min, 0);
        mMax = a.getInt(R.styleable.CustomSeekBarPreference_android_max, 100);
        mUnits = a.getString(R.styleable.CustomSeekBarPreference_units);
        if (mUnits == null) mUnits = "";
        a.recycle();
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public void setValue(int value) {
        if (value < mMin) value = mMin;
        if (value > mMax) value = mMax;
        mValue = value;
        persistInt(value);
        if (mValueText != null) {
            mValueText.setText(formatValue(value));
        }
        if (mSeekBar != null && !mTrackingTouch) {
            mSeekBar.setProgress(value - mMin);
        }
    }

    public int getValue() {
        return mValue;
    }

    private String formatValue(int value) {
        if (mUnits.isEmpty()) {
            return String.valueOf(value);
        }
        return value + " " + mUnits;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        
        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mValueText = (TextView) holder.findViewById(R.id.value_text);
        
        if (mSeekBar != null) {
            mSeekBar.setMax(mMax - mMin);
            mSeekBar.setProgress(mValue - mMin);
            mSeekBar.setOnSeekBarChangeListener(this);
        }
        
        if (mValueText != null) {
            mValueText.setText(formatValue(mValue));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int newValue = progress + mMin;
            if (mValueText != null) {
                mValueText.setText(formatValue(newValue));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackingTouch = false;
        int newValue = seekBar.getProgress() + mMin;
        setValue(newValue);
        callChangeListener(newValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, mMin);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mValue = getPersistedInt(mMin);
        } else {
            mValue = (Integer) defaultValue;
            persistInt(mValue);
        }
    }
}