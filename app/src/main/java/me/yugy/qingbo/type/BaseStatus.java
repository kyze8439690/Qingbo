package me.yugy.qingbo.type;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by yugy on 2014/5/24.
 */
public interface BaseStatus {

    public static final int TYPE_NO_REPOST_NO_PIC = 0;
    public static final int TYPE_NO_REPOST_ONE_PIC = 1;
    public static final int TYPE_NO_REPOST_MULTI_PICS = 2;
    public static final int TYPE_HAS_REPOST_NO_PIC = 3;
    public static final int TYPE_HAS_REPOST_ONE_PIC = 4;
    public static final int TYPE_HAS_REPOST_MULTI_PICS = 5;

    public void parse(JSONObject json) throws JSONException, ParseException;

}
