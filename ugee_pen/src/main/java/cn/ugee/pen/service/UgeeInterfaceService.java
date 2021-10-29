package cn.ugee.pen.service;

import cn.ugee.pen.IUgeeServiceCallBack;
import cn.ugee.pen.model.UgeeDevice;

public interface UgeeInterfaceService {

    UgeeDevice getConnectedDevice();
    void onDeviceChanged(UgeeDevice device);
    void updateDeviceType(int deviceType);
    void registerCallBack(IUgeeServiceCallBack callback);
    void unRegisterCallBack(IUgeeServiceCallBack callback);
    void connectBle();
    void  disConnectBle();
    boolean connectBluetoothDevice(String address);
    void disconnectDevice(boolean isReport);
    void exitSelf();
    void reportState(int state, String address);
    void reportError(String error);
    void reportUGEE_NotifyData(byte[] data);
    void reportUGEE_BLEPosition(byte[]data);
    void reportKeyEvent(int keyEvent);
    void setCorrectPoint(int p);
    boolean queryBattery();
    boolean sendByteCommand(byte command);
    void reportUGEE_ETPosition(byte[]data);

}
