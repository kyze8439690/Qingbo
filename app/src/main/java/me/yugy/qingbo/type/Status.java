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
import me.yugy.qingbo.dao.datahelper.RepostStatusesDataHelper;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.utils.ArrayUtils;
import me.yugy.qingbo.utils.TextUtils;

/**
 * Created by yugy on 2014/4/16.
 */
public class Status implements Parcelable{

    public long id;
    public SpannableString text;
    public UserInfo user = new UserInfo();
    public String[] topics;
//    public ArrayList<String> topics = new ArrayList<String>();
    public long time;
    public int commentCount= 0;
    public int repostCount = 0;
    public boolean hasPic = false;
    public boolean hasPics = false;
    public String[] pics;
//    public ArrayList<String> pics = new ArrayList<String>();
    public RepostStatus repostStatus = null;

    public void parse(JSONObject json) throws JSONException, ParseException {
        id = json.getLong("id");
        text = TextUtils.parseStatusText(json.getString("text"));
        time = TextUtils.parseDate(json.getString("created_at"));
        if(json.has("deleted")){
            topics = new String[0];
            pics = new String[0];
            commentCount = 0;
            repostCount = 0;
            hasPic = hasPics = false;
        }else {
            user.parse(json.getJSONObject("user"));
            topics = TextUtils.getTopic(json.getString("text"));
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
            if (json.has("retweeted_status")) {
                repostStatus = new RepostStatus();
                repostStatus.parse(json.getJSONObject("retweeted_status"));
                topics = ArrayUtils.concatenate(topics, repostStatus.topics);
            } else {
                repostStatus = null;
            }
        }
    }

    @Override
    public int describeContents() {
        return 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLongArray(new long[]{
                id,
                time
        });
        dest.writeIntArray(new int[]{
                commentCount,
                repostCount
        });
        dest.writeBooleanArray(new boolean[]{
                hasPic,
                hasPics
        });

        dest.writeString(text.toString());

        ArrayList<String[]> stringArrayData = new ArrayList<String[]>();
        stringArrayData.add(topics);
        stringArrayData.add(pics);
        dest.writeList(stringArrayData);

        dest.writeParcelable(user, flags);
        dest.writeParcelable(repostStatus, flags);
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel source) {
            Status status = new Status();

            long[] longs = new long[2];
            source.readLongArray(longs);
            status.id = longs[0];
            status.time = longs[1];

            int[] ints = new int[2];
            source.readIntArray(ints);
            status.commentCount = ints[0];
            status.repostCount = ints[1];

            boolean[] booleans = new boolean[2];
            source.readBooleanArray(booleans);
            status.hasPic = booleans[0];
            status.hasPics = booleans[1];

            status.text = TextUtils.parseStatusText(source.readString());

            ArrayList<String[]> stringArrayData = source.readArrayList(String[].class.getClassLoader());
            status.topics = stringArrayData.get(0);
            status.pics = stringArrayData.get(1);

            status.user = source.readParcelable(UserInfo.class.getClassLoader());
            status.repostStatus = source.readParcelable(RepostStatus.class.getClassLoader());

            return status;
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    public static Status fromCursor(Cursor cursor) throws JSONException, ParseException {
        Status status = new Status();
        status.id = cursor.getLong(cursor.getColumnIndex(StatusDBInfo.ID));
        status.text = TextUtils.parseStatusText(cursor.getString(cursor.getColumnIndex(StatusDBInfo.TEXT)));
        UserInfoDataHelper userInfoDataHelper = new UserInfoDataHelper(Application.getContext());
        status.user = userInfoDataHelper.select(cursor.getLong(cursor.getColumnIndex(StatusDBInfo.UID)));

        String topicString = cursor.getString(cursor.getColumnIndex(StatusDBInfo.TOPICS));
        status.topics = ArrayUtils.convertStringToArray(topicString);

        status.time = cursor.getLong(cursor.getColumnIndex(StatusDBInfo.TIME));
        status.commentCount = cursor.getInt(cursor.getColumnIndex(StatusDBInfo.COMMENT_COUNT));
        status.repostCount = cursor.getInt(cursor.getColumnIndex(StatusDBInfo.REPOST_COUNT));

        String picString = cursor.getString(cursor.getColumnIndex(StatusDBInfo.PICS));
        status.pics = ArrayUtils.convertStringToArray(picString);
        if (status.pics.length == 1) {//single pic
            status.hasPic = true;
            status.hasPics = false;
        } else if (status.pics.length > 1) {//multi pic
            status.hasPics = true;
            status.hasPic = false;
        } else {//no pic
            status.hasPics = status.hasPic = false;
        }

        long id = cursor.getLong(cursor.getColumnIndex(StatusDBInfo.REPOST_STATUS_ID));
        if(id != -1){
            status.repostStatus = new RepostStatusesDataHelper(Application.getContext()).select(id);
        }else{
            status.repostStatus = null;
        }

        return status;
    }
}
