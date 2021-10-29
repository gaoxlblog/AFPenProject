package cn.ugee.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class WriteView extends View {
    private Paint mPaint;
    private Canvas mCanvas;
    public float paintWidth = 2f;
    private Bitmap mBitmap;
    private Paint myPoint;
    private float minWidth;

    public WriteView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
      initCanvas();
    }

    public void initCanvas() {
        if(getWidth()>0&&getHeight()>0){
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        }
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(paintWidth);
        mPaint.setStyle(Paint.Style.FILL);

        BlurMaskFilter maskFilter = new BlurMaskFilter(0.3f, BlurMaskFilter.Blur.NORMAL);//NORMAL
        mPaint.setMaskFilter(maskFilter);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);

        minWidth=paintWidth/2;

        myPoint=new Paint();
        myPoint.setColor(Color.RED);
        myPoint.setStrokeWidth(1f);
        myPoint.setStyle(Paint.Style.STROKE);
        myPoint.setAntiAlias(true);
        myPoint.setDither(true);
    }

    public void setPenWidth(float width) {
        this.paintWidth = width;
        mPaint.setStrokeWidth(width);
        minWidth=width/2;
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initCanvas();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    public void setWidthAndHeight(int maxX, int maxY, int maxP) {
        mA5_W = maxX;
        mA5_H = maxY;
        maxPressure=maxP;
        if (maxX > maxY) {//横屏
            MID = mA5_H / mA5_W;
        } else {//竖屏
            MID = mA5_W / mA5_H;
        }
    }
    float mA5_H = 0;
    float mA5_W = 0;
    int maxPressure=0;
    private float downXX;
    private float downYY;
    private float MID = 0;
    private float lastX=-1,lastY=-1;

    public void setViewPoint(int state, float X, float Y, float P) {
        float w, h;
        float m;
        if (mA5_W > mA5_H) {//横屏
            m = (float) getHeight() / getWidth();
            if (m > MID) {
                w = getWidth();
                h = getWidth() * MID;
            } else {
                w = getHeight() / MID;
                h = getHeight();
            }
        } else {//竖屏
            m = (float) getWidth() / getHeight();
            if (m > MID) {
                w = getHeight() * MID;
                h = getHeight();
            } else {
                w = getWidth();
                h = getWidth() / MID;
            }
        }
        downXX = X * w / mA5_W;
        downYY = Y * h / mA5_H;
        if (state==16||state==0) {
            lastX=-1;
            lastY=-1;
        } else if(state==17){
            if(lastX==-1&&lastY==-1){
                lastX=downXX;
                lastY=downYY;
            }
            float w1=paintWidth*P/maxPressure;
            float w2=w1*paintWidth;
            if(w2<minWidth){
                w2=minWidth;
            }
            mPaint.setStrokeWidth(w2);
            myPoint.setColor(Color.RED);
           mCanvas.drawLine(lastX,lastY,downXX,downYY,mPaint);
            //画点 对比查看是否有漏点现象
          //  mCanvas.drawPoint(downXX,downYY,myPoint);
            postInvalidate();
            lastX=downXX;
            lastY=downYY;
        }

    }

    public void cleanScreen() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
