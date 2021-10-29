package cn.ugee.pen.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.ugee.pen.handler.BaseHandlerManager;
import cn.ugee.pen.handler.ErrorHandler;
import cn.ugee.pen.handler.UgeeDataHandler;
import cn.ugee.pen.model.ConnectState;
import cn.ugee.pen.model.UgeeCommand;
import cn.ugee.pen.model.UgeeDevice;
import cn.ugee.pen.util.ByteUtil;

public class UgeeGattScan extends BluetoothGattCallback {
    private final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private final UUID PEN_DATA_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private final UUID PEN_WRITE_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private final UUID NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final UUID NOTIFICATION_DESCRIPTOR_UUID_PEN = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private final UUID UGEE_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final UUID UGEE_WRITE_UUID = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    private final UUID UGEE_QUERY_UUID = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");
    private final UUID UGEE_DATA_UUID = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");
    private BluetoothGattCharacteristic mPenDataCharacteristic;//坐标
    private BluetoothGattCharacteristic mPenWriteCharacteristic;//写入
    private BluetoothGattCharacteristic mQueryDataCharacteristic;//查询

    private UgeeInterfaceService servicePresenter;
    private BluetoothGatt mBluetoothGatt;
    private ByteUtil byteUtils;
    private BaseHandlerManager<byte[]> gattaBaseHandlerManager;
    public volatile boolean sIsWriting = false;
    public Queue queue;
    private int STATES=0;//关闭硬件蓝牙，用mac地址强制连接，提示错误信息
    private boolean isET;//ET系列板子
    public UgeeGattScan(UgeeInterfaceService presenter) {
        this.servicePresenter = presenter;
        queue = new ConcurrentLinkedQueue();
        this.gattaBaseHandlerManager = new BaseHandlerManager.HandlersBuilder<byte[]>()
                .addHandler(new UgeeDataHandler(servicePresenter))
                .addHandler(new ErrorHandler(servicePresenter))
                .build();
        this.byteUtils = new ByteUtil();
        mBluetoothGatt=null;
    }
    private String bName="";
    @Override
    public void onConnectionStateChange(BluetoothGatt gatts, int status, int newState) {
        super.onConnectionStateChange(gatts, status, newState);
        mBluetoothGatt = gatts;
        String address = gatts.getDevice().getAddress();
        String name=gatts.getDevice().getName();
        bName=gatts.getDevice().getName();
        bName=gatts.getDevice().getName();
        if(!TextUtils.isEmpty(name)&&name.contains("ET")){
            isET=true;
        }else {
            isET=false;
        }
        if (BluetoothGatt.STATE_CONNECTED == newState && status == 0) {//&& status != 133
            mBluetoothGatt.discoverServices();
            BluetoothDevice bluetoothDevice = mBluetoothGatt.getDevice();
            UgeeDevice ugeeDevice = new UgeeDevice(
                    bluetoothDevice.getName(),
                    bluetoothDevice.getAddress(), 0);
            servicePresenter.onDeviceChanged(ugeeDevice);
            servicePresenter.updateDeviceType(2);
            STATES=0;
        } else {
            if (status == 133) {
                mBluetoothGatt.close();
                servicePresenter.onDeviceChanged(null);
                mBluetoothGatt.disconnect();
                queue.clear();
                sIsWriting = false;
                    servicePresenter.reportState(ConnectState.STATE_DISCONNECTED, address);
            } else {
                STATES=0;
                servicePresenter.reportState(status == 0 ? newState : ConnectState.STATE_DISCONNECTED, address);//STATE_DISCONNECTED
                mBluetoothGatt.close();
                servicePresenter.onDeviceChanged(null);
                mBluetoothGatt.disconnect();
                queue.clear();
                sIsWriting = false;
                servicePresenter.disConnectBle();
            }
        }

    }


    private boolean isUGEE;

