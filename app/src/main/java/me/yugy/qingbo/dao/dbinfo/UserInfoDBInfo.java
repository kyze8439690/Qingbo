package me.yugy.qingbo.dao.dbinfo;

import android.provider.BaseColumns;

import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.utils.database.Column;
import me.yugy.qingbo.utils.database.SQLiteTable;

/**
 * Created by yugy on 2014/4/16.
 */
public class UserInfoDBInfo implements BaseColumns {

    public static final String UID = "uid";
    public static final String FOLLOWING = "following";
    public static final String FOLLOW_ME = "follow_me";
    public static final String SCREEN_NAME = "screen_name";
    public static final String LOCATION = "location";
    public static final String STATUSES_COUNT = "statuses_count";
    public static final String DESCRIPTION = "description";
    public static final String FOLLOWERS_COUNT = "followers_count";
    public static final String AVATAR = "avatar";
    public static final String COVER = "cover";
    public static final String FRIENDS_COUNT = "friends_count";

    public static final SQLiteTable TABLE = new SQLiteTable(UserInfoDataHelper.TABLE_NAME)
            .addColumn(UID, Column.Constraint.UNIQUE, Column.DataType.INTEGER)
            .addColumn(FOLLOWING, Column.DataType.INTEGER)
            .addColumn(FOLLOW_ME, Column.DataType.INTEGER)
            .addColumn(SCREEN_NAME, Column.DataType.TEXT)
            .addColumn(LOCATION, Column.DataType.TEXT)
            .addColumn(STATUSES_COUNT, Column.DataType.TEXT)
            .addColumn(DESCRIPTION, Column.DataType.TEXT)
            .addColumn(FOLLOWERS_COUNT, Column.DataType.TEXT)
            .addColumn(AVATAR, Column.DataType.TEXT)
            .addColumn(COVER, Column.DataType.TEXT)
            .addColumn(FRIENDS_COUNT, Column.DataType.TEXT);

}
