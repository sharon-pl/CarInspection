package com.coretal.carinspection.preferences;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import com.jaredrummler.android.colorpicker.ColorPreference;

public abstract class PreferenceFragmentCompat extends androidx.preference.PreferenceFragmentCompat {

    private static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    @Override public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof NumberPickerPreference) {
            // Inherit the same behaviour as parent
            if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                return;
            }
            final DialogFragment fragment = NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else if (preference instanceof TimePickerPreference) {
            // Inherit the same behaviour as parent
            if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                return;
            }
            final DialogFragment fragment = TimePickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        }else if (preference instanceof ColorPreference) {
            final DialogFragment f = ((ColorPreference) preference).createDialog();
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

}
