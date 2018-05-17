package com.coretal.carinspection.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.DateAndPictureRecyclerViewAdapter;
import com.coretal.carinspection.dialogs.DateAndPictureDialog;
import com.coretal.carinspection.models.DateAndPicture;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class DateAndPictureFragment extends Fragment implements DateAndPictureDialog.Callback, DateAndPictureRecyclerViewAdapter.Callback {

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_JSON_STRING = "json_string";
    private ArrayList<DateAndPicture> dateAndPictures;
    private ArrayList<DateAndPicture> deletedItems;
    private DateAndPictureRecyclerViewAdapter adapter;
    private String category;
    private MyPreference myPref;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DateAndPictureFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DateAndPictureFragment newInstance(String catetory, String jsonString) {
        DateAndPictureFragment fragment = new DateAndPictureFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, catetory);
        args.putString(ARG_JSON_STRING, jsonString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dateAndPictures = new ArrayList<>();
        deletedItems = new ArrayList<>();
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
            String jsonString = getArguments().getString(ARG_JSON_STRING);
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    DateAndPicture item = new DateAndPicture(jsonObject);
                    if(item.status.equals(DateAndPicture.STATUS_DELETED)){
                        deletedItems.add(item);
                    }else{
                        dateAndPictures.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dateandpicture_list, container, false);

        Context context = view.getContext();
        final RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new DateAndPictureRecyclerViewAdapter(getActivity(), dateAndPictures, this, category);
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Delete");
                alertDialog.setMessage("Are you sure to delete it?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                synchronized(adapter){
                                    int index = viewHolder.getAdapterPosition();
                                    DateAndPicture item = dateAndPictures.remove(index);
                                    item.status = DateAndPicture.STATUS_DELETED;
                                    deletedItems.add(item);
                                    adapter.notifyItemRemoved(index);
                                }
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                synchronized(adapter){
                                    int index = viewHolder.getAdapterPosition();
                                    adapter.notifyItemChanged(index);
                                }
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton addFab = (FloatingActionButton)view.findViewById(R.id.fab_add);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = DateAndPictureDialog.newInstance(category, DateAndPictureFragment.this);
                fragment.show(getFragmentManager(), "dialog_date_and_picture");
            }
        });
        myPref = new MyPreference(getContext());
        addFab.setBackgroundTintList(ColorStateList.valueOf(myPref.getColorButton()));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDoneDateAndPictureDialog(DateAndPicture item) {
        Log.d("Kangtle", "on done date and picture dialog");
        if(item.status == DateAndPicture.STATUS_NEW)
            dateAndPictures.add(item);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClickItem(int position) {
        DateAndPictureDialog fragment = DateAndPictureDialog.newInstance(category, DateAndPictureFragment.this);
        fragment.editingItem = dateAndPictures.get(position);
        fragment.show(getFragmentManager(), "dialog_date_and_picture");
    }

    public JSONArray getOutput(){
        JSONArray jsonArray = new JSONArray();
        for (DateAndPicture item : dateAndPictures) {
            try {
                jsonArray.put(item.getJSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (DateAndPicture item : deletedItems) {
            if(item.status == DateAndPicture.STATUS_NEW) continue;
            try {
                jsonArray.put(item.getJSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonArray;
    }
}
