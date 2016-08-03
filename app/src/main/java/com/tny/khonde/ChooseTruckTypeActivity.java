package com.tny.khonde;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class ChooseTruckTypeActivity extends AppCompatActivity {
    int truck_type_group=0;
    boolean isAttachedTruck = false;
    ArrayList<String> truck_type_selected;
    final int NO_EXTRA = 0, CRANE=1,DUMP=2,HYDROLIC=3,DUCK=4,OPEN_SIDE=5,HEAVY_LOAD=6;

    @Override
    public void onResume(){
        super.onResume();
        if(MainActivity.job==null)
            finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_truck_type);

        LinearLayout layout = (LinearLayout)findViewById(R.id.truck_types_layout);
        for(int i=0;i<layout.getChildCount();i++){
            ((CheckBox)layout.getChildAt(i)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    LinearLayout layout1 = (LinearLayout)findViewById(R.id.truck_types_layout);

                    ArrayList<String> isSelectable = new ArrayList<String>();
                    truck_type_selected = new ArrayList<String>();
                    isAttachedTruck = false;

                    for(int i=0;i<layout1.getChildCount();i++) {
                        CheckBox the_view = ((CheckBox)layout1.getChildAt(i));
                        the_view.setEnabled(true);
                        boolean isChecked1 = the_view.isChecked();

                        if(isChecked1){
                            truck_type_selected.add(the_view.getText().toString());
                            //((TextView) findViewById(R.id.roof_type_label)).setText("เลือกชนิดตัวถัง");
                            findViewById(R.id.roof_type_layout).setVisibility(View.VISIBLE);

                            if(the_view.getText().toString().equals("รถกระบะ") || the_view.getText().toString().equals("รถบรรทุก4ล้อใหญ่")){
                                isSelectable.add("รถกระบะ");
                                isSelectable.add("รถบรรทุก4ล้อใหญ่");

                                if(!((TextView)findViewById(R.id.roof_type_label0)).getText().toString().equals("ชนิดตัวถัง")) {
                                    ((TextView) findViewById(R.id.roof_type_label0)).setText("ชนิดตัวถัง");
                                    ((TextView) findViewById(R.id.roof_type_label)).setText("เลือกชนิดตัวถัง");
                                }
                                truck_type_group = 1;
                            } else if (the_view.getText().toString().equals("รถเทรลเลอร์2เพลา") || the_view.getText().toString().equals("รถเทรลเลอร์3เพลา")) {
                                isSelectable.add("รถเทรลเลอร์2เพลา");
                                isSelectable.add("รถเทรลเลอร์3เพลา");
                                if(!((TextView)findViewById(R.id.roof_type_label0)).getText().toString().equals("ชนิดหาง")) {
                                    ((TextView) findViewById(R.id.roof_type_label0)).setText("ชนิดหาง");
                                    ((TextView) findViewById(R.id.roof_type_label)).setText("เลือกชนิดหาง");
                                }
                                //findViewById(R.id.extras_layout).setVisibility(View.VISIBLE);
                                truck_type_group=3;

                            } else{
                                isSelectable.add(the_view.getText().toString());
                                //findViewById(R.id.extras_layout).setVisibility(View.VISIBLE);
                                if(!((TextView)findViewById(R.id.roof_type_label0)).getText().toString().equals("ชนิดตัวถัง")) {
                                    ((TextView) findViewById(R.id.roof_type_label0)).setText("ชนิดตัวถัง");
                                    ((TextView) findViewById(R.id.roof_type_label)).setText("เลือกชนิดตัวถัง");
                                }

                                isAttachedTruck = true;
                                if(!the_view.getText().toString().equals("รถบรรทุก10ล้อพ่วง")){
                                    findViewById(R.id.trunk_width_layout).setVisibility(View.VISIBLE);
                                    isAttachedTruck = false;
                                    isSelectable.add("รถบรรทุก6ล้อ");
                                    isSelectable.add("รถบรรทุก10ล้อ");
                                    isSelectable.add("รถบรรทุก12ล้อ");
                                }

                                truck_type_group =2;
                            }

                        }
                    }

                    if(isSelectable.size()==0){
                        truck_type_group=0;
                        findViewById(R.id.gotoConfirmButton).setEnabled(false);
                        ((TextView)findViewById(R.id.trunk_width_label)).setText("เลือกความยาวกระบะท้าย");
                        findViewById(R.id.extras_layout).setVisibility(View.GONE);
                        findViewById(R.id.roof_type_layout).setVisibility(View.GONE);
                        findViewById(R.id.trunk_width_layout).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.roof_type_label0)).setText(((TextView) findViewById(R.id.roof_type_label0)).getText().toString() + " ");
                        return;
                    }

                    for(int i=0;i<layout1.getChildCount();i++){
                        boolean canSelect = false;
                        for(int j=0;j<isSelectable.size();j++){
                            if(isSelectable.get(j).equals(((CheckBox)layout1.getChildAt(i)).getText().toString())) {
                                canSelect = true;
                            }
                        }

                        if(!canSelect)
                            layout1.getChildAt(i).setEnabled(false);
                    }


                }
            });
        }


        LinearLayout truck_types_layout = (LinearLayout)findViewById(R.id.truck_types_layout);
        if(MainActivity.job.truck_type!=null){
            String[] truck_types = MainActivity.job.truck_type.split(";");
            for(int i=0;i<truck_types.length;i++){
                for (int j=0;j<truck_types_layout.getChildCount();j++){
                    if(((CheckBox)truck_types_layout.getChildAt(j)).getText().toString().equals(truck_types[i])){
                        ((CheckBox)truck_types_layout.getChildAt(j)).setChecked(true);
                    }
                }
            }

            ((TextView)findViewById(R.id.roof_type_label)).setText(MainActivity.job.roof_type);
            ((TextView)findViewById(R.id.trunk_width_label)).setText(MainActivity.job.trunk_width);

            for(int i=0;i<((RadioGroup)findViewById(R.id.extras_radio)).getChildCount();i++){
                RadioButton extra_radio = (RadioButton)((RadioGroup)findViewById(R.id.extras_radio)).getChildAt(i);
                if(extra_radio.getText().toString().equals(MainActivity.job.extra))
                    extra_radio.setChecked(true);
            }

            ((TextView)findViewById(R.id.truck_count_label)).setText(MainActivity.job.truck_count);
            ((TextView)findViewById(R.id.helper_count_label)).setText(MainActivity.job.helper_count);

            filterExtras(MainActivity.job.roof_type);
            validateAllInputs();

        }

    }

    public void goToConfirmContact(View v){
        LinearLayout layout = (LinearLayout)findViewById(R.id.truck_types_layout);
        String truck_types="";
        for (int i=0;i<layout.getChildCount();i++){
            if(((CheckBox)layout.getChildAt(i)).isChecked())
                truck_types+= ((CheckBox)layout.getChildAt(i)).getText().toString() + ";";
        }

        MainActivity.job.truck_type= truck_types;
        MainActivity.job.roof_type= ((TextView)findViewById(R.id.roof_type_label)).getText().toString();
        MainActivity.job.trunk_width= ((TextView)findViewById(R.id.trunk_width_label)).getText().toString();
        int extra_checked_id = ((RadioGroup)findViewById(R.id.extras_radio)).getCheckedRadioButtonId();
        MainActivity.job.extra= ((RadioButton)findViewById(extra_checked_id)).getText().toString();
        MainActivity.job.truck_count= ((TextView)findViewById(R.id.truck_count_label)).getText().toString();
        MainActivity.job.helper_count= ((TextView)findViewById(R.id.helper_count_label)).getText().toString();

        Intent intent = new Intent(this,ConfirmContactActivity.class);
        startActivityForResult(intent,5000);
    }

    public void selectTruckChoice(View v){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int defaultChoice = 0;
        String[] items = null;
        TextView label_tv = null;
        String title="";
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1);
        boolean choosingRoofType = false;

        switch (v.getId()){
            case R.id.roof_type_layout:
                label_tv = (TextView) findViewById(R.id.roof_type_label);
                title = "เลือกชนิดตัวถัง";
                choosingRoofType = true;

                switch (truck_type_group){
                    case 1:
                        items = new String[]{"คอกผ้าใบ","ตู้ทึบ","กระบะ","ตู้เย็น"};
                        break;
                    case 2:
                        items = new String[]{"คอกผ้าใบ","ตู้ทึบ","กระบะเตี้ย","กระบะขนดินหิน","พื้นเรียบ","ตู้เย็น"};
                        break;
                    case 3:
                        title="เลือกชนิดหาง";
                        items = new String[]{"หางก้างปลา","หางเรียบ","หางตู้ทึบ","หางกระบะ","หางคอกผ้าใบ","หางโลว์เบด","หางตู้เย็น"};

                        break;
                }

                break;

            case R.id.trunk_width_layout:
                label_tv = (TextView) findViewById(R.id.trunk_width_label);
                title = "เลือกความยาวกระบะท้ายขั้นต่ำ";
                items = new String[]{"3 ม.","4 ม.","5 ม.","6 ม.","7 ม.","8 ม.","9 ม.","10 ม."};

                break;

            case R.id.truck_count_layout:
                label_tv = (TextView) findViewById(R.id.truck_count_label);
                title = "เลือกจำนวนรถ (คัน)";
                items = new String[]{"1","2","3","4","5"};
                break;

            case R.id.helper_count_layout:
                label_tv = (TextView) findViewById(R.id.helper_count_label);
                title = "ผู้ช่วยยกของไม่รวมคนขับ (คน)";
                items = new String[]{"0","1","2","3","4","5"};
                break;
        }

        for(int i=0;i<items.length;i++){
            adapter.add(items[i]);
            /*
            if(label_tv.getText().toString().equals(items[i])){
                defaultChoice = i;
                break;
            }
            */
        }
        final String[]items2 = items;
        final TextView label_tv2 = label_tv;
        final boolean choosingRoofType1 = choosingRoofType;
        /*
        builder.setSingleChoiceItems(items, defaultChoice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                label_tv2.setText(items2[item].toString());
                if(choosingRoofType1 && (truck_type_group==2||truck_type_group==3))
                    findViewById(R.id.extras_layout).setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        */

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                String selected_string = items2[item].toString();
                label_tv2.setText(selected_string);

                if(choosingRoofType1) {
                    filterExtras(selected_string);
                }
                validateAllInputs();
                dialog.dismiss();
            }
        });

        /*
        final boolean[] isSelected = new boolean[items.length];
        for(int i=0;i<isSelected.length;i++)
            isSelected[i] = false;

        builder.setMultiChoiceItems(items, isSelected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                isSelected[which] = isChecked;

            }
        })
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selected = "";
                for(int i=0;i<isSelected.length;i++){
                    if(isSelected[i])
                        selected += items2[i] + ",";
                }
                selected = selected.substring(0,selected.length()-1);

                label_tv2.setText(selected);
                dialog.dismiss();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        */
        builder.setTitle(title+"\n");
        final AlertDialog choiceDialog = builder.create();

        choiceDialog.show();

    }

    public void filterExtras(String selected_string){

        RadioGroup layout = (RadioGroup) findViewById(R.id.extras_radio);
        int selected_extra=0;

        for(int i=1;i<layout.getChildCount();i++){
            layout.getChildAt(i).setVisibility(View.GONE);
            if(((RadioButton)layout.getChildAt(i)).isChecked())
                selected_extra = i;
        }
        if (truck_type_group ==1){
            layout.getChildAt(HEAVY_LOAD).setVisibility(View.VISIBLE);
            findViewById(R.id.extras_layout).setVisibility(View.VISIBLE);
        } else if (truck_type_group == 2) {
            if(selected_string.equals("คอกผ้าใบ")){
                layout.getChildAt(HYDROLIC).setVisibility(View.VISIBLE);
                layout.getChildAt(OPEN_SIDE).setVisibility(View.VISIBLE);
                layout.getChildAt(DUMP).setVisibility(View.VISIBLE);
            }else if(selected_string.equals("ตู้ทึบ")){
                layout.getChildAt(HYDROLIC).setVisibility(View.VISIBLE);
                layout.getChildAt(OPEN_SIDE).setVisibility(View.VISIBLE);
            }else if(selected_string.equals("กระบะเตี้ย")) {
                layout.getChildAt(CRANE).setVisibility(View.VISIBLE);
                layout.getChildAt(OPEN_SIDE).setVisibility(View.VISIBLE);
                layout.getChildAt(DUMP).setVisibility(View.VISIBLE);
            }else if(selected_string.equals("กระบะขนดินหิน")){
                layout.getChildAt(DUMP).setVisibility(View.VISIBLE);
            }else if(selected_string.equals("พื้นเรียบ")){
                layout.getChildAt(CRANE).setVisibility(View.VISIBLE);
                layout.getChildAt(DUCK).setVisibility(View.VISIBLE);
            }else {
                layout.getChildAt(HYDROLIC).setVisibility(View.VISIBLE);
                layout.getChildAt(OPEN_SIDE).setVisibility(View.VISIBLE);
            }

            if(isAttachedTruck){
                layout.getChildAt(CRANE).setVisibility(View.GONE);
                layout.getChildAt(HYDROLIC).setVisibility(View.GONE);
            }

            findViewById(R.id.extras_layout).setVisibility(View.VISIBLE);

        } else if(truck_type_group ==3){
            if(selected_string.equals("หางก้างปลา") || selected_string.equals("หางตู้เย็น")){
                findViewById(R.id.extras_layout).setVisibility(View.GONE);
            }else{

                if(selected_string.equals("หางเรียบ")){
                    layout.getChildAt(DUCK).setVisibility(View.VISIBLE);
                } else if(selected_string.equals("หางตู้ทึบ")){
                    layout.getChildAt(OPEN_SIDE).setVisibility(View.VISIBLE);
                } else if(selected_string.equals("หางกระบะ")){
                    layout.getChildAt(OPEN_SIDE).setVisibility(View.VISIBLE);
                    layout.getChildAt(DUMP).setVisibility(View.VISIBLE);
                } else if(selected_string.equals("หางคอกผ้าใบ")){
                    layout.getChildAt(OPEN_SIDE).setVisibility(View.VISIBLE);
                    layout.getChildAt(DUMP).setVisibility(View.VISIBLE);
                } else if(selected_string.equals("หางโลว์เบด")){
                    layout.getChildAt(DUCK).setVisibility(View.VISIBLE);
                }

                findViewById(R.id.extras_layout).setVisibility(View.VISIBLE);
            }
        }

        if(layout.getChildAt(selected_extra).getVisibility()==View.GONE)
            ((RadioButton)layout.getChildAt(0)).setChecked(true);
    }

    public void validateAllInputs(){
        findViewById(R.id.gotoConfirmButton).setEnabled(false);
        if(truck_type_group==0)
            return;

        View [] tvs = {
                findViewById(R.id.roof_type_label),
                findViewById(R.id.trunk_width_label),
                findViewById(R.id.truck_count_label),
                findViewById(R.id.helper_count_label)
        };

        View [] tvs2 = {
                findViewById(R.id.roof_type_layout),
                findViewById(R.id.trunk_width_layout),
                findViewById(R.id.truck_count_layout),
                findViewById(R.id.helper_count_layout)
        };


        for(int i =0;i<tvs.length;i++){
            TextView textView = (TextView)tvs[i];
            RelativeLayout layout = (RelativeLayout)tvs2[i];
            if(textView.getText().toString().length()>5 && layout.getVisibility()==View.VISIBLE) {
                if (textView.getText().toString().substring(0, 5).equals("เลือก")) {
                    return;
                }
            }
        }

        findViewById(R.id.gotoConfirmButton).setEnabled(true);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 5000) {
            Intent res = new Intent();
            setResult(RESULT_OK, res);
            finish();
        }
    }
}
