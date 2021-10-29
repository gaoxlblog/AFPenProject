package cn.ugee.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import cn.ugee.pen.model.UgeeDevice;


/**
 * 设备已连接界面
 * Created by gaoxl on 2018/11/13.
 */

public class DeviceInfoActivity extends Activity implements View.OnClickListener{

    private TextView tvName;        //当前连接设备名称
    private UgeeDevice mDevice;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        initView();

    }

    private void initView() {
        try {
            tvName = findViewById(R.id.tv_device_name);
            mDevice=getIntent().getParcelableExtra("device");
            showDevice(mDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    //显示view
    private void showDevice(UgeeDevice mDevice){
        if(mDevice!=null){
            String name = mDevice.getName();
            tvName.setText(name);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.disconnect://断开连接
                PenMsg msg=new PenMsg();
                msg.setConnect(false);
                EventBus.getDefault().postSticky(msg);
                finish();
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
