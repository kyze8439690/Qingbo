package me.yugy.qingbo.dao.dbinfo;

        import android.provider.BaseColumns;

        import me.yugy.qingbo.dao.datahelper.UserIndexDataHelper;
        import me.yugy.qingbo.utils.database.Column;
        import me.yugy.qingbo.utils.database.SQLiteTable;

/**
 * Created by yugy on 2014/6/1.
 */
public class UserIndexDBInfo implements BaseColumns{

    public static final String UID = "uid";
    public static final String SCREEN_NAME = "screen_name";
    public static final String AVATAR = "avatar";
    public static final String SEARCH_INDEX = "search_index";

    public static final SQLiteTable TABLE = new SQLiteTable(UserIndexDataHelper.TABLE_NAME)
            .addColumn(UID, Column.Constraint.UNIQUE, Column.DataType.INTEGER)
            .addColumn(SCREEN_NAME, Column.DataType.TEXT)
            .addColumn(AVATAR, Column.DataType.TEXT)
            .addColumn(SEARCH_INDEX, Column.DataType.TEXT);

}
