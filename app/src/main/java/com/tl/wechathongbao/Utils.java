package com.tl.wechathongbao;

import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Random;

/**
 * Created by tttony3 on 16-1-29.
 */
public class Utils {
    Random random = new Random(10);
    private String sender="", content="", time="";

    public boolean generateSignature(AccessibilityNodeInfo node, List<String> ignoreFieldList, int wechat_probability) {
        try {
            AccessibilityNodeInfo hongbaoNode = node.getParent();
            if (hongbaoNode.getChild(0) == null)
                return false;
            CharSequence cs = hongbaoNode.getChild(0).getText();

            if (cs == null) return false;
            String hongbaoContent = cs.toString();
            if(!ignoreFieldList.isEmpty()){
                for(String str :ignoreFieldList){
                    if(hongbaoContent.contains(str)){
                        return false;
                    }
                }
            }
            AccessibilityNodeInfo messageNode = hongbaoNode.getParent();
            if (null == messageNode)
                return false;
            String[] hongbaoInfo = getSenderContentDescriptionFromNode(messageNode);
             String signature = getSignature(hongbaoInfo[0], hongbaoContent, hongbaoInfo[1]);
            if(signature.equals("")) return false;
            if (signature.equals(this.toString())) return false;

            this.sender = hongbaoInfo[0];
            this.time = hongbaoInfo[1];
            this.content = hongbaoContent;
            return random.nextInt() <= wechat_probability;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getSignature(this.sender, this.content, this.time);
    }
    @NonNull
    private String getSignature(String... strings) {
        String signature = "";
        for (String str : strings) {
            if (str == null) return "";
            signature += str + "|";
        }

        return signature.substring(0, signature.length() - 1);
    }

    @NonNull
    private String[] getSenderContentDescriptionFromNode(AccessibilityNodeInfo node) {
        int count = node.getChildCount();
        String[] result = {"unknownSender", "unknownTime"};
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo thisNode = node.getChild(i);
            if ("android.widget.ImageView".equals(thisNode.getClassName())) {
                CharSequence contentDescription = thisNode.getContentDescription();
                if (contentDescription != null) result[0] = contentDescription.toString();
            } else if ("android.widget.TextView".equals(thisNode.getClassName())) {
                CharSequence thisNodeText = thisNode.getText();
                if (thisNodeText != null) result[1] = thisNodeText.toString();
            }
        }
        return result;
    }

    public void cleanSignature(){
        this.content = "";
        this.time = "";
        this.sender ="";
    }

}