package me.yugy.qingbo.type;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import me.yugy.qingbo.Application;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.dao.dbinfo.CommentDBInfo;
import me.yugy.qingbo.utils.TextUtils;

/**
 * Created by yugy on 2014/4/21.
 */
public class Comment implements Parcelable{

    public long id;
    public long statusId;
    public long time;
    public SpannableString text;
    public UserInfo user;

    public void parse(JSONObject json) throws JSONException, ParseException {
        id = json.getLong("id");
        statusId = json.getJSONObject("status").getLong("id");
        time = TextUtils.parseDate(json.getString("created_at"));
        text = TextUtils.parseStatusText(json.getString("text"));
        user = new UserInfo();
        user.parse(json.getJSONObject("user"));
    }

    @Override
    public int describeContents() {
        return 3;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(statusId);
        dest.writeLong(time);
        dest.writeString(text.toString());
        dest.writeParcelable(user, flags);
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            Comment comment = new Comment();
            comment.id = source.readLong();
            comment.statusId = source.readLong();
            comment.time = source.readLong();
            comment.text = TextUtils.parseStatusText(source.readString());
            comment.user = source.readParcelable(UserInfo.class.getClassLoader());
            return comment;
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public static Comment fromCursor(Cursor cursor) {
        Comment comment = new Comment();
        comment.id = cursor.getLong(cursor.getColumnIndex(CommentDBInfo.ID));
        comment.statusId = cursor.getLong(cursor.getColumnIndex(CommentDBInfo.STATUS_ID));
        long uid = cursor.getLong(cursor.getColumnIndex(CommentDBInfo.UID));
        UserInfoDataHelper userInfoDataHelper = new UserInfoDataHelper(Application.getContext());
        comment.user = userInfoDataHelper.select(uid);
        comment.time = cursor.getLong(cursor.getColumnIndex(CommentDBInfo.TIME));
        comment.text = TextUtils.parseStatusText(cursor.getString(cursor.getColumnIndex(CommentDBInfo.TEXT)));
        return comment;
    }
}
