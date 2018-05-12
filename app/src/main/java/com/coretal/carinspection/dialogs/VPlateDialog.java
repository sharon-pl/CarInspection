package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.coretal.carinspection.R;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.MyHelper;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class VPlateDialog extends DialogFragment {
    public interface Callback {
        public void onSubmitVPlateDialog(String vPlate);
    }

    private VPlateDialog.Callback callback;
    private DBHelper dbHelper;

    public static VPlateDialog newInstance(VPlateDialog.Callback callback){
        VPlateDialog dialog = new VPlateDialog();
        dialog.callback = callback;
//        dialog.setCancelable(false);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dbHelper = new DBHelper(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_v_plate, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button btnSubmit = (Button) dialogView.findViewById(R.id.btn_submit);
        final EditText vPlateEdit = (EditText) dialogView.findViewById(R.id.edit_v_plate);

        Submission draft = dbHelper.getDraftSubmission();
        if(draft != null) {
            String draftVPlate = draft.vehiclePlate;
            vPlateEdit.setText(draftVPlate);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vPlate = vPlateEdit.getText().toString();
                if (vPlate.isEmpty()) return;
                if (dbHelper.checkUnsubmittedSubmission(vPlate)){
                    AlertHelper.message(getContext(), "Warning", "There is a submission for the vehicle number to do submit\nPlease enter another one.");
                    return;
                }
                MyHelper.hideKeyBoard(getActivity(), vPlateEdit);
                alertDialog.dismiss();
                callback.onSubmitVPlateDialog(vPlateEdit.getText().toString());
            }
        });

        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

}
