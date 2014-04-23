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
import me.yugy.qingbo.dao.dbinfo.UserInfoDBInfo;
import me.yugy.qingbo.type.Comment;

/**
 * Created by yugy on 2014/4/21.
 */
public class CommentsDataHelper extends BaseDataHelper{

    public static final String TABLE_NAME = "comments";

    public CommentsDataHelper(Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.COMMENTS_CONTENT_URI;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME   ;
    }

    protected ContentValues getContentValues(Comment comment){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CommentDBInfo.ID, comment.id);
        contentValues.put(CommentDBInfo.STATUS_ID, comment.statusId);
        contentValues.put(CommentDBInfo.UID, comment.user.uid);
        contentValues.put(CommentDBInfo.TEXT, comment.text.toString());
        contentValues.put(CommentDBInfo.TIME, comment.time);
        return contentValues;
    }

    public Uri insert(Comment comment){
        ContentValues values = getContentValues(comment);
        return insert(values);
    }

    public Comment select(long id){
        Cursor cursor = query(null, CommentDBInfo.ID + "=?", new String[]{String.valueOf(id)}, null);
        if(cursor.moveToFirst()){
            Comment comment = Comment.fromCursor(cursor);
            cursor.close();
            return comment;
        }else{
            cursor.close();
            return null;
        }
    }

    public int bulkInsert(List<Comment> commentList){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getWritableDatabase();
            db.beginTransaction();
            try{
                for(Comment comment : commentList){
                    ContentValues values = getContentValues(comment);
                    if(select(comment.id) == null){
                        db.insert(getTableName(), null, values);
                    }else{
                        db.update(getTableName(), values, CommentDBInfo.ID + "=?", new String[]{String.valueOf(comment.id)});
                    }

                    values = UserInfoDataHelper.getContentValues(comment.user);
                    if(new UserInfoDataHelper(getContext()).select(comment.user.uid) == null){
                        //insert
                        db.insert(UserInfoDataHelper.TABLE_NAME, null, values);
                    }else{
                        //update
                        db.update(UserInfoDataHelper.TABLE_NAME, values, UserInfoDBInfo.UID + "=?", new String[]{String.valueOf(comment.user.uid)});
                    }
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(getContentUri(), null);
                return commentList.size();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }
            throw new SQLException("Fail to insert row into " + getContentUri());
        }
    }

    public long getCommentCount(long statusId){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getReadableDatabase();
            Cursor cursor = db.query(getTableName(), new String[] {"count(*)"}, CommentDBInfo.STATUS_ID + "=?", new String[]{String.valueOf(statusId)}, null, null, null);
            long count;
            if(cursor.moveToFirst()){
                count = cursor.getLong(0);
            }else{
                count = 0;
            }
            return count;
        }
    }

    public long getNewestId(long statusId){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getReadableDatabase();
            Cursor cursor = db.query(getTableName(), new String[] {"MAX(" + CommentDBInfo.ID + ")"}, CommentDBInfo.STATUS_ID + "=?", new String[]{String.valueOf(statusId)}, null, null, null);
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

    public long getOldestId(long statusId){
        synchronized (DataProvider.obj){
            SQLiteDatabase db = DataProvider.getDBHelper().getReadableDatabase();
            Cursor cursor = db.query(getTableName(), new String [] {"MIN(" + CommentDBInfo.ID + ")"}, CommentDBInfo.STATUS_ID + "=?", new String[]{String.valueOf(statusId)}, null, null, null);
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
}
