package cn.ugee.pen.service;

import android.os.RemoteException;

import cn.ugee.pen.IUgeeService;
import cn.ugee.pen.IUgeeServiceCallBack;
import cn.ugee.pen.model.UgeeDevice;

public class UgeeBinderService  extends IUgeeService.Stub{

    private UgeeInterfaceService binderPresenter;

    public UgeeBinderService(UgeeInterfaceService presenter) {
        this.binderPresenter = presenter;
    }

    public void registerCallBack(IUgeeServiceCallBack callback) throws RemoteException {
        this.binderPresenter.registerCallBack(callback);
    }

    public void unRegisterCallBack(IUgeeServiceCallBack callback) throws RemoteException {
        this.binderPresenter.unRegisterCallBack(callback);
    }



    public boolean connectDevice(String res) throws RemoteException {
        return binderPresenter.connectBluetoothDevice(res);
    }


    public void disconnectDevice() throws RemoteException {
        binderPresenter.disconnectDevice(true);
    }


    public UgeeDevice getConnectedDevice() throws RemoteException {
        return binderPresenter.getConnectedDevice();
    }
    public void setCorrectPoint(int point){
            binderPresenter.setCorrectPoint(point);
    }

    @Override
    public boolean queryUgeeBattery() throws RemoteException {
        return binderPresenter.queryBattery();
    }
}
