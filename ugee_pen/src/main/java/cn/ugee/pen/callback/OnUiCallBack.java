package cn.ugee.pen.callback;

public interface OnUiCallBack {

    /**
     *
     * @param state 连接状态
     */
    void onStateChanged(int state);
    /**
     *  返回错误信息
     * @param msg
     */

    void onPenServiceError(String msg);

    /**
     *
     * @param x
     * @param y
     * @param pressure
     * @param state
     * @param stateIos
     * @param width
     */
    void onPenPositionChanged( float x, float y, float pressure, int state,int stateIos,float width);
    /**
     *
     * @param e 状态值
     */
    void onUgeeKeyEvent(int e);

    /**
     * 电量状态
     * @param b 当前电量值
     * @param isCharge 是否处于充电中；1--充电；0--非充电
     */

    void onUgeeBattery(int b,boolean isCharge);

    void onUgeePenWidthAndHeight(int width,int height,int pressure,boolean isHorizontal);

    /**
     * 返回书写姿势
     * @param quadrant 笔落在四个象限
     * @param degree  笔与纸张的倾斜角度
     * @param isSuccess  书写姿势是否正确
     */
    void onUgeePenAngle(int quadrant,int degree,boolean isSuccess);

    /**
     * 返回校准点信息
     * @param point 对应的校准点
     * @param isSuccess 是否正确
     */
    void onCorrectInfo(int point,boolean isSuccess);

    /**
     *  点阵笔数据
     * @param x
     * @param y
     * @param type
     * @param page
     */

    void onAFPenPositionChanged(int x, int y, int type,int page);
}
