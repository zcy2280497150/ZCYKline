package zcy.app.zcykline.utils;

import android.util.Log;

/**
 * 自定义的日志输出类，添加开关
 * Created by zcy on 2017/7/5.
 */
public class MyLog {

    private static String TAG = "xxx";
    private static boolean flag;

    public static void open(boolean flag){
        MyLog.flag = flag;
    }

    public static void v(String msg){
        v(TAG, msg);
    }
    public static void v(String tag , String msg){
        if (flag && null != msg) Log.v(tag, msg);
    }

    public static void d(String msg){
        d(TAG, msg);
    }
    public static void d(String tag , String msg){
        if (flag && null != msg) Log.d(tag, msg);
    }

    public static void i(String msg){
        i(TAG, msg);
    }
    public static void i(String tag , String msg){
        if (flag && null != msg) Log.i(tag, msg);
    }

    public static void w(String msg){
        w(TAG, msg);
    }
    public static void w(String tag , String msg){
        if (flag && null != msg) Log.w(tag, msg);
    }

    public static void e(String msg){
        e(TAG, msg);
    }
    public static void e(String tag , String msg){
        if (flag && null != msg) Log.e(tag, msg);
    }
}
