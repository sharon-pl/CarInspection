package com.coretal.carinspection.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.StringRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.activities.MainActivity;
import com.coretal.carinspection.controls.DateEditText;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.dialogs.SignatureDialog;
import com.coretal.carinspection.dialogs.VPlateDialog;
import com.coretal.carinspection.models.DateAndPicture;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DateHelper;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class VehicleDetailFragment extends Fragment implements VPlateDialog.Callback, VolleyHelper.Callback, SignatureDialog.Callback {


    private Spinner monthSpinner;
    private Spinner inspectorSpinner;
    private TextView vPlateLabel;
    private Button userInputBtn;
    private TextView vehicleMakeLabel;
    private TextView vehicleTypeLabel;
    private TextView vehicleSubTypeLabel;
    private TextView vehicleDetailsLabel;
    private EditText odometerEdit;
    private EditText locationEdit;
    private DateEditText inspectionDateEdit;
    private DateEditText inspectionValidUntilDateEdit;
    private Button submitButton;

    private DBHelper dbHelper;

    private MyPreference myPreference;

    private ProgressDialog progressDialog;

    private VolleyHelper volleyHelper;
    private ArrayList<String> inspectorIDs;
    private ArrayList<String> inspectorNames;

    private boolean successAllRequests;
    private String driverID;
    private String driverName;
    private String lastError;
    private String vPlate;

    public VehicleDetailFragment() {
        // Required empty public constructor
    }

    public static VehicleDetailFragment newInstance() {
        return new VehicleDetailFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_vehicle_detail, container, false);
        inspectorIDs = new ArrayList<>();
        inspectorNames = new ArrayList<>();

        DateHelper.testDateFormat();

        dbHelper = new DBHelper(getContext());
        volleyHelper = new VolleyHelper(getContext(), this);
        myPreference = new MyPreference(getContext());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        monthSpinner = view.findViewById(R.id.month_spinner);
        inspectorSpinner = view.findViewById(R.id.inspector_spinner);
        userInputBtn = view.findViewById(R.id.btn_user_input);
        vPlateLabel = view.findViewById(R.id.label_v_plate);
        vehicleMakeLabel = view.findViewById(R.id.vehicle_make);
        vehicleTypeLabel = view.findViewById(R.id.vehicle_type);
        vehicleSubTypeLabel = view.findViewById(R.id.vehicle_subtype);
        vehicleDetailsLabel = view.findViewById(R.id.vehicle_details);
        odometerEdit = view.findViewById(R.id.current_odometer);
        locationEdit = view.findViewById(R.id.inspection_location_editText);
        inspectionDateEdit = view.findViewById(R.id.edit_inspect_date);
        inspectionValidUntilDateEdit = view.findViewById(R.id.edit_inspect_valid_until_date);
        submitButton = view.findViewById(R.id.btn_submit);

        inspectorSpinner.setEnabled(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, myPreference.get_conf_months());;
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
        monthSpinner.post(new Runnable() {
            @Override
            public void run() {
                monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d("Kangtle", "monthSpinner.setOnItemSelectedListener: " + position);
                        dbHelper.setMonthForDraftSubmission(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

        Date currentDate = Calendar.getInstance().getTime();
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        monthSpinner.setSelection(thisMonth);
        inspectionDateEdit.setDate(currentDate);

        Calendar validUntilCalendar = Calendar.getInstance();
        validUntilCalendar.add(Calendar.MONTH, 1);
        Date validUntilDate = validUntilCalendar.getTime();
        inspectionValidUntilDateEdit.setDate(validUntilDate);

        userInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Contents.IS_STARTED_INSPECTION){
                    AlertHelper.question(getContext(), "Start Over", "Would you like to start over?",
                            "OK", "Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Contents.IS_STARTED_INSPECTION = false;
                                    removeDraftSubmission();
                                    MainActivity activity = (MainActivity)getActivity();
                                    activity.refresh();
                                    startInspection();
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }
                    );
                } else {
                    startInspection();
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Contents.IS_STARTED_INSPECTION) {
                    if (checkFields() && checkDocuments()) {
                        saveValuesToFile();

                        String inspectorName = (String) inspectorSpinner.getSelectedItem();
                        String driverName = "";
                        JSONObject driverDataJson = JsonHelper.readJsonFromFile(Contents.JsonVehicleDriverData.FILE_PATH);
                        if(driverDataJson != null)
                             driverName = driverDataJson.optString(Contents.JsonVehicleDriverData.FULL_NAME);

                        SignatureDialog fragment = SignatureDialog.newInstance(VehicleDetailFragment.this);
                        fragment.setDriverName(driverName);
                        fragment.setInspectorName(inspectorName);
                        fragment.show(getFragmentManager(), "dialog_signature");
                    }
                }else{
                    AlertHelper.message(getContext(), "Warning", "Inspection not started yet");
                }
            }
        });

        DrawableHelper.setColor(userInputBtn.getBackground(), myPreference.getColorButton());
        DrawableHelper.setColor(submitButton.getBackground(), myPreference.getColorButton());
