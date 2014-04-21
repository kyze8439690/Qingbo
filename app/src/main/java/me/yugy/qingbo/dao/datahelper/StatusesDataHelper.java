package me.yugy.qingbo.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.List;

import me.yugy.qingbo.dao.DataProvider;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.dao.dbinfo.UserInfoDBInfo;
import me.yugy.qingbo.type.Status;

/**
 * Created by yugy on 2014/4/16.
 */
public class StatusesDataHelper extends BaseDataHelper{

    public static final String TABLE_NAME = "statuses";

    public StatusesDataHelper(Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.STATUSES_CONTENT_URI;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    protected ContentValues getContentValues(Status status){
        ContentValues values = new ContentValues();
        values.put(StatusDBInfo.ID, status.id);
        values.put(StatusDBInfo.TEXT, status.text.toString());
        values.put(StatusDBInfo.UID, status.user.uid);

        JSONArray topics = new JSONArray();
        for(String topic : status.topics){
            topics.put(topic);
        }
        values.put(StatusDBInfo.TOPICS, topics.toString());

        values.put(StatusDBInfo.TIME, status.time);
        values.put(StatusDBInfo.COMMENT_COUNT, status.commentCount);
        values.put(StatusDBInfo.REPOST_COUNT, status.repostCount);

        JSONArray pics = new JSONArray();
        for(String pic : status.pics){
            pics.put(pic);
        }
        values.put(StatusDBInfo.PICS, pics.toString());

        values.put(StatusDBInfo.REPOST_JSON, status.repostJson.toString());
        return values;
    }

    public Status select(long id){
        Cursor cursor = query(null, StatusDBInfo.ID + "=\"" + id + "\"", null, null);
        if(cursor.moveToFirst()){
            try {
                Status status = Status.fromCursor(cursor);
                cursor.close();
                return status;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            throw new SQLException("Fail to select row from " + getContentUri());
        }else{
            cursor.close();
            return null;
        }
    }

    public int update(Status status){
        ContentValues values = getContentValues(status);
        return update(values, StatusDBInfo.ID + "=" + status.id, null);
    }

    public long getNewestId(){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getReadableDatabase();
            Cursor cursor = db.query(getTableName(), new String [] {"MAX(" + StatusDBInfo.ID + ")"}, null, null, null, null, null);
            long id;
            if(cursor.moveToFirst()){
                id = cursor.getLong(0);
            }else{
                id = 0;
            }
            db.close();
            cursor.close();
            return id;
        }
    }

    public long getOldestId(){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getReadableDatabase();
            Cursor cursor = db.query(getTableName(), new String [] {"MIN(" + StatusDBInfo.ID + ")"}, null, null, null, null, null);
            long id;
            if(cursor.moveToFirst()){
                id = cursor.getLong(0);
            }else{
                id = 0;
            }
            db.close();
            cursor.close();
            return id;
        }
    }

    /**
     * 批量insert，需检查是否存在，存在则变为update
     * @param statusList
     * @return
     */
    public int bulkInsert(List<Status> statusList){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getWritableDatabase();
            db.beginTransaction();
            try{
                for(Status status : statusList){
                    ContentValues values = getContentValues(status);
                    if(select(status.id) == null){
                        //insert
                        db.insert(getTableName(), null, values);
                    }else{
                        //update
                        db.update(getTableName(), values, StatusDBInfo.ID + "=\"" + status.id + "\"", null);
                    }

                    values = UserInfoDataHelper.getContentValues(status.user);
                    if(new UserInfoDataHelper(getContext()).select(status.user.uid) == null){
                        //insert
                        db.insert(UserInfoDataHelper.TABLE_NAME, null, values);
                    }else{
                        //update
                        db.update(UserInfoDataHelper.TABLE_NAME, values, UserInfoDBInfo.UID + "=\"" + status.user.uid + "\"", null);
                    }
                    if(status.repostStatus != null){
                        values = UserInfoDataHelper.getContentValues(status.repostStatus.user);
                        if(new UserInfoDataHelper(getContext()).select(status.repostStatus.user.uid) == null){
                            //insert
                            db.insert(UserInfoDataHelper.TABLE_NAME, null, values);
                        }else{
                            //update
                            db.update(UserInfoDataHelper.TABLE_NAME, values, UserInfoDBInfo.UID + "=\"" + status.repostStatus.user.uid + "\"", null);
                        }
                    }
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(getContentUri(), null);
                return statusList.size();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }
            throw new SQLException("Fail to insert row into " + getContentUri());
        }
    }
}
