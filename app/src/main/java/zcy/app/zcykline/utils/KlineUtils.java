package zcy.app.zcykline.utils;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.List;

import zcy.app.zcykline.bean.Kline;

public class KlineUtils {

    public static final int N = 9;
    public static final int K = 3;
    public static final int D = 3;
    private static final int VMA5 = 5;//vol 5日均线
    private static final int VMA10 = 10;//vol 10日均线
    private static final int MA5 = 5;//主图 5日均线
    private static final int MA10 = 10;//主图 10日均线
    private static final int MA30 = 30;//主图 30日均线

    public static List<Kline> getKline(String data) {
        return initKlines(JSON.parseArray(data, Kline.class));
    }

    private static List<Kline> initKlines(List<Kline> klines) {
        if (null != klines) {
            Collections.sort(klines);
            initKDJ(klines, N, K, D);//计算KDJ
            initVma(klines);//计算VOL 平均值
            initMACD(klines);//计算MACD（）
            initMa(klines);//计算主图各条K线平均值（MA5,MA10,MA30）
        }
        return klines;
    }

    /**
     * 这里计算KDJ,
     *
     * @param data 需要计算的数据
     * @param n    周期
     * @param k    K指标（此K不是K值）
     * @param d    D指标（此D不是D值）
     */
    private static void initKDJ(List<Kline> data, int n, int k, int d) {
        int size = data.size();
        for (int i = 0; i < size; i++) {
            Kline k1 = data.get(i);
            if (i == 0) {
                k1.setK(50);
                k1.setD(50);
            } else {
                Kline k2 = data.get(i - 1);
                double ln = k1.getLow();//最低价
                double hn = k1.getHigh();//最高价
                //取N日内的最大最小值
                for (int j = Math.max(i - (n - 1), 0); j < i; j++) {
                    Kline bean = data.get(j);
                    ln = Math.min(ln, bean.getLow());
                    hn = Math.max(hn, bean.getHigh());
                }
                double rsv = hn == ln ? 50 : (k1.getClose() - ln) / (hn - ln) * 100;
                k1.setK(k2.getK() * (k - 1) / k + rsv / k);
                k1.setD(k2.getD() * (d - 1) / d + k1.getK() / d);
            }
            k1.setJ(k1.getK() * 3 - k1.getD() * 2);
        }
    }

    /**
     * MACD相关值计算
     *
     * @param klines 需要计算的数据
     */
    private static void initMACD(List<Kline> klines) {
        for (int i = 0; i < klines.size(); i++) {
            Kline kline = klines.get(i);//当天数据
            Kline kline1 = i == 0 ? null : klines.get(i - 1);//前一天数据
            kline.setEMA12(null == kline1 ? kline.getClose() : kline1.getEMA12() * 11 / 13 + kline.getClose() * 2 / 13);
            kline.setEMA26(null == kline1 ? kline.getClose() : kline1.getEMA26() * 25 / 27 + kline.getClose() * 2 / 27);
            kline.setDIF(kline.getEMA12() - kline.getEMA26());
            kline.setDEA(null == kline1 ? kline.getDIF() : kline1.getDEA() * 8 / 10 + kline.getDIF() * 2 / 10);
        }
    }

    /**
     * 计算每个数据的MA5两个平均值，这里属性值是ma5 , ma10,实际可以是任何值，
     * @param klines 需要计算的数组
     */
    private static void initVma(List<Kline> klines) {
        for (int i = 0; i < klines.size(); i++) {
            Kline kline = klines.get(i);
            //计算每个数据的VMA5
            double priceAllVma5 = kline.getVol();
            for (int j = Math.max(0, i - (VMA5 - 1)); j < i; j++)
                priceAllVma5 += klines.get(j).getVol();
            kline.setVma5(priceAllVma5 / Math.min(VMA5, i + 1));
            //计算每个数据的VMA10
            double priceAllVma10 = kline.getVol();
            for (int j = Math.max(0, i - (VMA10 - 1)); j < i; j++)
                priceAllVma10 += klines.get(j).getVol();
            kline.setVma10(priceAllVma10 / Math.min(VMA10, i + 1));
        }
    }

