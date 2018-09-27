package zcy.app.zcykline.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import zcy.app.zcykline.R;
import zcy.app.zcykline.customview.ZCYKlineView;
import zcy.app.zcykline.utils.DrawUtils;
import zcy.app.zcykline.utils.ReadDataUtils;

public class KlineActivity extends BaseActivity implements View.OnClickListener{

    private String title;
    private TabLayout tabLayout;
    private ZCYKlineView klineView;
    private TextView btnFT;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_kline;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        title = getIntent().getStringExtra("title");
        title = "BTC/USDT";//这里标题直接写死了
        klineView = findViewById(R.id.kline_view);
        klineView.setGraphType(ZCYKlineView.GRAPH_TYPE_MA);
        ((TextView)findViewById(R.id.title_txt)).setText(title);
        findViewById(R.id.btn_back_layout).setOnClickListener(this);
        findViewById(R.id.btn_enlarge).setOnClickListener(this);
        findViewById(R.id.btn_ft).setOnClickListener(this);
        btnFT = findViewById(R.id.btn_ft);

        initTabLayout();
    }

    private void initTabLayout() {
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setSelectedTabIndicatorHeight(0);
        ViewCompat.setElevation(tabLayout, 10);
        for (int i = 0 ; i < ReadDataUtils.tabNames.length ; i++){
            tabLayout.addTab(tabLayout.newTab());
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (null != tab){
                tab.setCustomView(R.layout.item_tab_xxx);
                ((TextView)tab.getCustomView().findViewById(R.id.tab_item_tv)).setText(ReadDataUtils.tabNames[i]);
            }
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ReadDataUtils.loadKline(ReadDataUtils.ids[tab.getPosition()],klineView, KlineActivity.this);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

//        tabLayout.getTabAt(2).getCustomView().setSelected(true);
//        tabLayout.getTabAt(0).getCustomView().setSelected(false);
        ReadDataUtils.loadKline(ReadDataUtils.ids[0],klineView,this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back_layout://返回
                finish();
                break;
            case R.id.btn_enlarge://全屏
                enlarge();
                break;
            case R.id.btn_ft://选择副图
                showAlertDialog();
                break;
        }
    }

    private String[] strs = {"隐藏","MACD","KDJ"};
    private int[] types = { ZCYKlineView.SUB_GRAPH_TYPE_NONE ,  ZCYKlineView.SUB_GRAPH_TYPE_MACD ,  ZCYKlineView.SUB_GRAPH_TYPE_KDJ};
    private int select;

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("选择副图类型")
                .setSingleChoiceItems(strs, select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btnFT.setText(strs[select = which]);
                        klineView.setSubGraphType(types[select]);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void enlarge() {
        Intent intent = new Intent(this, KlineLandscapeActivity.class);
        intent.putExtra("title" ,title);
        startActivity(intent);
    }
}
