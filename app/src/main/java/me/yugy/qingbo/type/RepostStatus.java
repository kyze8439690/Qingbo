package me.yugy.qingbo.type;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import me.yugy.qingbo.Application;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.dao.dbinfo.RepostStatusDBInfo;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.utils.ArrayUtils;
import me.yugy.qingbo.utils.TextUtils;

/**
 * Created by yugy on 2014/4/22.
 */
public class RepostStatus implements Parcelable, BaseStatus{

    public long id;
    public SpannableString text;
    public UserInfo user = new UserInfo();
    public long time;
    public int commentCount= 0;
    public int repostCount = 0;
    public boolean hasPic = false;
    public boolean hasPics = false;
    public String[] pics;
    public double latitude = -1;
    public double longitude = -1;

    @Override
    public void parse(JSONObject json) throws JSONException, ParseException {
        id = json.getLong("id");
        text = TextUtils.parseStatusText(json.getString("text"));
        time = TextUtils.parseDate(json.getString("created_at"));
        if(json.has("deleted")){
            pics = new String[0];
            commentCount = 0;
            repostCount = 0;
            hasPic = hasPics = false;
        }else {
            user.parse(json.getJSONObject("user"));
            commentCount = json.getInt("comments_count");
            repostCount = json.getInt("reposts_count");
            pics = ArrayUtils.getWeiboPicArray(json.getJSONArray("pic_urls"));
            if (pics.length == 1) {//single pic
                hasPic = true;
                hasPics = false;
            } else if (pics.length > 1) {//multi pic
                hasPics = true;
                hasPic = false;
            } else {//no pic
                hasPics = hasPic = false;
            }
            if(json.optJSONObject("geo") != null){
                latitude = json.getJSONObject("geo").getJSONArray("coordinates").getDouble(0);
                longitude = json.getJSONObject("geo").getJSONArray("coordinates").getDouble(1);
            }
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(time);
        dest.writeInt(commentCount);
        dest.writeInt(repostCount);
        dest.writeByte((byte) (hasPic ? 1 : 0));
        dest.writeByte((byte) (hasPics ? 1 : 0));
        dest.writeString(text.toString());
        dest.writeStringArray(pics);
        dest.writeParcelable(user, flags);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public static final Creator<RepostStatus> CREATOR = new Creator<RepostStatus>() {
        @Override
        public RepostStatus createFromParcel(Parcel source) {
            RepostStatus repostStatus = new RepostStatus();
            repostStatus.id = source.readLong();
            repostStatus.time = source.readLong();
            repostStatus.commentCount = source.readInt();
            repostStatus.repostCount = source.readInt();
            repostStatus.hasPic = source.readByte() != 0;
            repostStatus.hasPics = source.readByte() != 0;
            repostStatus.text = TextUtils.parseStatusText(source.readString());
            repostStatus.pics = source.createStringArray();
            repostStatus.user = source.readParcelable(UserInfo.class.getClassLoader());
            repostStatus.latitude = source.readDouble();
            repostStatus.longitude = source.readDouble();
            return repostStatus;
        }

        @Override
        public RepostStatus[] newArray(int size) {
            return new RepostStatus[size];
        }
    };

    public static RepostStatus fromCursor(Cursor cursor) throws JSONException, ParseException {
        RepostStatus repostStatus = new RepostStatus();
        repostStatus.id = cursor.getLong(cursor.getColumnIndex(StatusDBInfo.ID));
        repostStatus.text = TextUtils.parseStatusText(cursor.getString(cursor.getColumnIndex(StatusDBInfo.TEXT)));
        UserInfoDataHelper userInfoDataHelper = new UserInfoDataHelper(Application.getContext());
        repostStatus.user = userInfoDataHelper.select(cursor.getLong(cursor.getColumnIndex(StatusDBInfo.UID)));

        repostStatus.time = cursor.getLong(cursor.getColumnIndex(StatusDBInfo.TIME));
        repostStatus.commentCount = cursor.getInt(cursor.getColumnIndex(StatusDBInfo.COMMENT_COUNT));
        repostStatus.repostCount = cursor.getInt(cursor.getColumnIndex(StatusDBInfo.REPOST_COUNT));

        String picString = cursor.getString(cursor.getColumnIndex(StatusDBInfo.PICS));
        repostStatus.pics = ArrayUtils.convertStringToArray(picString);
        if (repostStatus.pics.length == 1) {//single pic
            repostStatus.hasPic = true;
            repostStatus.hasPics = false;
        } else if (repostStatus.pics.length > 1) {//multi pic
            repostStatus.hasPics = true;
            repostStatus.hasPic = false;
        } else {//no pic
            repostStatus.hasPics = repostStatus.hasPic = false;
        }

        repostStatus.latitude = cursor.getDouble(cursor.getColumnIndex(RepostStatusDBInfo.LAT));
        repostStatus.longitude = cursor.getDouble(cursor.getColumnIndex(RepostStatusDBInfo.LONG));

        return repostStatus;
    }

    public static RepostStatus fromStatus(Status status) {
        RepostStatus repostStatus = new RepostStatus();
        repostStatus.id = status.id;
        repostStatus.text = status.text;
        repostStatus.time = status.time;
        repostStatus.user = status.user;
        repostStatus.commentCount = status.commentCount;
        repostStatus.repostCount = status.repostCount;
        repostStatus.hasPic = status.hasPic;
        repostStatus.hasPics = status.hasPics;
        repostStatus.pics = status.pics;
        repostStatus.latitude = status.latitude;
        repostStatus.longitude = status.longitude;
        return repostStatus;
    }
}
