package com.coretal.carinspection.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.InspectionRecyclerViewAdapter;
import com.coretal.carinspection.dummy.DummyContent;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class InspectionFragment extends Fragment {

    private MyPreference myPreference;
    private InspectionRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchEditText;

    public ArrayList<InspectionRecyclerViewAdapter.SectionHeader> sectionHeaders;
    public ArrayList<InspectionRecyclerViewAdapter.SectionHeader> searchedSectionHeaders;

    public InspectionFragment() {
        // Required empty public constructor
    }

    public static InspectionFragment newInstance() {
        return new InspectionFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_inspection, container, false);
        myPreference = new MyPreference(getContext());

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        searchEditText = view.findViewById(R.id.search);

        sectionHeaders = new ArrayList<>();
        searchedSectionHeaders = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        setValuesFromFile();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Contents.IS_STARTED_INSPECTION) return;
                searchedSectionHeaders.clear();
                if (s == "") {
                    searchedSectionHeaders.addAll(sectionHeaders);
                }else{
                    for (InspectionRecyclerViewAdapter.SectionHeader header: sectionHeaders) {
                        if (header.getTitle().toLowerCase().contains(s)){
                            searchedSectionHeaders.add(header);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("Kangtle", "on hidden changed Inspection fragment " + hidden);
        if (!Contents.IS_STARTED_INSPECTION) return;
        if(!hidden){
            setValuesFromFile();
        }else{
            saveValuesToFile();
        }
    }

    private void saveValuesToFile(){
        JSONObject jsonObject = getOutput();
        JsonHelper.saveJsonObject(jsonObject, Contents.JsonInspectionData.FILE_PATH);
    }

    private void setValuesFromFile() {
        if(!Contents.IS_STARTED_INSPECTION || adapter != null) return;

        makeSectionContents();

        searchedSectionHeaders.clear();
        searchedSectionHeaders.addAll(sectionHeaders);
        adapter = new InspectionRecyclerViewAdapter(searchedSectionHeaders);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(500);
    }

    private void makeSectionContents(){
        sectionHeaders.clear();

        JSONObject inspectionDataJson = JsonHelper.readJsonFromFile(Contents.JsonInspectionData.FILE_PATH);

        if (inspectionDataJson == null) return;
        try {
            JSONArray sectionsArray = inspectionDataJson.getJSONArray(Contents.JsonInspectionData.SECTIONS);
            for (int sectionIndex=0; sectionIndex < sectionsArray.length(); sectionIndex++){
                JSONObject sectionObject = sectionsArray.getJSONObject(sectionIndex);
                String sectionId = sectionObject.getString(Contents.JsonInspectionData.IDENTIFIER);
                String sectionCaption = sectionObject.getString(Contents.JsonInspectionData.CAPTION);
                JSONArray subsectionsArray = sectionObject.getJSONArray(Contents.JsonInspectionData.SUBSECTIONS);

                ArrayList<InspectionRecyclerViewAdapter.SectionContent> sectionContents = new ArrayList<>();
                for(int subsectionIndex=0; subsectionIndex<subsectionsArray.length();subsectionIndex++){
                    JSONObject subsectionObject = subsectionsArray.getJSONObject(subsectionIndex);
                    String subsectionId = subsectionObject.getString(Contents.JsonInspectionData.IDENTIFIER);
                    String subsectionCaption = subsectionObject.getString(Contents.JsonInspectionData.CAPTION);
                    JSONArray questionsArray = subsectionObject.getJSONArray(Contents.JsonInspectionData.QUESTIONS);
                    for(int questionIndex=0; questionIndex<questionsArray.length();questionIndex++){
                        JSONObject questionObject = questionsArray.getJSONObject(questionIndex);
                        String questionId = questionObject.getString(Contents.JsonInspectionData.IDENTIFIER);
                        String questionCaption = questionObject.getString(Contents.JsonInspectionData.CAPTION);
                        String questionNotes = questionObject.optString(Contents.JsonInspectionData.NOTES);
                        String questionStatus = questionObject.optString(Contents.JsonInspectionData.STATUS);
                        boolean isChecked = questionStatus.equals("true");

                        InspectionRecyclerViewAdapter.SectionContent sectionContent =
                                new InspectionRecyclerViewAdapter.SectionContent(
                                        subsectionId,
                                        subsectionCaption,
                                        questionId,
                                        questionCaption,
                                        questionNotes,
                                        isChecked
                                );
                        sectionContents.add(sectionContent);
                    }
                }

                boolean confCheck = myPreference.get_conf_chek_box_submit();
                InspectionRecyclerViewAdapter.SectionHeader sectionHeader =
                        new InspectionRecyclerViewAdapter.SectionHeader(sectionId, sectionCaption, sectionContents, true);
                sectionHeaders.add(sectionHeader);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getOutput(){
        JSONObject allSectionObject = new JSONObject();
        try {
            JSONArray sectionsArray = new JSONArray();
            allSectionObject.put(Contents.JsonInspectionData.SECTIONS, sectionsArray);
            for (InspectionRecyclerViewAdapter.SectionHeader header: sectionHeaders){
                String sectionId = header.sectionId;
                String sectionCaption = header.sectionCaption;
                ArrayList<InspectionRecyclerViewAdapter.SectionContent> sectionContents = header.sectionContents;

                JSONObject sectionObject = new JSONObject();
                sectionObject.put(Contents.JsonInspectionData.IDENTIFIER, sectionId);
                sectionObject.put(Contents.JsonInspectionData.CAPTION, sectionCaption);

                JSONArray subsectionsArray = new JSONArray();
                sectionObject.put(Contents.JsonInspectionData.SUBSECTIONS, subsectionsArray);

                HashMap<String, JSONObject> subsectionsMap = new HashMap<>();
                for (InspectionRecyclerViewAdapter.SectionContent sectionContent: sectionContents){
                    String subsectionId = sectionContent.subsectionId;
                    String subsectionCaption = sectionContent.subsectionCaption;
                    String questionId = sectionContent.questionId;
                    String questionCaption = sectionContent.questionCaption;
                    String questionNotes = sectionContent.questionNotes;
                    boolean isChecked = sectionContent.isChecked;

                    if(!subsectionsMap.containsKey(subsectionId)){
                        JSONObject subsectionObject = new JSONObject();
                        subsectionObject.put(Contents.JsonInspectionData.IDENTIFIER, subsectionId);
                        subsectionObject.put(Contents.JsonInspectionData.CAPTION, subsectionCaption);
                        subsectionObject.put(Contents.JsonInspectionData.QUESTIONS, new JSONArray());
                        subsectionsMap.put(subsectionId, subsectionObject);
                        subsectionsArray.put(subsectionObject);
                    }

                    JSONObject questionObject = new JSONObject();
                    questionObject.put(Contents.JsonInspectionData.IDENTIFIER, questionId);
                    questionObject.put(Contents.JsonInspectionData.CAPTION, questionCaption);
                    questionObject.put(Contents.JsonInspectionData.NOTES, questionNotes);
                    questionObject.put(Contents.JsonInspectionData.STATUS, isChecked);

                    JSONArray questionArray = subsectionsMap.get(subsectionId).getJSONArray(Contents.JsonInspectionData.QUESTIONS);
                    questionArray.put(questionObject);
                }
                sectionsArray.put(sectionObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allSectionObject;
    }
}