    /**
     * 计算平均值
     *
     * @param klines 需要计算的数据
     */
    private static void initMa(List<Kline> klines) {
        int size = klines.size();
        for (int i = 0; i < size; i++) {
            Kline kline = klines.get(i);
            //5日平均值计算
            double closeAll5 = kline.getClose();
            for (int j = Math.max(0, i - (MA5 - 1)); j < i; j++)
                closeAll5 += klines.get(j).getClose();
            kline.setMa5(closeAll5 / Math.min(MA5, i + 1));

            //10日平均值计算
            double closeAll10 = kline.getClose();
            for (int j = Math.max(0, i - (MA10 - 1)); j < i; j++)
                closeAll10 += klines.get(j).getClose();
            kline.setMa10(closeAll10 / Math.min(MA10, i + 1));

            //30天平均值计算
            double closeAll30 = kline.getClose();
            for (int j = Math.max(0, i - (MA30 - 1)); j < i; j++)
                closeAll30 += klines.get(j).getClose();
            kline.setMa30(closeAll30 / Math.min(MA30, i + 1));
        }
    }

    public static void upKline(List<Kline> klines, Kline kline) {
        if (null == klines || klines.size() < 1)
            return;
        Kline lastKline = klines.get(klines.size() - 1);
        if (lastKline.getId() > kline.getId())
            return;
        klines.add(kline);//这里需要先添加再移除，否则小概率会导致数组下标越界
        if (lastKline.getId() == kline.getId())
            klines.remove(lastKline);
        //数据有变，重新计算最后一个数据的各项指标
        upKdj(klines, N, K, D);
        upVma(klines);
        upMACD(klines);
        upMa(klines);
    }

    private static void upMa(List<Kline> klines) {
        int size = klines.size();
        Kline kline = klines.get(size - 1);//当天数据

        //5日平均值计算
        double closeAll5 = kline.getClose();
        for (int j = Math.max(0, size - MA5); j < size - 1; j++)
            closeAll5 += klines.get(j).getClose();
        kline.setMa5(closeAll5 / Math.min(MA5, size));

        //10日平均值计算
        double closeAll10 = kline.getClose();
        for (int j = Math.max(0, size - MA10); j < size - 1; j++)
            closeAll10 += klines.get(j).getClose();
        kline.setMa10(closeAll10 / Math.min(MA10, size));

        //30天平均值计算
        double closeAll30 = kline.getClose();
        for (int j = Math.max(0, size - MA30); j < size - 1; j++)
            closeAll30 += klines.get(j).getClose();
        kline.setMa30(closeAll30 / Math.min(MA30, size));

    }

    private static void upMACD(List<Kline> klines) {
        int size = klines.size();
        Kline kline = klines.get(size - 1);//当天数据
        Kline kline1 = size < 2 ? null : klines.get(size - 2);//前一天数据
        kline.setEMA12(null == kline1 ? kline.getClose() : kline1.getEMA12() * 11 / 13 + kline.getClose() * 2 / 13);
        kline.setEMA26(null == kline1 ? kline.getClose() : kline1.getEMA26() * 25 / 27 + kline.getClose() * 2 / 27);
        kline.setDIF(kline.getEMA12() - kline.getEMA26());
        kline.setDEA(null == kline1 ? kline.getDIF() : kline1.getDEA() * 8 / 10 + kline.getDIF() * 2 / 10);
    }

    private static void upVma(List<Kline> klines) {
        int size = klines.size();
        Kline kline = klines.get(size - 1);
        //计算每个数据的VMA5
        double priceAllVma5 = kline.getVol();
        for (int j = Math.max(0, size - 1 - (VMA5 - 1)); j < size - 1; j++)
            priceAllVma5 += klines.get(j).getVol();
        kline.setVma5(priceAllVma5 / Math.min(VMA5, size));
        //计算每个数据的VMA10
        double priceAllVma10 = kline.getVol();
        for (int j = Math.max(0, size - 1 - (VMA10 - 1)); j < size - 1; j++)
            priceAllVma10 += klines.get(j).getVol();
        kline.setVma10(priceAllVma10 / Math.min(VMA10, size));
    }

    private static void upKdj(List<Kline> klines, int n, int k, int d) {
        int size = klines.size();
        Kline kline = klines.get(size - 1);
        if (size < 2) {
            kline.setK(50);
            kline.setD(50);
        } else {
            Kline kline2 = klines.get(size - 2);
            double ln = kline.getLow();//最低价
            double hn = kline.getHigh();//最高价
            //取N日内的最大最小值
            for (int j = size - n; j < size - 1; j++) {
                if (j >= 0) {
                    Kline bean = klines.get(j);
                    ln = Math.min(ln, bean.getLow());
                    hn = Math.max(hn, bean.getHigh());
                }
            }
            double rsv = hn == ln ? 50 : (kline.getClose() - ln) / (hn - ln) * 100;
            kline.setK(kline2.getK() * (k - 1) / k + rsv / k);
            kline.setD(kline2.getD() * (d - 1) / d + kline.getK() / d);
        }
        kline.setJ(kline.getK() * 2 - kline.getD() * 2);
    }
}
