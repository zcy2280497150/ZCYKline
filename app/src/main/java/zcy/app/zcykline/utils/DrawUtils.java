package zcy.app.zcykline.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import zcy.app.zcykline.APP;
import zcy.app.zcykline.bean.Kline;
import zcy.app.zcykline.customview.ZCYKlineView;

public class DrawUtils {


    private static String MA5_COLOR = "#FFFFFF";//
    private static String VMA5_COLOR = "#E4E45E";//
    private static String MA10_COLOR = "#E4E45E";//
    private static String VMA10_COLOR = "#FE01EF";//
    private static String MA30_COLOR = "#FE01EF";//
    private static String MACD_DIF_LINE_COLOR = "#E4E45E";//
    private static String MACD_DEA_LINE_COLOR = "#FE01EF";//
    private static String KDJ_LINE_K_COLOR = "#FFFFFF";//
    private static String KDJ_LINE_D_COLOR = "#E4E45E";//
    private static String KDJ_LINE_J_COLOR = "#FE01EF";//

    private static float DENSITY;
    private static RectF rectF1;
    private static RectF rectF2;
    private static Paint p;
    private static Path path1;
    private static Path path2;
    private static Path path3;

    public static void init(APP app){
        DENSITY = app.getResources().getDisplayMetrics().density;
        rectF1 = new RectF();
        rectF2 = new RectF();
        p = new Paint();
        path1 = new Path();
        path2 = new Path();
        path3 = new Path();
    }

