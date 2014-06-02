package me.yugy.qingbo.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.yugy.qingbo.R;
import me.yugy.qingbo.broadcast.RefreshTimelineBroadcastReceiver;
import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.intent.NewStatusIntent;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/5/28.
 * Service used to send new Weibo one by one
 */
public class WeiboQueueService extends IntentService{

    private int mNewWeiboQueueNum = 0;

    private static final int NEW_WEIBO_NOTIFY_ID = 10086;

    private NotificationManager mNotificationManager;
    private Handler mHandler;

    public WeiboQueueService() {
        super("WeiboQueueService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler = new Handler();
        if(intent.getAction().equals(NewStatusIntent.ACTION_SEND_WEIBO_ONLY_TEXT)
                || intent.getAction().equals(NewStatusIntent.ACTION_SEND_WEIBO_WITH_IMAGE)){
            mNewWeiboQueueNum++;
            newOrUpdateNewWeiboNotification();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getAction().equals(NewStatusIntent.ACTION_SEND_WEIBO_ONLY_TEXT)
                || intent.getAction().equals(NewStatusIntent.ACTION_SEND_WEIBO_WITH_IMAGE)){
            try {
                newWeibo(intent);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Send weibo failed.");
            }
        }
    }

    private void showToast(final String toast){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MessageUtils.toast(getApplicationContext(), toast);
            }
        });
    }

    private void newOrUpdateNewWeiboNotification(){
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Sending New Weibo...")
                .setContentText(String.format("There is %d weibo sending in the background", mNewWeiboQueueNum))
                .setOngoing(true)
                .setNumber(mNewWeiboQueueNum)
                .setSmallIcon(R.drawable.ic_stat_upload)
                .setTicker("Sending New Weibo...")
                .build();
        mNotificationManager.notify(NEW_WEIBO_NOTIFY_ID, notification);
    }

    private void newWeibo(Intent intent) throws InterruptedException, ExecutionException, JSONException, IOException, ParseException {

        //send new weibo synchronously
        JSONObject result = Weibo.syncNewStatusOnlyText(this, intent.getStringExtra("text"));
        handleNewWeiboResult(result);

        mNewWeiboQueueNum--;

        if(mNewWeiboQueueNum == 0){
            mNotificationManager.cancel(NEW_WEIBO_NOTIFY_ID);
        }else{
            newOrUpdateNewWeiboNotification();
        }
    }

    private void handleNewWeiboResult(JSONObject result) {
        if(result.has("error")){
            try {
                showToast(result.getString("error"));
            } catch (JSONException e) {
                e.printStackTrace();
                showToast("Send new weibo failed.");
            }
        }else {
            showToast("Send weibo successfully.");
            sendBroadcast(new Intent(RefreshTimelineBroadcastReceiver.ACTION_REFRESH_TIMELINE));
        }
    }

    @Override
    public void onDestroy() {
        DebugUtils.log("WeiboQueueService onDestroy");
        super.onDestroy();
    }
}
