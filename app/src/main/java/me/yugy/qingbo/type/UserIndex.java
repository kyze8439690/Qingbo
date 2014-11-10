package me.yugy.qingbo.type;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import me.yugy.qingbo.dao.dbinfo.UserIndexDBInfo;
import me.yugy.qingbo.utils.PinyinUtils;

/**
 * Created by yugy on 2014/5/30.
 */
public class UserIndex {

    public long uid = -1;
    public String screenName;
    public String avatar;
    public String searchIndex; //format: 杨辉#yanghui#yh

    public void parse(Context context, UserInfo userInfo){
        uid = userInfo.uid;
        screenName = userInfo.screenName;
        avatar = userInfo.avatar;
        String screenNameOnlyChinese = screenName.replaceAll("[^\\u4E00-\\u9FA5]", "");
        searchIndex = screenName + "#" + PinyinUtils.toPinyin(context, screenNameOnlyChinese) + "#" + PinyinUtils.toPinyinShortCut(context, screenNameOnlyChinese);
    }

    public static UserIndex fromCursor(Cursor cursor){
        UserIndex userIndex = new UserIndex();
        userIndex.uid = cursor.getLong(cursor.getColumnIndex(UserIndexDBInfo.UID));
        userIndex.screenName = cursor.getString(cursor.getColumnIndex(UserIndexDBInfo.SCREEN_NAME));
        userIndex.avatar = cursor.getString(cursor.getColumnIndex(UserIndexDBInfo.AVATAR));
        userIndex.searchIndex = cursor.getString(cursor.getColumnIndex(UserIndexDBInfo.SEARCH_INDEX));
        return userIndex;
    }

}
