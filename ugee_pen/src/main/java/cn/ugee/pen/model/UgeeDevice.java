package cn.ugee.pen.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class UgeeDevice implements Parcelable {
    private int deviceVersion = 0;
    private String name;
    private String address;
    private byte[] firVer;
    private byte[] mcuVer;
    private int battery;
    private int connectType;
    private int isConnect=0;



    public UgeeDevice(String name, String address, int connType) {
        this.name = name;
        this.address = address;
        this.connectType = connType;
        firVer = new byte[0];
    }

    protected UgeeDevice(Parcel in) {
        this.deviceVersion = in.readInt();
        this.isConnect=in.readInt();
        this.name = in.readString();
        this.address = in.readString();
        this.firVer = in.createByteArray();
        this.battery = in.readByte();
        this.connectType = in.readInt();
        this.mcuVer = in.createByteArray();
    }

    public static final Creator<UgeeDevice> CREATOR = new Creator<UgeeDevice>() {
        @Override
        public UgeeDevice createFromParcel(Parcel source) {
            return new UgeeDevice(source);
        }

        @Override
        public UgeeDevice[] newArray(int size) {
            return new UgeeDevice[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.deviceVersion);
        dest.writeInt(this.isConnect);
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeByteArray(this.firVer);
        dest.writeInt(battery);
        dest.writeInt(connectType);
        dest.writeByteArray(this.mcuVer);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public void setFirVer(byte[] firmwareVer) {
        this.firVer = firmwareVer;
    }


    @Deprecated
    public String getFirVerStr() {
        return getVStr(firVer);
    }

    public void setConnectType(int connectType){
        this.connectType=connectType;
    }
    @Deprecated
    public int getDeviceType() {
        return deviceVersion;
    }
    public int getDeviceVersion(){
        return deviceVersion;
    }
        public String getAddress(){
        return address;
}

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDeviceVersion(int deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    private String getVStr(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            sb.append(((int) b & 0xff)).append(".");
        }
        int len = sb.length();
        return len > 1 ? sb.substring(0, len - 1) : "";
    }


    @Deprecated
    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setIsConnect(int isConnect) {
        this.isConnect = isConnect;
    }

    public void setMcuVer(byte[] mcuVer) {
        this.mcuVer = mcuVer;
    }

    public byte[] getFirVer() {
        return firVer;
    }

    public byte[] getMcuVer() {
        return mcuVer;
    }

    public int getConnectType() {
        return connectType;
    }

    public int getIsConnect() {
        return isConnect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Device{" +
                "deviceVersion=" + deviceVersion +
                ",isConnect=" + isConnect +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", firVer=" + Arrays.toString(firVer) +
                ", battery=" + battery +
                ", connectType=" + connectType +
                '}';
    }
}
