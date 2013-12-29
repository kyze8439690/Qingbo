package com.yugy.qingbo.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

import com.yugy.qingbo.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by yugy on 13-10-4.
 */
public class TimeLineModel implements Parcelable{
    public String id;
    public SpannableString text;
    public String name;
    public String headUrl;
    public ArrayList<String> topics = new ArrayList<String>();
    public String time;
    public int commentCount;
    public int repostCount;
    public boolean hasPic;
    public boolean hasPics;
    public ArrayList<String> pics = new ArrayList<String>();
    public boolean hasRepost;
    public SpannableString repostName = new SpannableString("");
    public SpannableString repostText = new SpannableString("");
    public boolean hasRepostPic;
    public boolean hasRepostPics;

    private String unParseTime;

    public void parse(JSONObject json) throws JSONException, ParseException {
        id = json.getString("id");
        text = TextUtils.parseStatusText(json.getString("text"));
        name = json.getJSONObject("user").getString("screen_name");
        headUrl = json.getJSONObject("user").getString("avatar_large");
//        headUrl = json.getJSONObject("user").getString("profile_image_url");
        topics.addAll(TextUtils.getTopic(json.getString("text")));
        unParseTime = json.getString("created_at");
        time = TextUtils.parseTime(unParseTime);
        commentCount = json.getInt("comments_count");
        repostCount = json.getInt("reposts_count");
        JSONArray picsJson = json.getJSONArray("pic_urls");
        hasPics = (picsJson.length() > 1);
        for (int i = 0; i < picsJson.length(); i++){
            pics.add(picsJson.getJSONObject(i).getString("thumbnail_pic"));
        }
        hasPic = json.has("thumbnail_pic");
        if (hasRepost = json.has("retweeted_status")) {
            JSONObject repostJson = json.getJSONObject("retweeted_status");
            repostName = TextUtils.parseStatusText("此微博最初是由@" + repostJson.getJSONObject("user").getString("screen_name") + " 分享的");
            repostText = TextUtils.parseStatusText(repostJson.getString("text"));
            topics.addAll(TextUtils.getTopic(repostJson.getString("text")));
            JSONArray repostPicsJson = repostJson.getJSONArray("pic_urls");
            hasRepostPics = (repostPicsJson.length() > 1);
            for (int i = 0; i < repostPicsJson.length(); i++){
                pics.add(repostPicsJson.getJSONObject(i).getString("thumbnail_pic"));
            }
            hasRepostPic = repostJson.has("thumbnail_pic");
        }
    }

    public void reParseTime() throws ParseException {
        time = TextUtils.parseTime(unParseTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeIntArray(new int[]{
                commentCount,
                repostCount
        });
        dest.writeBooleanArray(new boolean[]{
                hasPic,
                hasPics,
                hasRepost,
                hasRepostPic,
                hasRepostPics
        });
        dest.writeStringArray(new String[]{
                id,
                text.toString(),
                name,
                headUrl,
                unParseTime,
                repostName.toString(),
                repostText.toString(),
        });
        ArrayList<ArrayList<String>> stringArrayData = new ArrayList<ArrayList<String>>();
        stringArrayData.add(topics);
        stringArrayData.add(pics);
        dest.writeList(stringArrayData);
    }

    public TimeLineModel(){}

    private TimeLineModel(Parcel in) throws ParseException {
        int[] intData = new int[2];
        in.readIntArray(intData);
        this.commentCount = intData[0];
        this.repostCount = intData[1];

        boolean[] booleanData = new boolean[5];
        in.readBooleanArray(booleanData);
        this.hasPic = booleanData[0];
        this.hasPics = booleanData[1];
        this.hasRepost = booleanData[2];
        this.hasRepostPic = booleanData[3];
        this.hasRepostPics = booleanData[4];

        String[] stringData = new String[7];
        in.readStringArray(stringData);
        this.id = stringData[0];
        this.text = TextUtils.parseStatusText(stringData[1]);
        this.name = stringData[2];
        this.headUrl = stringData[3];
        this.unParseTime = stringData[4];
        this.time = TextUtils.parseTime(this.unParseTime);
        this.repostName = TextUtils.parseStatusText(stringData[5]);
        this.repostText = TextUtils.parseStatusText(stringData[6]);

        ArrayList<ArrayList<String>> stringArrayData = new ArrayList<ArrayList<String>>();
        in.readList(stringArrayData, ArrayList.class.getClassLoader());
        this.topics = stringArrayData.get(0);
        this.pics = stringArrayData.get(1);
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            try {
                return new TimeLineModel(source);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Object[] newArray(int size) {
            return new TimeLineModel[size];
        }
    };
}