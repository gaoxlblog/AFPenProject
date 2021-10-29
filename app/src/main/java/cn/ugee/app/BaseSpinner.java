package cn.ugee.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatSpinner;

import java.util.ArrayList;
import java.util.List;

public class BaseSpinner  extends AppCompatSpinner {
    private WriteMatrixView writeMatrixView;
    private int pos;
    private List<String> widthList=new ArrayList<>();
    private List<String>pressureList=new ArrayList<>();
    public BaseSpinner(Context context) {
        super(context);
    }

    public BaseSpinner(Context context, int mode) {
        super(context, mode);
    }

    public BaseSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        widthList.add("1.8");
        widthList.add("1.5");
        widthList.add("1.2");
        pressureList.add("7000");
        pressureList.add("6000");
        pressureList.add("5000");
        pressureList.add("4000");
        pressureList.add("3000");
        pressureList.add("2000");
    }
    public void setView(WriteMatrixView view,int p){
        this.writeMatrixView=view;
        this.pos=p;
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
     //   if (sameSelected) {
            if(pos==2){
                String p=pressureList.get(position);
                writeMatrixView.setMaxPressure(Integer.parseInt(p));
            }else {
                String w= widthList.get(position);
                writeMatrixView.setPointWidth(Integer.parseInt(w));
            }


            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
       // }
    }
}
