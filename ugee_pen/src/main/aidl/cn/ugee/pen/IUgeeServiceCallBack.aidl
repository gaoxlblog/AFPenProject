// IUgeeServiceCallBack.aidl
package cn.ugee.pen;


interface IUgeeServiceCallBack {
      void onStateChanged(int state,String addr);
      void onPenServiceError(String msg);
      void onPenPosition(float x,float y,float presure,int state,int stateIos,float width);
      void onUgeeEvent(int e);
      void onUgeeDeviceBattery(int battery,boolean isCharge);
      void onUgeePenWidthAndHeight(int width,int heigh,int pressure,boolean isHorizontal);
      void onCorrectInfo(int point,boolean isSuccess);
      void onUgeePenAngle(int quadrant,int degree,boolean isSuccess);
}
