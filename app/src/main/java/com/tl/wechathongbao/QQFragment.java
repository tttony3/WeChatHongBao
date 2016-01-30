package com.tl.wechathongbao;

import android.accessibilityservice.AccessibilityServiceInfo;
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

import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;

import cn.waps.AppConnect;

/**
 * Created by tony on 2016/1/30.
 */
public class QQFragment extends Fragment {
    View view;
    Context baseContext;
    SharedPreferences sharedPreferences;
    private Button btn;
    public final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseContext = getActivity();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
        view = inflater.inflate(R.layout.fragment_qq, container, false);
        findView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
        updateServiceStatus();
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

    private void findView() {
        btn = (Button) view.findViewById(R.id.btn);
    }

    private void initView() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mAccessibleIntent);
            }
        });
        setWapsBanner();
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
}
