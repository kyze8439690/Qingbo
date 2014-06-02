package me.yugy.qingbo.intent;

import android.content.Context;
import android.content.Intent;

import me.yugy.qingbo.service.WeiboQueueService;

/**
 * Created by yugy on 2014/5/28.
 */
public class NewStatusIntent extends Intent{

    public static final String ACTION_SEND_WEIBO_ONLY_TEXT = "qingbo.intent.action.SEND_WEIBO_ONLY_TEXT";
    public static final String ACTION_SEND_WEIBO_WITH_IMAGE = "qingbo.intent.action.SEND_WEIBO_WITH_IMAGE";

    private NewStatusIntent(Context context, Class<?> cls){
        super(context, cls);
    }

    public static class Builder{

        private NewStatusIntent mIntent;

        public Builder(Context context){
            mIntent = new NewStatusIntent(context, WeiboQueueService.class);
        }

        public Builder setText(String text){
            mIntent.putExtra("text", text);
            mIntent.setAction(ACTION_SEND_WEIBO_ONLY_TEXT);
            return this;
        }

        public NewStatusIntent create(){
            return mIntent;
        }

    }

}
