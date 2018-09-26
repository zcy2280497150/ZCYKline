package zcy.app.zcykline.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


/**
 * 自定义Toast
 * Created by zhangchengyan on 2017/6/5.
 */
public class MyToast {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void init(Application context) {
        MyToast.context = context;
    }

    //开关
    private static boolean flag = true;
    //Toast对象
    private static Toast mToast;

    //自定义时间
    @SuppressLint("ShowToast")
    private static void showToast(String text, int length) {
        if (null != context && flag) {
            if (mToast != null)
                mToast.setText(text);
            else {
                mToast = Toast.makeText(context, text, length);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            }
            mToast.show();
        }
    }

    //自定义时间和XML里面的文字Toast
    public static void showToast(int resId, int length) {
        if (null != context) showToast(context.getResources().getString(resId), length);
    }

    //短时间的Toast
    public static void makeTextShort(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    //长时间的Toast
    public static void makeTextLong(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }
}
