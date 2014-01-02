package com.yugy.qingbo.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

import com.yugy.qingbo.utils.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by yugy on 13-12-29.
 */
public class RepostModel implements Parcelable{

    public String id;
    public SpannableString text;
    public String name;
    public String time;
    public String head;

    private String unParseTime;

    public void parse(JSONObject json) throws JSONException, ParseException {
        id = json.getString("id");
        text = TextUtils.parseStatusText(json.getString("text"));
        name = json.getJSONObject("user").getString("screen_name");
        unParseTime = json.getString("created_at");
        time = TextUtils.parseTime(unParseTime);
        head = json.getJSONObject("user").getString("avatar_large");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                id,
                text.toString(),
                name,
                time,
                head
        });
    }

    public RepostModel(){}

    private RepostModel(Parcel in) throws ParseException {
        String[] stringData = new String[5];
        in.readStringArray(stringData);
        id = stringData[0];
        text = TextUtils.parseStatusText(stringData[1]);
        name = stringData[2];
        unParseTime = stringData[3];
        time = TextUtils.parseTime(unParseTime);
        head = stringData[4];
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            try {
                return new RepostModel(source);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Object[] newArray(int size) {
            return new RepostModel[size];
        }
    };

}
