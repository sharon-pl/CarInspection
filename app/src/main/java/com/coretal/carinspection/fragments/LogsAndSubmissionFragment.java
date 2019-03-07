package com.coretal.carinspection.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
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
public class LogsAndSubmissionFragment extends Fragment {


    private SubmissionTableViewAdapter mTableViewAdapter;
    private DBHelper dbHelper;
    private TextView logTextView;
    private MyPreference myPref;

    public LogsAndSubmissionFragment() {
        // Required empty public constructor
    }

    public static LogsAndSubmissionFragment newInstance() {
        return new LogsAndSubmissionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logs_and_submission, container, false);
        TableView tableView = view.findViewById(R.id.submission_list);
        mTableViewAdapter = new SubmissionTableViewAdapter(getContext());
        tableView.setAdapter(mTableViewAdapter);

        dbHelper = new DBHelper(getContext());
        myPref = new MyPreference(getContext());
        List<Submission> submissions = dbHelper.getAllSubmissions();

        List<List<SubmissionTableViewAdapter.Cell>> cellList = getCellListForAllSubmissions(submissions); // getCellList();
        List<SubmissionTableViewAdapter.RowHeader> rowHeaders = createRowHeaderList(submissions.size());
        List<SubmissionTableViewAdapter.ColumnHeader> columnHeaders = createColumnHeaderModelList();
//        mTableViewAdapter.setAllItems(columnHeaders, rowHeaders, cellList);
        mTableViewAdapter.setRowHeaderItems(rowHeaders);
        mTableViewAdapter.setColumnHeaderItems(columnHeaders);
        mTableViewAdapter.setCellItems(cellList);

        logTextView = view.findViewById(R.id.log);
        logTextView.setMovementMethod(new ScrollingMovementMethod());

        logTextView.setText(MyHelper.getLogs());

        Button emailBtn = view.findViewById(R.id.btn_email);
        Button eraseBtn = view.findViewById(R.id.btn_erase);

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundMail.newBuilder(getContext())
                        .withUsername(myPref.get_conf_email_user())
                        .withPassword(myPref.get_conf_email_password())
                        .withMailto(myPref.get_conf_email_target_email())
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
                Log.d("Kangtle", "erased logs");
            }
        });

        return view;
    }

    private List<List<SubmissionTableViewAdapter.Cell>> getCellListForAllSubmissions(List<Submission> submissions) {
        List<List<SubmissionTableViewAdapter.Cell>> list = new ArrayList<>();

        for (Submission submission : submissions) {
            List<SubmissionTableViewAdapter.Cell> cellList = new ArrayList<>();

            cellList.add(new SubmissionTableViewAdapter.Cell(submission.id));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.vehiclePlate));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.month));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.dateToString(submission.date, Contents.DEFAULT_DATE_FORMAT)));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.type));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.status));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.numTry));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.notes));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.dateToString(submission.startedAt, Contents.DEFAULT_DATE_FORMAT)));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.dateToString(submission.endedAt, Contents.DEFAULT_DATE_FORMAT)));

            list.add(cellList);
        }

        return list;
    }

    private List<SubmissionTableViewAdapter.ColumnHeader> createColumnHeaderModelList() {
        List<SubmissionTableViewAdapter.ColumnHeader> list = new ArrayList<>();

        // Create Column Headers
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Id"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Vehicle"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Month"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Date"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Type"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Status"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Num Try"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Notes"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Started At"));
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Ended At"));

        return list;
    }


    private List<SubmissionTableViewAdapter.RowHeader> createRowHeaderList(int rowCount) {
        List<SubmissionTableViewAdapter.RowHeader> list = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            // In this example, Row headers just shows the index of the TableView List.
            list.add(new SubmissionTableViewAdapter.RowHeader(String.valueOf(i + 1)));
        }
        return list;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
