package cn.ugee.pen.callback;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Pair;

import cn.ugee.pen.IUgeeService;
import cn.ugee.pen.model.UgeeDevice;
import cn.ugee.pen.service.UgeeDataService;

public class UgeePenClass implements ServiceConnection {
    private Context context;
    private PenServiceCallBack penServiceCallBack;
    public IUgeeService iUgeePenService;

    public UgeePenClass(Context context, OnUiCallBack onUgeeUiCallBack) {
        this.context = context;
        penServiceCallBack = new PenServiceCallBack(onUgeeUiCallBack,context);
        onBindService();



    }
    public void onBindService(){
        Intent intent = new Intent(context, UgeeDataService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }



    /**
     * 解除绑定
     */
    private void unBindUgeePenService(Context ctx, ServiceConnection conn) {
        try {
            if (ctx != null) {
                ctx.unbindService(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 连接蓝牙
     *
     * @param address
     */
    public void connectDevice(String address) {
        try {
                if (iUgeePenService != null) {
                    iUgeePenService.connectDevice(address);
                } else {
                    penServiceCallBack.onPenServiceError("server==null");
                }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     *  点阵笔连接蓝牙
     * @param address
     * @return
     */
    public boolean connectAFPen(String address,String name){
        boolean isAF=penServiceCallBack.connectAFPen(address);
        try {
            if(isAF){
                iUgeePenService.getConnectedDevice().setName(name);
                iUgeePenService.getConnectedDevice().setAddress(address);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return isAF;

    }

    /**
     * 断开蓝牙
     */
    public void disconnectDevice() {
        try {
            penServiceCallBack.disConnectAFPen();
            if (iUgeePenService != null) {
                iUgeePenService.disconnectDevice();
            } else {
                penServiceCallBack.onPenServiceError("server==null");

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前连接设备信息
     *
     * @return
     */
    public UgeeDevice getConnectedDevice() {
        try {
            if (iUgeePenService != null) {
                return iUgeePenService.getConnectedDevice();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        iUgeePenService = IUgeeService.Stub.asInterface(service);
        try {
            if (iUgeePenService != null) {
                iUgeePenService.registerCallBack(penServiceCallBack);
                service.linkToDeath(penServiceCallBack, 0);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     *设置对应的校准点
     * 0--原始点 校准过后要重新设置为0
     * 1--左侧点
     * 2--右侧点
     * @param point
     */
    public void setCorrectPoint(int point){
        try {
            if(iUgeePenService!=null){
                iUgeePenService.setCorrectPoint(point);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private int MaxCount=0;
    /**
     * 查询电量
     */
    public Pair<Integer, Boolean> onQueryUgeeBattery(){
        try {
            penServiceCallBack.queryBattery(true);
            if(iUgeePenService!=null) {
             iUgeePenService.queryUgeeBattery();
                    do{
                        MaxCount++;
                        Thread.sleep(200);
                    }while (!penServiceCallBack.isCompleteBattery&&MaxCount<10);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        MaxCount=0;
        int b=penServiceCallBack.battery;
        penServiceCallBack.battery=-1;
        return Pair.create(b, penServiceCallBack.isCharge);
    }



    public interface  batteryCallBack{
        void onCompleteBattery();
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
    }



    public void onUgeePenDestroy() {
        try {
            penServiceCallBack.disConnectAFPen();
            if (iUgeePenService != null) {
                iUgeePenService.unRegisterCallBack(penServiceCallBack);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (penServiceCallBack != null) {
            penServiceCallBack.onDestroy();
        }
        unBindUgeePenService(context, this);
        try {
            iUgeePenService.asBinder().unlinkToDeath(penServiceCallBack, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
