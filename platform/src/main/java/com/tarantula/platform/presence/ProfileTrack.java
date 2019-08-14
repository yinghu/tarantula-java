package com.tarantula.platform.presence;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Profile;

import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.io.IOException;
import java.util.Map;

/**
 * Updated by yinghu lu on 10/9/2018.
 */
public class ProfileTrack extends RecoverableObject implements Profile {


    private String nickname;
    private String avatar;
    private String emailAddress;
    private String video;
    public ProfileTrack(){
        this.vertex = "Profile";
    }
    public ProfileTrack(String bucket,String oid){
        this();
        this.bucket = bucket;
        this.oid = oid;
    }

    public String nickname() {
        return this.nickname;
    }

    public void nickname(String nickname) {
        this.nickname = nickname;
    }
    public void avatar(String avatar){
        this.avatar = avatar;
    }
    public String avatar(){
        return  this.avatar;
    }
    public void emailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }
    public String emailAddress(){
        return this.emailAddress;
    }

    public String video(){
        return this.video;
    }
    public void video(String video){
        this.video = video;
    }

    @Override
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return UserPortableRegistry.PROFILE_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        super.writePortable(out);
        out.writeUTF("1",this.nickname);
        out.writeUTF("2",this.avatar);
        out.writeUTF("3",this.emailAddress);
        out.writeUTF("4",this.video);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        super.readPortable(in);
        this.nickname = in.readUTF("1");
        this.avatar = in.readUTF("2");
        this.emailAddress = in.readUTF("3");
        this.video = in.readUTF("4");
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("emailAddress",this.emailAddress!=null?this.emailAddress:"n/a");
        this.properties.put("avatar",this.avatar!=null?this.avatar:"n/a");
        this.properties.put("nickname",this.nickname!=null?this.nickname:"n/a");
        this.properties.put("video",this.video!=null?this.video:"n/a");
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.emailAddress = (String)properties.get("emailAddress");
        this.avatar = (String)properties.get("avatar");
        this.nickname = (String)properties.get("nickname");
        this.video = (String)properties.get("video");
    }

    @Override
    public String toString(){
        return "["+nickname+"/"+emailAddress+"]";
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}
