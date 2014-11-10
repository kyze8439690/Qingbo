package me.yugy.qingbo.tasker;

import android.app.ProgressDialog;
import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import me.yugy.qingbo.R;
import me.yugy.qingbo.type.Comment;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/4/25.
 */
public class CreateRepostCommentTasker {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private OnCommentListener mOnCommentListener;

    private String mComment;
    private long mStatusId;

    public CreateRepostCommentTasker(Context context){
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

    public CreateRepostCommentTasker add(String comment, long statusId){
        mComment = comment;
        mStatusId = statusId;
        return this;
    }

    public void execute(){
        mProgressDialog.show();
        Weibo.createComment(mContext, mComment, mStatusId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Comment comment = new Comment();
                    comment.parse(response);
                    mProgressDialog.dismiss();
                    mOnCommentListener.onSuccess(comment);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if(errorResponse.getInt("error_code") == 20019){
                        MessageUtils.toast(mContext, "评论内容重复");
                    }else{
                        MessageUtils.toast(mContext, errorResponse.getString("error"));
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                mProgressDialog.dismiss();
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static interface OnCommentListener {
        public void onSuccess(Comment comment);
    }

}
