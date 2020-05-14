package com.tarantula.platform.presence;


import com.tarantula.Access;
import com.tarantula.platform.RecoverableObject;
import java.util.Map;

/**
 * Updated by yinghu on 8/26/19
 */
public class User extends RecoverableObject implements Access {

    private String login;
    private String password;//hash of the password
    private String emailAddress="n/a"; //reset validation email address
    private boolean active;
    private boolean validated;
    private String validator;
    private String role;
    public User(){
        this.vertex = "User";
        this.label = "VA";
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
    public boolean active(){
        return this.active;
    }
    public void active(boolean active){
        this.active = active;
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
        properties.put("4",active);
        properties.put("5",routingNumber);
        properties.put("6",validated);
        properties.put("7",emailAddress);
        properties.put("8",validator);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.login = (String) properties.get("1");
        this.password = (String) properties.get("2");
        this.role = (String) properties.get("3");
        this.active = (boolean) properties.get("4");
        this.routingNumber = ((Number) properties.get("5")).intValue();
        this.validated = (boolean) properties.get("6");
        this.emailAddress = (String)properties.get("7");
        this.validator = (String)properties.get("8");
    }
}
