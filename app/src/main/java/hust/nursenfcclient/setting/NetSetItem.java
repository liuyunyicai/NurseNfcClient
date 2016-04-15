package hust.nursenfcclient.setting;

/**
 * Created by admin on 2015/11/20.
 */
public class NetSetItem {
    private int iconResId;
    private int textResId;
    private boolean isClosed;

    public final static int ISCLOSED = 1;
    public final static int ISOPEN   = 0;

    public NetSetItem(int iconResId, int textResId) {
        this(iconResId, textResId, false);
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getTextResId() {
        return textResId;
    }

    public void setTextResId(int textResId) {
        this.textResId = textResId;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setIsClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    public void changeIsClosed() {
        isClosed = !isClosed;
    }

    public NetSetItem(int iconResId, int textResId, boolean isClosed) {

        this.iconResId = iconResId;
        this.textResId = textResId;
        this.isClosed = isClosed;
    }

    public NetSetItem(int[] datas) {
        if (datas == null || datas.length < 3)
            return;
        iconResId = datas[0];
        textResId = datas[1];
        isClosed = datas[2] == ISCLOSED ? true : false;
    }

}
