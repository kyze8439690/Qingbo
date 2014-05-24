package me.yugy.qingbo.dao.dbinfo;

import android.provider.BaseColumns;

import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.utils.database.Column;
import me.yugy.qingbo.utils.database.SQLiteTable;

/**
 * Created by yugy on 2014/4/16.
 */
public class StatusDBInfo implements BaseColumns {

    public static final String ID = "id";
    public static final String TEXT = "text";
    public static final String UID = "uid";
    public static final String TOPICS = "topics";
    public static final String TIME = "time";
    public static final String COMMENT_COUNT = "comment_count";
    public static final String REPOST_COUNT = "repost_count";
    public static final String PICS = "pics";
    public static final String REPOST_STATUS_ID = "repost_json";
    public static final String TYPE = "type";

    public static final SQLiteTable TABLE = new SQLiteTable(StatusesDataHelper.TABLE_NAME)
            .addColumn(ID, Column.Constraint.UNIQUE, Column.DataType.INTEGER)
            .addColumn(TEXT, Column.DataType.TEXT)
            .addColumn(UID, Column.DataType.TEXT)
            .addColumn(TOPICS, Column.DataType.TEXT)
            .addColumn(TIME, Column.DataType.INTEGER)
            .addColumn(COMMENT_COUNT, Column.DataType.INTEGER)
            .addColumn(REPOST_COUNT, Column.DataType.INTEGER)
            .addColumn(PICS, Column.DataType.TEXT)
            .addColumn(REPOST_STATUS_ID, Column.DataType.INTEGER)
            .addColumn(TYPE, Column.DataType.INTEGER);

}
