package cn.ugee.pen.util;

public class ByteUtil {
    public int bytesToInteger(byte... data) {
        int value = 0;
        for (int i = Math.max(data.length - 4, 0); i < data.length; i++) {
            int w = ((data.length - i - 1) * 8);
            value= value | ((data[i] & 0xFF) << w);
        }
        return value;
    }
    public short bytesToShort(byte...  buf){

        short r = 0;
        for (int i = 0; i < buf.length; i++) {
            r <<= 8;
            r |= (buf[i] & 0x00ff);

        }
        return r;
    }

    /**
     * 打印byte数据
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b)
    {
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++) {
            String   snmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((snmp.length()==1)? "0"+snmp : snmp);
            //分割符 可以为空格等
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

}
