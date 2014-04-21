package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import me.yugy.qingbo.R;
import me.yugy.qingbo.adapter.CommentAdapter;
import me.yugy.qingbo.dao.datahelper.CommentsDataHelper;
import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.dao.dbinfo.CommentDBInfo;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.tasker.CreateCommentTasker;
import me.yugy.qingbo.type.Comment;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.vendor.Weibo;
import me.yugy.qingbo.view.DetailHeaderViewHelper;

/**
 * Created by yugy on 2014/4/20.
 */
public class DetailActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener,
        OnLoadMoreListener, TextWatcher, View.OnClickListener, CreateCommentTasker.OnCommentListener {

    private boolean mIsLoading;

    private Status mStatus;
    private ListView mListView;
    private DetailHeaderViewHelper mDetailHeaderViewHelper;
    private CommentsDataHelper mCommentsDataHelper;
    private CommentAdapter mCommentAdapter;
    private View mNoCommentView;
    private EditText mEditText;
    private ImageButton mSendCommentButton;

    private boolean mNoCommentViewIsShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_detail);

        mListView = (ListView) findViewById(R.id.list);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mSendCommentButton = (ImageButton) findViewById(R.id.add_comment);
        mEditText.addTextChangedListener(this);
        mSendCommentButton.setOnClickListener(this);
        mSendCommentButton.setEnabled(false);

        mStatus = getIntent().getParcelableExtra("status");
        mCommentsDataHelper = new CommentsDataHelper(this);
        mCommentAdapter = new CommentAdapter(this);

        mDetailHeaderViewHelper = new DetailHeaderViewHelper(this);
        mListView.addHeaderView(mDetailHeaderViewHelper.getHeaderView(mStatus), null, false);
        if(mStatus.repostCount != 0){
            TextView detailView = (TextView) getLayoutInflater().inflate(R.layout.view_detail_description, null);
            detailView.setText(String.format(getString(R.string.detail_description), mStatus.repostCount));
            mListView.addHeaderView(detailView, null, false);
        }
        mListView.setAdapter(mCommentAdapter);
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        mListView.setOnItemClickListener(this);

        mIsLoading = true;
        setProgressBarIndeterminateVisibility(true);
        getNewCommentData();

        getLoaderManager().initLoader(0, null, this);
    }

    private void getNewCommentData(){
        Weibo.getNewComments(this, mStatus.id, mCommentsDataHelper.getNewestId(mStatus.id), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    mStatus.commentCount = response.getInt("total_number");
                    updateStatusData();
                    ArrayList<Comment> comments = new ArrayList<Comment>();
                    JSONArray commentJsonArray = response.getJSONArray("comments");
                    int commentSize = commentJsonArray.length();
                    for(int i = 0; i < commentSize; i++){
                        Comment comment = new Comment();
                        comment.parse(commentJsonArray.getJSONObject(i));
                        comments.add(comment);
                    }
                    if(comments.size() != 0) {
                        if(mNoCommentViewIsShown){
                            mListView.removeHeaderView(mNoCommentView);
                            mNoCommentViewIsShown = false;
                        }
                        mCommentsDataHelper.bulkInsert(comments);
                    }else if(mCommentsDataHelper.getCommentCount(mStatus.id) == 0){
                        if(!mNoCommentViewIsShown) {
                            mNoCommentView = getLayoutInflater().inflate(R.layout.view_no_comment, null);
                            mListView.addHeaderView(mNoCommentView, null, false);
                            mNoCommentViewIsShown = true;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                setLoading(false);
                super.onSuccess(response);
            }
        });
    }

    private void getOldCommentData(){
        Weibo.getOldComments(this, mStatus.id, mCommentsDataHelper.getOldestId(mStatus.id), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    mStatus.commentCount = response.getInt("total_number");
                    updateStatusData();
                    ArrayList<Comment> comments = new ArrayList<Comment>();
                    JSONArray commentJsonArray = response.getJSONArray("comments");
                    int commentSize = commentJsonArray.length();
                    for (int i = 0; i < commentSize; i++) {
                        Comment comment = new Comment();
                        comment.parse(commentJsonArray.getJSONObject(i));
                        comments.add(comment);
                    }
                    if (comments.size() != 0) {
                        mCommentsDataHelper.bulkInsert(comments);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                setLoading(false);
                super.onSuccess(response);
            }
        });
    }

    private void updateStatusData(){
        if(mStatus.commentCount == 0){
            mDetailHeaderViewHelper.getCommentCountView().setText("");
        }else {
            mDetailHeaderViewHelper.getCommentCountView().setText(String.valueOf(mStatus.commentCount));
        }
        new StatusesDataHelper(this).update(mStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!mIsLoading){
            getMenuInflater().inflate(R.menu.detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh:
                if(!mIsLoading) {
                    setLoading(true);
                    getNewCommentData();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setLoading(boolean isLoading) {
        mIsLoading = isLoading;
        setProgressBarIndeterminateVisibility(isLoading);
        invalidateOptionsMenu();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mCommentsDataHelper.getCursorLoader(CommentDBInfo.STATUS_ID + "=" + mStatus.id, CommentDBInfo.ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCommentAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommentAdapter.changeCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position - mListView.getHeaderViewsCount();
        MessageUtils.toast(this, index + "");
    }

    @Override
    public void onLoadMore() {
        if(!mIsLoading){
            long existCommentCount = mCommentsDataHelper.getCommentCount(mStatus.id);
            if(existCommentCount < mStatus.commentCount){
                setLoading(true);
                getOldCommentData();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mSendCommentButton.setEnabled(mEditText.getText().length() > 0);
    }

    @Override
    public void onClick(View v) {
        new CreateCommentTasker(this)
                .add(mEditText.getText().toString(), mStatus.id)
                .execute();
    }

    @Override
    public void onSuccess() {
        mEditText.setText("");
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        if(mNoCommentViewIsShown){
            mListView.removeHeaderView(mNoCommentView);
        }
    }
}
