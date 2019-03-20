package com.coretal.carinspection.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.FileHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class VehicleInfoFragment extends Fragment {

    private TextView infoLabel;

    public VehicleInfoFragment() {
        // Required empty public constructor
    }

    public static VehicleInfoFragment newInstance() {
        return new VehicleInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vehicle_info, container, false);
        infoLabel = view.findViewById(R.id.info);
        infoLabel.setMovementMethod(new ScrollingMovementMethod());
        setValuesFromFile();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("Kangtle", "on hidden vehicle info fragment");
        if (!Contents.IS_STARTED_INSPECTION) return;
        if(!hidden){
            setValuesFromFile();
        }
    }

    private void setValuesFromFile(){
        if(!Contents.IS_STARTED_INSPECTION) return;
        String vehicleAdditionalDetails= FileHelper.readStringFromFile(Contents.VehicleAdditionalDetails.FILE_PATH);
        infoLabel.setText(vehicleAdditionalDetails);
    }
}
