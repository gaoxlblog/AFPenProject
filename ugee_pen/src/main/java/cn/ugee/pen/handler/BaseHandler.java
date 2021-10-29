package cn.ugee.pen.handler;

import cn.ugee.pen.service.UgeeInterfaceService;
import cn.ugee.pen.util.ByteUtil;

public abstract class BaseHandler<D>{
    protected UgeeInterfaceService servicePresenter;
    protected BaseHandler<D> nextBaseHandler;
    protected ByteUtil byteUtils;

    public BaseHandler(UgeeInterfaceService servicePresenter) {
        this.servicePresenter = servicePresenter;
        byteUtils = new ByteUtil();
    }

    public abstract void handle(D data);

    public void setNextBaseHandler(BaseHandler<D> baseHandler) {
        this.nextBaseHandler = baseHandler;
    }
}
