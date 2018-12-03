package com.asmdemo;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author chenjun
 * create at 2018/11/16
 */
public class ToastUtils {

    public static void showToast(View v){
        Toast.makeText(v.getContext(), ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
    }
}
