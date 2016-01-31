package com.tl.wechathongbao.fragments;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;

import com.kyleduo.switchbutton.SwitchButton;
import com.tl.wechathongbao.R;

import java.util.List;

/**
 * Created by tony on 2016/1/30.
 */
public class AlipayFragment extends Fragment {
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
        view = inflater.inflate(R.layout.fragment_alipay, container, false);
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
            if (info.getId().equals(baseContext.getPackageName() + "/.services.AlipayService")) {
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

    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
