package me.yugy.qingbo.view.text;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by yugy on 2014/4/19.
 */
public class LinkTextView extends TextView{
    public LinkTextView(Context context) {
        super(context);
        init();
    }

    public LinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinkTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        Object text = getText();
        if (text instanceof SpannableString) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_DOWN) {

                SpannableString buffer = (SpannableString) text;

                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();

                x += getScrollX();
                y += getScrollY();

                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                TouchClickableSpan[] link = buffer.getSpans(off, off,
                        TouchClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(this);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
