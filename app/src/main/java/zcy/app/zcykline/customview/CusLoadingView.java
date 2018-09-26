package zcy.app.zcykline.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import zcy.app.zcykline.R;

public class CusLoadingView extends View {

    private float rotate;//旋转偏移0-1f
    private Bitmap bitmap;//图片资源
    private Paint paint;//画笔
    private RectF rectF;//图片目标矩形
    private float iconWidth;//图片宽度
    private Paint p2;
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            rotate += (Math.abs(rotate-0.5f)+0.1f) * 0.02f;
            if (rotate > 1){
                rotate -= 1;
            }
            postInvalidate();
        }
    };

    public CusLoadingView(Context context) {
        super(context);
        init(context,null);
    }

    public CusLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context ,attrs);
    }

    public CusLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setStrokeCap(Paint.Cap.ROUND);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        if (null != attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CusLoadingView);
            Drawable drawable = typedArray.getDrawable(R.styleable.CusLoadingView_icon_image);
            iconWidth = typedArray.getDimension(R.styleable.CusLoadingView_icon_width , getResources().getDimension(R.dimen.dp_40));
            paint.setColor(typedArray.getColor(R.styleable.CusLoadingView_round_bg_color,Color.WHITE));
            paint.setStrokeWidth(typedArray.getDimension(R.styleable.CusLoadingView_round_bg_width , getResources().getDimension(R.dimen.dp_2)));
            p2.setStrokeWidth(paint.getStrokeWidth() * 2);
            p2.setColor(Color.WHITE);
            if (null != drawable && drawable instanceof BitmapDrawable){
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }else {
                bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.icon_loading);
            }
            typedArray.recycle();
        }
        new Timer().schedule(timerTask,0,16);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        if (null != bitmap){
            if(null == rectF){
                rectF = new RectF((getWidth() - iconWidth)/2 , (getHeight()-iconWidth)/2 , (getWidth()+ iconWidth)/2 , (getHeight()+iconWidth)/2);
            }
            canvas.drawBitmap(bitmap,null,rectF,null);
        }
        canvas.drawCircle(getWidth()/2 , getHeight()/2 , iconWidth*8/10 , paint);

        canvas.save();
        canvas.translate(getWidth()/2,getHeight()/2);
        canvas.rotate(rotate*360);
        canvas.drawPoint(0,iconWidth*8/10 , p2);
        canvas.restore();
    }



}
