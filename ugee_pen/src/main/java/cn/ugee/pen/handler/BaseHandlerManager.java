package cn.ugee.pen.handler;

public class BaseHandlerManager<D> {
    private BaseHandler<D> baseHandler;

    private BaseHandlerManager(BaseHandler<D> h) {
        this.baseHandler = h;
    }

    public void handle(D data) {
        if(baseHandler!=null){
            baseHandler.handle(data);
        }
    }

    public static class HandlersBuilder<D> {
        private BaseHandler<D> header;
        private BaseHandler<D> tail;

        public HandlersBuilder() {
            header = null;
            tail = null;
        }

        public HandlersBuilder<D> addHandler(BaseHandler<D> baseHandler) {
            if (header == null) {
                this.header = baseHandler;
                this.tail = baseHandler;
            } else {
                this.tail.setNextBaseHandler(baseHandler);
                this.tail = baseHandler;
            }
            return this;
        }

        public BaseHandlerManager<D> build() {
            return new BaseHandlerManager<>(header);
        }
    }
}
