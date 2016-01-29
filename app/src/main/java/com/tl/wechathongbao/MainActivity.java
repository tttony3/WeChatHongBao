package com.tl.wechathongbao;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences ;
    SwitchButton btn_list;
    SwitchButton btn_notification;
    SwitchButton btn_chat;
    SwitchButton btn_defense;
    EditText ed_delay;
    EditText ed_field;
    Button btn;
    private final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        findView();
    }

    private void initView() {
        btn_list.setChecked(sharedPreferences.getBoolean("look_list",true));
        btn_notification.setChecked(sharedPreferences.getBoolean("look_notification",true));
        btn_chat.setChecked(sharedPreferences.getBoolean("look_chat",true));
        btn_defense.setChecked(sharedPreferences.getBoolean("defense",true));
        ed_delay.setText(sharedPreferences.getString("delay_time","0"));
        ed_field.setText(sharedPreferences.getString("ignore_field",""));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mAccessibleIntent);
            }
        });

    }

    private void findView() {
        btn_list = (SwitchButton) findViewById(R.id.SwitchButton_list);
        btn_notification = (SwitchButton) findViewById(R.id.SwitchButton_notification);
        btn_chat = (SwitchButton) findViewById(R.id.SwitchButton_chat);
        btn_defense = (SwitchButton) findViewById(R.id.SwitchButton_defense);
        ed_delay = (EditText) findViewById(R.id.ed_delay);
        ed_field= (EditText) findViewById(R.id.ed_field);
        btn = (Button) findViewById(R.id.btn);
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("look_list",btn_list.isChecked());
        editor.putBoolean("look_notification",btn_notification.isChecked());
        editor.putBoolean("look_chat",btn_chat.isChecked());
        editor.putBoolean("defense",btn_defense.isChecked());
        String delay=ed_delay.getText().toString();
        if(delay.equals(""))
            editor.putString("delay_time",0+"");
        else {
            int delay_num = Integer.valueOf(delay);
            if (delay_num < 0)
                editor.putString("delay_time", 0 + "");
            else if (delay_num > 1000)
                editor.putString("delay_time", 1000 + "");
            else
                editor.putString("delay_time", delay_num + "");
        }
        editor.putString("ignore_field",ed_field.getText().toString());
        editor.apply();
        super.onStop();
    }

    @Override
    protected void onStart() {
        initView();
        updateServiceStatus();
        super.onStart();
    }

    private void updateServiceStatus() {
        boolean serviceEnabled = false;

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.HongbaoService")) {
                serviceEnabled = true;
                break;
            }
        }

        if (!serviceEnabled) {
            btn.setText(R.string.service_open);
        } else {
            btn.setText(R.string.service_off);
        }
    }
}
