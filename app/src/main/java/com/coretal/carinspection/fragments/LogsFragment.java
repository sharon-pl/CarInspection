package com.coretal.carinspection.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.SubmissionTableViewAdapter;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DateHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.evrencoskun.tableview.TableView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LogsFragment extends Fragment {

    private TextView logTextView;
    private MyPreference myPref;

    public LogsFragment() {
        // Required empty public constructor
    }

    public static LogsFragment newInstance() {
        return new LogsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        myPref = new MyPreference(getContext());

        logTextView = view.findViewById(R.id.log);
//        logTextView.setMovementMethod(new ScrollingMovementMethod());

        logTextView.setText(MyHelper.getLogs());

        Button emailBtn = view.findViewById(R.id.btn_email);
        Button eraseBtn = view.findViewById(R.id.btn_erase);

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundMail.newBuilder(getContext())
                        .withUsername(myPref.get_conf_email_user())
                        .withPassword(myPref.get_conf_email_password())
//                        .withMailto(myPref.get_conf_email_target_email())
                        .withMailto("xiaolin.dev@gmail.com")
                        .withType(BackgroundMail.TYPE_PLAIN)
                        .withSubject("Logs")
                        .withBody(MyHelper.getLogs())
                        .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("Kangtle", "successfully send email logs");
                            }
                        })
                        .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                            @Override
                            public void onFail() {
                                Log.d("Kangtle", "failed to send email logs");
                            }
                        })
                        .send();

            }
        });

        eraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyHelper.clearLogs();
                logTextView.setText(MyHelper.getLogs());
            }
        });

        return view;
    }
}
