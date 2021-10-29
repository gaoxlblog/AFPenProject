// IUgeeService.aidl
package cn.ugee.pen;

import cn.ugee.pen.IUgeeServiceCallBack;
import cn.ugee.pen.model.UgeeDevice;

// Declare any non-default types here with import statements

interface IUgeeService {
      void registerCallBack(IUgeeServiceCallBack callback);
       void unRegisterCallBack(IUgeeServiceCallBack callback);
       boolean connectDevice(String macAddr);
       void disconnectDevice();
       UgeeDevice getConnectedDevice();
       void setCorrectPoint(int point);
       boolean queryUgeeBattery();
}
