package com.tarantula.platform.service.deployment;

import com.icodesoftware.service.Content;

public class ContentMapping implements Content {

    public static final ContentMapping NOT_EXISTED = new ContentMapping(new byte[0],"",false);

    private final byte[] content;
    private final String contentType;
    private final boolean existed;

    public ContentMapping(byte[] content,String contentType,boolean existed){
        this.content = content;
        this.contentType = contentType;
        this.existed = existed;
    }
    @Override
    public byte[] data() {
        return content;
    }

    @Override
    public String type() {
        return contentType;
    }

    public boolean existed(){
        return this.existed;
    }
}