    @Override
    public void onServicesDiscovered(BluetoothGatt gatts, int status) {
        super.onServicesDiscovered(gatts, status);
        mPenDataCharacteristic = null;
        mPenWriteCharacteristic = null;
        if (BluetoothGatt.GATT_SUCCESS == status) {
            for (BluetoothGattService service : gatts.getServices()) {
                if (UGEE_SERVICE_UUID.toString().equals(service.getUuid().toString())) {
                    mPenDataCharacteristic = service.getCharacteristic(UGEE_DATA_UUID);
                    mQueryDataCharacteristic = service.getCharacteristic(UGEE_QUERY_UUID);
                    mPenWriteCharacteristic = service.getCharacteristic(UGEE_WRITE_UUID);
                    if(isET){
                        mPenWriteCharacteristic = service.getCharacteristic(UGEE_DATA_UUID);
                    }else {
                        mPenWriteCharacteristic = service.getCharacteristic(UGEE_WRITE_UUID);
                    }
                    isUGEE = true;
                    break;
                } else if (service.getUuid().equals(SERVICE_UUID)) {
                    mPenDataCharacteristic = service.getCharacteristic(PEN_DATA_UUID);
                    mPenWriteCharacteristic = service.getCharacteristic(PEN_WRITE_UUID);
                    isUGEE = false;
                    break;
                }
            }
        }

        if (mPenDataCharacteristic != null && mPenWriteCharacteristic != null) {
            mBluetoothGatt.setCharacteristicNotification(mPenDataCharacteristic, true);
            mBluetoothGatt.setCharacteristicNotification(mQueryDataCharacteristic, true);
            BluetoothGattDescriptor descriptor = null;
            BluetoothGattDescriptor queryDescriptor = null;
            BluetoothGattDescriptor writeDescriptor=null;
            if (isUGEE) {
                descriptor = mPenDataCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID_PEN);
                queryDescriptor = mQueryDataCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID_PEN);
            } else {
                descriptor = mPenDataCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID);
            }

            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                write(descriptor);

                if (queryDescriptor != null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    queryDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    write(queryDescriptor);
                }
            }

            if (isUGEE) {

                byte[] dataKey;
                if(!isET){
                    if(bName.contains("HUA503B")){
                        byte []data=new byte[10];
                        data[0] = (byte) 0xFD;
                        data[1] = (byte) 0xFD;
                        data[2] = 0x01;
                        data[3] = (byte) 0xF2;
                        data[4] = 0x00;
                        data[5] = 0x00;
                        data[6] = 0x00;
                        data[7] = 0x00;
                        data[8] = 0x00;
                        data[9] = 0x00;
                        mPenWriteCharacteristic.setValue(data);
                    }else if(bName.contains("BKGA518")){
                        //凤凰项目
                        boolean b=   sendMessage((byte)0x02);
                         dataKey = new byte[9];
                        dataKey[0] = (byte) 0x80;
                        dataKey[1] =(byte) 0x06;
                        dataKey[2] = (byte)0x64;
                        dataKey[3] = 0x00;
                        dataKey[4] = 0x00;
                        dataKey[5] = 0x00;
                        dataKey[6] = 0x00;
                        dataKey[7] = 0x00;
                        dataKey[8] = 0x00;
                    }else {
                        dataKey = new byte[10];
                        dataKey[0] = (byte) 0xFD;
                        dataKey[1] = (byte) 0xFD;
                        dataKey[2] = 0x01;
                        dataKey[3] = (byte) 0xF1;
                        dataKey[4] = 0x00;
                        dataKey[5] = 0x00;
                        dataKey[6] = 0x00;
                        dataKey[7] = 0x00;
                        dataKey[8] = 0x00;
                        dataKey[9] = 0x00;
                        mPenWriteCharacteristic.setValue(dataKey);
                    }
                    write(mPenWriteCharacteristic);
                }else {
                    if(bName.contains("ETA501")){
                        dataKey = new byte[8];
                        dataKey[0] = (byte) 0x02;
                        dataKey[1] = (byte)0x64;
                        dataKey[2] = 0x00;
                        dataKey[3] = 0x00;
                        dataKey[4] = 0x00;
                        dataKey[5] = 0x00;
                        dataKey[6] = 0x00;
                        dataKey[7] = 0x00;
                        mPenWriteCharacteristic.setValue(dataKey);
                    }else if(bName.contains("ETA403")){
                        byte[] bytes=new byte[4];
                        bytes[0] = 0;
                        bytes[1] = 0x02;
                        bytes[2] = 0;
                        bytes[3] = 0x64;
                        servicePresenter.reportUGEE_NotifyData(bytes);
                    }

                }
            }
            servicePresenter.reportState(ConnectState.STATE_DEVICE_INFO, "");
        }
    }
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        byte[] data = characteristic.getValue();
        gattaBaseHandlerManager.handle(data);

    }


    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        sIsWriting = false;
        nextWrite();
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        sIsWriting = false;
        nextWrite();
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
    }

    /**
     * 向连接的蓝牙设备发送消息
     *
     * @param command 命令
     */
    public synchronized boolean sendMessage(byte command) {
        byte[] dataKey;
        if(command== UgeeCommand.UGEE_CMD_F5||command==UgeeCommand.UGEE_CMD_F2){
            dataKey = new byte[10];
            dataKey[0] = (byte) 0xFD;
            dataKey[1] = (byte) 0xFD;
            dataKey[2] = 0x01;
            dataKey[3] = command;
            dataKey[4] = 0x00;
            dataKey[5] = 0x00;
            dataKey[6] = 0x00;
            dataKey[7] = 0x00;
            dataKey[8] = 0x00;
            dataKey[9] = 0x00;
        }else {
            dataKey = new byte[10];
            dataKey[0] = (byte) 0x02;
            dataKey[1] =(byte) 0xB8;
            dataKey[2] = (byte)0x04;;
            dataKey[3] = 0x00;
            dataKey[4] = 0x00;
            dataKey[5] = 0x00;
            dataKey[6] = 0x00;
            dataKey[7] = 0x00;
            dataKey[8] = 0x00;
            dataKey[9] = 0x00;
        }

        if (mBluetoothGatt != null) {
            mPenWriteCharacteristic.setValue(dataKey);
            if (!write(mPenWriteCharacteristic)) {
                return false;
            }
            return true;
        }
        return false;
    }



    private synchronized boolean write(Object o) {
        boolean flag = false;
        if (queue.isEmpty() && !sIsWriting) {
            flag = doWrite(o);
        } else {
            queue.add(o);
        }
        return flag;
    }


    private synchronized boolean nextWrite() {
        if (!queue.isEmpty() && !sIsWriting) {
            return doWrite(queue.poll());
        }
        return false;
    }


    private synchronized boolean doWrite(Object o) {
        boolean flag = false;
        if (o instanceof BluetoothGattCharacteristic) {
            sIsWriting = true;
            flag = mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
        } else if (o instanceof BluetoothGattDescriptor) {
            sIsWriting = true;
            flag = mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) o);
        } else {
            nextWrite();
        }
        return flag;
    }
}
