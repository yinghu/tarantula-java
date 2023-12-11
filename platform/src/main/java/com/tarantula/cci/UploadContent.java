package com.tarantula.cci;

public class UploadContent {
    public long sessionId;
    public String typeId;
    public byte[] content;

    public UploadContent(long sessionId,String typeId,byte[] content){
        this.sessionId = sessionId;
        this.typeId = typeId;
        this.content = content;
    }
}
