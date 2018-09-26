package zcy.app.zcykline.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.List;

import zcy.app.zcykline.R;
import zcy.app.zcykline.bean.Kline;
import zcy.app.zcykline.customview.ZCYKlineView;
import zcy.app.zcykline.utils.KlineUtils;
import zcy.app.zcykline.utils.MyLog;
import zcy.app.zcykline.utils.ReadDataUtils;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.btn_btc_usdt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_btc_usdt:
                startActivity(new Intent(this , KlineActivity.class));
                break;
        }
    }

}
