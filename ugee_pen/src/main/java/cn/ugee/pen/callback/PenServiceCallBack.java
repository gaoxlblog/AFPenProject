package cn.ugee.pen.callback;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;

import com.afpensdk.pen.DPenCtrl;
import com.afpensdk.pen.penmsg.IAFPenDotListener;
import com.afpensdk.structure.AFDot;

import java.lang.ref.WeakReference;

import cn.ugee.pen.IUgeeServiceCallBack;
import cn.ugee.pen.handler.RunnableAFPenMsg;
import cn.ugee.pen.handler.RunnableMsg;

public class PenServiceCallBack extends IUgeeServiceCallBack.Stub implements IBinder.DeathRecipient, IAFPenDotListener {
    private WeakReference<OnUiCallBack> uiCallbackWeakReference;
    private WeakReference<Handler> handlerWeakReference;
    private Handler mHandler;
    public boolean isQueryBattery, isCompleteBattery;
    public int battery = -1;
    public boolean isCharge;
    private DPenCtrl iPenCtrl;
    public PenServiceCallBack(OnUiCallBack uiCallback, Context context) {
        this.uiCallbackWeakReference = new WeakReference<>(uiCallback);
        mHandler = new Handler();
        this.handlerWeakReference = new WeakReference<Handler>(mHandler);
        initAFPen(context);
    }

    private void initAFPen(Context context) {
        iPenCtrl=DPenCtrl.getInstance();
        iPenCtrl.setContext(context);
        iPenCtrl.btStartForPeripheralsList(context);
        iPenCtrl.setDotListener(this);
    }
    public  boolean connectAFPen(String address){
        boolean isAF= iPenCtrl.connect("60WS-"+address);
        if(isAF){
            if (uiCallbackWeakReference.get() != null) {
                uiCallbackWeakReference.get().onStateChanged(6);
            }
        }
      return   isAF;
    }
    public void disConnectAFPen(){
       iPenCtrl.disconnect();
        if (uiCallbackWeakReference.get() != null) {
            uiCallbackWeakReference.get().onStateChanged(0);
        }
    }


    public void queryBattery(boolean b) {
        this.isQueryBattery = b;
    }

    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void onStateChanged(final int state, final String address) {
        if (handlerWeakReference.get() != null) {
            try {
                handlerWeakReference.get().post(new Runnable() {
                    @Override
                    public void run() {
                        if (uiCallbackWeakReference.get() != null) {
                            uiCallbackWeakReference.get().onStateChanged(state);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (uiCallbackWeakReference.get() != null) {
                uiCallbackWeakReference.get().onStateChanged(state);
            }
        }
    }

    public void onPenServiceError(final String msg) {
        if (handlerWeakReference.get() != null) {
            try {
                handlerWeakReference.get().post(new Runnable() {
                    @Override
                    public void run() {
                        if (uiCallbackWeakReference.get() != null) {
                            uiCallbackWeakReference.get().onPenServiceError(msg);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            uiCallbackWeakReference.get().onPenServiceError(msg);
        }
    }


    @Override
    public void onPenPosition(float x, float y, float pressure, int state, int stateIos, float width) {
        if (uiCallbackWeakReference.get() != null) {
            RunnableMsg.obtain(x, y, pressure, state, stateIos, width, uiCallbackWeakReference.get()).sendToTarget();
        }
    }


    public void onUgeeEvent(final int event) {
        if (handlerWeakReference.get() != null) {
            try {
                handlerWeakReference.get().post(new Runnable() {
                    @Override
                    public void run() {
                        if (uiCallbackWeakReference.get() != null) {
                            uiCallbackWeakReference.get().onUgeeKeyEvent(event);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onUgeeDeviceBattery(final int battery, final boolean isCharge) {
        if (!isQueryBattery) {
            if (handlerWeakReference.get() != null) {
                try {
                    handlerWeakReference.get().post(new Runnable() {
                        @Override
                        public void run() {
                            uiCallbackWeakReference.get().onUgeeBattery(battery, isCharge);
                        }
                    });
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                uiCallbackWeakReference.get().onUgeeBattery(100, false);
            }
        } else {
            this.battery = battery;
            this.isCharge = isCharge;
            isQueryBattery = false;
            isCompleteBattery = true;
        }

    }

    public void onUgeePenWidthAndHeight(final int width, final int height, final int pressure, final boolean isHorizontal) {
        if (handlerWeakReference.get() != null) {
            try {
                handlerWeakReference.get().post(new Runnable() {
                    @Override
                    public void run() {

                        uiCallbackWeakReference.get().onUgeePenWidthAndHeight(width, height, pressure, isHorizontal);
                    }
                });
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            uiCallbackWeakReference.get().onUgeePenWidthAndHeight(15010, 22499, 8191, true);
        }
    }

    public void onCorrectInfo(final int point, final boolean isSuccess) {
        if (handlerWeakReference.get() != null) {
            try {
                handlerWeakReference.get().post(new Runnable() {
                    @Override
                    public void run() {
                        uiCallbackWeakReference.get().onCorrectInfo(point, isSuccess);
                    }
                });
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public void onUgeePenAngle(final int quadrant, final int degree, final boolean isSuccess) {
        if (handlerWeakReference.get() != null) {
            try {
                handlerWeakReference.get().post(new Runnable() {
                    @Override
                    public void run() {
                        uiCallbackWeakReference.get().onUgeePenAngle(quadrant, degree, isSuccess);
                    }
                });
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    public void binderDied() {

    }

    @Override
    public void onReceiveDot(AFDot afDot) {
        if (uiCallbackWeakReference.get() != null) {
            RunnableAFPenMsg.obtain(afDot.X, afDot.Y, afDot.type, afDot.page, uiCallbackWeakReference.get()).sendToTarget();
        }
    }

}
