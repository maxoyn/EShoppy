package com.simeonch.eshoppy;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;


public class CurrentListActivity extends AppCompatActivity {

    private EditText theItem;
    private Button addItem;

    private String CLIST;

    private FirebaseFirestore db;
    private CollectionReference itemRef;

    private FirebaseUser currentUser;
    private String TheMail;

    private List<Item> nItems;


    @Override
    protected void onResume() {
        super.onResume();
        Intent in = getIntent();

        //from MainActivity (lists view)
        String curName = in.getStringExtra("listName");
        String EE = in.getStringExtra("email");

        if(curName != null) {
            CLIST = curName;
            if(EE != null) {
                TheMail = EE;
            }
            else {
                TheMail = currentUser.getEmail();
            }
            itemRef = db.collection(TheMail).document(CLIST).collection("list");
        }

        String oldName = in.getStringExtra("oldName");
        if(oldName != null) {
            itemRef.document(oldName).delete();
            Log.w("CARR", "ONRESUME DELETE " + oldName);
        }


        Log.w("CARR", CLIST + "-" + TheMail);
        NewWay();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(CurrentListActivity.this, MainActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        CurrentListActivity.this.finish();
        Log.w("CARR", "FINISH " + CurrentListActivity.this.toString());
        CurrentListActivity.this.startActivity(in);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        db = FirebaseFirestore.getInstance();

        theItem = findViewById(R.id.item_entry);
        addItem = findViewById(R.id.add_button);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        TheMail = currentUser.getEmail();

        if(CLIST != null) {
            Log.w("ASD", CLIST);
            itemRef = db.collection(TheMail).document(CLIST).collection("list");

            NewWay();
        }

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = theItem.getText().toString();

                //no item entered
                if(itemName.isEmpty()) {
                    Toast.makeText(CurrentListActivity.this, "No item", Toast.LENGTH_SHORT).show();
                    return;
                }

                //dont add already existing items
                if(nItems != null) {
                    for (Item it : nItems) {
                        if (itemName.toLowerCase().equals(it.getItemName())) {
                            Toast.makeText(CurrentListActivity.this, "Item " + itemName.toLowerCase() + " already added", Toast.LENGTH_SHORT).show();
                            theItem.setText("");
                            return;
                        }
                    }
                }

                //item to add
                final Item newItem = new Item(itemName.toLowerCase(), 1, 1, "none", -1);

                //was .document("list")
                db.collection(currentUser.getEmail()).document(CLIST).collection("list").document(newItem.getItemName()).set(newItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseAnalytics FA = FirebaseAnalytics.getInstance(CurrentListActivity.this);
                                Bundle asd = new Bundle();
                                asd.putString(FirebaseAnalytics.Param.ITEM_NAME, newItem.getItemName());
                                FA.logEvent(FirebaseAnalytics.Event.SET_CHECKOUT_OPTION, asd);
                                //Toast.makeText(CurrentListActivity.this, "add " + newItem.getItemName(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CurrentListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                theItem.setText("");
                NewWay();
            }
        });

    }

    //populate the view with the items in the current list
    public void Populate() {
        if(!haveNetworkConnection()) {
            Toast.makeText(CurrentListActivity.this, "No connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if(nItems == null || nItems.isEmpty()) {
            //Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
            return;
        }
        ListView LV = findViewById(R.id.TheList);
        MyItemAdapter itemAdapter = new MyItemAdapter(CurrentListActivity.this, (ArrayList)nItems);
        itemAdapter.SetMyRef(db, currentUser.getEmail(), CLIST);
        LV.setAdapter(itemAdapter);
    }

    //fetch items and call populate
    public void NewWay() {
        itemRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()) {
                    nItems = queryDocumentSnapshots.toObjects(Item.class);
                    Populate();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CurrentListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CurrentListActivity.this.CONNECTIVITY_SERVICE);
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
