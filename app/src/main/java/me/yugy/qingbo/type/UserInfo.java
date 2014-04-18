package me.yugy.qingbo.type;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import me.yugy.qingbo.dao.dbinfo.UserInfoDBInfo;

/**
 * Created by yugy on 2014/4/16.
 */
public class UserInfo implements Parcelable {

    public String uid = "-1";
    public String screenName = "";
    public String location = "";
    public String statusesCount = "";
    public String description = "";
    public String followersCount = "";
    public String avatar = "";
    public String cover = "";
    public String friendsCount = "";

    public void parse(JSONObject json) throws JSONException {
        uid = json.getString("idstr");
        screenName = json.getString("screen_name");
        location = json.getString("location");
        statusesCount = json.getString("statuses_count");
        description = json.getString("description");
        followersCount = json.getString("followers_count");
        avatar = json.getString("avatar_hd");
        if(json.has("cover_image")){
            cover = json.getString("cover_image");
        }else{
            cover = "";
        }
        friendsCount = json.getString("friends_count");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                uid,
                screenName,
                location,
                statusesCount,
                description,
                followersCount,
                avatar,
                cover,
                friendsCount
        });
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            UserInfo userInfo = new UserInfo();
            String[] strings = new String[9];
            source.readStringArray(strings);
            userInfo.uid = strings[0];
            userInfo.screenName = strings[1];
            userInfo.location = strings[2];
            userInfo.statusesCount = strings[3];
            userInfo.description = strings[4];
            userInfo.followersCount = strings[5];
            userInfo.avatar = strings[6];
            userInfo.cover = strings[7];
            userInfo.friendsCount = strings[8];
            return userInfo;
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public static UserInfo fromCursor(Cursor cursor) {
        UserInfo userInfo = new UserInfo();
        userInfo.uid = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.UID));
        userInfo.screenName = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.SCREEN_NAME));
        userInfo.location = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.LOCATION));
        userInfo.statusesCount = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.STATUSES_COUNT));
        userInfo.description = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.DESCRIPTION));
        userInfo.followersCount = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.FOLLOWERS_COUNT));
        userInfo.avatar = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.AVATAR));
        userInfo.cover = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.COVER));
        userInfo.friendsCount = cursor.getString(cursor.getColumnIndex(UserInfoDBInfo.FRIENDS_COUNT));
        return userInfo;
    }
}