    /**
     *
     * @param width 视图宽
     * @param height 视图高
     * @param graphType 主图指标类型
     * @param subGraphType 副图类型
     * @param klines 数据源
     * @param start 起始数据索引
     * @param end 结束数据索引
     * @param skn 屏幕当前显示K线数据个数
     * @param eth 顶部指标高度
     * @param ebh 底部指标高度
     */
    public static void draw(Canvas canvas , float width , float height ,int graphType , int subGraphType , List<Kline> klines , int start , int end , int skn , float eth , float ebh,int bgLineColor,int riseColor,int fallColor,boolean isShowCoordinate , float currX , boolean isFS){
        float hl = rectF1.width() / skn;//水平方向单个距离
        rectF1.set(0f,eth , width , height - ebh);
        drawBg(canvas, rectF1, ZCYKlineView.SUB_GRAPH_TYPE_NONE == subGraphType ? 5 : 6 ,4 ,bgLineColor);
        rectF1.set(0f,height - ebh , width , height);
        drawBottomTimes(canvas , rectF1, klines , start ,end , skn , 4);
        float vl = (height - eth - ebh) / ( ZCYKlineView.SUB_GRAPH_TYPE_NONE == subGraphType ? 5 : 6);//垂直方向单份长度
        if (null != klines){
            //得到最大值和最小值的索引
            int maxIndex = start;
            int minIndex = start;
            Kline kmax = klines.get(start);
            Kline kmin = klines.get(start);
            Kline vKmax = klines.get(start);
            for (int i = start+1 ; i <= end ; i++){
                Kline k2 = klines.get(i);
                if (k2.getHigh() > kmax.getHigh()){
                    maxIndex = i;
                    kmax = k2;
                }
                if (k2.getLow() < kmin.getLow()){
                    minIndex = i;
                    kmin = k2;
                }
                if (k2.getVol() > vKmax.getVol()){
                    vKmax = k2;
                }
            }

            //需要显示指示器，先计算出指示器所在数据源
            int index = !isShowCoordinate ? end : Math.min((int) (currX / hl) + start , end);

            //MA（均线）指标
            rectF1.set(0f,0f,width,eth);
            drawTopMa(canvas , rectF1, klines.get(index));
            //主图
            rectF1.set(0f,eth , width,eth + vl * 4f);
            drawCandles(canvas , rectF1, klines ,graphType, start , end , skn , maxIndex , minIndex , kmax , kmin ,riseColor,fallColor,index,isShowCoordinate,isFS);
            //Vol MA 均线指标
            rectF1.set(0f,eth + vl * 4f ,width , eth + vl * 4f + DENSITY * 15f);
            drawVMa(canvas, rectF1,klines.get(index));
            //vol 柱状图形
            rectF1.set(0f,eth + vl * 4f + DENSITY * 15f ,width , eth + vl * 5f);
            drawVolGraph(canvas , rectF1, klines , start ,end ,skn ,vKmax,riseColor , fallColor);

            if ( ZCYKlineView.SUB_GRAPH_TYPE_MACD == subGraphType){
                rectF1.set(0f,eth + vl * 5f, width ,eth + vl * 5f + DENSITY *15f);
                drawSubGraphMACDTitle(canvas , rectF1, klines.get(index));
                //副图MACD
                rectF1.set(0f,eth + vl * 5f + DENSITY *15f , width , height - ebh);
                drawSubGraphMACD(canvas , rectF1, klines , start , end , skn , riseColor , fallColor);
            }if ( ZCYKlineView.SUB_GRAPH_TYPE_KDJ == subGraphType){
                rectF1.set(0f,eth + vl * 5f, width ,eth + vl * 5f + DENSITY *15f);
                drawSubGraphKDJTitle(canvas , rectF1, klines.get(index));
                //副图KDJ
                rectF1.set(0f,eth + vl * 5f + DENSITY *15f , width , height - ebh);
                drawSubGraphKDJ(canvas , rectF1, klines , start , end ,skn , bgLineColor);
            }if ( ZCYKlineView.SUB_GRAPH_TYPE_RSI == subGraphType){
                // TODO: 2018/9/6 RSI
            }if ( ZCYKlineView.SUB_GRAPH_TYPE_WR == subGraphType){
                // TODO: 2018/9/6 WR
            }

            if (isShowCoordinate){
                float x = (index - start + 0.5f) * hl;
                Kline kline = klines.get(index);
                p.reset();
                p.setAntiAlias(true);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(0);
                p.setTextSize(DENSITY *8);
                p.setColor(Color.WHITE);
                canvas.drawLine(x , 0 , x , height,p);
                String time = DateFormatUtils.formatTime(kline.getId(), DateFormatUtils.FORMAT_TYPE1);
                float timeTextLen = p.measureText(time);

                if (x < (timeTextLen/2f + DENSITY * 5f) ){
                    rectF2.set(1,height - ebh +1 , timeTextLen + DENSITY * 10f ,height-1);
                }else if (x > width - (timeTextLen/2f + DENSITY * 5)){
                    rectF2.set(width - DENSITY * 10f - timeTextLen,height - ebh +1 ,width - 1,height-1);
                }else {
                    rectF2.set(x - timeTextLen/2f - DENSITY * 5f ,height - ebh +1  ,x + timeTextLen/2f + DENSITY * 5f , height-1);
                }

                p.setColor(Color.parseColor("#071724"));
                p.setStyle(Paint.Style.FILL);
                canvas.drawRect(rectF2 , p);
                p.setColor(Color.WHITE);
                p.setStyle(Paint.Style.STROKE);
                canvas.drawRect(rectF2 , p);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(time,(rectF2.left + rectF2.right)/2f , (rectF2.top + rectF2.bottom)/2f + DENSITY * 3 , p);

            }

        }
    }

    private static void drawSubGraphKDJTitle(Canvas canvas, RectF rectF, Kline kline) {
        if (null != kline){
            p.reset();
            p.setTextSize(DENSITY * 7f);
            p.setAntiAlias(true);

            String kdjName = "KDJ("+KlineUtils.N+","+KlineUtils.D+","+KlineUtils.K+")";
            String kStr = "K:"+StringUtils.formatDouble(kline.getK(),2);
            String dStr = "D:"+StringUtils.formatDouble(kline.getD(),2);
            String jStr = "J:"+StringUtils.formatDouble(kline.getJ(),2);

            float y = (rectF.top + rectF.bottom) / 2 + DENSITY * 3;

            p.setColor(Color.parseColor("#6D87A8"));
            canvas.drawText(kdjName , rectF.left + DENSITY * 10 ,y,p);
            p.setColor(Color.parseColor(MA5_COLOR));
            canvas.drawText(kStr , rectF.left + DENSITY * 20 + p.measureText(kdjName) ,y,p);
            p.setColor(Color.parseColor(MA10_COLOR));
            canvas.drawText(dStr , rectF.left + DENSITY * 30 + p.measureText(kdjName)+ p.measureText(kStr) , y, p );
            p.setColor(Color.parseColor(MA30_COLOR));
            canvas.drawText(jStr , rectF.left + DENSITY * 40 + p.measureText(kdjName)+ p.measureText(kStr)+p.measureText(dStr) , y, p );

        }
    }

