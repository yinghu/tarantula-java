package com.tarantula.platform.presence;


import com.tarantula.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;


/**
 * Created by yinghu lu on 4/25/2018.
 */
public class ContentChunk extends RecoverableObject {

    private byte[] data;
    private String suffix;
    public ContentChunk(){
        this.vertex = "ContentChunk";
        this.binary = true;
    }
    public ContentChunk(String systemId,String suffix,byte[] data){
        this();
        this.distributionKey(systemId);
        this.suffix = suffix;
        this.data = data;
    }
    public ContentChunk(String systemId,String suffix){
        this();
        this.distributionKey(systemId);
        this.suffix = suffix;
    }
    @Override
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return UserPortableRegistry.CONTENT_CHUNK_CID;
    }
    public byte[] toByteArray(){
        return data;
    }
    public void fromByteArray(byte[] data){
        this.data = data;
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.suffix);
    }
}
