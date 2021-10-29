package cn.ugee.pen.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import cn.ugee.pen.IUgeeServiceCallBack;
import cn.ugee.pen.handler.BaseHandlerManager;
import cn.ugee.pen.model.UgeeCommand;
import cn.ugee.pen.model.UgeeDevice;
import cn.ugee.pen.util.ByteUtil;


public class UgeeDataService extends Service implements UgeeInterfaceService {
    private int mBindCount = 0;
    private RemoteCallbackList<IUgeeServiceCallBack> mRegistedCallbacks;
    private BluetoothGatt mBluetoothGatt;
    private UgeeDevice mDevice;
    private UgeeGattScan ugeeGattScan;
    private BluetoothManager bluetoothManager;
    private ByteUtil byteUtils;
    private UgeeBinderService binder;
    private BaseHandlerManager<Intent> baseHandlerManager;
    private String address;
    private int POINT=0;//设置校准点

    @Override
    public void onCreate() {
        super.onCreate();
        mRegistedCallbacks = new RemoteCallbackList<>();
        binder = new UgeeBinderService(this);
        byteUtils = new ByteUtil();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        ugeeGattScan = new UgeeGattScan(this);
        baseHandlerManager = new BaseHandlerManager.HandlersBuilder<Intent>()
                .build();
    }

    @Override
    public void connectBle() {
        if (address == null) {
            return;
        }
        connectBluetoothDevice(address);
        address = null;

    }

    @Override
    public void disConnectBle() {
        address = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        baseHandlerManager.handle(intent);
        return START_STICKY;
    }

    public void registerCallBack(IUgeeServiceCallBack callback) {
        mRegistedCallbacks.register(callback);
    }

    public void unRegisterCallBack(IUgeeServiceCallBack callback) {
        mRegistedCallbacks.unregister(callback);
        System.gc();
    }


    @Override
    public void onDeviceChanged(UgeeDevice device) {
        this.mDevice = device;
    }


    @Override
    @SuppressWarnings("NewApi")
    public UgeeDevice getConnectedDevice() {
        if (mDevice == null) {
            return new UgeeDevice("","",0);
        }
        //蓝牙模式
        if (isBleConnectionEnable()) {
            return mDevice;
        } else {
            return null;
        }
    }

    @Override
    public void updateDeviceType(int deviceType) {
        if (mDevice != null) {
            this.mDevice.setDeviceVersion(deviceType);
        }
    }


