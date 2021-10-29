package cn.ugee.pen.ble;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UgeeScanClass {

    private BluetoothAdapter mBluetoothAdapter;
    private WeakReference<UgeeScanCallBack> mCallback;
    //默认扫描时间：5s
    private static final int SCAN_TIME = 5000;
    private BleHandler bleHandler;

    private UgeeScanClass() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public UgeeScanClass(UgeeScanCallBack ugeeScanCallback, Context context) {
        this();
        this.mCallback = new WeakReference<>(ugeeScanCallback);
        bleHandler=new BleHandler(context);
    }


    private boolean isBluetoothEnable() {
        return !mBluetoothAdapter.isEnabled();
    }

    /**
     * 开始扫描
     */
    public void startScan(int time) {
        if (mCallback == null) {
            return;
        }
        stopScan();
        UgeeScanCallBack ugeeScanCallback = mCallback.get();
        if (ugeeScanCallback == null) {
            return;
        }
        Object callback = ugeeScanCallback.getScanCallback();
        if (isBluetoothEnable() || callback == null) {
            return;
        }
        try {
            bleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //time后停止扫描
                    stopScan();
                }
            }, time <= 0 ? SCAN_TIME : time);
        } catch (Exception e) {
            e.printStackTrace();
        }
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            List<ScanFilter> filters = new ArrayList<>();


            ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
            scanFilterBuilder.setServiceUuid(ParcelUuid.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
            // filters.add(scanFilterBuilder.build());
            //filters.add(filter); //屏蔽掉是因为发现 不能成功
            mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings,
                    (android.bluetooth.le.ScanCallback) callback);
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if (mCallback == null) {
            return;
        }
        UgeeScanCallBack ugeeScanCallback = mCallback.get();
        if (ugeeScanCallback == null) {
            return;
        }
        Object callback = ugeeScanCallback.getScanCallback();
        if (isBluetoothEnable() || callback == null) {
            return;
        }
            mBluetoothAdapter.getBluetoothLeScanner()
                    .stopScan((android.bluetooth.le.ScanCallback) callback);
        bleHandler.removeCallbacksAndMessages(null);
    }
    @SuppressLint("HandlerLeak")
    private  class BleHandler  extends Handler {
        private final WeakReference reference;

        BleHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            if (reference.get() != null) {
                switch (msg.what) {
                    case 0x1001:
                        stopScan();
                        break;
                    default:
                        break;
                }

            }
        }
    }
}
