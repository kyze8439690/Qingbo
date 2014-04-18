package me.yugy.qingbo.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.yugy.qingbo.R;

/**
 * Created by yugy on 14-3-9.
 */
public class TextUtils {

    public static CharSequence getRelativeTimeDisplayString(Context context, long referenceTime) {
        long now = System.currentTimeMillis();
        long difference = now - referenceTime;
        return (difference >= 0 &&  difference<= DateUtils.MINUTE_IN_MILLIS) ?
                context.getResources().getString(R.string.just_now):
                DateUtils.getRelativeTimeSpanString(
                        referenceTime,
                        now,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
    }

    /*
    time sample: Fri Oct 04 18:20:31 +0800 2013
     */
    private static SimpleDateFormat decodeDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);

    public static long parseDate(String time) throws ParseException {
        Date date = decodeDateFormat.parse(time);
        return date.getTime();
    }

    public static SpannableString parseStatusText(String text){
        SpannableString parseString = new SpannableString(text);
        Pattern urlPattern = Pattern.compile("http://t.cn/.{7}");
        Pattern atPattern = Pattern.compile("@([0-9a-zA-Z\\u4e00-\\u9fa5_-]+)");
        Matcher urlMatcher = urlPattern.matcher(text);
        Matcher atMatcher = atPattern.matcher(text);
        while(urlMatcher.find()){
            String url = urlMatcher.group();
            int start = urlMatcher.start();
            int end = urlMatcher.end();
            parseString.setSpan(new URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            parseString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        while(atMatcher.find()){
            int start = atMatcher.start();
            int end = atMatcher.end();
            final String name = atMatcher.group();
            parseString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
//                    MessageUtils.toast(context, name);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            parseString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return parseString;
    }

    public static boolean isGifLink(String url){
        return url.endsWith(".gif");
    }

    public static ArrayList<String> getTopic(String text){
        ArrayList<String> topics = new ArrayList<String>();
        Pattern topicPattern = Pattern.compile("#[^#]+#");
        Matcher topicMatcher = topicPattern.matcher(text);
        while(topicMatcher.find()){
            topics.add(topicMatcher.group());
        }
        return topics;
    }

}
