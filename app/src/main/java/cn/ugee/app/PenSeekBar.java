package cn.ugee.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;


public class PenSeekBar {
    private Context context;
    private Dialog dialog;
    private MySeekBar seekBar;
    private WriteMatrixView customPenView;
    private TextView tvInfo;
    public  PenSeekBar(Context context, WriteMatrixView customPenView){
        this.context = context;
        this.customPenView=customPenView;
        dialog = new Dialog(context, R.style.dialog);
        dialog.setOnCancelListener(onCancelListener);
    }
    /**
     * 初始化进度对话框
     */
    public void initDialog() {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_seekbar, null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay(); //获取屏幕宽高

//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;//宽高可设置具体大小
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.width = (int) (display.getWidth() ); //宽度设置为屏幕宽度的0.5
        lp.height = (int) (display.getHeight() * 0.2); //高度设置为屏幕高度的0.5
        dialog.getWindow().setAttributes(lp);

        seekBar =  dialog.findViewById(R.id.seek_bar);
        tvInfo=dialog.findViewById(R.id.tv_info);
        seekBar.setProgress(BaseApplication.LEN);
        tvInfo.setText(String.valueOf(BaseApplication.LEN));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==0){
                    customPenView.setPenWidth(0.5f);
                }else {
                    customPenView.setPenWidth(progress);
                }
                tvInfo.setText(String.valueOf(progress));
                BaseApplication.LEN=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });;


        dialog.show();
    }
    DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            // TODO Auto-generated method stub
            dialog.dismiss();
        }
    };
}
