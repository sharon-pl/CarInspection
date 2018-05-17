package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.coretal.carinspection.R;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class PhoneNumberDialog extends DialogFragment {
    public interface Callback {
        public void onSubmitPhoneNumberDialog(String phoneNumber);
    }

    private EditText phoneNumberEdit;
    private Button btnSubmit;
    private MyPreference myPref;
    private Callback callback;

    public static PhoneNumberDialog newInstance(Callback callback){
        PhoneNumberDialog dialog = new PhoneNumberDialog();
        dialog.callback = callback;
        dialog.setCancelable(false);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        myPref = new MyPreference(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_phone_number, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnSubmit = dialogView.findViewById(R.id.btn_submit);
        phoneNumberEdit = dialogView.findViewById(R.id.edit_number);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEdit.getText().toString();
                if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
                    myPref.setPhoneNumber(phoneNumber);
                    dismiss();
                    callback.onSubmitPhoneNumberDialog(phoneNumber);
                }else{
                    Toast.makeText(getContext(), "Please enter phone number correctly.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LayerDrawable layerDrawable = (LayerDrawable) dialogView.getBackground();
        Drawable topDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_top);
        Drawable containerDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_container);
        DrawableHelper.setColor(topDrawable, myPref.getColorButton());
        DrawableHelper.setColor(containerDrawable, myPref.getColorBackground());
        DrawableHelper.setColor(btnSubmit.getBackground(), myPref.getColorButton());

        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
