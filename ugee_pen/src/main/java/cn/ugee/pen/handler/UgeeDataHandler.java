package cn.ugee.pen.handler;

import android.util.Log;

import cn.ugee.pen.model.UgeeCommand;
import cn.ugee.pen.service.UgeeInterfaceService;
import cn.ugee.pen.util.ByteUtil;

public class UgeeDataHandler  extends BaseHandler<byte[]>{
    public UgeeDataHandler(UgeeInterfaceService servicePresenter) {
        super(servicePresenter);
    }
    @Override
    public void handle(byte[] data) {
   Log.e("onUgeeData","data="+ ByteUtil.byte2HexStr(data));
        if (data[0] == UgeeCommand.UGEE_CMD_FD && data[1] == UgeeCommand.UGEE_CMD_FD) {
            if (data[2] == 3) {//A5数据协议
                if (data[3] == UgeeCommand.UGEE_CMD_F0 && data.length == 12 && data[4] > 0) {//a41b 按键返回
                    servicePresenter.reportKeyEvent(data[4]);
                } else {
                    servicePresenter.reportUGEE_BLEPosition(data);
                }

            } else if (data[2] == 2) {//ET系列数据和A4等查询返回命令
                int len = data.length;
                if (data[3] == UgeeCommand.UGEE_CMD_F1 || data[3] == UgeeCommand.UGEE_CMD_F5 || data[3] == UgeeCommand.UGEE_CMD_F2) {
                    if (data[3] == UgeeCommand.UGEE_CMD_F1) {
                        servicePresenter.sendByteCommand(UgeeCommand.UGEE_CMD_F5);
                    } else if (data[3] == UgeeCommand.UGEE_CMD_F5) {
                        servicePresenter.sendByteCommand(UgeeCommand.UGEE_CMD_F2);
                    }
                    servicePresenter.reportUGEE_NotifyData(data);
                } else {
                    if (len % 10 == 0) {
                        servicePresenter.reportUGEE_ETPosition(data);
                    }
                }
            } else if (data[2] == 4 && data.length % 10 == 0) {//ET系列查询命令
                byte[] bytes = new byte[4];
                bytes[0] = 0;
                bytes[1] = 0x01;
                bytes[2] = 0;
                bytes[3] = 0x64;
                servicePresenter.reportUGEE_NotifyData(bytes);
            }
        } else if (data[2] == 4 && data.length % 10 == 0) {//ET系列查询命令
            byte[] bytes = new byte[4];
            bytes[0] = 0;
            bytes[1] = 0x01;
            bytes[2] = 0;
            bytes[3] = 0x64;
            servicePresenter.reportUGEE_NotifyData(bytes);
        } else if (data[2] != 2 && data.length >= 7) {
            servicePresenter.reportUGEE_BLEPosition(data);
        } else if (data[0] == 2) {
            if (data[1] == (byte) 0xF2) {// 电池电量
                servicePresenter.reportUGEE_NotifyData(data);
            } else {
                servicePresenter.reportUGEE_BLEPosition(data);
            }
        } else if (data[0] == 80 && data[1] == 6) {//返回查询命令
            servicePresenter.reportUGEE_NotifyData(data);
        } else if (data.length == 9) {//暂时处理异常
            servicePresenter.reportUGEE_NotifyData(data);
        } else {
            if (nextBaseHandler != null)
                nextBaseHandler.handle(data);
        }


    }

}
