package com.simeonch.eshoppy;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mainText;
    private Button outButton;


    private FloatingActionButton fab;

    private FirebaseFirestore db;
    private List<String> availableLists = new ArrayList<>();
    private String userEmail;
    private List<String> localLists = new ArrayList<>();

    private NavigationView NV;
    //private SwipeRefreshLayout SRL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NV = findViewById(R.id.nav_view);

        db = FirebaseFirestore.getInstance();

        outButton = findViewById(R.id.out_button);
        outButton.setVisibility(View.INVISIBLE);


        fab = findViewById(R.id.FAB_LIST);
        //fab.hide();

//        //pull to refresh, working properly for new lists
//        //but not for deleted ones
//        SRL = findViewById(R.id.pullToRefresh);
//        SRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                if(userEmail != null) {
//                    Log.w("CARR", userEmail);
//                    FetchLists("refresh");
//                    SRL.setRefreshing(false);
//                }
//            }
//        });


        //if we remove this and only leave it onResume it does it twice anyways
        //string attribute for debugging
        //fetch lists for logged user
        if(CheckUserNew()) {
            FetchLists("onCreate");
        }


        //reload current activity after logout, so we're on the start screen
        outButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(myIntent);

            }
        });


        //create list
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!haveNetworkConnection()) {
                    Toast.makeText(MainActivity.this, "No connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                CreateList();
            }
        });

    }


    private void CreateList() {
        Intent myIntent = new Intent(MainActivity.this, CreateListActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //for checking if the list is already online
        String[] the_lists = new String[availableLists.size()];
        for(int i = 0; i < availableLists.size(); i++) {
            the_lists[i] = availableLists.get(i);
        }

        //for checking if the list is already offline
        String[] local_lists = new String[localLists.size()];
        for(int i = 0; i < localLists.size(); i++) {
            local_lists[i] = localLists.get(i);
        }

        myIntent.putExtra("lists", the_lists);
        myIntent.putExtra("local_lists", local_lists);
        myIntent.putExtra("email", userEmail);
        MainActivity.this.startActivity(myIntent);
    }

    //for when we get here after a successful registration or login
    @Override
    protected void onResume() {
        super.onResume();

        mainText = findViewById(R.id.main_text);
        FetchLocalLists();

        if(!CheckUserNew()) {
            mainText.setText("Hello, stealth.");
            return;
        }
        mainText.setText("Hello, " + userEmail);
        FetchLists("onResume");
    }

    //with login being main activity
    //you either exit like this and play an animation
    //login -> main when you start again
    //or it doesn't exit
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Log.w("CARR_MAIN", "FINISH " + MainActivity.this.toString());
        MainActivity.this.finish();
    }


    //get the local lists
    public void FetchLocalLists() {
        LocalDBHelper LH = new LocalDBHelper(MainActivity.this);
        localLists = LH.GetTables();

        Collections.sort(localLists);

        ListView LocalLV = findViewById(R.id.LISTVIEW_LISTSLOCAL);
        LocalListAdapter LLA = new LocalListAdapter(MainActivity.this, (ArrayList)localLists);
        LocalLV.setAdapter(LLA);
    }

    //for the user logged
    public void FetchLists(final String s) {
        if(!haveNetworkConnection()) {
            Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference CR = db.collection(userEmail);

        CR.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    //loop the documentsnapshots and add the LIST NAMES as STRINGS
                    //because each list if we add it as a class like the items, has subcollections
                    //and not only names
                    for(DocumentSnapshot dd : documents) {
                        //this was executing 2 times so check and only add once
                        //it's letter sensitive, LIST names are not normalized for now
                        if(!availableLists.contains(dd.getId())) {
                            availableLists.add(dd.getId());
                        }
                    }
                    PopulateLists();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //populate our VIew with the seperate lists
    public void PopulateLists() {
        if(availableLists == null || availableLists.isEmpty()) {
            Toast.makeText(this, "Nothing in lists", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(availableLists);

        ListView LV = findViewById(R.id.LISTVIEW_LISTS);
        MyListAdapter listAdapter = new MyListAdapter(MainActivity.this, (ArrayList)availableLists);
        listAdapter.SetMyRef(db, userEmail);
        LV.setAdapter(listAdapter);
    }

    //if user is logged set his email for us to use and logout button
    private boolean CheckUserNew() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null) {
            return false;
        }
        else {
            userEmail = currentUser.getEmail();
            outButton = findViewById(R.id.out_button);
            outButton.setVisibility(View.VISIBLE);
            return true;
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);
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

