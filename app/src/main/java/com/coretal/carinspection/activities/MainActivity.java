package com.coretal.carinspection.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.dialogs.API_PhoneNumberDialog;
import com.coretal.carinspection.fragments.VehicleDateAndPicturesFragment;
import com.coretal.carinspection.fragments.HomeFragment;
import com.coretal.carinspection.fragments.InspectionFragment;
import com.coretal.carinspection.fragments.NotesFragment;
import com.coretal.carinspection.fragments.SettingFragment;
import com.coretal.carinspection.receivers.AlarmReceiver;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements API_PhoneNumberDialog.Callback {

    String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private BottomNavigationViewEx navigation;
    private ImageView connectivityImageView;

    private MyPreference myPreference;

    private Fragment homeFragment;
    private Fragment inspectionFragment;
    private Fragment vehicleDateAndPicturesFragment;
    private Fragment notesFragment;
    private Fragment settingFragment;

    private Fragment selectedFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectFragment(item);
            return true;
        }
    };
    private ProgressDialog progressDialog;
    private ConnectivityReceiver connectivityReceiver;

    private void selectFragment(MenuItem item) {
        gotoFragment(item.getItemId());
    }

    private void gotoFragment(int menuId){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(selectedFragment != null) {
            transaction.hide(selectedFragment);
        }

        Fragment fragment = getFragmentByMenuId(menuId);
        if(fragment == null){
            fragment = setFragmentWithMenuId(menuId);
            transaction.add(R.id.frame_layout, fragment);
        }else{
            transaction.show(fragment);
        }

        selectedFragment = fragment;

        transaction.commit();
    }

    private Fragment getFragmentByMenuId(int menuId) {
        switch (menuId) {
            case R.id.navigation_home:
                return homeFragment;
            case R.id.navigation_inspection:
                return inspectionFragment;
            case R.id.navigation_camera:
                return vehicleDateAndPicturesFragment;
            case R.id.navigation_notes:
                return notesFragment;
            case R.id.navigation_setting:
                return settingFragment;
            default:
                return null;
        }
    }

    public Fragment setFragmentWithMenuId(int menuId) {
        switch (menuId) {
            case R.id.navigation_home:
                homeFragment = HomeFragment.newInstance();
                return homeFragment;
            case R.id.navigation_inspection:
                inspectionFragment = InspectionFragment.newInstance();
                return inspectionFragment;
            case R.id.navigation_camera:
                vehicleDateAndPicturesFragment = VehicleDateAndPicturesFragment.newInstance();
                return vehicleDateAndPicturesFragment;
            case R.id.navigation_notes:
                notesFragment = NotesFragment.newInstance();
                return notesFragment;
            case R.id.navigation_setting:
                settingFragment = SettingFragment.newInstance();
                return settingFragment;
            default:
                return null;
        }
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            if (hasPermissions(PERMISSIONS)){
                setupAfterPermissions();
            }else{
                int PERMISSION_ALL = 1;
                if(!hasPermissions(PERMISSIONS)){
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPreference = new MyPreference(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(myPreference.getColorButton());
        }

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.enableShiftingMode(false);
        navigation.setItemIconTintList(ColorStateList.valueOf(myPreference.getColorButton()));
        navigation.setItemTextColor(ColorStateList.valueOf(myPreference.getColorButton()));

        Menu menu = navigation.getMenu();
        selectFragment(menu.getItem(0));

        connectivityImageView = findViewById(R.id.connectivityImageView);
//        registerConnectivityAction();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        new Handler().postDelayed(checkRunnable, 200);

        startAlarmReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Kangtle", "MainActivity onResume");
        registerConnectivityAction();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Kangtle", "MainActivity onPause");
        unregisterReceiver(connectivityReceiver);
    }

    public void refresh(){
        finish();
        startActivity(getIntent());
    }

    private void setupAfterPermissions(){
        String apiRoot = myPreference.getAPIBaseURL();
        String phoneNumber = myPreference.getPhoneNumber();
        if (phoneNumber.isEmpty() || apiRoot.isEmpty()){
            getAPI_PhoneNumberWithDialog();
        }else{
            Contents.configAPIs(this);
            Contents.PHONE_NUMBER = phoneNumber;
            if(!myPreference.isGettedConfig()){
                getConfigFile();
            }
        }
    }

    private void startAlarmReceiver(){
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 15000, pendingIntent);
    }

    private boolean hasPermissions(String... permissions){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (hasPermissions(PERMISSIONS)){
            setupAfterPermissions();
        }else{
            AlertHelper.message(this, "Permissionss", "Need all permission", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
    }

    private void registerConnectivityAction(){
        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);
    }

    public void getAPI_PhoneNumberWithDialog(){

        DialogFragment fragment = API_PhoneNumberDialog.newInstance(this);
        fragment.show(getSupportFragmentManager(), "dialog_api_phone_number");

    }

    @Override
    public void onSubmitPhoneNumberDialog(String apiRoot, String phoneNumber) {
        setupAfterPermissions();
    }

    private void getConfigFile() {
        Log.d("Kangtle", "Getting config file...");
        progressDialog.setMessage("Getting config file...");
        progressDialog.show();
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET,
                String.format(Contents.API_GET_CONFIG, Contents.PHONE_NUMBER),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.has("error")){
                            String error = response.optString("error");
                            Log.d("Kangtle", error);
                            progressDialog.dismiss();
                            AlertHelper.message(MainActivity.this, "Error", error, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getAPI_PhoneNumberWithDialog();
                                }
                            });
                        }else{
                            Log.d("Kangtle", "Getted config file successfully");
                            progressDialog.hide();
                            myPreference.restoreFromJSONObject(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Kangtle", "Can't get config file.");
                        progressDialog.dismiss();
                        AlertHelper.message(MainActivity.this, "Error", "Can't get config file.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getAPI_PhoneNumberWithDialog();
                            }
                        });
                    }
                }
        );

        VolleyHelper volleyHelper = new VolleyHelper(this);
        volleyHelper.add(getRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressDialog.dismiss();
    }

    private class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = MyHelper.isConnectedInternet(MainActivity.this);
            Log.d("Kangtle", "network is connected " + isConnected);
            if(isConnected){
                connectivityImageView.setImageResource(R.drawable.ic_green_circle);
            }else{
                connectivityImageView.setImageResource(R.drawable.ic_red_circle);
            }
        }
    }
}
