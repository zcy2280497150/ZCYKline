package zcy.app.zcykline.dialog;

import android.support.annotation.LayoutRes;
import android.support.v4.app.DialogFragment;
import android.view.View;

public interface DialogViewProvider {

    @LayoutRes int getLayoutId();

    void initView(DialogFragment dialog, View view);

    void dismiss();

}
