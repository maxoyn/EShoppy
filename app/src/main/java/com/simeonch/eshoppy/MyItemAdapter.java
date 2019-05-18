package com.simeonch.eshoppy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MyItemAdapter extends ArrayAdapter<Item> {

    public MyItemAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.list_entry_layout, items);
    }

    //private CollectionReference itemRef;
    private FirebaseFirestore db;
    private String email;
    private String CurrentList;

    private View listItemView;

    public void SetMyRef(FirebaseFirestore mDB, String userEmail, String CL) {
        this.db = mDB;
        this.email = userEmail;
        this.CurrentList = CL;
        //this.itemRef = mDB.collection("users").document("list").collection("list");
    }

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
                db.collection(email).document(CurrentList).collection("list").document(currentItem.getItemName()).set(currentItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Toast.makeText(getContext(), String.valueOf(currentItem.getMyStatus()), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentItem.increaseQuantity();
                String s = Integer.toString(currentItem.getItemQuantity());
                currentQ.setText(s);


                db.collection(email).document(CurrentList).collection("list").document(currentItem.getItemName()).set(currentItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Toast.makeText(getContext(), "++" + currentItem.getItemName(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        //item view group to delete the item from the view
        final ViewGroup finalListItemView = (ViewGroup)listItemView;

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentItem.getItemQuantity() > 1) {
                    currentItem.decreaseQuantity();
                    String s = Integer.toString(currentItem.getItemQuantity());
                    currentQ.setText(s);

                    db.collection(email).document(CurrentList).collection("list").document(currentItem.getItemName()).set(currentItem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Toast.makeText(getContext(), "--" + currentItem.getItemName(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });



        //enter SelectedItemActivity (more detailed view of the item)
        //send all item specific info in the intent so that activity does less work
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getContext(), SelectedItemActivity.class);
                myIntent.putExtra("obj", currentItem);

//                DocumentReference DR = db.collection(email).document(CurrentList).collection("list").document(currentItem.getItemName());
//                SelectedItemActivity.ThisDR = DR;

                //collection reference, so SelectedItemActivity can do the update on it's own
                CollectionReference CR = db.collection(email).document(CurrentList).collection("list");
                SelectedItemActivity.ThisCR = CR;
                myIntent.putExtra("email", email);
                myIntent.putExtra("currentList", CurrentList);

                getContext().startActivity(myIntent);
            }
        });

        return listItemView;
    }


    private void DeleteItem(final Item toDel, final ViewGroup one, final ViewGroup two) {
        db.collection(email).document(CurrentList).collection("list").document(toDel.getItemName()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        one.removeView(two);
                        //Toast.makeText(getContext(), "DEL " + toDel.getItemName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
