package zcy.app.zcykline.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.RawRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import zcy.app.zcykline.R;
import zcy.app.zcykline.activity.KlineLandscapeActivity;
import zcy.app.zcykline.bean.Kline;
import zcy.app.zcykline.customview.ZCYKlineView;
import zcy.app.zcykline.dialog.BaseDialogFragment;
import zcy.app.zcykline.dialog.BaseDialogViewProvider;

public class ReadDataUtils {


    public static String[] tabNames = {"分时","1分钟" ,"5分钟","15分钟","30分钟","60分钟","1日","1周","1月"};
    public static int[] ids = {R.raw.btc_usdt_fs_1000,R.raw.btc_usdt_1min_1000,R.raw.btc_usdt_5min_1000
            ,R.raw.btc_usdt_15min_1000,R.raw.btc_usdt_30min_1000,R.raw.btc_usdt_60min_1000
            ,R.raw.btc_usdt_1day_1000,R.raw.btc_usdt_1week_1000,R.raw.btc_usdt_1mon_1000,};

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void init(Application context){
        ReadDataUtils.context = context;
        sparseArray = new SparseArray<>();
    }

    private static String readText(@RawRes int id){
        InputStream inputStream = context.getResources().openRawResource(id);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String str;
        StringBuilder sb = new StringBuilder();

        try {
            while (null != (str = reader.readLine())){
                sb.append(str);
            }
            reader.close();
            inputStream.close();
            return sb.toString();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private static SparseArray<List<Kline>> sparseArray;

    public static void loadKline(@RawRes final int id , final ZCYKlineView klineView , final AppCompatActivity activity){
        List<Kline> list = sparseArray.get(id);
        if (null == list){
            if (null != activity){
                if (null == dialogFragment){
                    dialogFragment = new BaseDialogFragment()
                            .setGravity(Gravity.CENTER)
                            .setCancel(false)
                            .setViewProvider(new BaseDialogViewProvider(R.layout.dialog_loading) {
                                @Override
                                public void initView(DialogFragment dialog, View view) {
                                    ((TextView)view.findViewById(R.id.text_view_loading)).setText("解析数据...");
                                }
                            });
                }
                dialogFragment.show(activity.getSupportFragmentManager(),null);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sparseArray.put(id,KlineUtils.getKline(JSON.parseObject(readText(id)).getString("data")));
                    klineView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != activity && null != dialogFragment)dialogFragment.dismiss();
                            if (null != activity && activity instanceof KlineLandscapeActivity)((KlineLandscapeActivity) activity).hideBottomNavigation();
                            klineView.setKlines(sparseArray.get(id),id == R.raw.btc_usdt_fs_1000);
                        }
                    });
                }
            }).start();
        }else {
            klineView.setKlines(sparseArray.get(id),id == R.raw.btc_usdt_fs_1000);
        }
    }

    private static BaseDialogFragment dialogFragment;

}
