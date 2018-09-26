package zcy.app.zcykline;

import android.app.Application;

import zcy.app.zcykline.utils.DrawUtils;
import zcy.app.zcykline.utils.MyLog;
import zcy.app.zcykline.utils.MyToast;
import zcy.app.zcykline.utils.ReadDataUtils;


public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ReadDataUtils.init(this);
        MyLog.open(true);
        MyToast.init(this);
        DrawUtils.init(this);
    }
}
