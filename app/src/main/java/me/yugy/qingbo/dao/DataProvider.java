package me.yugy.qingbo.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import me.yugy.qingbo.Application;
import me.yugy.qingbo.dao.datahelper.CommentsDataHelper;
import me.yugy.qingbo.dao.datahelper.RepostStatusesDataHelper;
import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.dao.dbinfo.CommentDBInfo;
import me.yugy.qingbo.dao.dbinfo.RepostStatusDBInfo;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.dao.dbinfo.UserInfoDBInfo;

/**
 * Created by yugy on 2014/4/16.
 */
public class DataProvider extends ContentProvider {

    public static final Object obj = new Object();
    private static final String AUTHORITY = "me.yugy.qingbo.provider";
    private static final String SCHEME = "content://";

    private static final String PATH_USER_INFO = "/userInfo";
    private static final String PATH_STATUSES = "/statuses";
    private static final String PATH_COMMENTS = "/comments";
    private static final String PATH_REPOST_STATUSES = "/repostStatuses";

    public static final Uri USERINFO_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_USER_INFO);
    public static final Uri STATUSES_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_STATUSES);
    public static final Uri COMMENTS_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_COMMENTS);
    public static final Uri REPOST_STATUSES_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_REPOST_STATUSES);

    private static final int USER_INFO = 0;
    private static final int STATUSES = 1;
    private static final int COMMENTS = 2;
    private static final int REPOST_STATUSES = 3;

    private static final String USER_INFO_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yugy.userinfo";
    private static final String STATUSES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yugy.statuses";
    private static final String COMMENTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yugy.comments";
    public static final String REPOST_STATUSES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yugy.repost.statuses";

    private static final UriMatcher sUriMATCHER = new UriMatcher(UriMatcher.NO_MATCH){{
        addURI(AUTHORITY, "userInfo", USER_INFO);
        addURI(AUTHORITY, "statuses", STATUSES);
        addURI(AUTHORITY, "comments", COMMENTS);
        addURI(AUTHORITY, "repostStatuses", REPOST_STATUSES);
    }};

    private static DBHelper mDBHelper;

    public static DBHelper getDBHelper() {
        if(mDBHelper == null){
            mDBHelper = new DBHelper(Application.getContext());
        }
        return mDBHelper;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        synchronized (obj){
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables(matchTable(uri));

            SQLiteDatabase db = getDBHelper().getReadableDatabase();
            Cursor cursor = queryBuilder.query(db,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
    }

    private String matchTable(Uri uri) {
        switch (sUriMATCHER.match(uri)){
            case USER_INFO:
                return UserInfoDataHelper.TABLE_NAME;
            case STATUSES:
                return StatusesDataHelper.TABLE_NAME;
            case COMMENTS:
                return CommentsDataHelper.TABLE_NAME;
            case REPOST_STATUSES:
                return RepostStatusesDataHelper.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown Uri" + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMATCHER.match(uri)){
            case USER_INFO:
                return USER_INFO_CONTENT_TYPE;
            case STATUSES:
                return STATUSES_CONTENT_TYPE;
            case COMMENTS:
                return COMMENTS_CONTENT_TYPE;
            case REPOST_STATUSES:
                return REPOST_STATUSES_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        synchronized (obj){
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            long rowId = 0;
            db.beginTransaction();
            try {
                rowId =db.insert(matchTable(uri), null, values);
                db.setTransactionSuccessful();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }
            if(rowId > 0){
                Uri returnUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(uri, null);
                return returnUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        synchronized (obj){
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            db.beginTransaction();
            try {
                for(ContentValues contentValues : values){
                    db.insertWithOnConflict(matchTable(uri), BaseColumns._ID, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                return values.length;
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }
            throw new SQLException("Failed to insert row into "+ uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        synchronized (obj){
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            int count = 0;
            db.beginTransaction();
            try {
                count = db.delete(matchTable(uri), selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        synchronized (obj){
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            int count;
            db.beginTransaction();
            try {
                count = db.update(matchTable(uri), values, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
    }

    public static class DBHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "qingbo.db";

        private static final int DB_VERSION = 1;

        private DBHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            UserInfoDBInfo.TABLE.create(db);
            StatusDBInfo.TABLE.create(db);
            CommentDBInfo.TABLE.create(db);
            RepostStatusDBInfo.TABLE.create(db);

            db.execSQL("CREATE UNIQUE INDEX index_userinfo_id ON " + UserInfoDBInfo.TABLE.getTableName() + "(" + UserInfoDBInfo.UID + ")");
            db.execSQL("CREATE UNIQUE INDEX index_status_id ON " + StatusDBInfo.TABLE.getTableName() + "(" + StatusDBInfo.ID + ")");
            db.execSQL("CREATE UNIQUE INDEX index_comment_id ON " + CommentDBInfo.TABLE.getTableName() + "(" + CommentDBInfo.ID + ")");
            db.execSQL("CREATE UNIQUE INDEX index_repost_status_id ON " + RepostStatusDBInfo.TABLE.getTableName() + "(" + RepostStatusDBInfo.ID + ")");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
