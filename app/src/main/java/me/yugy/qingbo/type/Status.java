package me.yugy.qingbo.type;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import me.yugy.qingbo.Application;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.utils.TextUtils;

/**
 * Created by yugy on 2014/4/16.
 */
public class Status implements Parcelable{

    public long id;
    public SpannableString text;
    public UserInfo user = new UserInfo();
    public ArrayList<String> topics = new ArrayList<String>();
    public long time;
    public int commentCount= 0;
    public int repostCount = 0;
    public boolean hasPic = false;
    public boolean hasPics = false;
    public ArrayList<String> pics = new ArrayList<String>();
    public Status repostStatus = null;

    public JSONObject repostJson = new JSONObject();

    public void parse(JSONObject json) throws JSONException, ParseException {
        id = json.getLong("id");
        text = TextUtils.parseStatusText(json.getString("text"));
        if(json.has("deleted")){
            time = TextUtils.parseDate(json.getString("created_at"));
        }else {
            user.parse(json.getJSONObject("user"));
            topics.addAll(TextUtils.getTopic(json.getString("text")));
            time = TextUtils.parseDate(json.getString("created_at"));
            commentCount = json.getInt("comments_count");
            repostCount = json.getInt("reposts_count");
            JSONArray picsJson = json.getJSONArray("pic_urls");
            if (picsJson.length() == 1) {//single pic
                hasPic = true;
                hasPics = false;
            } else if (picsJson.length() > 1) {//multi pic
                hasPics = true;
                hasPic = false;
            } else {//no pic
                hasPics = hasPic = false;
            }
            if (hasPic || hasPics) {
                int picsLength = picsJson.length();
                for (int i = 0; i < picsLength; i++) {
                    pics.add(picsJson.getJSONObject(i).getString("thumbnail_pic"));
                }
            }
            if (json.has("retweeted_status")) {
                repostStatus = new Status();
                repostJson = json.getJSONObject("retweeted_status");
                repostStatus.parse(repostJson);
                topics.addAll(repostStatus.topics);
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
        dest.writeStringArray(new String[]{
                text.toString(),
                repostJson.toString()
        });
        ArrayList<ArrayList<String>> stringArrayData = new ArrayList<ArrayList<String>>();
        stringArrayData.add(topics);
        stringArrayData.add(pics);
        dest.writeList(stringArrayData);

        dest.writeParcelable(user, flags);

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

            String[] strings = new String[2];
            source.readStringArray(strings);
            status.text = TextUtils.parseStatusText(strings[0]);
            try {
                status.repostJson = new JSONObject(strings[1]);
                if(status.repostJson.has("id")){
                    status.repostStatus = new Status();
                    status.repostStatus.parse(status.repostJson);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            ArrayList<ArrayList<String>> stringArrayData = new ArrayList<ArrayList<String>>();
            source.readList(stringArrayData, ArrayList.class.getClassLoader());
            status.topics = stringArrayData.get(0);
            status.pics = stringArrayData.get(1);

            status.user = source.readParcelable(UserInfo.class.getClassLoader());

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
        status.user = userInfoDataHelper.select(cursor.getString(cursor.getColumnIndex(StatusDBInfo.UID)));

        JSONArray topics = new JSONArray(cursor.getString(cursor.getColumnIndex(StatusDBInfo.TOPICS)));
        int size = topics.length();
        for(int i = 0; i < size; i++){
            status.topics.add(topics.getString(i));
        }

        status.time = cursor.getLong(cursor.getColumnIndex(StatusDBInfo.TIME));
        status.commentCount = cursor.getInt(cursor.getColumnIndex(StatusDBInfo.COMMENT_COUNT));
        status.repostCount = cursor.getInt(cursor.getColumnIndex(StatusDBInfo.REPOST_COUNT));

        JSONArray pics = new JSONArray(cursor.getString(cursor.getColumnIndex(StatusDBInfo.PICS)));
        size = pics.length();
        for(int i = 0; i < size; i++){
            status.pics.add(pics.getString(i));
        }
        if (pics.length() == 1) {//single pic
            status.hasPic = true;
            status.hasPics = false;
        } else if (pics.length() > 1) {//multi pic
            status.hasPics = true;
            status.hasPic = false;
        } else {//no pic
            status.hasPics = status.hasPic = false;
        }

        status.repostJson = new JSONObject(cursor.getString(cursor.getColumnIndex(StatusDBInfo.REPOST_JSON)));
        if(status.repostJson.has("id")){
            status.repostStatus = new Status();
            status.repostStatus.parse(status.repostJson);
        }

        return status;
    }
}
