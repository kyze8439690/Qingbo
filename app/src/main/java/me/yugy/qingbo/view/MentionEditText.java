package me.yugy.qingbo.view;

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.yugy.qingbo.utils.DebugUtils;

/**
 * Created by yugy on 2014/5/29.
 */
public class MentionEditText extends EditText{

//    private boolean mIsSelectorShown;
    private OnMentionListener mOnMentionListener;
    private OnBackPressedListener mOnBackPressedListener;
    private int mMentionStart;
    private int mMentionEnd;

    public MentionEditText(Context context, AttributeSet attrs){
        super(context, attrs);
        mMentionStart = 0;
        mMentionEnd = 0;
    }

    public MentionEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMentionStart = 0;
        mMentionEnd = 0;
    }

    public interface OnMentionListener{
        public void OnMentionStarted(String sequence);
        public void OnMentionFinished();
    }

    public interface OnBackPressedListener{
        public boolean onBackPressed();
    }

    public int getCurrentCursorLine()
    {
        int selectionStart = Selection.getSelectionStart(this.getText());
        Layout layout = getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    public int getCurrentLineTop(){
        int currentLine = getCurrentCursorLine();
        Rect r = new Rect();
        getLineBounds(currentLine, r);
        int y = r.top - this.getPaddingTop();
        return y;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkIfMentioning(getMentioningSequence(s, start, count));
    }

    public void setSelectedMention(String mention){
        try{
            this.getText().replace(mMentionStart, mMentionEnd, mention+" ");
        }catch ( IndexOutOfBoundsException exception){
            System.out.println("Not valid start-end values for the mention");
        }
    }

    private void checkIfMentioning(String mentionSequence){
        if(mentionSequence != null){
            if(mOnMentionListener !=null){
                mOnMentionListener.OnMentionStarted(mentionSequence);
            }
        }
        if( mentionSequence == null && mOnMentionListener !=null){
            mOnMentionListener.OnMentionFinished();
        }
    }

    private String getMentioningSequence(CharSequence s, int start, int count){

        DebugUtils.log("getMentioningSequence: " + s);

        Pattern pattern = Pattern.compile("(?<=\\s|^)@([a-z|A-Z|_|0-9|\\u4e00-\\u9fa5]*)(?=\\s|$)");
        Matcher matcher = pattern.matcher(s.toString());
        String mention = null;
        while (matcher.find()) {
            if( matcher.start(1) <= start+count &&
                    start+count <= matcher.end(1)
                    ){
                mMentionStart =matcher.start(1);
                mMentionEnd =matcher.end(1);
                mention = matcher.group(1);
                break;
            }
        }
        return mention;
    }

    public void setOnMentionListener(OnMentionListener onMentionListener) {
        mOnMentionListener = onMentionListener;
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        mOnBackPressedListener = onBackPressedListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && mOnBackPressedListener != null){
            return mOnBackPressedListener.onBackPressed();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}