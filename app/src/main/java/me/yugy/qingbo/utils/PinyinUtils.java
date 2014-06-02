package me.yugy.qingbo.utils;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class is pinyin4android main interface .
 * there are two methods you can call them to convert the chinese to pinyin.
 * PinyinUtil.toPinyin(Context context,char c);
 * PinyinUtil.toPinyin(Context context,String hanzi);
 * <p/>
 * User: Ryan
 * Date: 11-5-29
 * Time: 21:13
 */
public class PinyinUtils {
    /**
     * to convert chinese to pinyin
     *
     * @param context Android Context
     * @param c       the chinese character
     * @return pinyin
     */
    public static String toPinyin(Context context, char c) {
        if (c >= 'A' && c <= 'Z') {
            return String.valueOf((char) (c + 32));
        }
        if (c >= 'a' && c <= 'z') {
            return String.valueOf(c);
        }
        if (c == 0x3007) return "ling";
        if (c < 4E00 || c > 0x9FA5) {
            return null;
        }
        RandomAccessFile is = null;
        try {
            is = new RandomAccessFile(PinyinSource.getFile(context), "r");
            long sp = (c - 0x4E00) * 6;
            is.seek(sp);
            byte[] buf = new byte[6];
            is.read(buf);
            return new String(buf).trim();
        } catch (FileNotFoundException e) {
            //
        } catch (IOException e) {
            //
        } finally {
            try {
                if (null != is) is.close();
            } catch (IOException e) {
                //
            }
        }
        return null;
    }

    /**
     * to convert chinese to pinyin
     *
     * @param context Android Context
     * @param hanzi   the chinese string
     * @return pinyin
     */
    public static String toPinyin(Context context, String hanzi) {
        StringBuffer sb = new StringBuffer("");
        RandomAccessFile is = null;
        try {
            is = new RandomAccessFile(PinyinSource.getFile(context), "r");
            for (int i = 0; i < hanzi.length(); i++) {
                char ch = hanzi.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    sb.append((char) (ch + 32));
                    continue;
                }
                if (ch >= 'a' && ch <= 'z') {
                    sb.append(ch);
                    continue;
                }
                if (ch == 0x3007) {
                    sb.append("ling");
                } else if (ch >= 0x4E00 || ch <= 0x9FA5) {
                    long sp = (ch - 0x4E00) * 6;
                    is.seek(sp);
                    byte[] buf = new byte[6];
                    is.read(buf);
                    sb.append(new String(buf).trim());
                }
            }
        } catch (IOException e) {
            //
        } finally {
            try {
                if (null != is) is.close();
            } catch (IOException e) {
                //
            }
        }
        return sb.toString().trim();
    }

    public static String toPinyinShortCut(Context context, String hanzi){
        StringBuffer sb = new StringBuffer("");
        RandomAccessFile is = null;
        try {
            is = new RandomAccessFile(PinyinSource.getFile(context), "r");
            for (int i = 0; i < hanzi.length(); i++) {
                char ch = hanzi.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    sb.append((char) (ch + 32));
                    continue;
                }
                if (ch >= 'a' && ch <= 'z') {
                    sb.append(ch);
                    continue;
                }
                if (ch == 0x3007) {
                    sb.append("l");
                } else if (ch >= 0x4E00 || ch <= 0x9FA5) {
                    long sp = (ch - 0x4E00) * 6;
                    is.seek(sp);
                    byte[] buf = new byte[6];
                    is.read(buf);
                    sb.append(new String(buf).charAt(0));
                }
            }
        } catch (IOException e) {
            //
        } finally {
            try {
                if (null != is) is.close();
            } catch (IOException e) {
                //
            }
        }
        return sb.toString().trim();
    }
}