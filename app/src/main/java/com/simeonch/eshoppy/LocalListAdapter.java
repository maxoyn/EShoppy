package com.simeonch.eshoppy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LocalListAdapter extends ArrayAdapter<String> {

    public LocalListAdapter(Context context, ArrayList<String> AllLists) {
        super(context, R.layout.list_entry_layout, AllLists);
    }

    private View listMyListView;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        listMyListView = convertView;
        if (listMyListView == null) {
            LayoutInflater myInflater = LayoutInflater.from(getContext());
            listMyListView = myInflater.inflate(R.layout.every_list_layout, parent, false);
        }

        //final MyList myList = getItem(position);

        final String myList = getItem(position);

        TextView ListNameText = listMyListView.findViewById(R.id.list_name);
        ListNameText.setText(myList);
        Button deleteListButton = listMyListView.findViewById(R.id.delete_list);

        //enter CurrentListActivity for clicked list
        ListNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Open list: " + myList, Toast.LENGTH_SHORT).show();

                Intent myIntent = new Intent(getContext(), CurrentLocalListActivity.class);
                myIntent.putExtra("listName", myList);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getContext().startActivity(myIntent);

            }
        });

        final AlertDialog.Builder ADBuilder = new AlertDialog.Builder(getContext());
        //delete list
        deleteListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DeleteList(myList);

                DialogInterface.OnClickListener DCL = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                LocalDBHelper LH = new LocalDBHelper(getContext());
                                LH.DeleteList(myList);
                                Log.w("CARR", "Clicked YES");

                                Intent myIntent = new Intent(getContext(), MainActivity.class);
                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(myIntent);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Log.w("CARR", "Clicked NO");
                                break;

                        }
                    }
                };

                ADBuilder.setMessage("Do you want to delete " + myList).setPositiveButton("Yes", DCL)
                        .setNegativeButton("No", DCL).show();

            }
        });

        return listMyListView;
    }
}