    private static void drawSubGraphKDJ(Canvas canvas, RectF rectF, List<Kline> klines, int start, int end, int skn , int bgLineColor) {
        if (null == klines)return;
        double kdj_max = 0D;
        double kdj_min = 0D;
        float hl = (rectF.right - rectF.left) / skn;//横向每条数据所占宽度

        for (int i = start ; i <= end ; i++ ){
            if (i < 26)continue;
            Kline kline = klines.get(i);
            kdj_max = Math.max(kline.getK() , kdj_max);
            kdj_max = Math.max(kline.getD() , kdj_max);
            kdj_max = Math.max(kline.getJ() , kdj_max);

            kdj_min = Math.min(kline.getK() , kdj_min);
            kdj_min = Math.min(kline.getD() , kdj_min);
            kdj_min = Math.min(kline.getJ() , kdj_min);
        }

        if (0D == kdj_max && 0D == kdj_min)return;
        double vl = (rectF.bottom - rectF.top) / (kdj_max - kdj_min);

        p.reset();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0);
        p.setTextSize(DENSITY * 8);
        p.setAntiAlias(true);
        p.setColor(bgLineColor);
        canvas.drawLine(rectF.left , (rectF.top + rectF.bottom)/2 , rectF.right , (rectF.top + rectF.bottom)/2 , p);
        p.setTextAlign(Paint.Align.RIGHT);
        p.setColor(Color.parseColor("#6D87A8"));
        canvas.drawText(StringUtils.formatDouble(kdj_max,2) , rectF.right - DENSITY * 5 , rectF.top,p);
        canvas.drawText(StringUtils.formatDouble(kdj_min,2) , rectF.right - DENSITY * 5 , rectF.bottom,p);
        canvas.drawText(StringUtils.formatDouble((kdj_max + kdj_min)/2,2) , rectF.right - DENSITY * 5 , (rectF.top + rectF.bottom)/2,p);

