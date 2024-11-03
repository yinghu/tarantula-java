package com.tarantula.platform.presence;


import com.google.gson.JsonObject;
import com.icodesoftware.Access;
import com.icodesoftware.util.RecoverableObject;

public class User extends RecoverableObject implements Access {

    protected String login;
    protected String password;//hash of the password
    protected String emailAddress; //reset validation email address
    protected boolean activated;
    protected boolean validated;
    protected boolean primary;
    protected long primaryId;
    protected String validator;
    protected String role;

    public User(){
        this.label = Access.LABEL;
        this.onEdge = true;
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

    public long primaryId(){
        return this.primaryId;
    }
    public void primaryId(long primaryId){
        this.primaryId = primaryId;
    }
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }

    public int getClassId() {
        return UserPortableRegistry.USER_CID;
    }

    public boolean write(DataBuffer buffer){
        buffer.writeUTF8(login);
        buffer.writeUTF8(password);
        buffer.writeUTF8(role);
        buffer.writeBoolean(activated);
        buffer.writeInt(routingNumber);
        buffer.writeBoolean(validated);
        buffer.writeUTF8(emailAddress);
        buffer.writeUTF8(validator);
        buffer.writeBoolean(this.primary);
        buffer.writeLong(primaryId);
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
        this.primaryId = buffer.readLong();
        return true;
    }

    @Override
    public boolean validate() {
        return primary? primaryId>0 : true;
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("login",login);
        resp.addProperty("role",role);
        return  resp;
    }
}
