package me.yugy.qingbo.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.json.JSONException;

import java.text.ParseException;
import java.util.List;

import me.yugy.qingbo.dao.DataProvider;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.type.BaseStatus;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.ArrayUtils;

/**
 * Created by yugy on 2014/4/16.
 */
public class StatusesDataHelper extends BaseDataHelper implements BaseStatusesDataHelper{

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

        values.put(StatusDBInfo.TOPICS, ArrayUtils.convertArrayToString(status.topics));

        values.put(StatusDBInfo.TIME, status.time);
        values.put(StatusDBInfo.COMMENT_COUNT, status.commentCount);
        values.put(StatusDBInfo.REPOST_COUNT, status.repostCount);

        values.put(StatusDBInfo.PICS, ArrayUtils.convertArrayToString(status.pics));

        if(status.repostStatus != null) {
            values.put(StatusDBInfo.REPOST_STATUS_ID, status.repostStatus.id);
        }else{
            values.put(StatusDBInfo.REPOST_STATUS_ID, -1);
        }

        values.put(StatusDBInfo.TYPE, status.type);

        values.put(StatusDBInfo.LAT, status.latitude);
        values.put(StatusDBInfo.LONG, status.longitude);

        return values;
    }

    public Status select(long id){
        Cursor cursor = query(null, StatusDBInfo.ID + "=?", new String[]{String.valueOf(id)}, null);
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

    public long getNewestId(){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getReadableDatabase();
            Cursor cursor = db.query(getTableName(), new String[] {"MAX(" + StatusDBInfo.ID + ")"}, null, null, null, null, null);
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
                        db.update(getTableName(), values, StatusDBInfo.ID + "=?", new String[]{String.valueOf(status.id)});
                    }

                    new UserInfoDataHelper(getContext()).insert(status.user);
                    if(status.repostStatus != null){
                        new UserInfoDataHelper(getContext()).insert(status.user);
                        new RepostStatusesDataHelper(getContext()).insert(status.repostStatus);
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

    @Override
    public int update(BaseStatus baseStatus) {
        Status status = (Status) baseStatus;
        new UserInfoDataHelper(getContext()).insert(status.user);
        if(status.repostStatus != null){
            new RepostStatusesDataHelper(getContext()).insert(status.repostStatus);
        }
        ContentValues values = getContentValues(status);
        return update(values, StatusDBInfo.ID + "=?", new String[]{String.valueOf(status.id)});
    }
}
