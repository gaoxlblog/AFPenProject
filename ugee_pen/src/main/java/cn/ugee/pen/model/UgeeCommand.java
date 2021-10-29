package cn.ugee.pen.model;

public class UgeeCommand {
    public static final byte UGEE_CMD_FD=(byte)0xFD;           //标头
    public static final byte UGEE_CMD_F0=(byte)0xF0;            //a41b,返回按键信息
    public static final byte UGEE_CMD_F1=(byte)0xF1;           //返回版本信息
    public static final byte UGEE_CMD_F2=(byte)0xF2;           //返回解锁信息
    public static final byte UGEE_CMD_F3=(byte)0xF3;           //返回物理/虚拟按键数，板子的物理宽度和长度
    public static final byte UGEE_CMD_F4=(byte)0xF4;           //返回mac地址
    public static final byte UGEE_CMD_F5=(byte)0xF5;           //返回电量信息
    public static final byte UGEE_CMD_F6=(byte)0xF6;           //返回当前模式
    public static final byte UGEE_CMD_F7=(byte)0xF7;           //返回当前灯光亮度
    public static final byte UGEE_CMD_F9=(byte)0xF9;
    public static final byte UGEE_CMD_FF=(byte)0xFF;            //usb 返回宽度信息

    //ET系列查询命令
    public static final byte UGEE_ET_64=(byte)0x64;           //查询板子的物理宽度和长度
}
