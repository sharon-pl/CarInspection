package com.coretal.carinspection.fragments;


import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretal.carinspection.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    private Fragment configFragment;
    private Fragment logsFragment;
    private Fragment submissionFragment;
    private Fragment logsAndSubmissionFragment;
    private Fragment selectedFragment;

    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            selectTab(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    private void selectTab(int position) {
        switch (position) {
            case 0:
                configFragment = ConfigFragment.newInstance();
                selectedFragment = configFragment;
                break;
            case 1:
                submissionFragment = SubmissionFragment.newInstance();
                selectedFragment = submissionFragment;
                break;
            case 2:
                logsFragment = LogsFragment.newInstance();
                selectedFragment = logsFragment;
                break;
            default:
                break;
        }

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.setting_frame_layout, selectedFragment);

        transaction.commit();
    }

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        selectTab(0);
        return view;
    }

}
