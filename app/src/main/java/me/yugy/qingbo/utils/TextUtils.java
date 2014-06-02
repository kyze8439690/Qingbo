package me.yugy.qingbo.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.util.Patterns;
import android.view.View;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.yugy.qingbo.R;
import me.yugy.qingbo.activity.UserActivity;
import me.yugy.qingbo.view.text.TouchClickableSpan;

/**
 * Created by yugy on 14-3-9.
 */
public class TextUtils {

    /*
    time sample: Fri Oct 04 18:20:31 +0800 2013
     */
    private static SimpleDateFormat decodeDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);

    private static final Pattern WEIBO_SHORT_URL_PATTERN = Pattern.compile("http://t\\.cn/[0-9a-zA-Z]+");
    private static final Pattern AT_PATTERN = Pattern.compile("@([0-9a-zA-Z\\u4e00-\\u9fa5_-]+)");
    private static final Pattern TOPIC_PATTERN = Pattern.compile("#[^#]+#");

    public static SpannableString getClickForWholeText(String text, TouchClickableSpan clickableSpan){
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(clickableSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static CharSequence getRelativeTimeDisplayString(Context context, long referenceTime) {
        long now = System.currentTimeMillis();
        long difference = now - referenceTime;
        return (difference >= 0 &&  difference <= DateUtils.MINUTE_IN_MILLIS) ?
                context.getResources().getString(R.string.just_now):
                DateUtils.getRelativeTimeSpanString(
                        referenceTime,
                        now,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
    }

    public static long parseDate(String time) throws ParseException {
        Date date = decodeDateFormat.parse(time);
        return date.getTime();
    }

    public static SpannableString parseStatusText(String text){
        SpannableString parseString = new SpannableString(text);
        Matcher urlMatcher = WEIBO_SHORT_URL_PATTERN.matcher(text);
        Matcher atMatcher = AT_PATTERN.matcher(text);
        while(urlMatcher.find()){
            final String url = urlMatcher.group();
            int start = urlMatcher.start();
            int end = urlMatcher.end();
            parseString.setSpan(new TouchClickableSpan() {
                @Override
                public void onClick(View widget) {
                    widget.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        while(atMatcher.find()){
            int start = atMatcher.start();
            int end = atMatcher.end();
            final String name = atMatcher.group();
            parseString.setSpan(new TouchClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent intent = new Intent(widget.getContext(), UserActivity.class);
                    intent.putExtra("userName", name.substring(1));
                    widget.getContext().startActivity(intent);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return parseString;
    }

    public static boolean isGifLink(String url){
        return url.endsWith(".gif");
    }

    public static String[] getTopic(String text){
        ArrayList<String> topicsList = new ArrayList<String>();
        Matcher topicMatcher = TOPIC_PATTERN.matcher(text);
        while(topicMatcher.find()){
            topicsList.add(topicMatcher.group());
        }
        String[] topics = new String[topicsList.size()];
        topicsList.toArray(topics);
        return topics;
    }

    public static long calculateTextLength(CharSequence charSequence){
        double len = 0;
        for (int i = 0; i < charSequence.length(); i++) {
            int tmp = (int) charSequence.charAt(i);
            if (tmp > 0 && tmp < 127) {
                len += 0.5;
            } else {
                len++;
            }
        }
        return Math.round(len);
    }

    public static String md5(String s) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"));
        return new String(bytes, "UTF-8");
    }

    public static boolean isUrl(String urlString){
        return Patterns.WEB_URL.matcher(urlString).find();
    }

}
