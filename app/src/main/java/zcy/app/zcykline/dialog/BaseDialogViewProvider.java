package zcy.app.zcykline.dialog;

import android.support.annotation.LayoutRes;

public abstract class BaseDialogViewProvider implements DialogViewProvider {

    public BaseDialogViewProvider(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
    }

    private int layoutId;

    @Override
    public int getLayoutId() {
        return layoutId;
    }

    @Override
    public void dismiss() {

    }
}
