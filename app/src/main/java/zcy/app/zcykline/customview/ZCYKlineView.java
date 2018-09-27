package zcy.app.zcykline.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import zcy.app.zcykline.R;
import zcy.app.zcykline.bean.Kline;
import zcy.app.zcykline.utils.DrawUtils;
import zcy.app.zcykline.utils.MyLog;

public class ZCYKlineView extends View {


    public static final int GRAPH_TYPE_NONE = 0;//隐藏主图指标线
    public static final int GRAPH_TYPE_MA = 1;//显示主图指标线 MA
    public static final int GRAPH_TYPE_BOLL = 2;//显示主图指标线 BOLL

    public static final int SUB_GRAPH_TYPE_NONE = 0;//隐藏副图
    public static final int SUB_GRAPH_TYPE_MACD = 1;//副图 macd
    public static final int SUB_GRAPH_TYPE_KDJ = 2;//副图 kdj
    public static final int SUB_GRAPH_TYPE_RSI = 3;//副图 rsi
    public static final int SUB_GRAPH_TYPE_WR = 4;//副图 wr

    private int riseColor;//涨 color
    private int fallColor;//跌 color
    private int bgLineColor;
    private float eth;//顶部指标高度
    private float ebh;//底部指标高度
    private int graphType;//主图类型(MA BOLL 隐藏)
    private int subGraphType;//副图类型(MACD KDJ RSI WR 隐藏)

    private static int SKN_MAX = 100;//显示最大数量
    private static int SKN_MIN = 30;//显示最小数量
    private int skn = SKN_MAX;//当前显示数量

    private int start;//开始的索引
    private int end;//结束的索引

    private boolean isFS;//是否是分时图

    private List<Kline> klines;

    public ZCYKlineView(Context context) {
        super(context);
        init(context,null);
    }

    public void init(int max , int min){
        skn = SKN_MAX = max;
        SKN_MIN = min;
        setKlines(klines);
    }

    public void setKlines(List<Kline> klines , boolean isFS){
        this.isFS = isFS;
        setKlines(klines);
    }

    public void setKlines(List<Kline> klines) {
        this.klines = klines;
        if (null != klines){
            end = klines.size()-1;
            if (klines.size() <= skn){
                start = 0;
            }else {
                start = end + 1 - skn;
            }
        }
        postInvalidate();
    }

