package cn.ugee.app;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




public class ScanResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int TYPE_ITEM = 0;
    static final int TYPE_SECTION = 1;
    static final int TYPE_FOOTER = 2;
    //列表渲染数据
    public List<Object> wrapData;
    //未配对设备列表 //原始数据
    private List<DeviceWrap> data = new ArrayList<>();
    private HashMap<String, DeviceWrap> dataCache = new HashMap<>();
    private Context context;
    private MyClickListener myClickListener;


    public interface MyClickListener{
        void onItemClick(View view);
        void onItemLongClick(View view, String mac);
        void onViewClick(View view);
    }
    public ScanResultAdapter(Context context,MyClickListener myClickListener) {
        this.context = context;
        this.myClickListener=myClickListener;
        wrapData = new ArrayList<>();
    }


    /**
     * 将扫描到的设别添加到列表
     *
     * @param d
     */
    public void addData(DeviceWrap d) {
        String macAddr =d.getMac();
        DeviceWrap cache = dataCache.get(macAddr);
        if (cache == null) {
            data.add(d);
            dataCache.put(d.getMac(), d);
            if (data.size() != 0)
                notifyItemInserted(data.size() - 1);
            else
                notifyItemInserted(0);
        } else {
            int index = data.indexOf(cache);
            cache.setRssi(d.getRssi());

            if (index >= 0) {
                notifyItemChanged(index, d);
            }
        }
    }

    /**
     * 将扫描到的设备添加到列表
     *
     * @param d
     */
    public void addDataToGroup(DeviceWrap d, boolean isPaired) {
        String macAddr = d.getMac();
        DeviceWrap cache = dataCache.get(macAddr);
        if (cache == null) {
            data.add(d);
            wrapData.add(wrapData.size(), d);
            dataCache.put(d.getMac(), d);
            notifyDataSetChanged();
        } else {
            int index = wrapData.indexOf(cache);
            cache.setRssi(d.getRssi());
            notifyDataSetChanged();
            if (index >= 0) {
                notifyItemChanged(index, d);
            }
        }



    }


    public List<DeviceWrap> getData() {
        return data;
    }

    public void clearData() {
        if (data != null) {
            data.clear();
        }
        if (dataCache != null) {
            dataCache.clear();
        }
        if (wrapData != null) {
            wrapData.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object obje = wrapData.get(position);
        if (obje == null) {
            return TYPE_FOOTER;
        } else if (obje instanceof String) {
            return TYPE_SECTION;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false));
            case TYPE_SECTION:
                return new SectionItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_manger_section, parent, false));
            default:
                return new FooterItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.default_footer_item, parent, false));

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder o, int position) {
        Object data = getItem(position);
        if (o instanceof ItemHolder) {
            dataItem(((ItemHolder) o), ((DeviceWrap) data));
        } else if (o instanceof SectionItemHolder) {
            dataSection(((SectionItemHolder) o), ((String) data),position);
        } else {
            //footer
        }
    }

    private void dataSection(final SectionItemHolder holder, String data, int position) {
        holder.tv_section.setText(data);
        if ( position==0 ){
            holder.tv_scanning.setText("");
        } else {
            holder.tv_scanning.setText(R.string.bluetooth_device_scan);
            holder.tv_scanning.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(!isRef){
                        isRef=true;
                        holder.tv_scanning.setText(R.string.scanning);
                        mHandler.sendEmptyMessageDelayed(0x1001,1000);
                    }
                }
            });
        }
    }
    private boolean isRef=false;
    @SuppressLint("HandlerLeak")
    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 0x1001:
                    clearData();

                    isRef=false;
                    break;
            }
        }
    };
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (holder instanceof ItemHolder) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                for (Object payload : payloads) {
                    DeviceWrap deviceWrap = (DeviceWrap) payload;
                    int rss = Math.abs(deviceWrap.getRssi());
//                    ((ItemHolder) holder).tvRs.setText(String.valueOf(rss));
//
//                    ((ItemHolder) holder).tvDis.setText(String.valueOf(getDistance(rss)));
                }
            }
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
    private double getDistance(int rssi){
        int iRssi = Math.abs(rssi);
        double power = (iRssi-A_Value)/(10*n_Value);
        return Math.pow(10,power);
    }

    public Object getItem(int position) {
        return wrapData.get(position);
    }

    private void dataItem(final ItemHolder holder, final DeviceWrap d) {
        holder.itemView.setTag(d);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValueAnimator animator = ObjectAnimator.ofInt(view, "backgroundColor", 0xc3c9c8cd, 0x00f0f0f0);//对背景色颜色进行改变，操作的属性为"backgroundColor",此处必须这样写，不能全小写,后面的颜色为在对应颜色间进行渐变
                animator.setDuration(300);
                animator.setEvaluator(new ArgbEvaluator());//如果要颜色渐变必须要ArgbEvaluator，来实现颜色之间的平滑变化，否则会出现颜色不规则跳动
                animator.start();
                myClickListener.onItemClick(view);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                myClickListener.onItemLongClick(view, d.getMac());
                return true;
            }
        });
        

        String name = d.getName();
        holder.tv.setText(TextUtils.isEmpty(name) ? context.getString(R.string.unknow) : name);
        holder.tv_mac.setText(reverseMac(d.getMac()));
        int rss = Math.abs(d.getRssi());
        holder.tvRs.setText(String.valueOf(rss));
        DecimalFormat df = new DecimalFormat("#####0.00");
        String dis = df.format(getDistance(rss))+"m";
        holder.tvDis.setText(dis);
    }
    private  String reverseMac(String mac) {
        String[] macArray = mac.split(":");
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = macArray.length - 1; i >= 0; i--) {
            stringBuffer.append(macArray[i]);
            if (i > 0) {
                stringBuffer.append(":");
            }
        }
        return stringBuffer.toString();
    }

    @Override
    public int getItemCount() {
        return wrapData.size();
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        TextView tv;
        TextView tv_mac,tvRs,tvDis;
        LinearLayout llItem;

        public ItemHolder(View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv);
            tv_mac=itemView.findViewById(R.id.tv_mac);
            tvRs=itemView.findViewById(R.id.tv_rs);
            tvDis=itemView.findViewById(R.id.tv_dis);
            llItem=itemView.findViewById(R.id.ll_item);
        }
    }

    static class SectionItemHolder extends RecyclerView.ViewHolder {
        TextView tv_section;
        TextView tv_scanning;

        SectionItemHolder(View itemView) {
            super(itemView);
            tv_section=(TextView)itemView.findViewById(R.id.manger_tv_section);
            tv_scanning=(TextView)itemView.findViewById(R.id.scanning_section);
        }
    }


    static class FooterItemHolder extends RecyclerView.ViewHolder {
        FooterItemHolder(View itemView) {
            super(itemView);
        }
    }
    static class DeviceWrap  implements Parcelable {
        public static final Creator<DeviceWrap> CREATOR = new Creator<DeviceWrap>() {
            @Override
            public DeviceWrap createFromParcel(Parcel source) {
                return new DeviceWrap(source);
            }

            @Override
            public DeviceWrap[] newArray(int size) {
                return new DeviceWrap[size];
            }
        };
        String name;
        String mac;
        int rssi;
        boolean isParid = false;

        public DeviceWrap() {
        }

        public DeviceWrap(String name, String mac, int rssi, boolean isParid) {
            this.name = name;
            this.mac = mac;
            this.rssi = rssi;
            this.isParid = isParid;
        }

        protected DeviceWrap(Parcel in) {
            this.name = in.readString();
            this.mac = in.readString();
            this.rssi = in.readInt();
            this.isParid = in.readByte() != 0;
        }

        public String getName() {
            return name;
        }

        public String getMac() {
            return mac;
        }

        public int getRssi() {
            return rssi;
        }

        public void setRssi(int rssi) {
            this.rssi = rssi;
        }
        public void setMac(String mac){
            this.mac=mac;
        }
        public void setName(String name){
            this.name=name;
        }
        public void setParid(boolean isParid){
            this.isParid=isParid;
        }

        public boolean isParid() {
            return isParid;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.mac);
            dest.writeInt(this.rssi);
            dest.writeByte(this.isParid ? (byte) 1 : (byte) 0);
        }
    }
    private class DeviceComparator implements Comparable<DeviceWrap>{
        private DeviceWrap deviceWrap;
        public DeviceComparator(DeviceWrap d){
            this.deviceWrap=d;
        }

        @Override
        public int compareTo(DeviceWrap o) {
            double rs0=getDistance(deviceWrap.getRssi());
            double rs1=getDistance(o.getRssi());
            if(rs0>rs1){
                return 1;
            }else {
                return -1;
            }

        }
    }

}
