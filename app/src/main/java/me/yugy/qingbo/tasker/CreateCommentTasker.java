package me.yugy.qingbo.tasker;

import android.app.ProgressDialog;
import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import me.yugy.qingbo.R;
import me.yugy.qingbo.dao.datahelper.CommentsDataHelper;
import me.yugy.qingbo.type.Comment;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/4/21.
 */
public class CreateCommentTasker {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private OnCommentListener mOnCommentListener;

    private String mComment;
    private long mStatusId;

    public CreateCommentTasker(Context context){
        mContext = context;
        try{
            mOnCommentListener = (OnCommentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("the activity must implement the OnCommentListener interface");
        }
        mProgressDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(context.getString(R.string.send_comment));
        mProgressDialog.setIndeterminate(true);
    }

    public CreateCommentTasker add(String comment, long statusId){
        mComment = comment;
        mStatusId = statusId;
        return this;
    }

    public void execute(){
        mProgressDialog.show();
        Weibo.createComment(mContext, mComment, mStatusId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Comment comment = new Comment();
                    comment.parse(response);
                    new CommentsDataHelper(mContext).insert(comment);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mProgressDialog.dismiss();
                mOnCommentListener.onSuccess();
                super.onSuccess(response);
            }
        });
    }

    public static interface OnCommentListener {
        public void onSuccess();
    }
}
