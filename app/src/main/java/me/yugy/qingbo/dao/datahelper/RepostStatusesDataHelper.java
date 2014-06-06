package me.yugy.qingbo.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import org.json.JSONException;

import java.text.ParseException;

import me.yugy.qingbo.dao.DataProvider;
import me.yugy.qingbo.dao.dbinfo.RepostStatusDBInfo;
import me.yugy.qingbo.type.BaseStatus;
import me.yugy.qingbo.type.RepostStatus;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.ArrayUtils;

/**
 * Created by yugy on 2014/4/22.
 */
public class RepostStatusesDataHelper extends BaseDataHelper implements BaseStatusesDataHelper{

    public static final String TABLE_NAME = "repost_statuses";

    public RepostStatusesDataHelper(Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.REPOST_STATUSES_CONTENT_URI;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    protected ContentValues getContentValues(RepostStatus repostStatus){
        ContentValues values = new ContentValues();
        values.put(RepostStatusDBInfo.ID, repostStatus.id);
        values.put(RepostStatusDBInfo.TEXT, repostStatus.text.toString());
        values.put(RepostStatusDBInfo.UID, repostStatus.user.uid);

        values.put(RepostStatusDBInfo.TOPICS, ArrayUtils.convertArrayToString(repostStatus.topics));

        values.put(RepostStatusDBInfo.TIME, repostStatus.time);
        values.put(RepostStatusDBInfo.COMMENT_COUNT, repostStatus.commentCount);
        values.put(RepostStatusDBInfo.REPOST_COUNT, repostStatus.repostCount);

        values.put(RepostStatusDBInfo.PICS, ArrayUtils.convertArrayToString(repostStatus.pics));
        values.put(RepostStatusDBInfo.PICS, ArrayUtils.convertArrayToString(repostStatus.pics));

        values.put(RepostStatusDBInfo.LAT, repostStatus.latitude);
        values.put(RepostStatusDBInfo.LONG, repostStatus.longitude);
        return values;
    }

    public RepostStatus select(long id){
        Cursor cursor = query(null, RepostStatusDBInfo.ID + "=?", new String[]{String.valueOf(id)}, null);
        if(cursor.moveToFirst()){
            try {
                RepostStatus repostStatus = RepostStatus.fromCursor(cursor);
                cursor.close();
                return repostStatus;
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

    public void insert(RepostStatus repostStatus){
        new UserInfoDataHelper(getContext()).insert(repostStatus.user);
        if(select(repostStatus.id) == null){
            ContentValues values = getContentValues(repostStatus);
            insert(values);
        }else{
            update(repostStatus);
        }
    }

    @Override
    public int update(BaseStatus baseStatus) {
        RepostStatus repostStatus;
        if(baseStatus instanceof Status){
            repostStatus = RepostStatus.fromStatus((Status)baseStatus);
        }else {
            repostStatus = (RepostStatus) baseStatus;
        }
        new UserInfoDataHelper(getContext()).insert(repostStatus.user);
        ContentValues values = getContentValues(repostStatus);
        return update(values, RepostStatusDBInfo.ID + "=?", new String[]{String.valueOf(repostStatus.id)});
    }
}