    @Override
    @SuppressLint("NewApi")
    public boolean connectBluetoothDevice(String addr) {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
        if (device == null || mDevice != null) {
            return false;
        }
        if (mBluetoothGatt != null) {// isBleConnectionEnable
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        mBluetoothGatt = device.connectGatt(UgeeDataService.this, false, ugeeGattScan);
        address = addr;
        return true;
    }


    @Override
    public IBinder onBind(Intent intent) {
        startService(new Intent(this, UgeeDataService.class));
        mBindCount++;
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        mBindCount++;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBindCount--;
        return true;
    }

    @Override
    public void setCorrectPoint(int p) {
        this.POINT=p;
    }

    @Override
    public void reportUGEE_BLEPosition(byte[] data) {
//        if(POINT>0){
//            onCorrectPoint(data);
//        }else {
//            reportPosition(data);
//        }

        if(connectType==3){
            reportA5position(data);
        }else if(connectType==0){
            reportPosition(data);
        } else if(connectType==13){
            reportA503Position(data);
        }else {
            if (data.length >= 12 && data.length % 12 == 0) {
                //机器岛专用
                reportA41BPosition(data);
            } else {
                for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                    try {
                        int x, y, p,pressure;
                        int state;
                        for (int k = 0; k < data.length; k += 7) {
                            data[1 + k] = (byte) (((data[1 + k] & 0xff) >> 1) | ((data[1 + k] & 0xff) << 7));
                            data[2 + k] = (byte) (((data[2 + k] & 0xff) >> 1) | ((data[2 + k] & 0xff) << 7));
                            x = byteUtils.bytesToInteger(data[2 + k], data[1 + k]);
                            data[3 + k] = (byte) (((data[3 + k] & 0xff) >> 2) | ((data[3 + k] & 0xff) << 6));
                            data[4 + k] = (byte) (((data[4 + k] & 0xff) >> 2) | ((data[4 + k] & 0xff) << 6));
                            y = byteUtils.bytesToInteger(data[4 + k], data[3 + k]);
                            data[5 + k] = (byte) (((data[5 + k] & 0xff) >> 3) | ((data[5 + k] & 0xff) << 5));
                            data[6 + k] = (byte) (((data[6 + k] & 0xff) >> 3) | ((data[6 + k] & 0xff) << 5));
                            p = byteUtils.bytesToInteger(data[6 + k], data[5 + k]);
                            byte bs = data[k];
                            if (bs == (byte) 0xA1) {
                                state = 17;
                            } else if (bs == (byte) 0xA0) {
                                state = 16;
                            } else {
                                state = 0;
                            }

                            if(p>0){
                                if(p>8000){
                                    pressure=800;
                                }else if(p > 5000){
                                    pressure=600;
                                }else if(p>3500){
                                    pressure=400;
                                }else {
                                    pressure=300;
                                }
                            }else {
                                pressure=0;
                            }
                            if (mRegistedCallbacks != null && mDevice != null) {
                                mRegistedCallbacks.getBroadcastItem(i)
                                        .onPenPosition(x, y, pressure, (byte)state, 0, 0);
                            }

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                assert mRegistedCallbacks != null;
                mRegistedCallbacks.finishBroadcast();
            }
        }


    }
    private void reportA41BPosition(byte[] data){
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try{
                int len=data.length;
                int x,y,p;
                for(int k=0;k<len;k+=12){
                    x = byteUtils.bytesToInteger(data[5+k], data[4+k]);
                    y = byteUtils.bytesToInteger(data[7+k], data[6+k]);
                    p = byteUtils.bytesToInteger(data[9+k], data[8+k]);
                    //   int s = bytesHelper.bytesToInteger(data[3]);
                    int state;
                    byte bs=data[3+k];
                    if (bs ==(byte)0xA1||bs==(byte)0x81) {
                        state = 17;
                    } else if (bs ==(byte) 0xA0||bs==(byte)0x80) {
                        state = 16;
                    } else {
                        state = 0;
                    }
                    int pressure;
                    if(p>0){
                        if(p>8000){
                            pressure=800;
                        }else if(p > 5000){
                            pressure=600;
                        }else if(p>3500){
                            pressure=400;
                        }else {
                            pressure=300;
                        }
                    }else {
                        pressure=0;
                    }
                    if (mRegistedCallbacks != null && mDevice != null) {
                        mRegistedCallbacks.getBroadcastItem(i)
                                .onPenPosition( x, y, pressure, (byte) state,0,0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assert mRegistedCallbacks != null;
        mRegistedCallbacks.finishBroadcast();
    }

    /**
     *  字节跳动 修改增加角度 参数
     * @param data
     */
    private void reportA503Position(byte[] data){
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try{
                int len=data.length;
                int x,y,p;
                for(int k=0;k<len;k+=12){
                    if(k>12&&k%12!=0){
                        break;
                    }
                    data[4+k]=(byte)( ((data[4+k]&0xff)>>1)|((data[4+k]&0xff)<<7));
                    data[5+k]=(byte)( ((data[5+k]&0xff)>>1)|((data [5+k]&0xff)<<7));
                    x = byteUtils.bytesToInteger(data[5+k], data[4+k]);
                    data[6+k]=(byte)( ((data[6+k]&0xff)>>2)|((data[6+k]&0xff)<<6));
                    data[7+k]=(byte)( ((data[7+k]&0xff)>>2)|((data[7+k]&0xff)<<6));
                    y = byteUtils.bytesToInteger(data[7+k], data[6+k]);
                    data[8+k]=(byte)( ((data[8+k]&0xff)>>3)|((data[8+k]&0xff)<<5));
                    data[9+k]=(byte)( ((data[9+k]&0xff)>>3)|((data[9+k]&0xff)<<5));
                    p = byteUtils.bytesToInteger(data[9+k], data[8+k]);
                    PointAngle pointAngle=reportAngle(data[10],data[11]);
                    int azimuth=pointAngle.getAzimuth();
                    int dAltitude=pointAngle.getdAltitude();
                    //   int s = bytesHelper.bytesToInteger(data[3]);

                    int state;
                    byte bs=data[3+k];
                    if (bs ==(byte)0xA1||bs==(byte)0x81) {
                        state = 17;
                    } else if (bs ==(byte) 0xA0||bs==(byte)0x80) {
                        state = 16;
                    } else {
                        state = 0;
                    }
                    if (mRegistedCallbacks != null && mDevice != null) {
                        mRegistedCallbacks.getBroadcastItem(i)
                                .onPenPosition( x, y, p, (byte) state,azimuth,dAltitude);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assert mRegistedCallbacks != null;
        mRegistedCallbacks.finishBroadcast();
    }

    /**
     *  字节跳动 板子
     * @param data
     */
    private void reportA503BPosition(byte[] data){
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try{
                int x,y,p;
                int state;
                for(int k=0;k<data.length;k+=12){
                    data[4+k]=(byte)( ((data[4+k]&0xff)>>1)|((data[4+k]&0xff)<<7));
                    data[5+k]=(byte)( ((data[5+k]&0xff)>>1)|((data [5+k]&0xff)<<7));
                    x = byteUtils.bytesToInteger(data[5+k], data[4+k]);
                    data[6+k]=(byte)( ((data[6+k]&0xff)>>2)|((data[6+k]&0xff)<<6));
                    data[7+k]=(byte)( ((data[7+k]&0xff)>>2)|((data[7+k]&0xff)<<6));
                    y = byteUtils.bytesToInteger(data[7+k], data[6+k]);
                    data[8+k]=(byte)( ((data[8+k]&0xff)>>3)|((data[8+k]&0xff)<<5));
                    data[9+k]=(byte)( ((data[9+k]&0xff)>>3)|((data[9+k]&0xff)<<5));
                    p = byteUtils.bytesToInteger(data[9+k], data[8+k]);
                    byte bs=data[3+k];
                    if (bs ==(byte)0xA1) {
                        state = 17;
                    } else if (bs ==(byte) 0xA0) {
                        state = 16;
                    } else {
                        state = 0;
                    }
                    if (mRegistedCallbacks != null && mDevice != null) {
                        mRegistedCallbacks.getBroadcastItem(i)
                                .onPenPosition( x, y, p, (byte) state,0,0);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assert mRegistedCallbacks != null;
        mRegistedCallbacks.finishBroadcast();
    }
    public void reportA5position(byte[] data){
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try{
                int len=data.length;
                data[4]=(byte)( ((data[4]&0xff)>>1)|((data[4]&0xff)<<7));
                data[5]=(byte)( ((data[5]&0xff)>>1)|((data [5]&0xff)<<7));
                int  x = byteUtils.bytesToInteger(data[5], data[4]);
                data[6]=(byte)( ((data[6]&0xff)>>2)|((data[6]&0xff)<<6));
                data[7]=(byte)( ((data[7]&0xff)>>2)|((data[7]&0xff)<<6));
                int  y = byteUtils.bytesToInteger(data[7], data[6]);
                data[8]=(byte)( ((data[8]&0xff)>>3)|((data[8]&0xff)<<5));
                data[9]=(byte)( ((data[9]&0xff)>>3)|((data[9]&0xff)<<5));
                int  p = byteUtils.bytesToInteger(data[9], data[8]);
                byte s = data[3];
                int state;
                if (s ==(byte) 0xA1) {
                    state = 17;
                } else if (s == (byte)0xA0) {
                    state = 16;
                } else {
                    state = 0;
                }
                if (mRegistedCallbacks != null && mDevice != null) {
                    mRegistedCallbacks.getBroadcastItem(i)
                            .onPenPosition( x, y, p, (byte) state,0,0);
                }

                if(len>10&&len%10==0){
                    int state1,pressure1;
                    data[14]=(byte)( ((data[14]&0xff)>>1)|((data[14]&0xff)<<7));
                    data[15]=(byte)( ((data[15]&0xff)>>1)|((data[15]&0xff)<<7));
                    int  x1 = byteUtils.bytesToInteger(data[15], data[14]);
                    data[16]=(byte)( ((data[16]&0xff)>>2)|((data[16]&0xff)<<6));
                    data[17]=(byte)( ((data[17]&0xff)>>2)|((data[17]&0xff)<<6));
                    int  y1 = byteUtils.bytesToInteger(data[17], data[16]);
                    data[18]=(byte)( ((data[18]&0xff)>>3)|((data[18]&0xff)<<5));
                    data[19]=(byte)( ((data[19]&0xff)>>3)|((data[19]&0xff)<<5));
                    int  p1 = byteUtils.bytesToInteger(data[19], data[18]);
//                    int x1=bytesHelper.bytesToInteger(data[15],data[14]);
//                    int y1=bytesHelper.bytesToInteger(data[17],data[16]);
//                    int p1=bytesHelper.bytesToInteger(data[19],data[18]);
                    int s1=data[13];
                    if (s1 == (byte)0xA1) {
                        state1 = 17;
                    } else if (s1 == (byte)0xA0) {
                        state1 = 16;
                    } else {
                        state1 = 0;
                    }
                    if (mRegistedCallbacks != null && mDevice != null) {
                        mRegistedCallbacks.getBroadcastItem(i)
                                .onPenPosition( x1, y1, p, (byte) state1,0,0);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assert mRegistedCallbacks != null;
        mRegistedCallbacks.finishBroadcast();
    }

    /**
     *  ET系列板子 已经是旧板
     * @param data
     */
    @Override
    public void reportUGEE_ETPosition(byte[] data) {
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try{
                int len=data.length;
                int x,y,p;
                for(int k=0;k<len;k+=10){
                    x = byteUtils.bytesToInteger(data[5+k], data[4+k]);
                    y = byteUtils.bytesToInteger(data[7+k], data[6+k]);
                    p = byteUtils.bytesToInteger(data[9+k], data[8+k]);
                    //   int s = bytesHelper.bytesToInteger(data[3]);
                    int state;
                    byte bs=data[3+k];
                    if (bs ==(byte)0xA1||bs==(byte)0x81) {
                        state = 17;
                    } else if (bs ==(byte) 0xA0||bs==(byte)0x80) {
                        state = 16;
                    } else {
                        state = 0;
                    }
                    if (mRegistedCallbacks != null && mDevice != null) {
                        mRegistedCallbacks.getBroadcastItem(i)
                                .onPenPosition( x, y, p, (byte) state,0,0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assert mRegistedCallbacks != null;
        mRegistedCallbacks.finishBroadcast();

    }

    /**
     * 校准点计算
     * 判断第一个点：x
     * @param data
     */
    private long LEN=0;
    private long LEN_ERROR=0;
    private int x1=0,y1=0;
    private int x2=0,y2=0;
    private void onCorrectPoint(byte[] data){
        for (int k = 0; k < data.length; k += 12) {
            int x = byteUtils.bytesToInteger(data[3 + k], data[2 + k]);
            int y = byteUtils.bytesToInteger(data[5 + k], data[4 + k]);
            byte bs = data[1 + k];
                if(bs == (byte) 0xA1){//只有按下时才进入判断
                    if(POINT==1){//判断第一个点
                        if(x>550&&x<650&&y>1600&&y<1700){
                            LEN++;
                            if(LEN<=10){
                                x1=x+x1;
                                y1=y+y1;
                            } else{
                                LEN=0;
                                reportCorrectInfo(1,true);
                                break;

                            }
                        }else {
                            LEN_ERROR++;
                            if(LEN_ERROR>5){
                                LEN_ERROR=0;
                                reportCorrectInfo(1,false);
                            }

                        }
                    }else if(POINT==2){//判断第二点
                            if(x>14350&&x<14450&&y>1600&&y<1700){
                                LEN++;
                                if(LEN<=10){
                                    x2=x+x2;
                                    y2=y+y2;
                                } else{
                                    LEN=0;
                                    setCorrectXY(x1,y1,x2,y2);
                                    reportCorrectInfo(2,true);
                                    break;
                                }
                            }else {
                                LEN_ERROR++;

                                if(LEN_ERROR>5){
                                    LEN_ERROR=0;
                                    reportCorrectInfo(2,false);
                                }
                            }
                    }
            }


        }
    }
    @Override
    public boolean queryBattery() {
        return ugeeGattScan.sendMessage((byte)0x02);
    }

    @Override
    public boolean sendByteCommand(byte command) {
        return ugeeGattScan.sendMessage(command);
    }

    private void setCorrectXY(int x1, int y1, int x2, int y2){
          int x0=  Math.round((x2-x1)/10);
          int y0=Math.round((y2-y1)/10);

    }
    private void reportCorrectInfo( int point,  boolean isSuccess){
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try {
             mRegistedCallbacks.getBroadcastItem(i).onCorrectInfo(point, isSuccess);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mRegistedCallbacks.finishBroadcast();
    }
    /**
     * 直接上报数据
     * @param data
     */
    private int preX,preY;
    private void reportPosition(byte[] data){
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try {
                int x, y, p, x1, y1;
                int state;
                float w;
                    for (int k = 0; k < data.length; k += 10) {
                        x = byteUtils.bytesToInteger(data[3 + k], data[2 + k]);
                        y = byteUtils.bytesToInteger(data[5 + k], data[4 + k]);
                        p = byteUtils.bytesToInteger(data[7 + k], data[6 + k]);
                        byte bs = data[1 + k];
                        PointAngle angle = null;
                        if (bs == (byte) 0xA1) {
                            state = 17;
                            angle=reportAngle(data[8],data[9]);
                        } else if (bs == (byte) 0xA0) {
                            state = 16;
                        } else {
                            state = 0;
                        }
                        w = (float) p/ 8191+0.36f;
                    
                        if(x>15000){
                            x=15000;
                        }
                       // y1 = y+1601;
                        y1 = y;
                        if (mRegistedCallbacks != null && mDevice != null) {
                            if(state==17){
                                if(preX!=x&&preY!=y1){
                                    preX=x;
                                    preY=y1;
                                    mRegistedCallbacks.getBroadcastItem(i)
                                            .onPenPosition(x, y1, p, state, 0, w);
                                }
                                int a=angle.getAzimuth();
                                int b=angle.getdAltitude();
                                boolean isSuccess;
                                if(b>=30&&b<=90&&a>=90&&a<=270){
                                    isSuccess=true;
                                }else {
                                    isSuccess=false;
                                }
                             mRegistedCallbacks.getBroadcastItem(i).onUgeePenAngle(a,b,isSuccess);
                            }else {
                                mRegistedCallbacks.getBroadcastItem(i)
                                        .onPenPosition(x, y1, p, state, 0, 0);
                            }

                        }

                    }



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assert mRegistedCallbacks != null;
        mRegistedCallbacks.finishBroadcast();
    }

    private PointAngle reportAngle(byte x, byte y) {
        int tilt_x=x;
        int tilt_y = y;
        double pi = 3.1415926535;
        double dX1 = (Math.abs(tilt_x) * pi) / 180;
        double dY1 = (Math.abs(tilt_y) * pi) / 180;
        double dXY = Math.sqrt(Math.tan(dX1) * Math.tan(dX1) + Math.tan(dY1) * Math.tan(dY1) + 1);
        double dAltitude = (Math.asin(1 / dXY) * 180.0f) / pi;
        double TanXDivTanY = Math.tan(dY1) / Math.tan(dX1);
        double Azimuth = (Math.atan(TanXDivTanY) * 180.0f) / pi;
        if ((tilt_x != 0) || (tilt_y != 0)){
            if (tilt_x > 0 && tilt_y < 0)//1
            {
                Azimuth = 90 - Azimuth;
            }
            else if (tilt_x == 0 && tilt_y < 0)//0°
            {
                Azimuth = 0;
            }
            else if (tilt_x == 0 && tilt_y > 0)//180°
            {
                Azimuth = 180;
            }
            else if (tilt_x > 0 && tilt_y == 0)//90°
            {
                Azimuth = 90;//
            }
            else if (tilt_x < 0 && tilt_y == 0)//270°
            {
                Azimuth = 270;
            }
            else if (tilt_x > 0 && tilt_y > 0)//2
            {
                Azimuth = 90 + Azimuth;//
            }

            else if (tilt_x < 0 && tilt_y > 0)//3
            {
                Azimuth = 270 - Azimuth;
            }

            else if (tilt_x < 0 && tilt_y < 0)//4
            {
                Azimuth = 270 + Azimuth;//
            }

        }else {
            Azimuth=0;
            dAltitude=90;
        }

        PointAngle pointAngle = new PointAngle();
        pointAngle.setAzimuth((int) Azimuth);
        pointAngle.setdAltitude((int) dAltitude);
        pointAngle.setX(tilt_x);
        pointAngle.setY(tilt_y);
        return pointAngle;
    }

    private class PointAngle {
        public int getAzimuth() {
            return Azimuth;
        }

        public void setAzimuth(int azimuth) {
            Azimuth = azimuth;
        }

        public int getdAltitude() {
            return dAltitude;
        }

        public void setdAltitude(int dAltitude) {
            this.dAltitude = dAltitude;
        }

        int Azimuth;
        int dAltitude;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        int x;
        int y;
    }


    /**
     * 通知硬件设备的链接状态
     *
     * @param state   状态吗
     * @param address mac地址
     */
    @Override
    public synchronized void reportState(int state, String address) {
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try {
                mRegistedCallbacks.getBroadcastItem(i).onStateChanged(state, address);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mRegistedCallbacks.finishBroadcast();
    }

    /**
     * 通知错误信息
     *
     * @param error 错误信息
     */
    @Override
    public void reportError(String error) {
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try {
                mRegistedCallbacks.getBroadcastItem(i).onPenServiceError(error);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mRegistedCallbacks.finishBroadcast();
    }
    private int connectType=0;
    /**
     * 2:名称
     * 3：序列号
     * 4:固件版本
     * 5:固件日期
     * 64：固件物理凡物
     * 67：mac地址
     * 6E：按键
     * 83：实现协议版本
     * F1：数据长度
     * @param data
     */
    @Override
    public void reportUGEE_NotifyData(byte[] data) {

        if(data[3]== UgeeCommand.UGEE_CMD_F5){
            int b=data[5];
            for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    mRegistedCallbacks.getBroadcastItem(i).onUgeeDeviceBattery(b,true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if(data[3]==UgeeCommand.UGEE_CMD_F1){
            byte []v=new byte[2];
            System.arraycopy(data,6,v,0,2);
            String version=byte2HexStr(v);
            if(version.equals("A501")){
                connectType=3;
            }else if(version.equals("A503")){
                connectType=13;
            }else {
                connectType=data[7];
            }
        }else if(data[3]==UgeeCommand.UGEE_CMD_F2){
            int width=byteUtils.bytesToInteger(data[7],data[6]);
            int height=byteUtils.bytesToInteger(data[9],data[8]);
            int pressure=byteUtils.bytesToInteger(data[11],data[10]);
            for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    //和ios SDK统一格式 输出类型为x永远小于y
                    if(width>height){
                        mRegistedCallbacks.getBroadcastItem(i).onUgeePenWidthAndHeight(height,width,pressure,true);
                    }else {
                        mRegistedCallbacks.getBroadcastItem(i).onUgeePenWidthAndHeight(width,height,pressure,false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if(data[3]==UgeeCommand.UGEE_ET_64){//ET系列
            int w,h,p;
            boolean isHorizontal;
            if(data[1]==(byte)0x01){
                w=30480;
                h=50800;
                p=8191;
                connectType=5;
                isHorizontal=true;
            }else if(data[1]==(byte)0x02){
                w=21200;
                h=30100;
                p=2047;
                connectType=11;
                isHorizontal=false;
            }else {
                w=30480;
                h=50800;
                p=8191;
                isHorizontal=true;
            }
            for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    mRegistedCallbacks.getBroadcastItem(i).onUgeePenWidthAndHeight(w,h,p,isHorizontal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if(data[3]==UgeeCommand.UGEE_CMD_F9){
            byte []v=new byte[6];
            System.arraycopy(data,10,v,0,6);
            String version=byte2HexStr(v);
        }else if(data[3]==UgeeCommand.UGEE_CMD_FF){//USB尺寸
            int w,h,p;
            if(data[4]==0x01){
                w=14800;
                h=21000;
                p=1023;
            }else if(data[4]==0x02){
                w=29452;
                h=41994;
                p=8152;
            }else {
                w=0;h=0;p=0;
            }
            for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    mRegistedCallbacks.getBroadcastItem(i).onUgeePenWidthAndHeight(w,h,p,true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if(data[1]==(byte)0xF2){
            for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                if(data[1]==(byte)0xF2){
                    if(data[4]==1){
                        sendUgeeBattery(i,data[3],true);
                    }else {
                        sendUgeeBattery(i,data[3],false);
                    }
                }else {
                    sendUgeePenWidthAndHeight(i);
                }
            }
        }
        try {
            mRegistedCallbacks.finishBroadcast();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    /**
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            //分割符 可以为空格等
            sb.append("");
        }
        return sb.toString().toUpperCase().trim();
    }
    private void sendUgeeBattery(int i,int battery,boolean isCharge){
        try {
            mRegistedCallbacks.getBroadcastItem(i).onUgeeDeviceBattery(battery,isCharge);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void sendUgeePenWidthAndHeight(int i){
        try {
            mRegistedCallbacks.getBroadcastItem(i).onUgeePenWidthAndHeight(15000, 20899, 8191, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知按键事件
     *
     * @param ev 事件
     */
    @Override
    public void reportKeyEvent(int ev) {
        for (int i = mRegistedCallbacks.beginBroadcast() - 1; i >= 0; i--) {
            try {
                mRegistedCallbacks.getBroadcastItem(i).onUgeeEvent(ev);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mRegistedCallbacks.finishBroadcast();
    }


    /**
     * 验证当前的连接状态
     *
     * @return true 当前的连接是有效的
     */
    private boolean isBleConnectionEnable() {
        if ( mBluetoothGatt != null) {
            BluetoothDevice dev = mBluetoothGatt.getDevice();
            return bluetoothManager.getConnectionState(dev, BluetoothProfile.GATT) == BluetoothGatt.STATE_CONNECTED;
        } else {
            return false;
        }
    }

    /**
     * 断开连接设备
     */
    @Override
    public void disconnectDevice(boolean isReport) {
        disconnectBluDevice();
    }


    /**
     * 断开当前连接的设备
     * Build.VERSION_CODES.JELLY_BEAN_MR2 以下的设备是不支持BLE的
     */
    private synchronized void disconnectBluDevice() {
            //断开蓝牙
            if (isBleConnectionEnable()) {
                mBluetoothGatt.disconnect();
            } else {
                //.d("不是有效的连接，无法断开");
                mDevice = null;
            }
            if (ugeeGattScan != null) {
                ugeeGattScan.sIsWriting = false;
                ugeeGattScan.queue.clear();
            }
            mBluetoothGatt = null;
            mDevice = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭蓝牙链接
        if (mBluetoothGatt != null) {
            try {
                mBluetoothGatt.disconnect();
                mBluetoothGatt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            mRegistedCallbacks.kill();
            mRegistedCallbacks = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void exitSelf() {

        if (mBindCount > 0
                || mDevice != null
                || (mBluetoothGatt != null)) {
        } else {
            stopSelf();
        }
    }
}
