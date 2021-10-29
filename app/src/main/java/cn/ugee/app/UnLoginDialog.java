package cn.ugee.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class UnLoginDialog extends Dialog {
    
    private TextView textView;
    
    
    public UnLoginDialog(Context context) {
        super(context, R.style.TranslucentDialogStyle);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_unlogin_account);
        textView = (TextView) findViewById(R.id.tv_tip_dialog);
        init();
    }
    
    //设置对话框的文字信息
    private void setDialogText(String dialogText){
        if (textView!=null) textView.setText(dialogText);
    }
    
    public void show4Text(String msg) {
        this.show();
        setDialogText(msg);
    }

    private void init() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();//获取屏幕参数
        lp.width = (int) (displayMetrics.widthPixels * 0.4);
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
    }
}
