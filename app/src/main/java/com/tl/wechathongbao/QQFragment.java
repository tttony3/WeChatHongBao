package com.tl.wechathongbao;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tony on 2016/1/30.
 */
public class QQFragment extends Fragment {
    View view;
    Context baseContext;
    SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseContext = getActivity();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
        view = inflater.inflate(R.layout.fragment_qq, container, false);
        return view;
    }
}
