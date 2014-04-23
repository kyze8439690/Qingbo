package me.yugy.qingbo.dao.dbinfo;

import android.provider.BaseColumns;

import me.yugy.qingbo.dao.datahelper.CommentsDataHelper;
import me.yugy.qingbo.utils.database.Column;
import me.yugy.qingbo.utils.database.SQLiteTable;

/**
 * Created by yugy on 2014/4/21.
 */
public class CommentDBInfo implements BaseColumns{

    public static final String ID = "id";
    public static final String STATUS_ID = "status_id";
    public static final String UID = "uid";
    public static final String TEXT = "text";
    public static final String TIME = "time";

    public static final SQLiteTable TABLE = new SQLiteTable(CommentsDataHelper.TABLE_NAME)
            .addColumn(ID, Column.Constraint.UNIQUE, Column.DataType.INTEGER)
            .addColumn(STATUS_ID, Column.DataType.INTEGER)
            .addColumn(UID, Column.DataType.INTEGER)
            .addColumn(TEXT, Column.DataType.TEXT)
            .addColumn(TIME, Column.DataType.INTEGER);

}
