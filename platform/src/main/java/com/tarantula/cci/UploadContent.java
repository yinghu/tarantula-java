package com.tarantula.cci;

public class UploadContent {
    public String sessionId;
    public String typeId;
    public byte[] content;

    public UploadContent(String sessionId,String typeId,byte[] content){
        this.sessionId = sessionId;
        this.typeId = typeId;
        this.content = content;
    }
}
