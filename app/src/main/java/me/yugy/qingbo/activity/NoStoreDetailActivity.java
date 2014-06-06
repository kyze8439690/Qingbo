package me.yugy.qingbo.activity;

import android.app.Activity;
import android.content.Context;
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
import me.yugy.qingbo.adapter.NoStoreCommentAdapter;
import me.yugy.qingbo.dao.datahelper.BaseStatusesDataHelper;
import me.yugy.qingbo.dao.datahelper.RepostStatusesDataHelper;
import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.tasker.CreateRepostCommentTasker;
import me.yugy.qingbo.type.Comment;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.vendor.Weibo;
import me.yugy.qingbo.view.DetailHeaderViewHelper;
import me.yugy.qingbo.view.RepostDetailHeaderViewHelper;

import static me.yugy.qingbo.tasker.CreateRepostCommentTasker.OnCommentListener;

/**
 * Created by yugy on 2014/4/24.
 */
public class NoStoreDetailActivity extends Activity implements TextWatcher, View.OnClickListener, AdapterView.OnItemClickListener, OnLoadMoreListener, OnCommentListener{

    private boolean mIsLoading;
    private boolean mNoCommentViewIsShown = false;

    private ListView mListView;
    private View mNoCommentView;
    private EditText mEditText;
    private ImageButton mSendCommentButton;
    private BaseStatusesDataHelper mBaseStatusesDataHelper;
    private NoStoreCommentAdapter mNoStoreCommentAdapter;
    private DetailHeaderViewHelper mDetailHeaderViewHelper;

    private Status mStatus;
    private boolean mIsRepost;

    private long mNewestCommentId = 0;
    private long mOldestCommentId = 0;

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
        mIsRepost = getIntent().getBooleanExtra("isRepost", false);
        if(mIsRepost){
            mBaseStatusesDataHelper = new RepostStatusesDataHelper(this);
        }else{
            mBaseStatusesDataHelper = new StatusesDataHelper(this);
        }
        mNoStoreCommentAdapter = new NoStoreCommentAdapter(this);

        mDetailHeaderViewHelper = new DetailHeaderViewHelper(this);
        mListView.addHeaderView(mDetailHeaderViewHelper.getHeaderView(mStatus), null, false);
        if(mStatus.repostCount != 0){
            TextView detailView = (TextView) getLayoutInflater().inflate(R.layout.view_detail_description, null);
            detailView.setText(String.format(getString(R.string.detail_description), mStatus.repostCount));
            mListView.addHeaderView(detailView, null, false);
        }
        mListView.setAdapter(mNoStoreCommentAdapter);
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        mListView.setOnItemClickListener(this);

        mIsLoading = true;
        setProgressBarIndeterminateVisibility(true);
        getNewCommentData();
    }

    private void getNewCommentData(){
        Weibo.getNewComments(this, mStatus.id, mNewestCommentId, new JsonHttpResponseHandler(){
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
                        if(i == 0){
                            mNewestCommentId = comment.id;
                        }
                        if(mOldestCommentId == 0 && i == commentSize - 1){
                            mOldestCommentId = comment.id;
                        }
                        comments.add(comment);
                    }
                    if(comments.size() != 0) {
                        if(mNoCommentViewIsShown){
                            mListView.removeHeaderView(mNoCommentView);
                            mNoCommentViewIsShown = false;
                        }
                        mNoStoreCommentAdapter.appendNewData(comments);
                    }else if(mNoStoreCommentAdapter.getCount() == 0){
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
        Weibo.getOldComments(this, mStatus.id, mOldestCommentId, new JsonHttpResponseHandler(){
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
                        if(i == commentSize - 1){
                            mOldestCommentId = comment.id;
                        }
                        comments.add(comment);
                    }
                    if (comments.size() != 0) {
                        mNoStoreCommentAdapter.appendOldData(comments);
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
        mBaseStatusesDataHelper.update(mStatus);
    }

    public void setLoading(boolean isLoading) {
        mIsLoading = isLoading;
        setProgressBarIndeterminateVisibility(isLoading);
        invalidateOptionsMenu();
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
            case android.R.id.home:
                finish();
                return true;
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        mSendCommentButton.setEnabled(mEditText.getText().length() > 0);
    }

    @Override
    public void onClick(View v) {
        new CreateRepostCommentTasker(this)
                .add(mEditText.getText().toString(), mStatus.id)
                .execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position - mListView.getHeaderViewsCount();
        MessageUtils.toast(this, index + "");
    }

    @Override
    public void onLoadMore() {
        if(!mIsLoading){
            if(mNoStoreCommentAdapter.getCount() < mStatus.commentCount){
                setLoading(true);
                getOldCommentData();
            }
        }
    }

    @Override
    public void onSuccess(final Comment comment) {
        mEditText.setText("");
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        if(mNoCommentViewIsShown){
            mListView.removeHeaderView(mNoCommentView);
        }
        mStatus.commentCount++;
        updateStatusData();
        mNoStoreCommentAdapter.appendNewData(comment);
        mNewestCommentId = comment.id;
    }
}