        path1.reset();
        path2.reset();
        path3.reset();
        for (int i = start ; i <= end ; i++ ){
            Kline kline = klines.get(i);
            if (i > KlineUtils.N && start != i){
                path1.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getK() - kdj_min) * vl));
            }else {
                path1.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getK() - kdj_min) * vl));
            }
            if (i > KlineUtils.N + 2 && start != i){
                path2.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getD() - kdj_min) * vl));
                path3.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getJ() - kdj_min) * vl));
            }else {
                path2.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getD() - kdj_min) * vl));
                path3.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getJ() - kdj_min) * vl));
            }
        }

        p.reset();
        p.setAntiAlias(true);
        p.setStrokeWidth(0);
        p.setStyle(Paint.Style.STROKE);

        p.setColor(Color.parseColor(KDJ_LINE_K_COLOR));
        canvas.drawPath(path1,p);
        p.setColor(Color.parseColor(KDJ_LINE_D_COLOR));
        canvas.drawPath(path2,p);
        p.setColor(Color.parseColor(KDJ_LINE_J_COLOR));
        canvas.drawPath(path3,p);

    }

    private static void drawSubGraphMACDTitle(Canvas canvas, RectF rectF, Kline kline) {
        if (null != kline){
            p.reset();
            p.setTextSize(DENSITY * 7f);
            p.setAntiAlias(true);

            String macdName = "MACD(12,26,9)";
            String macdStr = "MACD:"+StringUtils.formatDouble((kline.getDIF() - kline.getDEA())*2,2);
            String difStr = "DIF:"+StringUtils.formatDouble(kline.getDIF(),2);
            String deaStr = "DEA:"+StringUtils.formatDouble(kline.getDEA(),2);

            float y = (rectF.top + rectF.bottom) / 2 + DENSITY * 3;

            p.setColor(Color.parseColor("#6D87A8"));
            canvas.drawText(macdName , rectF.left + DENSITY * 10 ,y,p);
            p.setColor(Color.parseColor(MA5_COLOR));
            canvas.drawText(macdStr , rectF.left + DENSITY * 20 + p.measureText(macdName) ,y,p);
            p.setColor(Color.parseColor(MA10_COLOR));
            canvas.drawText(difStr , rectF.left + DENSITY * 30 + p.measureText(macdName)+ p.measureText(macdStr) , y, p );
            p.setColor(Color.parseColor(MA30_COLOR));
            canvas.drawText(deaStr , rectF.left + DENSITY * 40 + p.measureText(macdName)+ p.measureText(macdStr)+p.measureText(difStr) , y, p );

        }
    }

    private static void drawSubGraphMACD(Canvas canvas, RectF rectF, List<Kline> klines, int start, int end, int skn, int riseColor, int fallColor) {
        if (null == klines)return;
        double macd_max = 0D;
        double macd_min = 0D;
        float hl = (rectF.right - rectF.left) / skn;//横向每条数据所占宽度

        for (int i = start ; i <= end ; i++ ){
            if (i < 26)continue;
            Kline kline = klines.get(i);
            macd_max = Math.max(kline.getDIF() , macd_max);
            macd_max = Math.max(kline.getDEA() , macd_max);
            macd_max = Math.max((kline.getDIF()-kline.getDEA())*2 , macd_max);

            macd_min = Math.min(kline.getDIF() , macd_min);
            macd_min = Math.min(kline.getDEA() , macd_min);
            macd_min = Math.min((kline.getDIF()-kline.getDEA())*2 , macd_min);
        }

        if (0D == macd_max && 0D == macd_min)return;
        double vl = (rectF.bottom - rectF.top) / (macd_max - macd_min);

        //右边数字指标
        p.reset();
        p.setColor(Color.WHITE);
        p.setTextSize(DENSITY*8f);
        p.setAntiAlias(true);
        p.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("-"+StringUtils.formatDouble(-macd_min,10,StringUtils.FORMAT_TYPE1),rectF.right,rectF.bottom ,p);
        canvas.drawText("0",rectF.right , (float) (rectF.bottom + macd_min * vl),p);
        canvas.drawText(StringUtils.formatDouble(macd_max,10,StringUtils.FORMAT_TYPE1),rectF.right ,rectF.top,p);

        //绘制柱状图
        path1.reset();
        path2.reset();
        p.reset();
        p.setStrokeWidth(DENSITY);
        p.setStrokeCap(Paint.Cap.BUTT);
        for (int i = start ; i <= end ; i++) {
            Kline kline = klines.get(i);
            if (i >= 34){
                double macd = (kline.getDIF() - kline.getDEA())*2;
                p.setColor(macd >= 0 ? riseColor : fallColor);
                canvas.drawLine((i-start + 0.5f)*hl , (float) (rectF.bottom + macd_min * vl) ,(i-start + 0.5f)*hl , (float) (rectF.bottom - (macd - macd_min) * vl) ,p);
            }

            //DIF 数据填充，26日开始
            if (i > 26 && start != i){
                path1.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getDIF() - macd_min) * vl));
            }else {
                path1.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getDIF() - macd_min) * vl));
            }

            //DEA 数据填充，34日开始
            if (i > 34 && start != i){
                path2.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getDEA() - macd_min) * vl));
            }else {
                path2.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getDEA() - macd_min) * vl));
            }

        }

        //绘制 DIF 和 DEA
        p.setStyle(Paint.Style.STROKE);
        p.setAntiAlias(true);
        p.setStrokeWidth(0);
        p.setColor(Color.parseColor(MACD_DIF_LINE_COLOR));
        canvas.drawPath(path1 , p);
        p.setColor(Color.parseColor(MACD_DEA_LINE_COLOR));
        canvas.drawPath(path2 , p);

    }

    private static void drawVolGraph(Canvas canvas, RectF rectF, List<Kline> klines, int start, int end, int skn, Kline vKmax , int riseColor , int fallColor) {
        if (null != klines && null != vKmax){
            double vl = (rectF.bottom - rectF.top) / vKmax.getVol();
            float hl = (rectF.right - rectF.left) / skn;//横向每条数据所占宽度
            p.reset();
            p.setTextSize(DENSITY * 10);
            p.setAntiAlias(true);
            p.setTextAlign(Paint.Align.RIGHT);
            p.setColor(Color.parseColor("#6D87A8"));
            canvas.drawText(StringUtils.formatDouble(vKmax.getVol() , 2) , rectF.right - DENSITY * 5 , rectF.top - DENSITY * 5,p);

            p.setStrokeWidth(hl - Math.max(hl*0.1f,2f));
            p.setStrokeCap(Paint.Cap.BUTT);

            //这里就直接使用MA5和MA10的path
            path1.reset();
            path2.reset();

            for (int i = start ; i <= end ; i++ ){
                Kline kline = klines.get(i);
                if (kline.getOpen() == kline.getClose()){
                    p.setColor(i > 0 && kline.getClose() > klines.get(i - 1).getClose() ?  riseColor: fallColor);
                }else {
                    p.setColor(kline.getOpen() < kline.getClose() ? riseColor : fallColor);
                }
                canvas.drawLine((i-start + 0.5f)*hl , rectF.bottom , (i-start + 0.5f)*hl ,  Math.max ((float)(rectF.bottom - vl * kline.getVol()),1f),p);

                //5日均线path数据填充
                if (i > 5 && start != i){
                    path1.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - kline.getVma5()*vl));
                }else {
                    path1.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - kline.getVma5()*vl));
                }
                //10日均线path初始化
                if (i > 10 && start != i){
                    path2.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - kline.getVma10()*vl));
                }else {
                    path2.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - kline.getVma10()*vl));
                }
            }

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0);
            p.setColor(Color.parseColor(VMA5_COLOR));
            canvas.drawPath(path1,p);
            p.setColor(Color.parseColor(VMA10_COLOR));
            canvas.drawPath(path2,p);
        }
    }

    private static void drawVMa(Canvas canvas, RectF rectF, Kline kline) {
        if (null != kline){
            p.reset();
            p.setTextSize(DENSITY * 8);
            p.setAntiAlias(true);

            double vol = kline.getVol();
            double vma5 = kline.getVma5();
            double vma10 = kline.getVma10();
            String volStr = "VOL:" + StringUtils.formatDoubleLen(vol, 2, 12);
            String vma5Str = "MA5:" + StringUtils.formatDoubleLen(vma5, 2, 12);
            String vma10Str = "MA10:" + StringUtils.formatDoubleLen(vma10, 2, 12);
            float y = (rectF.top + rectF.bottom) / 2 + DENSITY * 3;

            p.setColor(Color.WHITE);
            canvas.drawText(volStr , rectF.left + DENSITY * 10 ,y,p);
            p.setColor(Color.parseColor(VMA5_COLOR));
            canvas.drawText(vma5Str , rectF.left + DENSITY * 20 + p.measureText(volStr) , y, p );
            p.setColor(Color.parseColor(VMA10_COLOR));
            canvas.drawText(vma10Str , rectF.left + DENSITY * 30 + p.measureText(volStr)+ p.measureText(vma5Str) , y, p );

        }
    }

    private static void drawTopMa(Canvas canvas, RectF rectF, Kline kline) {
        if (null != kline){
            p.reset();
            p.setTextSize(DENSITY * 8);
            p.setAntiAlias(true);

            String ma5Str = "MA5:" + StringUtils.formatDoubleLen(kline.getMa5(), 2, 12);
            String ma10Str = "MA10:" + StringUtils.formatDoubleLen(kline.getMa10(), 2, 12);
            String ma30Str = "MA30:" + StringUtils.formatDoubleLen(kline.getMa30(), 2, 12);
            float y = (rectF.top + rectF.bottom) / 2 + DENSITY * 3;

            p.setColor(Color.parseColor(MA5_COLOR));
            canvas.drawText(ma5Str , rectF.left + DENSITY * 10 ,y,p);
            p.setColor(Color.parseColor(MA10_COLOR));
            canvas.drawText(ma10Str , rectF.left + DENSITY * 20 + p.measureText(ma5Str) , y, p );
            p.setColor(Color.parseColor(MA30_COLOR));
            canvas.drawText(ma30Str , rectF.left + DENSITY * 30 + p.measureText(ma5Str)+ p.measureText(ma10Str) , y, p );

        }
    }

    /**
     * 绘制底部时间
     * @param canvas
     * @param rectF
     * @param klines
     * @param start
     * @param end
     * @param skn
     * @param hn
     */
    private static void drawBottomTimes(Canvas canvas, RectF rectF, List<Kline> klines, int start, int end, int skn, int hn) {
        if (null != klines){
            String formatDateType = DateFormatUtils.getFormatType(klines.size() >= 2 ? Math.abs(klines.get(1).getId() - klines.get(0).getId()) : 0);
            int num = skn / hn;//每一个方格内数据条数
            p.reset();
            p.setColor(Color.parseColor("#6D87A8"));
            p.setTextSize(DENSITY * 8f);
            p.setAntiAlias(true);
            for (int i = 0 ; i < hn ; i++ ){
                if (i * num + start >= klines.size())break;
                Kline kline = klines.get(i * num + start);
                p.setTextAlign(0 == i ? Paint.Align.LEFT : Paint.Align.CENTER);
                canvas.drawText(DateFormatUtils.formatTime(kline.getId()*1000L,formatDateType) , rectF.left + (rectF.right - rectF.left)/4f * i,(rectF.top + rectF.bottom)/2f + DENSITY * 3f ,p);
            }
            if (skn + start - 1 < klines.size()){
                Kline kline = klines.get(skn + start - 1);
                p.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(DateFormatUtils.formatTime(kline.getId()*1000L,formatDateType) , rectF.left + (rectF.right - rectF.left)/4f * hn,(rectF.top + rectF.bottom)/2f + DENSITY * 3f ,p);
            }
        }
    }

    private static void drawCandles(Canvas canvas, RectF rectF, List<Kline> klines, int graphType , int start, int end, int skn, int maxIndex, int minIndex, Kline kmax, Kline kmin,int riseColor,int fallColor,int index , boolean isShowCoordinate,boolean isFS) {
        double min = kmin.getLow();
        double max = kmax.getHigh();
        double vl = rectF.height() / (max - min);//单位价格所占的像素高度
        float hl = rectF.width() / skn;//横向每条数据所占宽度
        p.reset();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.BUTT);

        //右侧指标
        p.setColor(Color.parseColor("#6D87A8"));
        p.setTextAlign(Paint.Align.RIGHT);
        p.setTextSize(DENSITY * 8f);
        p.setAntiAlias(true);
        for (int i = 0 ; i <= 4 ; i++){
            String str = StringUtils.formatDouble(min + (max - min) / 4 * i, 8, StringUtils.FORMAT_TYPE1);
            canvas.drawText(str , rectF.right - DENSITY * 5 , rectF.bottom - i * (rectF.bottom - rectF.top)/4 , p);
        }

        path1.reset();
        path2.reset();
        path3.reset();
        //绘制烛形图
        for (int i = start ; i <= end ; i++ ){
            Kline kline = klines.get(i);

            if (isFS){
                //如果是分时图,显示一条30日均线
                if ( ZCYKlineView.GRAPH_TYPE_MA == graphType){
                    //30日均线path初始化
                    if (i > 30 && start != i){
                        path3.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa30() - min)*vl));
                    }else {
                        path3.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa30() - min)*vl));
                    }
                }

                //显示分时图，path1用于装载收盘线路径
                if (i == start){
                    path1.moveTo(0,(float) (rectF.bottom - (kline.getClose() - min)*vl));
                }else if (i == end) {
                    path1.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getOpen() - min)*vl));
                    path2.addPath(path1);
                    path2.lineTo((i-start + 0.5f)*hl , rectF.bottom);
                    path2.lineTo(rectF.left,rectF.bottom);
                    path2.close();
                }else {
                    path1.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getOpen() - min)*vl));
                }


                //path2用于绘制分时封闭图形


            }else {
                //非分时图，显示蜡烛图，需要正常显示MA三条指标线
                //显示MA指标
                if ( ZCYKlineView.GRAPH_TYPE_MA == graphType){
                    //5日均线path数据填充
                    if (i > 5 && start != i){
                        path1.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa5() - min)*vl));
                    }else {
                        path1.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa5() - min)*vl));
                    }
                    //10日均线path初始化
                    if (i > 10 && start != i){
                        path2.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa10() - min)*vl));
                    }else {
                        path2.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa10() - min)*vl));
                    }
                    //30日均线path初始化
                    if (i > 30 && start != i){
                        path3.lineTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa30() - min)*vl));
                    }else {
                        path3.moveTo((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getMa30() - min)*vl));
                    }
                }

                //绘制烛形图主体
                if (kline.getOpen() == kline.getClose()){
                    p.setColor(i > 0 && kline.getClose() > klines.get(i - 1).getClose() ?  riseColor: fallColor);
                    p.setStrokeWidth(0);
                    canvas.drawLine((i-start)*hl,(float) (rectF.bottom - (kline.getClose() - min)*vl) , (i-start + 1)*hl ,(float) (rectF.bottom - (kline.getClose() - min)*vl ),p);
                }else {
                    p.setColor(kline.getOpen() < kline.getClose() ? riseColor : fallColor);
                    p.setStrokeWidth(hl - Math.max(hl*0.1f,2f));
                    canvas.drawLine((i-start + 0.5f)*hl , (float) (rectF.bottom - (kline.getOpen() - min)*vl) ,(i-start + 0.5f)*hl ,(float) (rectF.bottom - (kline.getClose() - min)*vl) ,p);
                }
                //绘制烛形图上下影线
                p.setStrokeWidth(0);
                canvas.drawLine((i-start + 0.5f)*hl ,(float) (rectF.bottom - (kline.getLow() - min)*vl),(i-start + 0.5f)*hl ,(float) (rectF.bottom - (kline.getHigh() - min)*vl),p);
            }
        }

        p.setAntiAlias(true);
        p.setStrokeWidth(0);

        if (isFS){
            canvas.save();
            canvas.clipRect(rectF);

            // TODO: 2018/9/19 Shader 用法还没有很熟练，这里有个重复创建的问题，待解决
            Shader shader = new LinearGradient((rectF.left + rectF.right) / 2f,rectF.top ,(rectF.left + rectF.right) / 2f, rectF.bottom , Color.parseColor("#335786d2"),Color.parseColor("#005786d2"),Shader.TileMode.CLAMP);

            p.setStyle(Paint.Style.FILL);
            p.setShader(shader);
            canvas.drawPath(path2,p);

            p.setShader(null);
            p.setStrokeWidth(DENSITY);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.parseColor("#5786d2"));
            canvas.drawPath(path1,p);

            p.setStrokeWidth(0);
            p.setColor(Color.parseColor(MA30_COLOR));
            canvas.drawPath(path3,p);

            canvas.restore();

        }else {
            //绘制MA指标线
            if ( ZCYKlineView.GRAPH_TYPE_MA == graphType){
                p.setColor(Color.parseColor(MA5_COLOR));
                canvas.save();
                canvas.clipRect(rectF);
                canvas.drawPath(path1,p);
                p.setColor(Color.parseColor(MA10_COLOR));
                canvas.drawPath(path2,p);
                p.setColor(Color.parseColor(MA30_COLOR));
                canvas.drawPath(path3,p);
                canvas.restore();
            }
        }


        //没有分时线 ，绘制最高最低点指示
        if (!isFS){
            //绘制最高点和最低点的指示，这一块可以抽离出去，因为绘制所需要的信息在这里都计算完毕了，就直接写在这里了
            String maxStr = StringUtils.formatDouble(max, 8, StringUtils.FORMAT_TYPE1);
            String minStr = StringUtils.formatDouble(min, 8, StringUtils.FORMAT_TYPE1);
            p.setColor(Color.WHITE);
            p.setAntiAlias(true);
            p.setTextSize(DENSITY * 8);

            //绘制最高点所在的指标
            p.setTextAlign((maxIndex-start) < skn/2 ? Paint.Align.LEFT : Paint.Align.RIGHT);
            canvas.drawText((maxIndex-start) < skn/2 ? "<---" + maxStr : maxStr + "--->" , (maxIndex-start + 0.5f)*hl , rectF.top + DENSITY * 3 , p);

            //绘制最低点所在的指标
            p.setTextAlign((minIndex-start) < skn/2 ? Paint.Align.LEFT : Paint.Align.RIGHT);
            canvas.drawText((minIndex-start) < skn/2 ? "<---" + minStr : minStr + "--->" , (minIndex-start + 0.5f)*hl , rectF.bottom + DENSITY * 3 , p);

        }


        //如果显示指示器，绘制指示器
        if (isShowCoordinate){
            p.setColor(Color.WHITE);
            Kline kline = klines.get(index);
            String close = StringUtils.formatDouble(kline.getClose(), 2);
            float y = (float) (rectF.bottom - (kline.getClose() - min) * vl);
            float x = (index - start + 0.5f) * hl;
            canvas.drawLine(rectF.left , y ,rectF.right ,y ,p);

            float closeTextLen = p.measureText(close);

            if (x < closeTextLen *1.5f || x > rectF.right - closeTextLen * 1.5f){
                rectF2.set((rectF.left + rectF.right - closeTextLen)/2f - DENSITY * 5 , y - DENSITY * 10 ,(rectF.left + rectF.right + closeTextLen)/2f + DENSITY * 5, y + DENSITY * 10);
            }else {
                if (x > (rectF.left + rectF.right)/2f ){
                    rectF2.set(rectF.right - closeTextLen - DENSITY * 10f -1 , y - DENSITY * 10 , rectF.right - 1 , y + DENSITY * 10f );
                }else {
                    rectF2.set(rectF.left + 1 , y - DENSITY * 10f , rectF.left + 1 + closeTextLen + DENSITY * 10f , y + DENSITY* 10f );
                }
            }
            p.setColor(Color.parseColor("#071724"));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(rectF2 , p);
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rectF2 , p);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(close,(rectF2.left + rectF2.right)/2f , y + DENSITY * 3 , p);

            if ( x < (rectF.left + rectF.right)/2f ){
                rectF2.set(rectF.right - DENSITY * 115f,rectF.top + DENSITY * 5 ,rectF.right - DENSITY * 5f,DENSITY * 145f);
            }else {
                rectF2.set(rectF.left + DENSITY * 5f , rectF.top + DENSITY * 5 , rectF.left + DENSITY * 115f , DENSITY * 145f);
            }
            p.setColor(Color.parseColor("#071724"));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(rectF2 , p);
            p.setColor(Color.parseColor("#8298B0"));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rectF2 , p);

            Map<String,String> map = formatKline(kline);
            Iterator<String> iterator = map.keySet().iterator();
            for (int i = 0 ; iterator.hasNext() ; i++ ){
                p.setColor(Color.WHITE);
                String key = iterator.next();
                p.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(key , rectF2.left + DENSITY * 5f ,rectF2.top - DENSITY * 5 +  (i+1)*15*DENSITY , p);
                p.setTextAlign(Paint.Align.RIGHT);
                if (5 == i || 6 == i){
                    //绘制烛形图主体
                    if (kline.getOpen() == kline.getClose()){
                        p.setColor(index > 0 && kline.getClose() > klines.get(index - 1).getClose() ?  riseColor: fallColor);
                    }else {
                        p.setColor(kline.getOpen() < kline.getClose() ? riseColor : fallColor);
                    }
                }
                canvas.drawText(map.get(key) , rectF2.right - DENSITY * 5f ,rectF2.top - DENSITY * 5 +  (i+1)*15*DENSITY , p);
            }

        }



    }

    private static Map<String ,String > map;
    private static Map<String ,String > formatKline(Kline kline){
        if (null == map) map = new LinkedHashMap<>();
        map.put("时间",DateFormatUtils.formatTime(kline.getId(),DateFormatUtils.FORMAT_TYPE5));
        map.put("开",StringUtils.formatDouble(kline.getOpen(),2));
        map.put("高",StringUtils.formatDouble(kline.getHigh(),2));
        map.put("低",StringUtils.formatDouble(kline.getLow(),2));
        map.put("收",StringUtils.formatDouble(kline.getClose(),2));
        map.put("涨跌额",StringUtils.formatDouble(kline.getClose() - kline.getOpen(),2));
        map.put("涨跌幅",StringUtils.formatDouble((kline.getClose() - kline.getOpen())/kline.getOpen() * 100D,2) + "%");
        map.put("成交量",StringUtils.formatDouble(kline.getAmount(),4));
        return map;
    }

    /**
     * 绘制网格背景
     * @param rectF 绘制区域
     * @param vn 纵向数量
     * @param hn 横向数量
     * @param color 颜色
     */
    public static void drawBg(Canvas canvas ,RectF rectF , int vn , int hn , int color){
        p.reset();
        p.setColor(color);
        p.setStrokeWidth(0);
        float lenH = (rectF.right - rectF.left) / hn;
        float lenV = (rectF.bottom - rectF.top) / vn;
        for (int i = 0 ; i <= vn ; i++)
            canvas.drawLine(rectF.left , rectF.top + i*lenV , rectF.right , rectF.top + i*lenV,p);
        for (int i = 0 ; i <= hn ; i++)
            canvas.drawLine(rectF.left + i*lenH , rectF.top , rectF.left + i*lenH , rectF.bottom , p);
    }



}
