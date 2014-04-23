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
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.utils.ArrayUtils;
import me.yugy.qingbo.utils.TextUtils;

/**
 * Created by yugy on 2014/4/22.
 */
public class RepostStatus implements Parcelable{

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

    public void parse(JSONObject json) throws JSONException, ParseException {
        id = json.getLong("id");
        text = TextUtils.parseStatusText(json.getString("text"));
        if(json.has("deleted")){
            time = TextUtils.parseDate(json.getString("created_at"));
        }else {
            user.parse(json.getJSONObject("user"));
            topics = TextUtils.getTopic(json.getString("text"));
            time = TextUtils.parseDate(json.getString("created_at"));
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
        }
    }


    @Override
    public int describeContents() {
        return 0;
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
    }

    public static final Creator<RepostStatus> CREATOR = new Creator<RepostStatus>() {
        @Override
        public RepostStatus createFromParcel(Parcel source) {
            RepostStatus repostStatus = new RepostStatus();

            long[] longs = new long[2];
            source.readLongArray(longs);
            repostStatus.id = longs[0];
            repostStatus.time = longs[1];

            int[] ints = new int[2];
            source.readIntArray(ints);
            repostStatus.commentCount = ints[0];
            repostStatus.repostCount = ints[1];

            boolean[] booleans = new boolean[2];
            source.readBooleanArray(booleans);
            repostStatus.hasPic = booleans[0];
            repostStatus.hasPics = booleans[1];

            repostStatus.text = TextUtils.parseStatusText(source.readString());

            ArrayList<String[]> stringArrayData = source.readArrayList(String[].class.getClassLoader());
            repostStatus.topics = stringArrayData.get(0);
            repostStatus.pics = stringArrayData.get(1);

            repostStatus.user = source.readParcelable(UserInfo.class.getClassLoader());

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
        repostStatus.user = userInfoDataHelper.select(cursor.getString(cursor.getColumnIndex(StatusDBInfo.UID)));

        String topicString = cursor.getString(cursor.getColumnIndex(StatusDBInfo.TOPICS));
        repostStatus.topics = ArrayUtils.convertStringToArray(topicString);

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

        return repostStatus;
    }
}
