package com.simeonch.eshoppy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyListAdapter extends ArrayAdapter<String> {

    public MyListAdapter(Context context, ArrayList<String> AllLists) {
        super(context, R.layout.list_entry_layout, AllLists);
    }

    private FirebaseFirestore db;
    private String email;
    private View listMyListView;

    public void SetMyRef(FirebaseFirestore mDB, String ss) {
        this.db = mDB;
        this.email = ss;
    }

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
                Intent myIntent = new Intent(getContext(), CurrentListActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                myIntent.putExtra("listName", myList);
                myIntent.putExtra("email", email);
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
                                DeleteList(myList);
                                Log.w("CARR", "Clicked YES");
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

    //as you delete an item for the list as a whole, BUT
    //CollectionReference IR, then on get success for each querydoc snapshot
    //and delete the specific document for the item
    //otherwise they are not deleted (if you delete list only)
    //and if you create a list with the same name, you get the old items
    //according to FIRESTORE docs, NOT RECOMMENDED to DELETE in android :(
    private void DeleteList(final String ListName) {
        final DocumentReference DR = db.collection(email).document(ListName);

        DR.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                CollectionReference IR = DR.collection("list");
                IR.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            for(QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                Log.w("CARRR", q.toString());
                                q.getReference().delete();
                            }
                        }
                        DR.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.w("CARR", "DELETED LIST: " + ListName);
                                Toast.makeText(getContext(), "Deleted list: " + ListName, Toast.LENGTH_SHORT).show();
                                Intent myIntent = new Intent(getContext(), MainActivity.class);
                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(myIntent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.w("CARR", "CANT DELETE " + e.getMessage());
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w("CARR", e.getMessage());
            }
        });
    }
}

