package zcy.app.zcykline.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import zcy.app.zcykline.utils.MyLog;

/**
 * BaseActivity  统一使用了ButterKnife初始化视图
 * Created by zcy on 2017/9/10.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * ----------  start  ---------------
     * 通过反射手段在onSaveInstanceState 方法里调用 FragmentManagerImpl noteStateNotSaved方法将 mStateSaved 变量置为false ，这样既不会导致activity状态丢失，也能确保退出时不会抛出异常
     */
    private Method noteStateNotSavedMethod;
    private Object fragmentMgr;
    private String[] activityClassName = {"Activity", "FragmentActivity"};
    private int screenHeight;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        invokeFragmentManagerNoteStateNotSaved();
    }

    private void invokeFragmentManagerNoteStateNotSaved() {
        //java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
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

    /*
     * --------  end  -----------
     * */


    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 110;


    protected abstract int getLayoutId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        MIUISetStatusBarLightMode(this.getWindow(), true);
        registerKeyboard();//注册软键盘状态监听
    }

    //小米手机
    public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    //魅族手机
    public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            //                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onDestroy() {
        removeKeyboard();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MyLog.i("同意授权");
                // 进行正常操作。
            } else {
                MyLog.i("拒绝授权");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //-------------------------------------------------------------------------------------------

    public static int activityActive; //全局变量,表示当前在前台的activity数量，当计数为0时表示APP在后台，此时间HTTP请求队列不添加
    public static boolean isBackground;

    @Override
    protected void onStart() {
        activityActive++;
        isBackground = false;
        super.onStart();
    }

    @Override
    protected void onStop() {
        activityActive--;
        if (activityActive <= 0) {
            activityActive = 0;
            isBackground = true;
            if (null != iAppBackGround) {
                iAppBackGround.onBackground(true);
            }
        }
        super.onStop();
    }

    public static boolean isBackground() {
        return isBackground;
    }

    static IAppBackGround iAppBackGround;

    public static void setAppBackGround(IAppBackGround _iAppBackGround) {
        iAppBackGround = _iAppBackGround;
    }

    public interface IAppBackGround {
        void onBackground(boolean isBackground);
    }


    //设置状态栏为黑色字体，背景全透明
    public void setStatusBarTxtBlack(boolean isBlack) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        if (isBlack) {
            //状态栏透明，黑色字体
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    //-------------------------------------------- 软键盘状态相关 -----------------------------------------------
    private ArrayList<OnSoftKeyboardStateChangedListener> mKeyboardStateListeners;      //软键盘状态监听列表
    private ViewTreeObserver.OnGlobalLayoutListener mLayoutChangeListener;
    private boolean mIsSoftKeyboardShowing;

    public interface OnSoftKeyboardStateChangedListener {
        void OnSoftKeyboardStateChanged(boolean isKeyBoardShow, int keyboardHeight);
    }

    //注册软键盘状态变化监听
    public void addSoftKeyboardChangedListener(OnSoftKeyboardStateChangedListener listener) {
        if (listener != null) {
            mKeyboardStateListeners.add(listener);
        }
    }

    //取消软键盘状态变化监听
    public void removeSoftKeyboardChangedListener(OnSoftKeyboardStateChangedListener listener) {
        if (listener != null) {
            mKeyboardStateListeners.remove(listener);
        }
    }


    //注册布局变化监听(主要是监听软键盘状态变化)
    private void registerKeyboard() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        screenHeight = Objects.requireNonNull(windowManager).getDefaultDisplay().getHeight();
        mIsSoftKeyboardShowing = false;
        mKeyboardStateListeners = new ArrayList<>();
        mLayoutChangeListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //判断窗口可见区域大小
                Rect r = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                //如果屏幕高度和Window可见区域高度差值大于整个屏幕高度的1/3，则表示软键盘显示中，否则软键盘为隐藏状态。
                //                int heightDifference = screenHeight - (r.bottom - r.top);
                int heightDifference = screenHeight - r.bottom;//沉浸式状态栏有区别
                isKeyboardShowing = heightDifference > screenHeight / 3;

                bottomY = r.bottom;

                //如果之前软键盘状态为显示，现在为关闭，或者之前为关闭，现在为显示，则表示软键盘的状态发生了改变
                if ((mIsSoftKeyboardShowing && !isKeyboardShowing) || (!mIsSoftKeyboardShowing && isKeyboardShowing)) {
                    mIsSoftKeyboardShowing = isKeyboardShowing;
                    for (int i = 0; i < mKeyboardStateListeners.size(); i++) {
                        OnSoftKeyboardStateChangedListener listener = mKeyboardStateListeners.get(i);
                        listener.OnSoftKeyboardStateChanged(mIsSoftKeyboardShowing, heightDifference);
                    }
                }
            }
        };
        //注册布局变化监听
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mLayoutChangeListener);
    }

    private boolean isKeyboardShowing;
    private int bottomY;

    public boolean isKeyboardShowing() {
        return isKeyboardShowing;
    }

    private void removeKeyboard() {
        //移除布局变化监听
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutChangeListener);
        if (null != mKeyboardStateListeners)
            mKeyboardStateListeners.clear();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return isKeyboardShowing && MotionEvent.ACTION_DOWN == ev.getAction() || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_UP == event.getAction()){
            //监听到抬起动作
            if (isKeyboardShowing && event.getRawY() <= bottomY ){
                InputMethodManager systemService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                Objects.requireNonNull(systemService).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }
}
