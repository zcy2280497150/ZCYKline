package zcy.app.zcykline.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import zcy.app.zcykline.R;

public class BaseDialogFragment extends DialogFragment {

    private DialogViewProvider viewProvider;
    private int gravity = Gravity.BOTTOM;
    private boolean isBgTransparent;//背景是否全透明（默认false）
    private boolean isFullScreen = true;//宽度是否全屏(true全屏，false非全屏)
    private boolean cancel;//外部点击取消，false - 不要该事件（默认不要该事件）
    private boolean isBlack = true;//状态栏字体颜色

    public BaseDialogFragment setBgTransparent(boolean bgTransparent) {
        isBgTransparent = bgTransparent;
        return this;
    }

    public BaseDialogFragment setBlack(boolean black) {
        isBlack = black;
        return this;
    }

    public BaseDialogFragment setFullScreen(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
        return this;
    }

    public BaseDialogFragment setCancel(boolean cancel) {
        this.cancel = cancel;
        return this;
    }

    public BaseDialogFragment setViewProvider(DialogViewProvider viewProvider) {
        this.viewProvider = viewProvider;
        return this;
    }

    public BaseDialogFragment setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (null == getDialog() || !getDialog().isShowing()) {
            super.show(manager, tag);
        }
    }

    @Override
    public void dismiss() {
        if (null != getDialog() && getDialog().isShowing()) {
            if (null != viewProvider) viewProvider.dismiss();
            super.dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_base, container, false);
        if (null != viewProvider) {
            ViewStub stub = rootView.findViewById(R.id.dialog_root_view);
            stub.setLayoutResource(viewProvider.getLayoutId());
            View view = stub.inflate();
            viewProvider.initView(this, view);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(cancel);//设置外部点击取消

        Window win = dialog.getWindow();

        //必须设置bg，要不然其他属性不生效
        Objects.requireNonNull(win).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));

        DisplayMetrics dm = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = this.gravity;//设置布局位置
        params.width = WindowManager.LayoutParams.MATCH_PARENT;//宽度充满全屏
        params.height = isBgTransparent ? WindowManager.LayoutParams.MATCH_PARENT : WindowManager.LayoutParams.WRAP_CONTENT;//高度包裹内容
        if (isBgTransparent) {
            params.dimAmount = 0.0f;
        }
        win.setAttributes(params);
        if (isFullScreen) {//无背景的时候用到字体颜色
            setStatusBarTxtBlack(win,isBlack);
        }

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (null != viewProvider)viewProvider.dismiss();
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (null != viewProvider) viewProvider.dismiss();
            }
        });
    }


    //设置状态栏为黑色字体，背景全透明
    private void setStatusBarTxtBlack(Window win , boolean isBlack) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || null == win)
            return;
        if (isBlack) {
            //状态栏透明，黑色字体
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    /*********IllegalStateException: Can not perform this action after onSaveInstanceState**********/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        invokeFragmentManagerNoteStateNotSaved();
    }

    private Method noteStateNotSavedMethod;
    private Object fragmentMgr;
    private String[] activityClassName = {"Activity", "FragmentActivity"};

    private void invokeFragmentManagerNoteStateNotSaved() {
        //java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }
        try {
            if (noteStateNotSavedMethod != null && fragmentMgr != null) {
                noteStateNotSavedMethod.invoke(fragmentMgr);
                return;
            }
            Class cls = getClass();
            do {
                cls = cls.getSuperclass();
            } while (!(activityClassName[0].equals(cls.getSimpleName())
                    || activityClassName[1].equals(cls.getSimpleName())));

            Field fragmentMgrField = prepareField(cls, "mFragments");
            if (fragmentMgrField != null) {
                fragmentMgr = fragmentMgrField.get(this);
                noteStateNotSavedMethod = getDeclaredMethod(fragmentMgr, "noteStateNotSaved");
                if (noteStateNotSavedMethod != null) {
                    noteStateNotSavedMethod.invoke(fragmentMgr);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Field prepareField(Class<?> c, String fieldName) throws NoSuchFieldException {
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            } finally {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException();
    }

    private Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
