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


public class SelectedItemActivity extends AppCompatActivity {

    //public static DocumentReference ThisDR;
    public static CollectionReference ThisCR;

    private String uEmail;
    private String cList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_item);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


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
        //final String itemName = in.getStringExtra("name");
        //String itemQ = in.getStringExtra("quantity");
        //final Double itemPrice = in.getDoubleExtra("price", 1.00);

        //final String fromList = in.getStringExtra("fromList");

        uEmail = in.getStringExtra("email");
        cList = in.getStringExtra("currentList");

//        selectedText.setText(itemName);
//        selectedQ.setText(itemQ);
//        selectedPrice.setText(itemPrice.toString());

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

//                int check = Integer.parseInt(String.valueOf(selectedQ.getText()));
//
//                if(check > 1) {
//                    check--;
//                    selectedQ.setText(String.valueOf(check));
//                }
            }
        });

        //increase button
        increaseSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NEWY.increaseQuantity();
                selectedQ.setText(String.valueOf(NEWY.getItemQuantity()));
//                int check = Integer.parseInt(String.valueOf(selectedQ.getText()));
//                check++;
//                selectedQ.setText(String.valueOf(check));
            }
        });

        //the update button, send an intent to the CurrentListActivity, which OnResume() updates the selected item
        updateSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SelectedItemActivity.this, CurrentListActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //myIntent.putExtra("action", "update");

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

                DoUpdate(NEWY, "update", oldName, myIntent);
            }
        });

        //same but delete
        deleteSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SelectedItemActivity.this, CurrentListActivity.class);
                myIntent.putExtra("listName", cList);

                DoUpdate(NEWY, "delete", null, myIntent);
            }
        });
    }

    //changing the name only does it on the "itemName" and not the document name
    public void DoUpdate(final Item TT, final String action, final String oldName, final Intent IN) {

        if(!haveNetworkConnection()) {
            Toast.makeText(SelectedItemActivity.this, "No connection, try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String iName = TT.getItemName(); //can remove
        final DocumentReference DR = ThisCR.document(TT.getItemName());

        if(action.equals("update")) {
            if(oldName == null) {
                DR.set(TT).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(SelectedItemActivity.this, "update: " + iName, Toast.LENGTH_SHORT).show();
                        Log.w("CARR", "UPDATE: " + iName);
                        SelectedItemActivity.this.finish();
                        SelectedItemActivity.this.startActivity(IN);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CARR", "UPDATE FAIL: " + iName + " " + e.getMessage());
                    }
                });
            }
            else if(oldName != null) {
                DR.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ThisCR.document(TT.getItemName()).set(TT).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.w("CARR", "UPDATE FROM: " + oldName + " to " + TT.getItemName());
                                SelectedItemActivity.this.finish();
                                SelectedItemActivity.this.startActivity(IN);
                            }
                        });
                    }
                });
            }
        }
        else {
            DR.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.w("CARR", "DELETE: " + iName);
                    SelectedItemActivity.this.finish();
                    SelectedItemActivity.this.startActivity(IN);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("CARR", "DELETE FAIL: " + iName);
                }
            });
        }
    }

    //got this from the internet
    //checks if wifi/mobile are on and connected to something
    //but not if their speed is enough
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(SelectedItemActivity.this.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected())
                {
                    haveConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected())
                {
                    haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
