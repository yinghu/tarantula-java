package com.tarantula.platform.presence;


import com.tarantula.Access;
import com.tarantula.platform.RecoverableObject;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Updated by yinghu on 8/26/19
 */
public class AccessTrack extends RecoverableObject implements Access {

    private String login;
    private String password;
    private boolean active;
    private String role;
    public AccessTrack(){
        this.vertex = "Access";
        this.label = "VA";
        this.binary = false;
    }
    public AccessTrack(String login){
        this();
        this.login = login;
    }
    public String login(){
        return this.login;
    }
    public void login(String login){
        this.login = login;
    }
    public String password(){
        return password;
    }
    public void password(String password){
        this.password = password;
    }
    public boolean active(){
        return this.active;
    }
    public void active(boolean active){
        this.active = active;
    }

    public String role(){
        return this.role;
    }
    public void role(String role){
        this.role = role;
    }
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public int getClassId() {
        return UserPortableRegistry.ACCESS_CID;
    }
    public Map<String,Object> toMap(){
        properties.put("1",login);
        properties.put("2",password);
        properties.put("3",role);
        properties.put("4",active);
        properties.put("5",routingNumber);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.login = (String) properties.get("1");
        this.password = (String) properties.get("2");
        this.role = (String) properties.get("3");
        this.active = (boolean) properties.get("4");
        this.routingNumber = ((Number) properties.get("5")).intValue();
    }

    public byte[] toByteArray(){
        byte[] _login = login.getBytes(Charset.forName("UTF-8"));
        byte[] _pwd = password.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer = ByteBuffer.allocate(_login.length+_pwd.length+16);
        buffer.putInt(this.active?(byte)1:0);
        buffer.putInt(this.routingNumber);
        buffer.putInt(_login.length);
        buffer.put(_login);
        buffer.putInt(_pwd.length);
        buffer.put(_pwd);
        return buffer.array();
    }
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.active = buffer.getInt()==1?true:false;
        this.routingNumber = buffer.getInt();
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.login = sb.toString();
        len = buffer.getInt();
        sb.setLength(0);
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.password = sb.toString();

    }
    @Override
    public String toString(){
        return "Access ["+bucket+"]["+oid+"]["+active+"/"+login+"/"+role+"/"+routingNumber+"]";
    }
}
