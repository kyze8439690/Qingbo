package me.yugy.qingbo.view.text;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * Created by yugy on 2014/4/20.
 */
public abstract class TouchClickableSpan extends ClickableSpan {

    private static final int TEXT_COLOR = Color.rgb(66, 127, 237);
    private static final int BG_COLOR_NORMAL = Color.TRANSPARENT;
    private static final int BG_COLOR_PRESSED = Color.rgb(51, 181, 229);

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(TEXT_COLOR);
        ds.bgColor = BG_COLOR_NORMAL;
        ds.setUnderlineText(false);
    }
}