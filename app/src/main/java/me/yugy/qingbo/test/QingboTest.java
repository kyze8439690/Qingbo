package me.yugy.qingbo.test;

import android.test.InstrumentationTestCase;

import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.PinyinUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/5/26.
 */
public class QingboTest extends InstrumentationTestCase {

    public void testWeiboShortenUrl() throws Throwable{
        final CountDownLatch signal = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                Weibo.getShortUrl(getInstrumentation().getTargetContext(), "http://yanghui.name", new TextHttpResponseHandler(){
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                        try{
                            assertEquals("http://t.cn/8sJEZjp", responseBody);
                        }finally {
                            signal.countDown();
                        }
                    }
                });
            }
        });
        signal.await(30, TimeUnit.SECONDS);
    }

    public void testPinyinConvert() throws Throwable{
        String chinese = "杨辉";
        String pinyin = PinyinUtils.toPinyin(getInstrumentation().getTargetContext(), chinese);
        assertEquals(pinyin, "yanghui");
    }
}
