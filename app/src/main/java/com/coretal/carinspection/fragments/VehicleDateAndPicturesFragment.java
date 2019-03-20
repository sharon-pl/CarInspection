package com.coretal.carinspection.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.JsonHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class VehicleDateAndPicturesFragment extends Fragment {

    private DateAndPictureFragment dateAndPictureFragment;

    public VehicleDateAndPicturesFragment() {
        // Required empty public constructor
    }

    public static VehicleDateAndPicturesFragment newInstance() {
        return new VehicleDateAndPicturesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        setValuesFromFile();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("Kangtle", "on hidden VehicleDateAndPictures fragment");
        if (!Contents.IS_STARTED_INSPECTION) return;
        if(!hidden){
            setValuesFromFile();
        }else{
            saveValuesToFile();
        }
    }

    private void saveValuesToFile() {
        if(dateAndPictureFragment == null) return;
        JSONArray jsonArray = dateAndPictureFragment.getOutput();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Contents.JsonDateAndPictures.DATES_AND_PICTURES, jsonArray);
            JsonHelper.saveJsonObject(jsonObject, Contents.JsonDateAndPictures.FILE_PATH);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setValuesFromFile(){
        if(!Contents.IS_STARTED_INSPECTION) return;

        JSONObject jsonObject = JsonHelper.readJsonFromFile(Contents.JsonDateAndPictures.FILE_PATH);
        String jsonArrayStr = "[]";
        if(jsonObject != null) {
            JSONArray jsonArray = jsonObject.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES);
            jsonArrayStr = jsonArray.toString();
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        dateAndPictureFragment = DateAndPictureFragment.newInstance(Contents.JsonFileTypesEnum.CATEGORIE_VEHICLE, jsonArrayStr);
        fragmentTransaction.replace(R.id.fragment_container, dateAndPictureFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveValuesToFile();
    }

}

