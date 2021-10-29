package cn.ugee.pen.handler;

import cn.ugee.pen.service.UgeeInterfaceService;

public class ErrorHandler extends BaseHandler<byte[]>{
    public ErrorHandler(UgeeInterfaceService servicePresenter) {
        super(servicePresenter);
    }

    @Override
    public void handle(byte[] data) {
        if (data == null || data.length < 3) {
            //异常数据暂不处理
        } else {
            if (nextBaseHandler != null) {
                nextBaseHandler.handle(data);
            }
        }
    }
}
