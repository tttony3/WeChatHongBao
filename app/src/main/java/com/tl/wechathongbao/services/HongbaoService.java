package com.tl.wechathongbao.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.tl.wechathongbao.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HongbaoService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private AccessibilityNodeInfo mReceiveNode, mUnpackNode;

    private boolean mLuckyMoneyPicked, mLuckyMoneyReceived, mNeedUnpack, mNeedBack;

    private String lastContentDescription = "";
    private Utils signature = new Utils();

    private AccessibilityNodeInfo rootNodeInfo;

    private static final String WECHAT_DETAILS_EN = "Details";
    private static final String WECHAT_DETAILS_CH = "红包详情";
    private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "红包已失效";
    private static final String WECHAT_VIEW_SELF_CH = "查看红包";
    private static final String WECHAT_VIEW_OTHERS_CH = "领取红包";
    private final static String WECHAT_NOTIFICATION_TIP = "[微信红包]";
    private final static String WECHAT_LUCKMONEY_ACTIVITY = "luckymoney";

    private boolean mMutex = false;

    private int delayTime = 0;
    private List<String> ignoreFieldList = new ArrayList<>();
    private boolean isLookList = true;
    private boolean isLookNotification = true;
    private boolean isLookChat = true;
    private boolean isDefense = true;
    private long lastTime = 0;
    private int wechat_probability = 10;

    //  public static Map<String, Boolean> watchedFlags = new HashMap<>();

    /**
     * AccessibilityEvent的回调方法
     *
     * @param event 事件
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (ignoreFieldList == null) return;
        if (isDefense) {
            long thistime = System.currentTimeMillis();

            if (thistime - lastTime < 3000)
                return;
            else
                lastTime = thistime;
        }
        /* 检测通知消息 */
        if (!mMutex) {
            if (isLookNotification && watchNotifications(event)) return;
            if (isLookList && watchList(event)) return;
        }

        if (!isLookChat) return;

        this.rootNodeInfo = event.getSource();

        if (rootNodeInfo == null) return;

        mReceiveNode = null;
        mUnpackNode = null;

        checkNodeInfo(event);

        /* 如果已经接收到红包并且还没有戳开 */
        if (mLuckyMoneyReceived && !mLuckyMoneyPicked && (mReceiveNode != null)) {
            mMutex = true;


            AccessibilityNodeInfo cellNode = mReceiveNode;
            cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            mLuckyMoneyReceived = false;
            mLuckyMoneyPicked = true;
        }
        /* 如果戳开但还未领取 */
        if (mNeedUnpack && (mUnpackNode != null)) {
            AccessibilityNodeInfo cellNode = mUnpackNode;
            if (delayTime > 0) {
                try {
                    Thread.sleep(delayTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cellNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            mNeedUnpack = false;
        }


        if (mNeedBack) {
            performGlobalAction(GLOBAL_ACTION_BACK);
            mMutex = false;
            mNeedBack = false;
        }
    }

    private boolean watchList(AccessibilityEvent event) {
        // Not a message
        if ((event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) || event.getSource() == null)
            return false;

        List<AccessibilityNodeInfo> nodes = event.getSource().findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
        if (!nodes.isEmpty()) {
            CharSequence contentDescription;
            AccessibilityNodeInfo nodeToClick;
            synchronized (this) {
                nodeToClick = nodes.get(0);
                if (nodeToClick == null) return false;
                contentDescription = nodeToClick.getContentDescription();
                if (contentDescription != null)
                    if (!lastContentDescription.equals(contentDescription)) {
                        nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        lastContentDescription = contentDescription.toString();
                        return true;
                    }
            }
        }
        return false;
    }

    private boolean watchNotifications(AccessibilityEvent event) {
        // Not a notification
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
            return false;

        // Not a hongbao
        String tip = event.getText().toString();
        Log.e("tl", "tip" + tip);
        if (!tip.contains(WECHAT_NOTIFICATION_TIP)) return true;

        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            try {
                /* 清除signature,避免进入会话后误判*/
                signature.cleanSignature();

                notification.contentIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 检查节点信息
     */
    private void checkNodeInfo(AccessibilityEvent event) {
        if (this.rootNodeInfo == null) return;

        /* 聊天会话窗口，遍历节点匹配“领取红包”和"查看红包" */
        List<AccessibilityNodeInfo> nodes1 = this.findAccessibilityNodeInfosByTexts(this.rootNodeInfo, new String[]{
                WECHAT_VIEW_OTHERS_CH});

        if (!nodes1.isEmpty()) {
            AccessibilityNodeInfo targetNode = nodes1.get(nodes1.size() - 1);


            if (this.signature.generateSignature(targetNode, ignoreFieldList, wechat_probability)) {
                mLuckyMoneyReceived = true;
                mReceiveNode = targetNode;
            }
            return;
        }


        if (getCurrentActivity(event).contains(WECHAT_LUCKMONEY_ACTIVITY)) {
            Path path = new Path();
            path.moveTo(540, 1060);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(path, 450, 50)).build();
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d("tl", "onCompleted");
                    mMutex = false;
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    mMutex = false;
                    super.onCancelled(gestureDescription);
                }
            }, null);

        }
         /* 戳开红包，红包还没抢完，遍历节点匹配“拆红包”

        AccessibilityNodeInfo node2 = (this.rootNodeInfo.getChildCount() > 3 && this.rootNodeInfo.getChildCount() < 10) ? this.rootNodeInfo.getChild(3) : null;
        if (node2 != null && node2.getClassName().equals("android.widget.Button") && getCurrentActivity(event).contains(WECHAT_LUCKMONEY_ACTIVITY)) {
            mUnpackNode = node2;
            mNeedUnpack = true;
            return;
        }
 */
        /* 戳开红包，红包已被抢完，遍历节点匹配“红包详情”和“手慢了”
        if (mLuckyMoneyPicked) {
            List<AccessibilityNodeInfo> nodes3 = this.findAccessibilityNodeInfosByTexts(this.rootNodeInfo, new String[]{
                    WECHAT_BETTER_LUCK_CH, WECHAT_DETAILS_CH,
                    WECHAT_BETTER_LUCK_EN, WECHAT_DETAILS_EN, WECHAT_EXPIRES_CH});
            if (!nodes3.isEmpty()) {
                mNeedBack = true;
                mLuckyMoneyPicked = false;
            }
        }*/
    }


    /**
     * 批量化执行AccessibilityNodeInfo.findAccessibilityNodeInfosByText(text).
     * 由于这个操作影响性能,将所有需要匹配的文字一起处理,尽早返回
     *
     * @param nodeInfo 窗口根节点
     * @param texts    需要匹配的字符串们
     * @return 匹配到的节点数组
     */
    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String[] texts) {
        for (String text : texts) {
            if (text == null) continue;

            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);

            if (!nodes.isEmpty()) return nodes;
        }
        return new ArrayList<>();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        watchFlagsFromPreference();
    }

    private void watchFlagsFromPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        isDefense = sharedPreferences.getBoolean("defense", false);
        isLookChat = sharedPreferences.getBoolean("look_chat", true);
        isLookNotification = sharedPreferences.getBoolean("look_notification", true);
        isLookList = sharedPreferences.getBoolean("look_list", true);
        String strDelayTime = sharedPreferences.getString("delay_time", "0");
        delayTime = Integer.valueOf(strDelayTime);
        String strIgnore = sharedPreferences.getString("ignore_field", "");
        if (!strIgnore.equals("")) {
            ignoreFieldList = Arrays.asList(strIgnore.split(","));
        }
    }

    /**
     * 获取当前activity名称
     *  @param event
     *  @return
     *
     */
    private String getCurrentActivity(AccessibilityEvent event) {
        ComponentName componentName = new ComponentName(
                event.getPackageName().toString(),
                event.getClassName().toString()
        );

        try {
            getPackageManager().getActivityInfo(componentName, 0);
            return componentName.flattenToShortString();

        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "look_chat":
                isLookChat = sharedPreferences.getBoolean("look_chat", true);
                break;
            case "look_notification":
                isLookNotification = sharedPreferences.getBoolean("look_notification", true);
                break;
            case "look_list":
                isLookList = sharedPreferences.getBoolean("look_list", true);
                break;
            case "defense":
                isDefense = sharedPreferences.getBoolean("defense", false);
                break;
            case "delay_time":
                String strDelayTime = sharedPreferences.getString("delay_time", "0");
                delayTime = Integer.valueOf(strDelayTime);
                break;
            case "ignore_field":
                String strIgnore = sharedPreferences.getString("ignore_field", "");
                if (!strIgnore.equals("")) {
                    ignoreFieldList = Arrays.asList(strIgnore.split(","));
                }
                break;
            case "wechat_probability":
                String strProbability = sharedPreferences.getString("wechat_probability", "0");
                wechat_probability = Integer.valueOf(strProbability);
                break;
            default:
                break;
        }
    }

}

