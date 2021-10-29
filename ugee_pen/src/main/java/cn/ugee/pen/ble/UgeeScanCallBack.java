package cn.ugee.pen.ble;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

public abstract class UgeeScanCallBack {

    private Object mScanCallback;
    private void ScanData(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new android.bluetooth.le.ScanCallback() {
                @Override
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice bluetoothDevice=result.getDevice();
                    String address=bluetoothDevice.getAddress();
                    String name=bluetoothDevice.getName();
                    if( !TextUtils.isEmpty(address) ){
                        String[] addressDevice=address.split(":");
                        if( addressDevice.length>1 ){
                            if(!TextUtils.isEmpty(name)&&(name.contains("BPUA")
                                    ||name.contains("FM-A")
                                    ||name.contains("UB")||name.contains("A5")
                                    ||name.contains("HU")||name.contains("ET")
                                    ||name.contains("BKGA")||name.contains("60WS"))){
                                scanFilter(result.getDevice(), result.getRssi());
                            }

                        }
                    }
                }
            };
        }

    }
    public UgeeScanCallBack() {
        ScanData();
    }

    /**
     * 扫描结果
     * @param
     * @return
     */
    private void scanFilter(BluetoothDevice device, int rs) {
        onBleScanResult(device, rs);
    }

    public Object getScanCallback() {
        return mScanCallback;
    }

    /**
     * 返回扫描结果
     *
     * @param device   蓝牙设备
     * @param rsi     信号强度
     */
    public abstract void onBleScanResult(BluetoothDevice device, int rsi);

    public abstract void onBleScanFailed(int error);
}
