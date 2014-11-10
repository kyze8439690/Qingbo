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

    public long uid = -1;
    public boolean following = false;
    public boolean followMe = false;
    public String screenName = "";
    public String location = "";
    public String statusesCount = "";
    public String description = "";
    public String followersCount = "";
    public String avatar = "";
    public String cover = "";
    public String friendsCount = "";

    public void parse(JSONObject json) throws JSONException {
        uid = json.getLong("id");
        following = json.getBoolean("following");
        followMe = json.getBoolean("follow_me");
        screenName = json.getString("screen_name");
        location = json.getString("location");
        statusesCount = json.getString("statuses_count");
        description = json.getString("description");
        followersCount = json.getString("followers_count");
        avatar = json.getString("avatar_hd");
        if(json.has("cover_image")){
            cover = json.getString("cover_image");
        }else if(json.has("cover_image_phone")){
            cover = json.getString("cover_image_phone");
        }
        friendsCount = json.getString("friends_count");
    }

    @Override
    public int describeContents() {
        return 2;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(uid);
        dest.writeByte((byte) (following ? 1 : 0));
        dest.writeByte((byte) (followMe ? 1 : 0));
        dest.writeString(screenName);
        dest.writeString(location);
        dest.writeString(statusesCount);
        dest.writeString(description);
        dest.writeString(followersCount);
        dest.writeString(avatar);
        dest.writeString(cover);
        dest.writeString(friendsCount);
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            UserInfo userInfo = new UserInfo();
            userInfo.uid = source.readLong();
            userInfo.following = source.readByte() != 0;
            userInfo.followMe = source.readByte() != 0;
            userInfo.screenName = source.readString();
            userInfo.location = source.readString();
            userInfo.statusesCount = source.readString();
            userInfo.description = source.readString();
            userInfo.followersCount = source.readString();
            userInfo.avatar = source.readString();
            userInfo.cover = source.readString();
            userInfo.friendsCount = source.readString();
            return userInfo;
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public static UserInfo fromCursor(Cursor cursor) {
        UserInfo userInfo = new UserInfo();
        userInfo.uid = cursor.getLong(cursor.getColumnIndex(UserInfoDBInfo.UID));
        int following = cursor.getInt(cursor.getColumnIndex(UserInfoDBInfo.FOLLOWING));
        userInfo.following = following == 1;
        int followMe = cursor.getInt(cursor.getColumnIndex(UserInfoDBInfo.FOLLOW_ME));
        userInfo.followMe = followMe == 1;
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