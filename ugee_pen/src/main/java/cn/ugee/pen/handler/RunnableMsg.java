package cn.ugee.pen.handler;

import android.os.Handler;
import android.os.Looper;

import cn.ugee.pen.callback.OnUiCallBack;

public class RunnableMsg implements Runnable{
    private static final Handler mainThread = new Handler(Looper.getMainLooper());
    private static final Object sPoolSync = new Object();
    private static final int MAX_POOL_SIZE = 50;
    private static final int FLAG_IN_USE = 1;   //等待复用标志
    private static RunnableMsg sPool;
    private static int sPoolSize = 0;
    private RunnableMsg next;
    private OnUiCallBack callback;
    private int flags;                  //0标示正在被使用
    private float pointX, pointY, pointPressure,pointWidth;
    //pointStateIos 为了和ios端统一数据添加的
    private int pointState,pointStateIos;


    RunnableMsg(float x, float y, float pressure, int state, int stateIos, float width, OnUiCallBack remoteCallback) {
        this.pointX = x;
        this.pointY = y;
        this.pointPressure = pressure;
        this.pointState = state;
        this.pointStateIos=stateIos;
        this.pointWidth=width;
        this.callback = remoteCallback;

    }

    public static RunnableMsg obtain(float x, float y, float pressure, int state, int stateIos, float width, OnUiCallBack callback) {
        synchronized (sPoolSync) {
            if (sPool != null) {
                RunnableMsg m = sPool;
                sPool = m.next;
                m.next = null;
                m.flags = 0;
                m.callback = callback;
                m.pointX = x;
                m.pointY = y;
                m.pointPressure = pressure;
                m.pointState = state;
                m.pointStateIos=stateIos;
                m.pointWidth=width;
                sPoolSize--;
                return m;
            }
        }
        return new RunnableMsg( x, y, pressure, state,stateIos,width, callback);
    }




    private boolean isInUse() {
        return ((flags & FLAG_IN_USE) == FLAG_IN_USE);
    }

    private void recycle(){
        if (isInUse()) {
            return;
        }
        recycleUnchecked();
    }

    private void recycleUnchecked() {
        flags = FLAG_IN_USE;
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                callback = null;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    public void sendToTarget() {
        mainThread.post(this);
    }

    @Override
    public void run() {
        try {
            if (callback != null) {
                callback.onPenPositionChanged( pointX, pointY, pointPressure, pointState,pointStateIos,pointWidth);
            }
            recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
