package com.refridginator.app.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.refridginator.app.R;
import com.refridginator.app.data.Item;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AddGrocery extends AppCompatActivity {
    private static final String TAG = "GroceryListAct"; //used to debug

    ArrayList<Item> items;  //The container of the items, an item is actually a category.
    private int itemQuantity;   //The total number of all kind of items
    private GrolistAdapter glAdapter;   //The adapter of the recycleriew
    private LinearLayoutManager layoutManager;  //used to manage the layout of this activity
    private RecyclerView recyclerView;  //the recyclerview
    private View addItemButton;
    private Parcelable mListState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_view);

        loadData();
        recyclerView = this.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        addItemButton = findViewById(R.id.btn_add_item);

        showRecyclerview(items);
    }

    private void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("spitems", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Item item: items){
            if (item.getName() == ""){items.remove(item);}
        }
        Gson gson = new Gson();
        String json = gson.toJson(items);
        editor.putString("items",json);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("spitems", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("items",null);
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();

        items = gson.fromJson(json, type);
        if (items == null){
            items = new ArrayList<>();
        }
    }

    public void showRecyclerview(ArrayList<Item> items){
        glAdapter = new GrolistAdapter(this, items);
        glAdapter.setBtnDelListener(new GrolistAdapter.BtnOnClickListerner() {
            @Override
            //remove item entry
            public void onDeleteClick(int postion) {
                items.remove(postion);
                glAdapter.notifyItemRangeRemoved(postion, 1);
                get_set_ItemQuantity();
            }

            @Override
            //update quantity of an item
            public void onAddQuantityClick(int position) {
                Item e = items.get(position);
                e.setName(e.getName());
                e.setNumber(e.getNumber()+1);
                glAdapter.notifyItemChanged(position);
                get_set_ItemQuantity();
            }

            @Override
            public void onMinusQuantityClick(int position) {
                Item e = items.get(position);
                e.setName(e.getName());
                if (e.getNumber() >1) {e.setNumber(e.getNumber()-1);}
                else return;
                glAdapter.notifyItemChanged(position);
                get_set_ItemQuantity();
            }
        });
        recyclerView.setAdapter(glAdapter);
        recyclerView.setLayoutManager(layoutManager);

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLastItemEmpty() && items.size()!=0){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(AddGrocery.this);
                    builder.setTitle("Type your wish item before insert new item").setIcon(android.R.drawable.ic_dialog_info);
//                    Toast.makeText(AddGrocery.this, isLastItemEmpty().toString()+items.size(), Toast.LENGTH_LONG).show();
                    AlertDialog dlg = builder.create();
                    dlg.show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            dlg.dismiss();
                        }
                    }, 1000);
                    return;
                }
                int fillPosition = items.size();
                items.add(fillPosition, new Item("", 1));
                glAdapter.notifyItemInserted(fillPosition);
                get_set_ItemQuantity();
            }
        });
    }

    @Override
    //  return the uppper class page of this activity, window slide from left to right.
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //A method to set the quantity of items, this quantity will be shown in the activity view.
    public void get_set_ItemQuantity(){
        itemQuantity=0;
        for (int i=0; i< items.size(); i++){
            itemQuantity += items.get(i).getNumber();
        }
        TextView quantity_tv = findViewById(R.id.textView4); //the view with id being 'textView4' shows the quantity.
        quantity_tv.setText(itemQuantity+"");
    }

    public Boolean isLastItemEmpty() {
        if (items.size() ==0) return false;
        Boolean lastItemEmpty = items.get(items.size()-1).getName() == ""? true:false;
        return lastItemEmpty;
    }

    @Override
    //save the current data when the ativity was interrupted.
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        saveData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        loadData();
        recyclerView = this.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        addItemButton = findViewById(R.id.btn_add_item);

        showRecyclerview(items);
        get_set_ItemQuantity();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}