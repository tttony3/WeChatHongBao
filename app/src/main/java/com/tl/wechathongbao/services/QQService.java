package com.tl.wechathongbao.services;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class QQService extends AccessibilityService {


    /**
     * AccessibilityEvent的回调方法
     *
     * @param event 事件
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {


    }


    @Override
    public void onInterrupt() {

    }


    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

    }
}

