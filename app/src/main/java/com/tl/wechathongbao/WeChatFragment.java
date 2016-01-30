package com.tl.wechathongbao;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kyleduo.switchbutton.SwitchButton;
import com.baidu.mobads.AdSettings;
import com.baidu.mobads.AdView;

import java.util.List;

import cn.waps.AppConnect;

/**
 * Created by tttony3 on 2016/1/30.
 */
public class WeChatFragment extends Fragment {
    public Context baseContext;
    View view;
    SwitchButton btn_list;
    SwitchButton btn_notification;
    SwitchButton btn_chat;
    SwitchButton btn_defense;
    EditText ed_delay;
    EditText ed_field;
    EditText ed_probability;
    Button btn;
    AdView adView;
    SharedPreferences sharedPreferences;
    public final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseContext = getActivity();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
        view = inflater.inflate(R.layout.fragment_wechat, container, false);
        findView();
        return view;
    }

    private void initView() {
        btn_list.setChecked(sharedPreferences.getBoolean("look_list", true));
        btn_notification.setChecked(sharedPreferences.getBoolean("look_notification", true));
        btn_chat.setChecked(sharedPreferences.getBoolean("look_chat", true));
        btn_defense.setChecked(sharedPreferences.getBoolean("defense", false));
        ed_delay.setText(sharedPreferences.getString("delay_time", "0"));
        ed_field.setText(sharedPreferences.getString("ignore_field", ""));
        ed_probability.setText(sharedPreferences.getString("wechat_probability", "10"));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mAccessibleIntent);
            }
        });

        setWapsBanner();
        //   baiduBanner();
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
        updateServiceStatus();
    }

    private void setWapsBanner() {
        //设置迷你广告背景颜色
        AppConnect.getInstance(baseContext).setAdBackColor(Color.argb(50, 197, 202, 233));
        //设置迷你广告字体颜色
        AppConnect.getInstance(baseContext).setAdForeColor(R.color.secondaryTextColor);
        LinearLayout miniLayout = (LinearLayout) view.findViewById(R.id.miniAdLinearLayout);
        AppConnect.getInstance(baseContext).showMiniAd(baseContext, miniLayout, 10); //默认 10 秒切换一次广告

        LinearLayout adlayout = (LinearLayout) view.findViewById(R.id.AdLinearLayout);
        AppConnect.getInstance(baseContext).showBannerAd(baseContext, adlayout);
    }


    private void findView() {
        btn_list = (SwitchButton) view.findViewById(R.id.SwitchButton_list);
        btn_notification = (SwitchButton) view.findViewById(R.id.SwitchButton_notification);
        btn_chat = (SwitchButton) view.findViewById(R.id.SwitchButton_chat);
        btn_defense = (SwitchButton) view.findViewById(R.id.SwitchButton_defense);
        ed_delay = (EditText) view.findViewById(R.id.ed_delay);
        ed_field = (EditText) view.findViewById(R.id.ed_field);
        ed_probability = (EditText) view.findViewById(R.id.ed_probability);
        btn = (Button) view.findViewById(R.id.btn);
    }

    @Override
    public void onStop() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("look_list", btn_list.isChecked());
        editor.putBoolean("look_notification", btn_notification.isChecked());
        editor.putBoolean("look_chat", btn_chat.isChecked());
        editor.putBoolean("defense", btn_defense.isChecked());
        String delay = ed_delay.getText().toString();
        if (delay.equals(""))
            editor.putString("delay_time", 0 + "");
        else {
            int delay_num = Integer.valueOf(delay);
            if (delay_num < 0)
                editor.putString("delay_time", 0 + "");
            else if (delay_num > 1000)
                editor.putString("delay_time", 1000 + "");
            else
                editor.putString("delay_time", delay_num + "");
        }
        String strProbability = ed_probability.getText().toString();
        if (strProbability.equals(""))
            editor.putString("wechat_probability", 10 + "");
        else {
            int probability = Integer.valueOf(strProbability);
            if (probability <= 0 || probability > 10)
                editor.putString("wechat_probability", 10 + "");
            else
                editor.putString("wechat_probability", probability + "");
        }
        editor.putString("ignore_field", ed_field.getText().toString());
        editor.apply();
        super.onStop();
    }

    private void updateServiceStatus() {
        boolean serviceEnabled = false;

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) baseContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(baseContext.getPackageName() + "/.HongbaoService")) {
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

    void baiduBanner() {
        //人群属性
        AdSettings.setKey(new String[]{"baidu", "中国"});
        //创建广告view
        String adPlaceID = "2404255";//重要：请填上你的代码位ID,否则无法请求到广告
        adView = new AdView(baseContext, adPlaceID);
        //设置监听器

        //将adView添加到父控件中（注：该父控件不一定为您的根控件，只要该控件能通过addView添加广告视图即可）
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        //rllp.addRule(LinearLayout.VERTICAL);
        ((LinearLayout) view.findViewById(R.id.content2)).addView(adView, rllp);
    }
}