//        DrawableHelper.setColor(view.getBackground(), myPreference.getColorBackground());

        startInspection();

        return view;
    }

    public void startInspection(){

        DialogFragment fragment = VPlateDialog.newInstance(VehicleDetailFragment.this);
        fragment.show(getFragmentManager(), "dialog_v_plate");

    }

    @Override
    public void onSubmitVPlateDialog(String vPlate) {
        Log.d("Kangtle", "Start vehidle number: " + vPlate);

        this.vPlate = vPlate;
        Contents.CURRENT_VEHICLE_NUMBER = vPlate;
        Contents.setVehicleNumber(vPlate);

        Submission submission = dbHelper.getDraftSubmission();
        vPlateLabel.setText(vPlate);
        if(submission != null && vPlate.equals(submission.vehiclePlate)){
            Contents.IS_STARTED_INSPECTION = true;
            setValuesFromJsonFiles();
        }else{
            removeDraftSubmission();

            if(!MyHelper.isConnectedInternet(getActivity())){
                newSubmission(vPlate);
                JSONObject inspectionDataJson = JsonHelper.readJsonFromAsset(Contents.JsonInspectionData.ASSET_FILE_NAME);
                JsonHelper.saveJsonObject(inspectionDataJson, Contents.JsonInspectionData.FILE_PATH);
                Contents.IS_STARTED_INSPECTION = true;
                setValuesFromJsonFiles();
                return;
            }

            progressDialog.setMessage("Please wait...\nGetting the vehicle details");
            progressDialog.show();

            successAllRequests = true;

            JsonObjectRequest getVehicleDataRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_VEHICLE_DATA, Contents.PHONE_NUMBER, vPlate),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonVehicleData.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            JsonObjectRequest getInspectorsRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_INSPECTORS, Contents.PHONE_NUMBER),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonInspectors.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            JsonObjectRequest getDriversRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_DRIVERS, Contents.PHONE_NUMBER, vPlate),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonDrivers.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            JsonObjectRequest getVehicleDriverDataRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_VEHICLE_DRIVER_DATA, Contents.PHONE_NUMBER, vPlate),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonVehicleDriverData.FILE_PATH);
                            }else{
//                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            JsonObjectRequest getTrailerRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_TRAILERS, Contents.PHONE_NUMBER, vPlate),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonTrailers.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            JsonObjectRequest getVehicleTrailerDataRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_VEHICLE_TRAILER_DATA, Contents.PHONE_NUMBER, vPlate),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonVehicleTrailerData.FILE_PATH);
                            }else{
//                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            StringRequest getVehicleAdditionalDetailsRequest = new StringRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_VEHICLE_ADDITIONAL_DETAILS, Contents.PHONE_NUMBER, vPlate),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (!response.contains("error")){
                                FileHelper.writeStringToFile(response, Contents.VehicleAdditionalDetails.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );

            JsonObjectRequest getInspectionDataRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_INSPECTION_DATA, Contents.PHONE_NUMBER, vPlate),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonInspectionData.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            JsonObjectRequest getDateAndPictureRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_DATE_AND_PICTURES, Contents.PHONE_NUMBER, vPlate),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonDateAndPictures.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            JsonObjectRequest getFileTypeEnumRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_CONFIG_FILE_TYPES_EMUM, Contents.PHONE_NUMBER),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (isSuccessResponse(response)){
                                JsonHelper.saveJsonObject(response, Contents.JsonFileTypesEnum.FILE_PATH);
                            }else{
                                successAllRequests = false;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );

            volleyHelper.add(getVehicleDataRequest);
            volleyHelper.add(getInspectorsRequest);
            volleyHelper.add(getDriversRequest);
            volleyHelper.add(getVehicleDriverDataRequest);
            volleyHelper.add(getTrailerRequest);
            volleyHelper.add(getVehicleTrailerDataRequest);
            volleyHelper.add(getVehicleAdditionalDetailsRequest);
            volleyHelper.add(getInspectionDataRequest);
            volleyHelper.add(getDateAndPictureRequest);
            volleyHelper.add(getFileTypeEnumRequest);
        }
    }

    private boolean isSuccessResponse(JSONObject response){
        if (response.has("error")){
            lastError = response.optString("error");
            return false;
        }else{
            return true;
        }
    };

    private void newSubmission(String vPlate) {
        dbHelper.newSubmission(vPlate);

        File picDir = new File(Contents.EXTERNAL_PICTURES_DIR_PATH);
        if (!picDir.exists()) picDir.mkdirs();

        File jsonDir = new File(Contents.EXTERNAL_JSON_DIR_PATH);
        if (!jsonDir.exists()) jsonDir.mkdirs();

    }

    private void removeDraftSubmission() {
        dbHelper.removeDraftSubmission();
        FileHelper.deleteRecursive(getContext().getExternalFilesDir(Contents.CURRENT_VEHICLE_NUMBER));
    }

    @Override
    public void onFinishedAllRequests() {
        progressDialog.hide();
        if (successAllRequests){
            newSubmission(vPlate);
            Contents.IS_STARTED_INSPECTION = true;
            setValuesFromJsonFiles();
        }else{
            AlertHelper.message(getContext(), "Error", lastError, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startInspection();
                }
            });
        }
    }

    private void setValuesFromJsonFiles(){
        if(!Contents.IS_STARTED_INSPECTION) return;

        Map<String, String> inspectors = Contents.JsonInspectors.getInspectors();
        if(inspectors.size() == 0){
            inspectors.put(myPreference.get_conf_inspector_id(), myPreference.get_conf_inspector_name());
        }
        inspectorIDs = new ArrayList<>();
        Collections.addAll(inspectorIDs, inspectors.keySet().toArray(new String[inspectors.size()]));
        inspectorNames = new ArrayList<>(inspectors.values());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, inspectorNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inspectorSpinner.setAdapter(adapter);

        int selectedInspectorIndex = inspectorIDs.indexOf(myPreference.get_conf_inspector_id());
        inspectorSpinner.setSelection(selectedInspectorIndex);

        JSONObject vehicleDataJson = JsonHelper.readJsonFromFile(Contents.JsonVehicleData.FILE_PATH);
        if (vehicleDataJson == null) return;
        try {
            String vehicleMake = vehicleDataJson.getString(Contents.JsonVehicleData.VEHICLE_MAKE);
            String type = vehicleDataJson.getString(Contents.JsonVehicleData.TYPE);
            String subtype = vehicleDataJson.getString(Contents.JsonVehicleData.SUBTYPE);
            String details = vehicleDataJson.getString(Contents.JsonVehicleData.DETAILS);
            String odometer = vehicleDataJson.getString(Contents.JsonVehicleData.CURRENTODOMETER);
            driverID = vehicleDataJson.optString(Contents.JsonVehicleData.DRIVERID);
            driverName = vehicleDataJson.optString(Contents.JsonVehicleData.DRIVERNAME);

            vehicleMakeLabel.setText(vehicleMake);
            vehicleTypeLabel.setText(type);
            vehicleSubTypeLabel.setText(subtype);
            vehicleDetailsLabel.setText(details);
            odometerEdit.setText(odometer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveValuesToFile() {
        String[] months = getResources().getStringArray(R.array.months);
        String selectedMonth = months[monthSpinner.getSelectedItemPosition()];
        String inspectDate = inspectionDateEdit.getDateString();
        String inspectValidUntilDate = inspectionValidUntilDateEdit.getDateString();
        String odometer = odometerEdit.getText().toString();
        String location = locationEdit.getText().toString();

        String inspectorID = myPreference.get_conf_inspector_id();
        String inspectorName = myPreference.get_conf_inspector_name();

        JSONObject vehicleDataJson = JsonHelper.readJsonFromFile(Contents.JsonVehicleData.FILE_PATH);
        if (vehicleDataJson == null) vehicleDataJson = new JSONObject();
        try {
            vehicleDataJson.put(Contents.JsonVehicleData.VEHICLE_PLATE, this.vPlate);
            vehicleDataJson.put(Contents.JsonVehicleData.INSPECTION_ID, inspectorID);
            vehicleDataJson.put(Contents.JsonVehicleData.INSPECTION_NAME, inspectorName);
            vehicleDataJson.put(Contents.JsonVehicleData.INSPECTION_MONTH, selectedMonth);
            vehicleDataJson.put(Contents.JsonVehicleData.INSPECTION_DATE, inspectDate);
            vehicleDataJson.put(Contents.JsonVehicleData.INSPECTION_VALID_UNTIL_DATE, inspectValidUntilDate);
            vehicleDataJson.put(Contents.JsonVehicleData.INSPECTION_LOCATION, location);
            vehicleDataJson.put(Contents.JsonVehicleData.CURRENTODOMETER, odometer);
            JsonHelper.saveJsonObject(vehicleDataJson, Contents.JsonVehicleData.FILE_PATH);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean checkFields(){
        if (inspectionDateEdit.getText().toString().isEmpty()){
            Toast.makeText(getContext(), getString(R.string.required_date_field), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (locationEdit.getText().toString().isEmpty()){
            Toast.makeText(getContext(), getString(R.string.required_location_field), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (odometerEdit.getText().toString().isEmpty()){
            Toast.makeText(getContext(), getString(R.string.required_odometer_field), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean checkDocuments(){
        JSONObject dateAndPicturesJson = JsonHelper.readJsonFromFile(Contents.JsonDateAndPictures.FILE_PATH);
        JSONObject driverDataJson = JsonHelper.readJsonFromFile(Contents.JsonVehicleDriverData.FILE_PATH);
        JSONObject trailerDataJson = JsonHelper.readJsonFromFile(Contents.JsonVehicleTrailerData.FILE_PATH);
        if (dateAndPicturesJson == null){
            Toast.makeText(getContext(), "Truck documents are missing.", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            JSONArray truckDocumentArray = dateAndPicturesJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES);
            String[] truckMandatoryTypes = myPreference.get_conf_truck_mandatory_documents();

            if (!checkMissingOrExpiredDocuments(truckDocumentArray, truckMandatoryTypes, "VEHICLE", getString(R.string.vehicle))){
                return false;
            }
        }

        if (driverDataJson != null){
            JSONArray driverDocumentArray = driverDataJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES);
            String[] driverMandatoryTypes = myPreference.get_conf_driver_mandatory_documents();
            if (!checkMissingOrExpiredDocuments(driverDocumentArray, driverMandatoryTypes, "DRIVER", getString(R.string.driver))){
                return false;
            }
        }

        if (trailerDataJson != null){
            JSONArray trailerDocumentArray = trailerDataJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES);
            String[] trailerMandatoryTypes = myPreference.get_conf_trailer_mandatory_documents();
            if (!checkMissingOrExpiredDocuments(trailerDocumentArray, trailerMandatoryTypes, "TRAILER", getString(R.string.trailer))){
                return false;
            }
        }

        return true;
    }

    private boolean checkMissingOrExpiredDocuments(JSONArray documentArray, String[] mandatoryTypes, String documentCategory, String translatedCategory){
        Map<String, String> fileTypes = Contents.JsonFileTypesEnum.getTypesByCategory(documentCategory);
        Date curDate = new Date();
        ArrayList<String> existingTypes = new ArrayList<>();
        for (int i=0; i < documentArray.length(); i++) {
            try {
                JSONObject documentObject = documentArray.getJSONObject(i);
                String dateStr = documentObject.optString(Contents.JsonDateAndPictures.DATE);
                Date date = DateHelper.stringToDate(dateStr);
                String type = documentObject.optString(Contents.JsonDateAndPictures.TYPE);
                String status = documentObject.optString(Contents.JsonDateAndPictures.STATUS);
                if (status.equals(DateAndPicture.STATUS_DELETED) || !Arrays.asList(mandatoryTypes).contains(type)) continue;

                existingTypes.add(type);
                if (date.before(curDate)){
                    Toast.makeText(getContext(), String.format("%s %s %s", getString(R.string.expired_document), translatedCategory, fileTypes.get(type)), Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        for (String mandatoryType : mandatoryTypes) {
            if (!existingTypes.contains(mandatoryType)) {
                Toast.makeText(getContext(), String.format("%s %s %s", getString(R.string.missing_document), translatedCategory, fileTypes.get(mandatoryType)), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    @Override
    public void onSubmitSignatures() {
        Contents.IS_STARTED_INSPECTION = false;
        dbHelper.setStatusForDraftSubmission(Submission.STATUS_READY_TO_SUBMIT);
        AlertHelper.message(getContext(),
                "Ready to Submit",
                "The submission is ready to submit \nWill start over again",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity activity = (MainActivity)getActivity();
                        activity.refresh();
                        startInspection();
                    }
                }
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        if(Contents.IS_STARTED_INSPECTION){
            saveValuesToFile();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        progressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
