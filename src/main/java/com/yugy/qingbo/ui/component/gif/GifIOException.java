package com.yugy.qingbo.ui.component.gif;

import java.io.IOException;

/**
 * Created by yugy on 13-12-28.
 */
public class GifIOException extends IOException
{

    private static final long serialVersionUID = 13038402904505L;
    /**
     * Reason which caused an exception
     */
    public final GifError reason;

    GifIOException(GifError reason)
    {
        super(reason.getFormattedDescription());
        this.reason=reason;
    }

}
