package zcy.app.zcykline.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import zcy.app.zcykline.R;
import zcy.app.zcykline.customview.ZCYKlineView;
import zcy.app.zcykline.utils.DrawUtils;
import zcy.app.zcykline.utils.ReadDataUtils;

public class KlineLandscapeActivity extends BaseActivity implements View.OnClickListener{

    private TextView titleTv;
    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private ZCYKlineView klineView;
    private TabLayout tabLayout;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_kline_landscape;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomUIMenu();

        klineView = findViewById(R.id.kline_view);

        initTitle();
        initRG1();
        initRG2();
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
                ReadDataUtils.loadKline(ReadDataUtils.ids[tab.getPosition()],klineView, KlineLandscapeActivity.this);
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

    private void initTitle() {
        findViewById(R.id.title_right_layout).setOnClickListener(this);
        titleTv = findViewById(R.id.title);
        titleTv.setText(getIntent().getStringExtra("title"));
    }

    private void initRG2() {
        findViewById(R.id.radio_button_rsi).setEnabled(false);
        findViewById(R.id.radio_button_wr).setEnabled(false);
        radioGroup2 = findViewById(R.id.radio_group2);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_button_macd:
                        klineView.setSubGraphType( ZCYKlineView.SUB_GRAPH_TYPE_MACD);
                        break;
                    case R.id.radio_button_kdj:
                        klineView.setSubGraphType( ZCYKlineView.SUB_GRAPH_TYPE_KDJ);
                        break;
                    default:
                        klineView.setSubGraphType( ZCYKlineView.SUB_GRAPH_TYPE_NONE);
                        break;
                }
            }
        });
        radioGroup2.check(R.id.radio_button_hide2);
    }

    private void initRG1() {
        findViewById(R.id.radio_button_boll).setEnabled(false);
        radioGroup1 = findViewById(R.id.radio_group1);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_button_ma:
                        klineView.setGraphType( ZCYKlineView.GRAPH_TYPE_MA);
                        break;
                    default:
                        klineView.setGraphType( ZCYKlineView.GRAPH_TYPE_NONE);
                        break;
                }
            }
        });
        radioGroup1.check(R.id.radio_button_ma);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_right_layout:
                finish();
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (MotionEvent.ACTION_DOWN == ev.getAction())hideBottomUIMenu();
        return super.dispatchTouchEvent(ev);
    }

    public void hideBottomNavigation(){
        runOnUiThread(r);
    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            hideBottomUIMenu();
        }
    };

}
