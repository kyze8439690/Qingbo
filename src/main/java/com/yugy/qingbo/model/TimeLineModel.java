package com.yugy.qingbo.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

import com.yugy.qingbo.Utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by yugy on 13-10-4.
 * Data has so many type, such as below:
 * 1.has topic or not(Does not matter)
 * 2.has repost or not
 * 3.pic
 *   (1) has no pic
 *   (2) has one pic
 *   (3) has more than one pic
 * The items above can be combined to so many situations.Sush as:
 * 1.no repost and no pic
 * 2.no repost and one pic
 * 3.no repost and multi pic
 * 4.has repost and no pic
 * 5.has repost and one pic
 * 6.has repost and multi pic
 * So there is totally 6 situations right now
 */
public class TimeLineModel implements Parcelable{

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_ONE_PIC = 1;
    public static final int TYPE_MULTI_PIC = 2;
    public static final int TYPE_REPOST = 3;
    public static final int TYPE_REPOST_ONE_PIC = 4;
    public static final int TYPE_REPOST_MULTI_PIC = 5;

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
    public int type;

    private String unParseTime;

    public void parse(JSONObject json) throws JSONException, ParseException {
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

        if(hasRepost){
            if(hasRepostPic){
                type = TYPE_REPOST_ONE_PIC;
            }else if(hasRepostPics){
                type = TYPE_REPOST_MULTI_PIC;
            }else{
                type = TYPE_REPOST;
            }
        }else{
            if(hasPic){
                type = TYPE_ONE_PIC;
            }else if(hasPics){
                type = TYPE_MULTI_PIC;
            }else{
                type = TYPE_NORMAL;
            }
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
            repostCount,
            type
        });
        dest.writeBooleanArray(new boolean[]{
                hasPic,
                hasPics,
                hasRepost,
                hasRepostPic,
                hasRepostPics
        });
        dest.writeStringArray(new String[]{
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
        int[] intData = new int[3];
        in.readIntArray(intData);
        this.commentCount = intData[0];
        this.repostCount = intData[1];
        this.type = intData[2];

        boolean[] booleanData = new boolean[5];
        in.readBooleanArray(booleanData);
        this.hasPic = booleanData[0];
        this.hasPics = booleanData[1];
        this.hasRepost = booleanData[2];
        this.hasRepostPic = booleanData[3];
        this.hasRepostPics = booleanData[4];

        String[] stringData = new String[6];
        in.readStringArray(stringData);
        this.text = TextUtils.parseStatusText(stringData[0]);
        this.name = stringData[1];
        this.headUrl = stringData[2];
        this.unParseTime = stringData[3];
        this.time = TextUtils.parseTime(this.unParseTime);
        this.repostName = TextUtils.parseStatusText(stringData[4]);
        this.repostText = TextUtils.parseStatusText(stringData[5]);

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
