package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.MyHelper;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class PhotoViewDialog extends DialogFragment {

    private String photoUrl;

    public static PhotoViewDialog newInstance(String pictureURL){
        PhotoViewDialog dialog = new PhotoViewDialog();
        dialog.photoUrl = pictureURL;
        return dialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_photo_view, container, true);

        Window dialogWindow = getDialog().getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button btnTakePhoto = (Button) dialogView.findViewById(R.id.btn_done);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoViewDialog.this.dismiss();
            }
        });

        PhotoView photoView = dialogView.findViewById(R.id.photo_view);
        Glide.with(getActivity())
                .load(photoUrl)
                .into(photoView);

        return dialogView;
    }
}
