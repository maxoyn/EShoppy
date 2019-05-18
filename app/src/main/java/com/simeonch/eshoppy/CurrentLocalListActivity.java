package com.simeonch.eshoppy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;


public class CurrentLocalListActivity extends AppCompatActivity {

    private EditText theItem;
    private Button addItem;

    private String CLIST;

    private List<Item> nItems;

    @Override
    protected void onResume() {
        super.onResume();

        Intent myIntent = getIntent();
        CLIST = myIntent.getStringExtra("listName");

        FetchItems();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(CurrentLocalListActivity.this, MainActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        CurrentLocalListActivity.this.finish();
        Log.w("CARR", "FINISH " + CurrentLocalListActivity.this.toString());
        CurrentLocalListActivity.this.startActivity(in);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        theItem = findViewById(R.id.item_entry);
        addItem = findViewById(R.id.add_button);

        Intent myIntent = getIntent();
        CLIST = myIntent.getStringExtra("listName");

        FetchItems();

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = theItem.getText().toString();

                //no item entered
                if(itemName.isEmpty()) {
                    Toast.makeText(CurrentLocalListActivity.this, "No item", Toast.LENGTH_SHORT).show();
                    return;
                }

                //dont add already existing items
                if(nItems != null) {
                    for (Item it : nItems) {
                        if (itemName.toLowerCase().equals(it.getItemName())) {
                            Toast.makeText(CurrentLocalListActivity.this, "Item " + itemName.toLowerCase() + " already added", Toast.LENGTH_SHORT).show();
                            theItem.setText("");
                            return;
                        }
                    }
                }

                //item to add
                final Item newItem = new Item(itemName.toLowerCase(), 1, 1, "none", -1);

                //add item
                LocalDBHelper LH = new LocalDBHelper(CurrentLocalListActivity.this);
                LH.AddTableItem(CLIST, newItem);


                theItem.setText("");
                FetchItems();
            }
        });

    }

    public void FetchItems() {
        LocalDBHelper LH = new LocalDBHelper(CurrentLocalListActivity.this);

        nItems = LH.GetTableItems(CLIST);

        ListView LocalItemsLV = findViewById(R.id.TheList);
        LocalItemAdapter LIA = new LocalItemAdapter(CurrentLocalListActivity.this, (ArrayList)nItems);
        LIA.SetLocalListName(CLIST);
        LocalItemsLV.setAdapter(LIA);
    }

}
