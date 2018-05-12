package com.coretal.carinspection.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.SubmissionTableViewAdapter;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DateHelper;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.evrencoskun.tableview.TableView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmissionFragment extends Fragment {


    private SubmissionTableViewAdapter mTableViewAdapter;
    private DBHelper dbHelper;
    private MyPreference myPref;
    private Spinner actionSpinner;

    public SubmissionFragment() {
        // Required empty public constructor
    }

    public static SubmissionFragment newInstance() {
        return new SubmissionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_submission, container, false);
        TableView tableView = view.findViewById(R.id.submission_list);
        mTableViewAdapter = new SubmissionTableViewAdapter(getContext());
        tableView.setAdapter(mTableViewAdapter);

        actionSpinner = view.findViewById(R.id.action_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, new String[]{"Reset"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(adapter);

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

        Button submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertHelper.question(getActivity(), "Confirm", "Are you sure?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (actionSpinner.getSelectedItemPosition()){
                            case 0: //Reset
                                Log.d("Kangtle", "Reset num try");
                                dbHelper.resetNumtry();
                                mTableViewAdapter.notifyDataSetChanged();
                                break;
                            default:
                                Log.d("Kangtle", "no action");
                                break;
                        }
                    }
                }, null);
            }
        });

        return view;
    }

    private List<List<SubmissionTableViewAdapter.Cell>> getCellListForAllSubmissions(List<Submission> submissions) {
        List<List<SubmissionTableViewAdapter.Cell>> list = new ArrayList<>();

        for (Submission submission : submissions) {
            String[] monthList = getResources().getStringArray(R.array.months);
            String selectedMonth = "No selected";
            if (submission.month >= 0) selectedMonth = monthList[submission.month];

            List<SubmissionTableViewAdapter.Cell> cellList = new ArrayList<>();

            cellList.add(new SubmissionTableViewAdapter.Cell(submission.id));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.vehiclePlate));
            cellList.add(new SubmissionTableViewAdapter.Cell(selectedMonth));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.dateToString(submission.date, Contents.DEFAULT_DATE_FORMAT)));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.type));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.status));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.errorDetail));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.numTry));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.notes));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.datetimeToString(submission.startedAt)));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.datetimeToString(submission.endedAt)));

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
        list.add(new SubmissionTableViewAdapter.ColumnHeader("Error Detail"));
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
