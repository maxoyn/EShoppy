package com.simeonch.eshoppy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class LocalItemAdapter extends ArrayAdapter<Item> {

    public LocalItemAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.list_entry_layout, items);
    }

    private String listName;

    public void SetLocalListName(String s) {
        this.listName = s;
    }

    private View listItemView;



    //was .document("list") , is now .document(CurrentList)

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        listItemView = convertView;
        if (listItemView == null) {
            LayoutInflater myInflater = LayoutInflater.from(getContext());
            listItemView = myInflater.inflate(
                    R.layout.list_entry_layout, parent, false);
        }

        final Item currentItem = getItem(position);

        //from here to the next comment get the views for every item and update them accordingly
        TextView ET = listItemView.findViewById(R.id.quantity);
        ET.setText(String.valueOf(currentItem.getItemQuantity()));
        //Log.w("IADAPT: ", String.valueOf(position) + " --- " + ET.getText().toString());

        TextView itemName = listItemView.findViewById(R.id.item_entry);
        itemName.setText(currentItem.getItemName());

        final Button decreaseButton = listItemView.findViewById(R.id.decrease);
        Button increaseButton = listItemView.findViewById(R.id.increase);
        //final Button deleteButton = listItemView.findViewById(R.id.delete);

        final TextView currentQ = listItemView.findViewById(R.id.quantity);

        Button infoButton = listItemView.findViewById(R.id.info);

        CheckBox checkB = listItemView.findViewById(R.id.checkBox);
        if(currentItem.getMyStatus() == 1) {
            checkB.setChecked(true);
        }
        else {
            checkB.setChecked(false);
        }

        //the Check Button
        checkB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentItem.changeMyStatus();
                LocalDBHelper LH = new LocalDBHelper(getContext());
                LH.ChangeItemStatus(listName, currentItem);
            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentItem.increaseQuantity();
                String s = Integer.toString(currentItem.getItemQuantity());
                currentQ.setText(s);

                LocalDBHelper LH = new LocalDBHelper(getContext());
                LH.UpdateItemQ(listName, currentItem);

            }
        });


        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentItem.getItemQuantity() > 1) {
                    currentItem.decreaseQuantity();
                    String s = Integer.toString(currentItem.getItemQuantity());
                    currentQ.setText(s);

                    LocalDBHelper LH = new LocalDBHelper(getContext());
                    LH.UpdateItemQ(listName, currentItem);
                }
            }
        });

        //enter SelectedItemActivity (more detailed view of the item)
        //send all item specific info in the intent so that activity does less work
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getContext(), SelectedLocalItemActivity.class);
                myIntent.putExtra("obj", currentItem);
                myIntent.putExtra("currentList", listName);

                getContext().startActivity(myIntent);
            }
        });

        return listItemView;
    }


}
