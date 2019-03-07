package com.coretal.carinspection.preferences;

import android.content.Context;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by Kangtle_R on 12/30/2017.
 */

public class MyEditTextPreference extends EditTextPreference implements Preference.OnPreferenceChangeListener {
    public MyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceChangeListener(this);
    }

    @Override
    public CharSequence getSummary() {
        return this.getText();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setSummary(getSummary());
        return true;
    }
}
