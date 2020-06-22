package com.example.hsear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ConfigurationClass extends MainMenu {

    CheckBox checkBox1, checkBox2, checkBox3, checkBox4;
    Button btn;
    int isModel = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configlayout);

        btn = (Button) findViewById(R.id.backBtn);
    }



    public void goBackBtn(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("checkedBox", isModel);
        startActivity(intent);
    }



    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.checkBox:
                if(checked) {
                    isModel = 1;
                } else {
                    isModel = 0;
                }
                break;
            case R.id.checkBox2:
                if(checked) {
                    isModel = 2;
                } else {
                    isModel = 0;
                }
                break;
            case R.id.checkBox3:
                if(checked) {
                    isModel = 3;
                } else {
                    isModel = 0;
                }
                break;
            case R.id.checkBox4:
                if(checked) {
                    isModel = 4;
                } else {
                    isModel = 0;
                }
                break;
        }

    }
}
