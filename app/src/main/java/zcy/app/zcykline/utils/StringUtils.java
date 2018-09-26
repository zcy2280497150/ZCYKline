package zcy.app.zcykline.utils;

import java.math.BigDecimal;

public class StringUtils {


    public static final int FORMAT_TYPE_DEFAULT = 0;//保留小数位数
    public static final int FORMAT_TYPE1 = 1;//保留总长度
    /**
     * 保留小数位数
     * @param number
     * @param len
     * @return
     */
    public static String formatDouble(double number , int len , int type){
        StringBuilder s = new StringBuilder(String.valueOf(new BigDecimal(number)));
        if (!s.toString().contains(".")){
            s.append(".");
        }
        int i = s.indexOf(".");
        switch (type){
            case FORMAT_TYPE1:
                if (s.length() > len){
                    return  s.toString().substring(0,s.toString().indexOf(".") == len-1 ? len-1 : len);
                }else {
                    while (len > s.length()) s.append(0);
                }
                break;
            default:
                if (s.length() > i+1+len){
                    return s.toString().substring(0,i+1+len);
                }else {
                    while (s.length() <i+1+len)s.append("0");
                }
                break;
        }
        return s.toString();
    }

    public static String formatDouble(double number , int len){
        return formatDouble(number,len,FORMAT_TYPE_DEFAULT);
    }

    public static String formatDoubleShowUnit(double number , int len){
        String unit;
        if (number > 10000 * 10000D){
            unit = "亿";
            number /= 10000 * 10000D;
        }else if (number > 10000D){
            unit = "万";
            number /= 10000D;
        }else {
            unit = "";
        }
        return formatDouble(number ,len )+unit;
    }

    public static String formatDoubleLen(double number , int min , int max){
        if (min <=0 || min > max)return String.valueOf(number);
        StringBuilder s = new StringBuilder(String.valueOf(new BigDecimal(number)));
        if (!s.toString().contains(".")){
            s.append(".");
        }
        int i = s.indexOf(".");
        while (s.length() < i+1+min)s.append("0");//小数位数少于最小值，补全
        while (s.length() > i+1+max)s.deleteCharAt(s.length()-1);//小数位数大于最大值 ， 删除
        while (s.length() > i+1+min && ('0' == s.charAt(s.length()-1) || s.length() > min + 6 ))s.deleteCharAt(s.length()-1);//两位小数以后的0移除，小数位数大于最小值得情况下，若数字本身长度大于6+min , 移除末尾数
        return s.toString();
    }
    public static String formatDoubleLen(double number , int min , int max , int len){
        if (min <=0 || min > max)return String.valueOf(number);
        StringBuilder s = new StringBuilder(String.valueOf(new BigDecimal(number)));
        if (!s.toString().contains(".")){
            s.append(".");
        }
        int i = s.indexOf(".");
        while (s.length() < i+1+min)s.append("0");//小数位数少于最小值，补全
        while (s.length() > i+1+max)s.deleteCharAt(s.length()-1);//小数位数大于最大值 ， 删除
        while (s.length() > i+1+min && ('0' == s.charAt(s.length()-1) || s.length() > len))s.deleteCharAt(s.length()-1);//两位小数以后的0移除，小数位数大于最小值得情况下，若数字本身长度大于6+min , 移除末尾数
        return s.toString();
    }

}
