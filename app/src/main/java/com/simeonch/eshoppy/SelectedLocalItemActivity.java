package com.simeonch.eshoppy;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;


public class SelectedLocalItemActivity extends AppCompatActivity {

    private String cList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_item);


        //get the views from the selected item layout
        final EditText selectedText = findViewById(R.id.selected_item);
        final EditText selectedQ = findViewById(R.id.selected_quantity);
        final EditText selectedPrice = findViewById(R.id.selected_price);
        Button decreaseSelected = findViewById(R.id.decrease_selected);
        Button increaseSelected = findViewById(R.id.increase_selected);
        Button deleteSelected = findViewById(R.id.delete_selected);
        Button updateSelected = findViewById(R.id.update_button);

        //if we are here, there is an item selected
        //so we get all info for that item from the intent
        //and populate the views
        Intent in = getIntent();

        cList = in.getStringExtra("currentList");


        final Item NEWY = (Item) in.getSerializableExtra("obj");
        selectedText.setText(NEWY.getItemName());
        selectedQ.setText(String.valueOf(NEWY.getItemQuantity()));
        selectedPrice.setText(String.valueOf(NEWY.getItemPrice()));

        //Log.w("CARR", "SELECTED ITEM INFO: " + NEWY.getItemName() + " " + NEWY.getItemQuantity() + " " + NEWY.getMyStatus() + " " + NEWY.getItemBrand()  + " " + NEWY.getItemPrice());

        //decrease button
        decreaseSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(NEWY.getItemQuantity() > 1) {
                    NEWY.decreaseQuantity();
                    selectedQ.setText(String.valueOf(NEWY.getItemQuantity()));
                }

                LocalDBHelper LH = new LocalDBHelper(SelectedLocalItemActivity.this);
                LH.UpdateItemQ(cList, NEWY);
            }
        });

        //increase button
        increaseSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NEWY.increaseQuantity();
                selectedQ.setText(String.valueOf(NEWY.getItemQuantity()));

                LocalDBHelper LH = new LocalDBHelper(SelectedLocalItemActivity.this);
                LH.UpdateItemQ(cList, NEWY);
            }
        });

        //the update button, send an intent to the CurrentListActivity, which OnResume() updates the selected item
        updateSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SelectedLocalItemActivity.this, CurrentLocalListActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //if we're going to change the item name
                String oldName = null;
                if(!NEWY.getItemName().equals(selectedText.getText().toString())) {
                    myIntent.putExtra("oldName", NEWY.getItemName());
                    oldName = NEWY.getItemName();
                    Log.w("CARR", oldName);
                }

                NEWY.setItemName(selectedText.getText().toString());
                NEWY.setQuantity(Integer.parseInt(selectedQ.getText().toString()));
                NEWY.setItemPrice(Double.parseDouble(selectedPrice.getText().toString()));
                myIntent.putExtra("listName", cList);

                LocalDBHelper LH = new LocalDBHelper(SelectedLocalItemActivity.this);
                LH.UpdateItem(cList, NEWY, oldName);
                SelectedLocalItemActivity.this.startActivity(myIntent);
            }
        });

        //same but delete
        deleteSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SelectedLocalItemActivity.this, CurrentLocalListActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                myIntent.putExtra("listName", cList);

                LocalDBHelper LH = new LocalDBHelper(SelectedLocalItemActivity.this);
                LH.DeleteItem(cList, NEWY);
                SelectedLocalItemActivity.this.startActivity(myIntent);
            }
        });
    }


}
