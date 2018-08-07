package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyPreference;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class APIRootDialog extends DialogFragment {
    public interface Callback {
        public void onSubmitAPIRootDialog(String apiRoot);
    }

    private EditText rootURLEdit;
    private Button btnSubmit;
    private MyPreference myPref;
    private Callback callback;

    public static APIRootDialog newInstance(Callback callback){
        APIRootDialog dialog = new APIRootDialog();
        dialog.callback = callback;
        dialog.setCancelable(false);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        myPref = new MyPreference(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_api_root, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnSubmit = dialogView.findViewById(R.id.btn_submit);
        rootURLEdit = dialogView.findViewById(R.id.edit_root_url);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rootURL = rootURLEdit.getText().toString();
                if (URLUtil.isValidUrl(rootURL)){
                    myPref.setAPIRoot(rootURL);
                    dismiss();
                    callback.onSubmitAPIRootDialog(rootURL);
                }else{
                    Toast.makeText(getContext(), "Please enter valid url.", Toast.LENGTH_SHORT).show();
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
