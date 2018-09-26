package zcy.app.zcykline.bean;

import android.support.annotation.NonNull;

public class Kline implements Comparable<Kline> {

    /**
     * amount : 2293.6837
     * close : 0.163765
     * count : 11
     * high : 0.163824
     * id : 1528860300
     * low : 0.16373
     * open : 0.163804
     * vol : 375.7014812534
     */

    private double amount;//成交量
    private double close; //收盘价
    private int count;//成交笔数
    private double high;//最高价
    private long id;//K线ID（单条数据的时间戳 （s））
    private double low;//最低价
    private double open;//开盘价
    private double vol;//成交额, 即 sum(每一笔成交价 * 该笔的成交量)



    //待计算指标
    private double vma5;
    private double vma10;
    private double ma5;
    private double ma10;
    private double ma30;
    private double EMA12;
    private double EMA26;
    private double DIF;
    private double DEA;
    private double k;
    private double d;
    private double j;

    public double getVma5() {
        return vma5;
    }

    public void setVma5(double vma5) {
        this.vma5 = vma5;
    }

    public double getVma10() {
        return vma10;
    }

    public void setVma10(double vma10) {
        this.vma10 = vma10;
    }

    public double getMa5() {
        return ma5;
    }

    public void setMa5(double ma5) {
        this.ma5 = ma5;
    }

    public double getMa10() {
        return ma10;
    }

    public void setMa10(double ma10) {
        this.ma10 = ma10;
    }

    public double getMa30() {
        return ma30;
    }

    public void setMa30(double ma30) {
        this.ma30 = ma30;
    }

    public double getEMA12() {
        return EMA12;
    }

    public void setEMA12(double EMA12) {
        this.EMA12 = EMA12;
    }

    public double getEMA26() {
        return EMA26;
    }

    public void setEMA26(double EMA26) {
        this.EMA26 = EMA26;
    }

    public double getDIF() {
        return DIF;
    }

    public void setDIF(double DIF) {
        this.DIF = DIF;
    }

    public double getDEA() {
        return DEA;
    }

    public void setDEA(double DEA) {
        this.DEA = DEA;
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getJ() {
        return j;
    }

    public void setJ(double j) {
        this.j = j;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    @Override
    public int compareTo(@NonNull Kline k) {
        return (int) (id - k.id);
    }
}