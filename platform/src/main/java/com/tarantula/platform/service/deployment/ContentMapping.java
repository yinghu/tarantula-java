package com.tarantula.platform.service.deployment;

import com.icodesoftware.service.Content;

public class ContentMapping implements Content {

    public static final ContentMapping NOT_EXISTED = new ContentMapping(new byte[0],"",false);

    private final byte[] content;
    private final String contentType;
    private final boolean existed;
    private final String fileName;
    private final int revisionNumber;
    public ContentMapping(byte[] content,String contentType,boolean existed){
        this.content = content;
        this.contentType = contentType;
        this.existed = existed;
        this.fileName = "";
        this.revisionNumber = 0;
    }

    private ContentMapping(byte[] content,String fileName,int revisionNumber,boolean existed){
        this.content = content;
        this.contentType = "save";
        this.existed = existed;
        this.fileName = fileName;
        this.revisionNumber = revisionNumber;
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

    public int revisionNumber(){
        return revisionNumber;
    }

    public String fileName(){
        return fileName;
    }

    public static Content forSave(byte[] data,String fileName,int revisionNumber){
        return new ContentMapping(data,fileName,revisionNumber,true);
    }
    public static Content forLoad(String fileName,int revisionNumber){
        return new ContentMapping(new byte[0],fileName,revisionNumber,false);
    }
}
