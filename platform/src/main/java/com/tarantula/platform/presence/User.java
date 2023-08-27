package com.tarantula.platform.presence;


import com.icodesoftware.Access;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.RecoverableObject;
import java.util.Map;

public class User extends RecoverableObject implements Access {

    protected String login;
    protected String password;//hash of the password
    protected String emailAddress; //reset validation email address
    protected boolean activated;
    protected boolean validated;
    protected boolean primary;
    protected String validator;
    protected String role;
    public User(){
        this.label = "users";
    }
    public User(String login,boolean validated,String validator){
        this();
        this.login = login;
        this.validated = validated;
        this.validator = validator;
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
    public String emailAddress(){
        return this.emailAddress;
    }
    public void emailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }
    public String validator(){
        return this.validator;
    }
    public boolean activated(){
        return this.activated;
    }
    public void activated(boolean activated){
        this.activated = activated;
    }
    public boolean validated(){
        return this.validated;
    }
    public String role(){
        return this.role;
    }
    public void role(String role){
        this.role = role;
    }
    public boolean primary(){
        return this.primary;
    }
    public void primary(boolean primary){
        this.primary = primary;
    }
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public int getClassId() {
        return UserPortableRegistry.USER_CID;
    }
    public Map<String,Object> toMap(){
        properties.put("1",login);
        properties.put("2",password);
        properties.put("3",role);
        properties.put("4",activated);
        properties.put("5",routingNumber);
        properties.put("6",validated);
        properties.put("7",emailAddress);
        properties.put("8",validator);
        properties.put("9",this.owner);
        properties.put("10",this.primary);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.login = (String) properties.get("1");
        this.password = (String) properties.get("2");
        this.role = (String) properties.get("3");
        this.activated = (boolean) properties.get("4");
        this.routingNumber = ((Number) properties.get("5")).intValue();
        this.validated = (boolean) properties.get("6");
        this.emailAddress = (String)properties.get("7");
        this.validator = (String)properties.get("8");
        this.owner = (String)properties.get("9");
        this.primary = (boolean)properties.get("10");
    }

    public boolean write(DataBuffer buffer){
        buffer.writeUTF8(login);
        buffer.writeUTF8(password);
        buffer.writeUTF8(role);
        buffer.writeBoolean(activated);
        buffer.writeInt(routingNumber);
        buffer.writeBoolean(validated);
        buffer.writeUTF8(emailAddress!=null?emailAddress: OnAccess.UTF_NULL);
        buffer.writeUTF8(validator);
        buffer.writeBoolean(this.primary);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.login = buffer.readUTF8();
        this.password = buffer.readUTF8();
        this.role = buffer.readUTF8();
        this.activated = buffer.readBoolean();
        this.routingNumber = buffer.readInt();
        this.validated = buffer.readBoolean();
        this.emailAddress = buffer.readUTF8();
        this.validator = buffer.readUTF8();
        this.primary = buffer.readBoolean();
        return true;
    }
}
