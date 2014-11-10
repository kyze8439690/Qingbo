package me.yugy.qingbo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by yugy on 2014/5/29.
 */
public abstract class RefreshTimelineBroadcastReceiver extends BroadcastReceiver{

    public static final String ACTION_REFRESH_TIMELINE = "qingbo.intent.action.REFRESH_TIMELINE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_REFRESH_TIMELINE)){
            onRefresh();
        }
    }

    public abstract void onRefresh();
}