    public ZCYKlineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context ,attrs);
    }

    public ZCYKlineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (null != attrs){
            TypedArray td = context.obtainStyledAttributes(attrs, R.styleable.ZCYKlineView);
            riseColor = td.getColor(R.styleable.ZCYKlineView_riseColor, getResources().getColor(R.color.default_rise_color));
            fallColor = td.getColor(R.styleable.ZCYKlineView_fallColor, getResources().getColor(R.color.default_fall_color));
            eth = td.getDimension(R.styleable.ZCYKlineView_exTopHeight, getResources().getDimension(R.dimen.default_ex_top_height));
            ebh = td.getDimension(R.styleable.ZCYKlineView_exBottomHeight, getResources().getDimension(R.dimen.default_ex_bottom_height));
            bgLineColor = td.getColor(R.styleable.ZCYKlineView_bgLineColor, getResources().getColor(R.color.kline_bg_line_color));
            td.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        DrawUtils.draw(canvas,getWidth(),getHeight(),graphType,subGraphType, klines , start,end , skn,eth , ebh ,bgLineColor ,riseColor , fallColor , isShowCoordinate , currX , isFS );
    }

    public void setSubGraphType(int subGraphType) {
        this.subGraphType = subGraphType;
        postInvalidate();
    }

    public void setGraphType(int graphType){
        this.graphType = graphType;
        postInvalidate();
    }


    private boolean isShowCoordinate;
    private long downTime;//单点按下时间
    private float downX;//单点按下X坐标
    private float downY;//单点按下Y坐标

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isShowCoordinate || 2 == event.getPointerCount()){
            //显示指示器,或者为双点触控的时候，不允许上层控件拦截触摸事件
            if (MotionEvent.ACTION_DOWN == event.getAction())
                getParent().requestDisallowInterceptTouchEvent(true);
        }else {
            if (1 == event.getPointerCount()){
                //按下之后一百毫秒内决定是否能被上层控件拦截（主要是为了解决ScrollView里面使用时触摸冲突）
                if (System.currentTimeMillis() - downTime < 100L){
                    getParent().requestDisallowInterceptTouchEvent(Math.abs(event.getX() - downX) >= Math.abs(event.getY() - downY));
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private float currX;//当前X坐标
    private float currY;//当前Y坐标

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (1 == event.getPointerCount()){
            if (MotionEvent.ACTION_DOWN == event.getAction()){
                //每一次单点按下都记录按下时间
                downTime = System.currentTimeMillis();
                currX = downX = event.getX();
                currY = downY = event.getY();
            }

            if (MotionEvent.ACTION_UP == event.getAction() && System.currentTimeMillis() - downTime < 100L && Math.abs(event.getX() - downX) < 30 && Math.abs(event.getY() - downY) < 30 ){
                //记一次点击事件，改变指示器显示状态
                isShowCoordinate = !isShowCoordinate;
            }

            if (MotionEvent.ACTION_MOVE == event.getAction()){
                if (!isShowCoordinate && 0L != downTime)
                translationX(event.getX() - currX);
            }
            currX = event.getX();
            currY = event.getY();
            postInvalidate();
        }else if (2 == event.getPointerCount()){
            if ( (event.getAction()& MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN){
                downTime = 0L;
                startLen = getLen((int) event.getX(0),(int) event.getY(0),(int) event.getX(1),(int) event.getY(1));
            }else if ((event.getAction()& MotionEvent.ACTION_MASK)== MotionEvent.ACTION_POINTER_UP){
                //抬起暂无动作
            }else {
                int newLen = getLen((int) event.getX(0), (int) event.getY(0), (int) event.getX(1), (int) event.getY(1));
                changeLen(newLen - startLen);
                startLen = newLen;
            }
        }
        return true;
    }

    /**
     * ---------- 单点触摸相关 -------------
     */

    private float txAll;

    private void translationX(float tx){
        if (null == klines)return;
        txAll += tx;
        calcTx(getWidth() / skn);
        postInvalidate();
    }

    private void calcTx(float hl) {
        if (Math.abs(txAll) >= hl){
            if (txAll > 0){
                if (start <= 0){
                    txAll = 0f;
                    return;
                }
                start--;
                end--;
                txAll -= hl;
            }else {
                if (end >= klines.size()-1){
                    txAll = 0f;
                    return;
                }
                start++;
                end++;
                txAll += hl;
            }
            if (0 != txAll)calcTx(hl);
        }
    }

    /**
     * ------------ 双点触控相关 --------------
     */
    private int startLen;
    private int clAll;

    public int getLen(int x1 , int y1 , int x2 , int y2){
        return (int) Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2) * y1 - y2);
    }

    private void changeLen(int cl) {
        if (null == klines)return;
        clAll += cl;
        calcCL();
        postInvalidate();
    }

    private void calcCL() {
        int hl = getWidth() / skn;
        if (Math.abs(clAll) >= hl){
            if (clAll > 0){
                //放大
                if (skn <= SKN_MIN){
                    clAll = 0;
                    return;
                }
                skn--;
                if (end - start > skn)end--;
                clAll -= hl;
            }else {
                //缩小
                if (skn >= SKN_MAX){
                    clAll = 0;
                    return;
                }
                skn++;
                if (end < klines.size()-1)end++;
                else {
                    if (start > 0 )start--;
                }
                clAll += hl;
            }
            if (0 != clAll)calcCL();
        }

    }

}
