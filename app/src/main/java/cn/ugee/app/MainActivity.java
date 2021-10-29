package cn.ugee.app;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ugee.pen.ble.UgeeScanCallBack;
import cn.ugee.pen.ble.UgeeScanClass;
import cn.ugee.pen.callback.OnUiCallBack;
import cn.ugee.pen.callback.UgeePenClass;
import cn.ugee.pen.model.ConnectState;
import cn.ugee.pen.model.UgeeDevice;
import pub.devrel.easypermissions.EasyPermissions;
@SuppressLint("NonConstantResourceId")
public class MainActivity extends Activity implements
        PopupMenu.OnMenuItemClickListener,ScanResultAdapter.MyClickListener,
        OnUiCallBack {

 @BindView(R.id.bt_screen)
    Button btScreen;
    @BindView(R.id.bt_title)
    Button btTitle;
    @BindView(R.id.bt_clean)
    Button btClean;
   @BindView(R.id.bt_photo)
    Button btPhoto;
  @BindView(R.id.bt_1)
    Button bt1;//临时测试压力值
  @BindView(R.id.bt_2)
    Button bt2;//临时测试最大宽度
   @BindView(R.id.rlv)
    RecyclerView recyclerView;
    @BindView(R.id.tv_ble)
    TextView tvBle;
    @BindView(R.id.tv_ble_name)
    TextView tvBleName;
    @BindView(R.id.tv_battery)
    TextView tvBattery;
    @BindView(R.id.tv_d)
    TextView tv_d;
    @BindView(R.id.iv_battery)
    ImageView ivBattery;
    @BindView(R.id.view_matrix)
    WriteMatrixView myView;
    @BindView(R.id.ll_ble)
    LinearLayout llBle;
    @BindView(R.id.tv_x)
    TextView tv_x;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.bt_break)
    Button btBreak;
    @BindView(R.id.tv_my_b)
    TextView tvMyB;
    private   PopupMenu popup;
    private PenSeekBar penSeekBar;
    private UgeeScanClass ugeeScanClass;

    private ScanResultAdapter adapter;
    private UgeeScanCallBack scanCallback;
    private UnLoginDialog dialog;
    private String mac = "";
    private  LinearLayout.LayoutParams params;
    public UgeePenClass ugeePenClass;
    private String address;//mac地址
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        if(isPad(this)){
//            MainActivity.this.  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            setContentView(R.layout.activity_land);
//        }else {
//            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            setContentView(R.layout.activity_main);
//        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind( this );
        initView();
        EventBus.getDefault().register(this);
        scanCallback = new MyScanCallback(this);
        ugeeScanClass = new UgeeScanClass(scanCallback,this);
        dialog = new UnLoginDialog(this);
        checkPermission();
        createMenu(btTitle);

    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     * @param context
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initView(){

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {// 布局管理器
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ScanResultAdapter(this, this);
        recyclerView.setAdapter(adapter);
       ugeePenClass=new UgeePenClass(this,this);
        try {
            if(ugeePenClass.getConnectedDevice()!=null){
                llBle.setVisibility(View.GONE);
            }else {
                llBle.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        myView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                myView.setMix(true);
                switch (MotionEvent.ACTION_MASK & event.getAction())

                {

                    case MotionEvent.ACTION_DOWN:
                        mode = TRANSLATION_MODE;
                        myView.setStartPoint(new Point((int)event.getX(), (int)event.getY()));
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_UP:
                        mode = NULL_MODE;
                        myView.savePreviousResult();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = SCALE_MODE;
                        myView.setInitDistance(calculateDistance(event));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(mode == SCALE_MODE)
                        {
                            float dis = calculateDistance(event);
                            myView.zoomIn(dis);
                        }
                        else if(mode == TRANSLATION_MODE)
                        {
                            myView.setMovePoint(new Point((int)event.getX(), (int)event.getY()));
                        }
                        else
                        {
                            Log.i("unknow mode tag","do nothing......");
                        }
                        break;
                }
                myView.invalidate();
                return true;
            }
        });

    }

    /**
     * 校验蓝牙是否打开
     * 6.0以上使用蓝牙的相关权限是否具备
     * ACCESS_COARSE_LOCATION 必须校验
     */
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //读取sd卡的权限
            String[] mPermissionList = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            if (EasyPermissions.hasPermissions(this, mPermissionList)) {
                if(adapter!=null){
                    adapter.clearData();
                    adapter.notifyDataSetChanged();
                }
            } else {
                //未同意过,或者说是拒绝了，再次申请权限
                EasyPermissions.requestPermissions(
                        this,  //上下文
                        "保存图片需要读取sd卡的权限", //提示文言
                        10, //请求码
                        mPermissionList //权限列表
                );
            }
        } else {
            if(adapter!=null){
                adapter.clearData();
                adapter.notifyDataSetChanged();
            }
        }
    }
    /**
     * 开始扫描Ble设备--带过滤
     */
    public void startScan() {
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        myRss0=0;
        handler.removeCallbacksAndMessages(null);
        dialog.show();
        pairedDeviceList.clear();
        ugeeScanClass.startScan(1000*5);
        handler.sendEmptyMessageDelayed(0x1003,3000);
    }

    /**
     * 停止扫描Ble设备
     */
    public void stopScan() {
        if(ugeeScanClass !=null){
            ugeeScanClass.stopScan();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            if(ugeePenClass!=null&&ugeePenClass.getConnectedDevice()!=null){
                llBle.setVisibility(View.GONE);
            }else {
                llBle.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(PenMsg message) {
       if(!message.isConnect()){
              ugeePenClass.disconnectDevice();
       }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
        if(ugeePenClass!=null){
            ugeePenClass.onUgeePenDestroy();
        }
    }


    @OnClick({R.id.bt_title,R.id.tv_ble,R.id.bt_clean,R.id.bt_photo,
            R.id.bt_1,R.id.bt_2 ,R.id.bt_break})
    public void onViewClicked(View v){
        switch (v.getId()){
            case R.id.bt_title:
                setBgColor(v,0xff888888);
                startMenu(v);
                break;
            case R.id.tv_ble:
                setBgColor(v,0xff0076ff);
                dialog.dismiss();
                stopScan();
                adapter.clearData();
                startScan();
//                if(ugeePenClass!=null){
//                    ugeePenClass.disconnectDevice();
//                }
                //防止快速点击
           // handler.sendEmptyMessageDelayed(0x1003,3000);
                break;
            case R.id.bt_clean:
                setBgColor(v,0xff888888);
                //   writeView.cleanScreen();
                if(myView!=null){
                    myView.cleanScreen();
                }
                break;


            case R.id.bt_1:
                startPressureMenu(v);
                break;
            case R.id.bt_2:
                startWidthMenu(v);
                break;
            case R.id.bt_break:
                if(ugeePenClass!=null){
                    ugeePenClass.disconnectDevice();
                }
                break;
        }
    }


    private   String readTxt( String path){
        String strResult = "";
        try {
            File urlFile = new File(path);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String mimeTypeLine = null ;
            while ((mimeTypeLine = br.readLine()) != null) {
                strResult = strResult+mimeTypeLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  strResult;
    }

    private void setBgColor(View v,int color){
        ValueAnimator animator = ObjectAnimator.ofInt(v, "backgroundColor", color, 0x00f0f0f0);//对背景色颜色进行改变，操作的属性为"backgroundColor",此处必须这样写，不能全小写,后面的颜色为在对应颜色间进行渐变
        animator.setDuration(200);
        animator.setEvaluator(new ArgbEvaluator());//如果要颜色渐变必须要ArgbEvaluator，来实现颜色之间的平滑变化，否则会出现颜色不规则跳动
        animator.start();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    private void startMenu(View v){
//        //创建弹出式菜单对象（最低版本11）
//         popup = new PopupMenu(this, v);//第二个参数是绑定的那个view
//        //获取菜单填充器
//        MenuInflater inflater = popup.getMenuInflater();
//        //填充菜单
//        inflater.inflate(R.menu.popupmenu_menu, popup.getMenu());
//        //绑定菜单项的点击事件
//        popup.setOnMenuItemClickListener( this);
        //显示(这一行代码不要忘记了)
        popup.show();
    }
    private void createMenu(View v){
        //创建弹出式菜单对象（最低版本11）
        popup = new PopupMenu(this, v);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        //填充菜单
        inflater.inflate(R.menu.popupmenu_menu, popup.getMenu());
        //绑定菜单项的点击事件
        popup.setOnMenuItemClickListener( this);
        //显示(这一行代码不要忘记了)
     //   popup.show();
    }
    private void startPressureMenu(View v){
        //创建弹出式菜单对象（最低版本11）
        popup = new PopupMenu(this, v);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        //填充菜单
        inflater.inflate(R.menu.pop_pressure, popup.getMenu());
        //绑定菜单项的点击事件
        popup.setOnMenuItemClickListener( this);
        //显示(这一行代码不要忘记了)
        popup.show();
    }
    private void startWidthMenu(View v){
        //创建弹出式菜单对象（最低版本11）
      PopupMenu  popup = new PopupMenu(this, v);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        //填充菜单
        inflater.inflate(R.menu.popwidth, popup.getMenu());
        //绑定菜单项的点击事件
        popup.setOnMenuItemClickListener( this);
        //显示(这一行代码不要忘记了)
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.finish:
                popup.dismiss();
                break;
            case R.id.ble://连接蓝牙
                try {
                    if(ugeePenClass.getConnectedDevice()!=null){
                        UgeeDevice ugeeDevice=ugeePenClass.getConnectedDevice();
                        Intent intent=new Intent(MainActivity.this,DeviceInfoActivity.class);
                        intent.putExtra("device",ugeeDevice);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                popup.dismiss();
                break;
            case R.id.clean://清除屏幕字迹
//                if(writeView !=null){
//                    writeView.cleanScreen();
//                }
                if(myView!=null){
                    myView.cleanScreen();
                }
                popup.dismiss();
                break;
            case R.id.size://设置字体宽度
             penSeekBar=new PenSeekBar(this, myView);
                penSeekBar.initDialog();
                break;
            case R.id.bt_delete:
                setBgColor(item.getActionView(),0xff888888);
                boolean isDelete=  LogToFile.deleteFile(LogToFile.logPath+"/log.txt");
                if(isDelete){
                    Toast.makeText(this,"删除成功!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"删除失败!",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_txt:
                setBgColor(item.getActionView(),0xff888888);
                dialog.show4Text("加载数据中...");
                handler.sendEmptyMessageDelayed(0x1005,500);
                break;
            case R.id.bt_next:
                setBgColor(item.getActionView(),0xff888888);
                if(myView!=null){
                    myView.cleanScreen();
                    spiltResult();
                }
                break;
            case R.id.pressure_7:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMaxPressure(7000);
                bt1.setText("7000");
                break;
            case R.id.pressure_6:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMaxPressure(6000);
                bt1.setText("6000");
                break;
            case R.id.pressure_5:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMaxPressure(5000);
                bt1.setText("5000");
                break;
            case R.id.pressure_4:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMaxPressure(4000);
                bt1.setText("4000");
                break;
            case R.id.pressure_3:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMaxPressure(3000);
                bt1.setText("3000");
                break;
            case R.id.pressure_2:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMaxPressure(2000);
                bt1.setText("2000");
                break;
            case R.id.width_1:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMinWidth(0.8f);
                bt2.setText("0.8");
                break;
                case R.id.width_2:
                setBgColor(item.getActionView(),0xff888888);
                    myView.setMinWidth(0.7f);
                    bt2.setText("0.7");
                break;
            case R.id.width_3:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMinWidth(0.6f);
                bt2.setText("0.6");
                break;
            case R.id.width_4:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMinWidth(0.5f);
                bt2.setText("0.5");
                break;
            case R.id.width_5:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMinWidth(0.4f);
                bt2.setText("0.4");
                break;
            case R.id.width_6:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMinWidth(0.3f);
                bt2.setText("0.3");
                break;
            case R.id.width_7:
                setBgColor(item.getActionView(),0xff888888);
                myView.setMinWidth(0.2f);
                bt2.setText("0.2");
                break;

            case R.id.bt_point:
                setBgColor(item.getActionView(),0xff888888);
                if(myView!=null){
                    if(item.getTitle().toString().equals("线连接")){
                        myView.setPointType(true);
                        item.setTitle("点绘制");
                    }else {
                        myView.setPointType(false);
                        item.setTitle("线连接");
                    }
                }

                break;
            case R.id.bt_photo:
                setBgColor(item.getActionView(),0xff888888);
                Bitmap bitmap=BitmapUtils.createBitmap(myView);
                boolean isSuccess=    BitmapUtils.saveImageToGallery(this,bitmap);
                if(isSuccess){
                    Toast.makeText(this,"保存成功!",Toast.LENGTH_SHORT).show();
                    myView.destroyDrawingCache();
                }else {
                    Toast.makeText(this,"保存失败!",Toast.LENGTH_SHORT).show();
                }
                break;


        }
        return false;
    }
    private String deviceName="";

    /**
     *
     * @param width  x最大值
     * @param height y最大值
     * @param pressure 压力极值
     */
    public void onUgeePenWidthAndHeight(int width, int height, int pressure,boolean isHorizontal) {
//        if(writeView !=null){
//            try {
//                try {
//                    isLandScape=false;
//                    writeView.setWidthAndHeight(width,height,pressure);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        if(myView !=null){
            try {
                try {
                    myView.setWidthAndHeight(width,height,pressure);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *  握笔姿势
     * @param quadrant 笔落在四个象限
     * @param degree  笔与纸张的倾斜角度
     * @param isSuccess  书写姿势是否正确
     */
    @Override
    public void onUgeePenAngle(int quadrant, int degree, boolean isSuccess) {
        String s="倾角="+degree+"**落点="+quadrant;
        if(isSuccess){
            tv_d.setTextColor(Color.BLACK);
        }else {
            tv_d.setTextColor(Color.RED);
        }
        tv_d.setText(s);
   }

    /**
     *
     * @param point 对应的校准点
     * @param isSuccess 是否正确
     */
    @Override
    public void onCorrectInfo(int point, boolean isSuccess) {

    }

    /**
     *  点阵笔 数据返回
     * @param x
     * @param y
     * @param type
     * @param page
     */

    @Override
    public void onAFPenPositionChanged(int x, int y, int type, int page) {
        int state;
        if(type==1){
            state=17;
        }else {
            state=0;
        }
        myView.setViewPoint(state,x, y,  1000);
    }


    @Override
    public void onStateChanged(int state) {
        try {
            switch (state) {
                case ConnectState.STATE_DEVICE_INFO:
                    //连接成功
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                   // handler.sendEmptyMessage(0x1007);
//                   UgeeDevice ugeeDevice=ugeePenClass.getConnectedDevice();
//                    deviceName=ugeeDevice.getName();
//                    tvBleName.setText(ugeeDevice.getAddress());
                    llBle.setVisibility(View.GONE);

                    break;
                case ConnectState.STATE_DISCONNECTED:
//                    //断开连接
                    if (adapter != null)
                        adapter.clearData();
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                    llBle.setVisibility(View.VISIBLE);
                    break;
            }
        } catch (Exception e) {
            if(dialog!=null){
                dialog.dismiss();
            }
            e.printStackTrace();
        }

    }

    @Override
    public void onPenServiceError(String s) {

    }
    private long s1=0;
    private boolean isT;
    private float x1=0;
    String s="";

    @Override
    public void onPenPositionChanged(float x,float y,float pressure,int state,int stateIos,float width) {
//        s1++;
//        if(state==17){
//            if(!isT){
//                isT=true;
//                time=System.currentTimeMillis();
//            }
//            handler.obtainMessage(0x1004,s1).sendToTarget();
//        }else {
//            isT=false;
//        }
        if (myView != null) {
             //s=System.currentTimeMillis()+" "+y+" "+x+" "+pressure*28/8191+" "+"0.0000"+" "+state;
            myView.setViewPoint(state,x, y,  pressure);
         //   handler.obtainMessage(0x1006,"x="+x+"**y="+y+"**p="+pressure).sendToTarget();
           // LogToFile.e(s);
        }
    }



    /**
     *  按键返回（部分设备才有）
     * @param i
     */
    @Override
    public void onUgeeKeyEvent(int i) {
    }

    /**
     * 返回电量
     * @param i
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onUgeeBattery(int i,boolean isCharge) {
        if(isCharge){
            ivBattery.setBackground(getDrawable(R.mipmap.icon_battery));
        }else {
            ivBattery.setBackground(getDrawable(R.mipmap.icon_battery_not));
        }
        String s=i+"%";
        tvBattery.setText(s);
    }


private static double myRss,myRss0;
     static class MyScanCallback extends UgeeScanCallBack {
        WeakReference<MainActivity> act;

        public MyScanCallback(MainActivity a) {
            act = new WeakReference<MainActivity>(a);
        }


        @Override
        public void onBleScanResult(BluetoothDevice bluetoothDevice, int i) {
            MainActivity myact = act.get();
            ScanResultAdapter.DeviceWrap deviceWrap = new ScanResultAdapter.DeviceWrap();
            deviceWrap.setRssi(i);
            deviceWrap.setMac(bluetoothDevice.getAddress());
            deviceWrap.setName(bluetoothDevice.getName());
            int rss = Math.abs(i);
            myRss=getDistance(rss);
           if(myRss<=100&&myact!=null){
               myact.addRobotDevice2list(deviceWrap);
           }
        }

        @Override
        public void onBleScanFailed(int i) {

        }
    }
    public void addRobotDevice2list(ScanResultAdapter.DeviceWrap deviceWrap) {
        if (deviceWrap != null ) {
          //  boolean isSelfPaired = isSelfParied(deviceWrap.getMac());
           // Log.e("gaoxiaolin","isSelfPaired="+isSelfPaired);
          //  if(!isSelfPaired){
        //  Log.e("gaoxiaolin","myRss0="+myRss0+"**myRss="+myRss);
            //  if(myRss0==0||myRss0>myRss){
                    adapter.addDataToGroup(deviceWrap, true);
                 //    pairedDeviceList.add(deviceWrap.getMac());
                 myRss0=myRss;
        //  }

            }
        }

    //A和n的值，需要根据实际环境进行检测得出
    private static final double A_Value=60;/**A - 发射端和接收端相隔1米时的信号强度*/
    private static final double n_Value=2.0;/** n - 环境衰减因子*/
    /**
     * 根据Rssi获得返回的距离,返回数据单位为m
     * @param rssi
     * @return
     */
    private static double getDistance(int rssi){
        int iRssi = Math.abs(rssi);
        double power = (iRssi-A_Value)/(10*n_Value);
        return Math.pow(10,power);
    }
    public  List<String> pairedDeviceList=new ArrayList<>();
    private boolean isSelfParied(String mac) {
        if (pairedDeviceList == null || pairedDeviceList.size() == 0)
            return false;
        for (int i = 0; i < pairedDeviceList.size(); i++) {
            if (pairedDeviceList.get(i).equals(mac)) {
                return true;
            }
        }
        return false;
    }
    private String name="";
    @Override
    public void onItemClick(View view) {
        ScanResultAdapter.DeviceWrap dw = (ScanResultAdapter.DeviceWrap) view.getTag();
        mac = dw.getMac();
        name=dw.getName();
        pairedDeviceList.add(mac);
        if(name.contains("60WS")){
        boolean isAF=    ugeePenClass.connectAFPen(mac,name);
            Log.e("gaoxiaolin","connect="+isAF);
        }else {
            try {
                if (ugeePenClass.getConnectedDevice() == null) {
                    stopScan();
                    dialog.show();
                    handler.sendEmptyMessageDelayed(0x1001, 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    private long time=0;
    private String strResult="";
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0x1001:
                        stopScan();
                        ugeePenClass.connectDevice(mac);
                        invalidateOptionsMenu();
                        break;
                    case 0x1002:
                        stopScan();
                        break;
                    case 0x1003:
                        if(dialog!=null){
                            dialog.dismiss();
                        }
                        break;
                    case 0x1004:
                        long time1=System.currentTimeMillis();
                        long time2=time1-time;
                        long a=(long)msg.obj;
                        long s=a*1000/time2;
                        Log.e("gaoxiaolin","报点率="+s);
                        break;
                    case 0x1005:
                        strResult="";
                        strResult=readTxt(LogToFile.logPath+"/log.txt");
                        if(!TextUtils.isEmpty(strResult)){
                            dialog.dismiss();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    spiltResult();
                                }
                            }).start();

                        }
                        break;
                    case 0x1006:
                        String s1=(String)msg.obj;
                        tv_x.setText(s1);
                        break;
                    case 0x1007:
                        Pair<Integer,Boolean> p;
                         p= ugeePenClass.onQueryUgeeBattery();
                    //    Log.e("gaoxiaolin","p="+p.first.toString());
                      //  tvMyB.setText(p.first.toString());
                        int sss=0;
                        while(p.first>0){
                            Thread.sleep(200);
                            p= ugeePenClass.onQueryUgeeBattery();
                            Log.e("gaoxiaolin","p="+p.first.toString());
                            sss++;
                            tvMyB.setText("循环第"+sss+"次"+p.first.toString());
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void spiltResult(){
        try {
            String [] strings=strResult.split(" ");
            int len=strings.length;
            for(int i=0;i<len;i+=6){
                float x=Float.parseFloat(strings[2+i]);
                float y=Float.parseFloat(strings[1+i]);
                float w=Float.parseFloat(strings[3+i]);
                int state=Integer.parseInt(strings[5+i]);
                myView.onPenDrawView(x,y,w,state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, String mac) {

    }

    @Override
    public void onViewClick(View view) {

    }

    public static final int SCALE_MODE = 4;
    public static final int TRANSLATION_MODE = 2;
    public static final int NULL_MODE = 1;

    private int mode;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private float calculateDistance(MotionEvent event) {
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0)  - event.getY(1);
        float distance = (float)Math.sqrt(dx*dx + dy*dy);
        return distance;
    }

}
