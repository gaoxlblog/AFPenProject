package cn.ugee.pen.handler;

import android.os.Handler;
import android.os.Looper;

import cn.ugee.pen.callback.OnUiCallBack;

public class RunnableAFPenMsg implements Runnable{
    private static final Handler mainThread = new Handler(Looper.getMainLooper());
    private static final Object sPoolSync = new Object();
    private static final int MAX_POOL_SIZE = 50;
    private static final int FLAG_IN_USE = 1;   //等待复用标志
    private static RunnableAFPenMsg sPool;
    private static int sPoolSize = 0;
    private RunnableAFPenMsg next;
    private OnUiCallBack callback;
    private int flags;                  //0标示正在被使用
    private  int pointX,pointY,pointType,pointPage;


    RunnableAFPenMsg(int x, int y, int type, int page, OnUiCallBack remoteCallback) {
        this.pointX = x;
        this.pointY = y;
        this.pointType = type;
        this.pointPage = page;
        this.callback = remoteCallback;

    }

    public static RunnableAFPenMsg obtain(int x, int y, int type, int page, OnUiCallBack callback) {
        synchronized (sPoolSync) {
            if (sPool != null) {
                RunnableAFPenMsg m = sPool;
                sPool = m.next;
                m.next = null;
                m.flags = 0;
                m.callback = callback;
                m.pointX = x;
                m.pointY = y;
                m.pointType = type;
                m.pointPage = page;
                sPoolSize--;
                return m;
            }
        }
        return new RunnableAFPenMsg( x, y, type, page, callback);
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
                callback.onAFPenPositionChanged( pointX, pointY, pointType, pointPage);
            }
            recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
