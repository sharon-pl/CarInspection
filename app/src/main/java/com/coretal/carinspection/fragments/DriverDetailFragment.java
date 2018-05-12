package com.coretal.carinspection.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverDetailFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    Spinner driverSpinner;
    TextView driverLicenceNumberLabel;
    TextView driverAddressLabel;
    EditText driverRemarksEdit;

    DateAndPictureFragment dateAndPictureFragment;
    private String fullName;
    private String licence;
    private String address;
    private String remarks;
    private ArrayList<String> driverIDs;
    private ArrayList<String> driverNames;
    private String driverID;
    private ProgressDialog progressDialog;
    private JSONArray dateAndPictures;

    public DriverDetailFragment() {
        // Required empty public constructor
    }

    public static DriverDetailFragment newInstance() {
        return new DriverDetailFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_detail, container, false);
        driverSpinner = (Spinner) view.findViewById(R.id.driver_spinner);
        driverLicenceNumberLabel = (TextView) view.findViewById(R.id.licence_number);
        driverAddressLabel = (TextView) view.findViewById(R.id.address);
        driverRemarksEdit = (EditText) view.findViewById(R.id.remarks);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Getting driver data");

        setValuesFromFile();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("Kangtle", "on hidden driver detail fragment");
        if (!Contents.IS_STARTED_INSPECTION) return;
        if(!hidden){
            setValuesFromFile();
        }else{
            saveValuesToFile();
        }
    }

    private void saveValuesToFile() {
        if(dateAndPictureFragment == null) return;
        JSONObject driverJsonObject = new JSONObject();
        try {
            driverID = driverIDs.get(driverSpinner.getSelectedItemPosition());

            String driverName = driverNames.get(driverSpinner.getSelectedItemPosition());
            remarks = driverRemarksEdit.getText().toString();

            driverJsonObject.put(Contents.JsonVehicleDriverData.FULL_NAME, fullName);
            driverJsonObject.put(Contents.JsonVehicleDriverData.DRIVER_ID, driverID);
            driverJsonObject.put(Contents.JsonVehicleDriverData.DRIVER_LICENSE_NUMBER, licence);
            driverJsonObject.put(Contents.JsonVehicleDriverData.DRIVER_ADDRESS, address);
            driverJsonObject.put(Contents.JsonVehicleDriverData.REMARKS, remarks);
            driverJsonObject.put(Contents.JsonDateAndPictures.DATES_AND_PICTURES, dateAndPictureFragment.getOutput());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonHelper.saveJsonObject(driverJsonObject, Contents.JsonVehicleDriverData.FILE_PATH);
    }

    public void setValuesFromFile(){
        if(!Contents.IS_STARTED_INSPECTION) return;

        Map<String, String> drivers = Contents.JsonDrivers.getDrivers();
        driverIDs = new ArrayList<>();
        driverIDs.add("");
        Collections.addAll(driverIDs, drivers.keySet().toArray(new String[drivers.size()]));
        driverNames = new ArrayList<>(drivers.values());
        driverNames.add(0, "");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, driverNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverSpinner.setAdapter(adapter);

        JSONObject driverDataJson = JsonHelper.readJsonFromFile(Contents.JsonVehicleDriverData.FILE_PATH);
        if (driverDataJson == null){
            fullName = "";
            driverID = "";
            licence = "";
            address = "";
            remarks = "";
            dateAndPictures = null;
        }else{
            try {
                fullName = driverDataJson.getString(Contents.JsonVehicleDriverData.FULL_NAME);
                driverID = driverDataJson.getString(Contents.JsonVehicleDriverData.DRIVER_ID);
                licence = driverDataJson.getString(Contents.JsonVehicleDriverData.DRIVER_LICENSE_NUMBER);
                address = driverDataJson.getString(Contents.JsonVehicleDriverData.DRIVER_ADDRESS);
                remarks = driverDataJson.optString(Contents.JsonVehicleDriverData.REMARKS);
                dateAndPictures = driverDataJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        driverSpinner.setOnItemSelectedListener(null);
        driverSpinner.setSelection(driverIDs.indexOf(driverID));
        driverSpinner.post(new Runnable() {
            @Override
            public void run() {
                driverSpinner.setOnItemSelectedListener(DriverDetailFragment.this);
            }
        });

        driverLicenceNumberLabel.setText(licence);
        driverAddressLabel.setText(address);
        driverRemarksEdit.setText(remarks);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (dateAndPictures == null){
            if (dateAndPictureFragment != null) fragmentTransaction.remove(dateAndPictureFragment);
        }else{
            dateAndPictureFragment = DateAndPictureFragment.newInstance(Contents.JsonFileTypesEnum.CATEGORIE_DRIVER, dateAndPictures.toString());
            fragmentTransaction.replace(R.id.driver_fragment_container, dateAndPictureFragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            String driverId = driverIDs.get(position);

            progressDialog.show();
            Log.d("Kangtle", "getting driver data.");
            JsonObjectRequest getDriverDataRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_DRIVER, Contents.PHONE_NUMBER, driverId),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.hide();
                            if (!response.has("error")) {
                                Log.d("Kangtle", "got driver data succcessfully.");
                                JsonHelper.saveJsonObject(response, Contents.JsonVehicleDriverData.FILE_PATH);
                                setValuesFromFile();
                            } else {
                                Log.d("Kangtle", "error while getting driver data.");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Kangtle", "error while getting driver data.");
                            progressDialog.hide();
                        }
                    }
            );
            VolleyHelper volleyHelper = new VolleyHelper(getContext());
            volleyHelper.add(getDriverDataRequest);
        }else{
            FileHelper.writeStringToFile("", Contents.JsonVehicleDriverData.FILE_PATH);
            setValuesFromFile();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
