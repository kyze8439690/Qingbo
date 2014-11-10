package me.yugy.qingbo.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.List;

import me.yugy.qingbo.dao.DataProvider;
import me.yugy.qingbo.dao.dbinfo.UserInfoDBInfo;
import me.yugy.qingbo.type.UserInfo;

/**
 * Created by yugy on 2014/4/16.
 */
public class UserInfoDataHelper extends BaseDataHelper{

    public static final String TABLE_NAME = "userinfo";

    public UserInfoDataHelper(Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.USERINFO_CONTENT_URI;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public static ContentValues getContentValues(UserInfo userInfo){
        ContentValues values = new ContentValues();
        values.put(UserInfoDBInfo.UID, userInfo.uid);
        int following = userInfo.following ? 1 : 0;
        values.put(UserInfoDBInfo.FOLLOWING, following);
        int followMe = userInfo.followMe ? 1 : 0;
        values.put(UserInfoDBInfo.FOLLOW_ME, followMe);
        values.put(UserInfoDBInfo.SCREEN_NAME, userInfo.screenName);
        values.put(UserInfoDBInfo.LOCATION, userInfo.location);
        values.put(UserInfoDBInfo.STATUSES_COUNT, userInfo.statusesCount);
        values.put(UserInfoDBInfo.DESCRIPTION, userInfo.description);
        values.put(UserInfoDBInfo.FOLLOWERS_COUNT, userInfo.followersCount);
        values.put(UserInfoDBInfo.AVATAR, userInfo.avatar);
        values.put(UserInfoDBInfo.COVER, userInfo.cover);
        values.put(UserInfoDBInfo.FRIENDS_COUNT, userInfo.friendsCount);
        return values;
    }

    public UserInfo select(long uid){
        Cursor cursor = query(null, UserInfoDBInfo.UID + "=?", new String[]{String.valueOf(uid)}, null);
        UserInfo userInfo = null;
        if(cursor.moveToFirst()){
            userInfo = UserInfo.fromCursor(cursor);
        }
        cursor.close();
        return userInfo;
    }

    public UserInfo select(String username){
        Cursor cursor = query(null, UserInfoDBInfo.SCREEN_NAME + "=?", new String[]{username}, null);
        UserInfo userInfo = null;
        if(cursor.moveToFirst()){
            userInfo = UserInfo.fromCursor(cursor);
        }
        cursor.close();
        return userInfo;
    }

    /**
     * insert or update
     * @param userInfo
     */
    public void insert(UserInfo userInfo){
        if(select(userInfo.uid) == null){
            //insert
            ContentValues values = getContentValues(userInfo);
            insert(values);
        }else{
            //update
            update(userInfo);
        }
    }

    /**
     * 批量insert，需检查是否存在，存在则变为update
     * @param userInfoList userinfos to insert
     * @return result
     */
    public int bulkInsert(List<UserInfo> userInfoList){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getWritableDatabase();
            db.beginTransaction();
            try{
                for(UserInfo userInfo : userInfoList){
                    ContentValues values = getContentValues(userInfo);
                    if(select(userInfo.uid) == null){
                        //insert
                        db.insert(getTableName(), null, values);
                    }else{
                        //update
                        db.update(getTableName(), values, UserInfoDBInfo.UID + "=?", new String[]{String.valueOf(userInfo.uid)});
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

    public int update(UserInfo userInfo){
        ContentValues values = getContentValues(userInfo);
        return update(values, UserInfoDBInfo.UID + "=?", new String[]{String.valueOf(userInfo.uid)});
    }
}
