package cn.ugee.app;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;


public class WriteMatrixView extends View {
    private Paint mPaint;
    private Matrix matrix;
    public float paintWidth = 1.4f;
    private Bitmap mBitmap;

    // 平移开始点与移动点
    private Point startPoint;
    private Point movePoint;
    private float initDistance;

    // 记录当前平移距离
    private int sx;
    private int sy;

    // 保存平移状态
    private int oldsx;
    private int oldsy;

    // scale rate
    private float widthRate;
    private float heightRate;
    private Canvas mCanvas;
    private float minWidth;
    private boolean isPoint;//绘点/线方式切换

    private PaintFlagsDrawFilter pfd;
    //initView()中
    private float minLen=0.5f;

private int myWidth,myHeight;
    public WriteMatrixView(Context context) {
        super(context);
    }

    public WriteMatrixView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCanvas();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }
    public void setPenWidth(float width) {
        this.paintWidth = width;
        mPaint.setStrokeWidth(width);
        minWidth=width/2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initCanvas();
    }
    public void setPointWidth(float width){
        this.paintWidth=width;
    }
    public void setMinWidth(float w){
        this.minLen=w;
    }
    public void setMaxPressure(int pressure){
        this.maxPressure=pressure;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public void initCanvas() {

        if (getWidth() > 0 && getHeight() > 0) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            myWidth=getWidth();
            myHeight=getHeight();
            mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        }
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(paintWidth);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
       pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        BlurMaskFilter maskFilter = new BlurMaskFilter(0.3f, BlurMaskFilter.Blur.NORMAL);//NORMAL
        mPaint.setMaskFilter(maskFilter);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
        minWidth = paintWidth / 2;

    }

    private void initParameters() {
        // 初始化画笔
        //    mPaint = new Paint();
        //  mPaint.setColor(Color.RED);
        matrix = new Matrix();
        if (mBitmap != null) {
            float iw = mBitmap.getWidth();
            float ih = mBitmap.getHeight();
            float width = this.getWidth();
            float height = this.getHeight();
            // 初始放缩比率
            widthRate = width / iw;
            heightRate = height / ih;
        }

        sx = 0;
        sy = 0;

        oldsx = 0;
        oldsy = 0;

    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public void setInitDistance(float initDistance) {
        this.initDistance = initDistance;
    }

    public void zoomIn(float distance) {
        float rate = distance / this.initDistance;
        float iw = mBitmap.getWidth();
        float ih = mBitmap.getHeight();
        float width = this.getWidth();
        float height = this.getHeight();
        // get scale rate
        widthRate = (width / iw) * rate;
        heightRate = (height / ih) * rate;

        // make it same as view size
        float iwr = (width / iw);
        float ihr = (height / ih);
        if (iwr >= widthRate) {
            widthRate = (width / iw);
        }
        if (ihr >= heightRate) {
            heightRate = (height / ih);
        }

        // go to center
        oldsx = (int) ((width - widthRate * iw) / 2);
        oldsy = (int) ((height - heightRate * ih) / 2);
    }

    public void setWidthAndHeight(int maxX, int maxY, int maxP) {
        mA5_W = maxX;
      //  mA5_H = maxY-1601;
        mA5_H = maxY;
        maxPressure = maxP;
        if (maxX > maxY) {//横屏
            MID = mA5_H / mA5_W;
        } else {//竖屏
            MID = mA5_W / mA5_H;
        }
    }

    public void setMovePoint(Point movePoint) {
        this.movePoint = movePoint;
        sx = this.movePoint.x - this.startPoint.x;
        sy = this.movePoint.y - this.startPoint.y;

        float iw = mBitmap.getWidth();
        float ih = mBitmap.getHeight();

        // 检测边缘
        int deltax = (int) ((widthRate * iw) - this.getWidth());
        int deltay = (int) ((heightRate * ih) - this.getHeight());
        if ((sx + this.oldsx) >= 0) {
            this.oldsx = 0;
            sx = 0;
        } else if ((sx + this.oldsx) <= -deltax) {
            this.oldsx = -deltax;
            sx = 0;
        }

        if ((sy + this.oldsy) >= 0) {
            this.oldsy = 0;
            this.sy = 0;
        } else if ((sy + this.oldsy) <= -deltay) {
            this.oldsy = -deltay;
            this.sy = 0;
        }

        float width = this.getWidth();

        // 初始放缩比率
        float iwr = width / iw;
        if (iwr == widthRate) {
            sx = 0;
            sy = 0;
            oldsx = 0;
            oldsy = 0;
        }
    }

    public void savePreviousResult() {
        this.oldsx = this.sx + this.oldsx;
        this.oldsy = this.sy + this.oldsy;

        // zero
        sx = 0;
        sy = 0;
    }

    float mA5_H = 4600;
    float mA5_W = 3200;
    int maxPressure = 2000;
    private float downXX;
    private float downYY;
    private float MID = 0.6666666667f;
    private float lastX = -1, lastY = -1;

    /**
     * 正常传输
     * @param state
     * @param X
     * @param Y
     * @param P
     */
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
//        downXX = X * w / mA5_W;
//        downYY = Y * h / mA5_H;
        downXX = X * getWidth() / mA5_W;
        downYY = (Y)* getHeight() / mA5_H;
        if (state == 16 || state == 0) {
            lastX = -1;
            lastY = -1;
        } else if (state == 17) {
            if (lastX == -1 && lastY == -1) {
                lastX = downXX;
                lastY = downYY;
            }

            if(!isPoint){

                float w1 =(( (1.4f-minLen)* P) / maxPressure);
                w1=w1+minLen;
               //  float w1=1.4f*P/maxPressure;
               float num=(float)Math.round(w1*100)/100;
                mPaint.setStrokeWidth(num);
                mCanvas.drawLine(lastX, lastY, downXX, downYY, mPaint);
            }else {
                //画点 对比查看是否有漏点现象
                    mPaint.setStrokeWidth(1.5f);
                    mCanvas.drawPoint(downXX,downYY,mPaint);
            }

            postInvalidate();
            lastX = downXX;
            lastY = downYY;
        }

    }

    public void onPenDrawView(float x,float y,float width,int state) {

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
        downXX = x * w / mA5_W;
        downYY = y * h / mA5_H;
        if (state == 16 || state == 0) {
            lastX = -1;
            lastY = -1;
        } else if (state == 17) {
            if (lastX == -1 && lastY == -1) {
                lastX = downXX;
                lastY = downYY;
            }


            if (isPoint) {
                //画点 对比查看是否有漏点现象
                if (state == 17) {
                    mPaint.setStrokeWidth(1.5f);
                    mCanvas.drawPoint(downXX, downYY, mPaint);
                }
            } else {
                mPaint.setStrokeWidth(width/14);
                mCanvas.drawLine(lastX, lastY, downXX, downYY, mPaint);
            }
            postInvalidate();
            lastX = downXX;
            lastY = downYY;
        }

    }
    public void setPointType(boolean b){
        this.isPoint=b;
    }

    public void cleanScreen() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    private boolean isMix;

    /**
     * 是否处于缩放模式
     *
     * @param isMix
     */
    public void setMix(boolean isMix) {
        this.isMix = isMix;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isMix) {
            if (matrix == null) {
                initParameters();
            }

            if (mBitmap != null) {
                matrix.reset();
                matrix.postScale(widthRate, heightRate);
                matrix.postTranslate(oldsx + sx, oldsy + sy);
                 canvas.setDrawFilter(pfd);
                canvas.drawBitmap(mBitmap, matrix, mPaint);
            }
        } else {
         canvas.setDrawFilter(pfd);
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }

    }
}
