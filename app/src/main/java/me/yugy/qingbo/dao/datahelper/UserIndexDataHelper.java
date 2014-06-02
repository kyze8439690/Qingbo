package me.yugy.qingbo.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.List;

import me.yugy.qingbo.dao.DataProvider;
import me.yugy.qingbo.dao.dbinfo.CommentDBInfo;
import me.yugy.qingbo.dao.dbinfo.UserIndexDBInfo;
import me.yugy.qingbo.dao.dbinfo.UserInfoDBInfo;
import me.yugy.qingbo.type.UserIndex;
import me.yugy.qingbo.type.UserInfo;

/**
 * Created by yugy on 2014/6/1.
 */
public class UserIndexDataHelper extends BaseDataHelper{

    public static final String TABLE_NAME = "userindex";

    public UserIndexDataHelper(Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.USERINDEX_CONTENT_URI;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    protected ContentValues getContentValues(UserIndex userIndex){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserIndexDBInfo.UID, userIndex.uid);
        contentValues.put(UserIndexDBInfo.SCREEN_NAME, userIndex.screenName);
        contentValues.put(UserIndexDBInfo.AVATAR, userIndex.avatar);
        contentValues.put(UserIndexDBInfo.SEARCH_INDEX, userIndex.searchIndex);
        return contentValues;
    }

    public UserIndex select(long uid){
        Cursor cursor = query(null, UserIndexDBInfo.UID + "=?", new String[]{String.valueOf(uid)}, null);
        if(cursor.moveToFirst()){
            UserIndex userIndex = UserIndex.fromCursor(cursor);
            cursor.close();
            return userIndex;
        }else{
            cursor.close();
            return null;
        }
    }

    public int bulkInsert(List<UserInfo> userInfoList){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getWritableDatabase();
            db.beginTransaction();
            try{
                for(UserInfo userInfo : userInfoList){
                    UserIndex userIndex = new UserIndex();
                    userIndex.parse(getContext(), userInfo);
                    ContentValues values = getContentValues(userIndex);
                    if(select(userIndex.uid) == null){
                        db.insert(getTableName(), null, values);
                    }else{
                        db.update(getTableName(), values, CommentDBInfo.ID + "=?", new String[]{String.valueOf(userIndex.uid)});
                    }

                    values = UserInfoDataHelper.getContentValues(userInfo);
                    if(new UserInfoDataHelper(getContext()).select(userInfo.uid) == null){
                        //insert
                        db.insert(UserInfoDataHelper.TABLE_NAME, null, values);
                    }else{
                        //update
                        db.update(UserInfoDataHelper.TABLE_NAME, values, UserInfoDBInfo.UID + "=?", new String[]{String.valueOf(userInfo.uid)});
                    }
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(getContentUri(), null);
                return userInfoList.size();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }
            throw new SQLException("Fail to insert row into " + getContentUri());
        }
    }

    public long getUserIndexCount(){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getReadableDatabase();
            Cursor cursor = db.query(getTableName(), new String[] {"count(*)"}, null, null, null, null, null);
            long count;
            if(cursor.moveToFirst()){
                count = cursor.getLong(0);
            }else{
                count = 0;
            }
            return count;
        }
    }

}
