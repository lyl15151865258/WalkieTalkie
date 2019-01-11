package jp.co.shiratsuki.walkietalkie.network.retrofit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 请求过程实体类
 * Created at 2018/11/28 13:46
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ProgressModel implements Parcelable {

    private long currentBytes;
    private long contentLength;
    private boolean done = false;

    public ProgressModel(long currentBytes, long contentLength, boolean done) {
        this.currentBytes = currentBytes;
        this.contentLength = contentLength;
        this.done = done;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    private static final Creator<ProgressModel> CREATOR = new Creator<ProgressModel>() {
        @Override
        public ProgressModel createFromParcel(Parcel parcel) {
            return new ProgressModel(parcel);
        }

        @Override
        public ProgressModel[] newArray(int i) {
            return new ProgressModel[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(currentBytes);
        parcel.writeLong(contentLength);
        parcel.writeByte((byte) (done==true?1:0));
    }

    protected ProgressModel(Parcel parcel) {
        currentBytes = parcel.readLong();
        contentLength = parcel.readLong();
        done = parcel.readByte()!=0;
    }
}
