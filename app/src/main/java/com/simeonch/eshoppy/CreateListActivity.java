package com.simeonch.eshoppy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    //private CollectionReference itemRef;
    private FirebaseUser currentUser;

    private EditText ListName;
    private Button AddList;

    private CheckBox ListType;

    private String[] checkLists;
    private String[] checkLocalLists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ListName = findViewById(R.id.list_name);
        AddList = findViewById(R.id.add_list);
        ListType = findViewById(R.id.SHOULD_BE_LOCAL);

        Intent in = getIntent();
        checkLists = in.getStringArrayExtra("lists");
        checkLocalLists = in.getStringArrayExtra("local_lists");

        //if we're in offline mode, create only offline lists
        String email = in.getStringExtra("email");
        if(email == null) {
            ListType.setChecked(true);
            ListType.setClickable(false);
        }

        AddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String theName = ListName.getText().toString();

                //nothing entered
                if(theName.isEmpty()) {
                    Toast.makeText(CreateListActivity.this, "No item entered.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //if list online
                for(String L : checkLists) {
                    if(L.equals(theName)) {
                        Toast.makeText(CreateListActivity.this, "List " + theName.toLowerCase() + " already exists online.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //if list offline
                for(String L : checkLocalLists) {
                    if(L.equals(theName)) {
                        Toast.makeText(CreateListActivity.this, "List " + theName.toLowerCase() + " already exists locally.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                MyList myList = new MyList(theName.toLowerCase());

                //make list online
                if(!ListType.isChecked()) {
                    //add lists with the name, +listName property
                    db.collection(currentUser.getEmail()).document(myList.getListName()).set(myList).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CreateListActivity.this, "Created list: " + theName, Toast.LENGTH_SHORT).show();
                            Intent myIntent = new Intent(CreateListActivity.this, CurrentListActivity.class);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            myIntent.putExtra("listName", theName);
                            CreateListActivity.this.startActivity(myIntent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //make list offline
                else {
                    //sqlite has problems if the name begins with a number or has some special symbols and stuff
                    //haven't really tested this much
                    if(CheckString(theName)) {

                        LocalDBHelper LH = new LocalDBHelper(CreateListActivity.this, theName);
                        LH.CreateLocalList();

                        Toast.makeText(CreateListActivity.this, "Created Local list " + theName, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(CreateListActivity.this, "Please use only letters for local lists. Sorry", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                ListName.setText("");

                CreateListActivity.this.finish();
            }
        });
    }

    private boolean CheckString(String name) {
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(CreateListActivity.this, MainActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        CreateListActivity.this.finish();
        CreateListActivity.this.startActivity(in);
    }

}
