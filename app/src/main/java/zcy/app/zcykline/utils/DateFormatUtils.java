package zcy.app.zcykline.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DateFormatUtils {

    public static Map<String , SimpleDateFormat> map;
    public static final String FORMAT_TYPE1 = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_TYPE2 = "yyyy-MM-dd";
    public static final String FORMAT_TYPE3 = "HH:mm";
    public static final String FORMAT_TYPE4 = "MM-dd HH:mm";
    public static final String FORMAT_TYPE5 = "yy-MM-dd HH:mm";

    public static String formatTime(long time , String formatType){
        int length = String.valueOf(time).length();
        if (length > 13){
            time/=1000L;
        }else if (length <= 10){
            time*=1000L;
        }
        if (null == map)map = new HashMap<>();
        if (!map.containsKey(formatType)){
            map.put(formatType , new SimpleDateFormat(formatType));
        }
        return map.get(formatType).format(time);
    }

    public static String getFormatType(long time){
        String type;
        if (time <= 0){
            type = FORMAT_TYPE1;
        }else if (time <= 1000*60){
            type = FORMAT_TYPE3;
        }else if (time < 1000*60*60*24){
            type = FORMAT_TYPE4;
        }else {
            type = FORMAT_TYPE2;
        }
        return type;
    }


}
