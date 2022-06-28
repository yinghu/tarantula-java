## API  `Module.java`

```
public interface Module {

    default void onJoin(Session session) throws Exception{}

    boolean onRequest(Session session, byte[] payload) throws Exception;

    void setup(ApplicationContext context) throws Exception;

    default void clear(){}
}

```
NOTES : 
1. Session inteface is the exchange point between http client and module.

2. ApplicationContext interface is the system resournce access point in module.
 
## Create module to implement Module interface `ProfileModule.java`

```
public class ProfileModule implements Module  {

    private ApplicationContext context;


    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //This call is the system call delegated by http request on target cluster node.
        //The call is not called on http request thread completed
        //The call must write data back the http request node or throw an exception 
        if(session.action().equals("onLoad")){
            //get local key-value store 
            DataStore profileDataStore = context.dataStore("profile);
            //create profile persistence object
            Profile profile = new Profile();
            //set distribution key
            profile.distributionKey(session.systemId);
            //load or create if not existed
            profileDataStore.createIfAbsent(profile);
            //send flat json to http client
            session.write(profile.toJson().toString().getBytes());    
        }
        else if(session.action.equals("onUpdate")){
            throw new UnsupportedOperationException("pending impl");    
        }
        else{
            //throw ex to send 500 http code to client
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        //call once when the module is starting up by system start-up or game cluster launching
        this.context = applicationContext;
    }
}
```

## Deploy profile module as a game service module
1. Create a deploymenet descriptor `profile.json` for ProfileModule

```
{
  "typeId":"game-service",
  "name":"Profile",
  "type":"application",
  "category":"profile",
  "tag":"game/profile",
  "moduleName":"com.tarantula.game.module.ProfileModule",
  "applicationClassName": "com.tarantula.platform.service.deployment.SingletonModuleApplication",
  "disabled": false
}
```
NOTE: tag `game/profile` will be replaced as `[a game name]/profile` once it is launched with a new game creationg, for instance game RobotQuest `robotquest/profile`

2. Save `profile.json` in `src/main/config/deploy`

3. Launch profile module when creating a new game cluster

NOTE: Module also can be dynamically deployed in runtime (Pending documenetation)  

## Write C# client call in Unity GameClusterClient package

```
    var headers = new[]
    {
        HttpCallHeaderHelper.Tag(_typeId + "/profile"),
        HttpCallHeaderHelper.Token(Presence.Token),
        HttpCallHeaderHelper.Action("onLoad"),
    };
	var response = await _httpCaller.GetJson(caller, HttpCallHeaderHelper.ServicePath, headers);
    //Parse response json payload
```

## Reference files handling client requests
1. `com.tarantula.cci.ServerEventHandler.java` -- dispatches http requests

2. `com.tarantula.platform.service.cluster.IntegrationCluster.java` -- handling event messages

3. `com.tarantula.platform.service.deployment.SingletonModuleApplication` -- requesting events on /application context/module

4. `com.tarantula.platform.TarantulaApplicationContext` -- Module context 

5. `com.tarantula.platform.event.ServerActionEvent` -- dispatches responses (Session.write())

## Deploy profile module globally (system module)

1. Create deploy desctiptor profile.xml 
```
<?xml version="1.0" encoding="UTF-8"?>
<tarantula>
    <lobby-context>
        <type-id>profile</type-id>
        <type>lobby</type>
        <category>service</category>
        <name>User Profile</name>
        <access-mode>12</access-mode>
        <deploy-code>1</deploy-code>
        <application-list>
            <application>
                <type-id>profile</type-id>
                <name>User Profile</name>
                <type>application</type>
                <category>user</category>
                <tag>user/profile</tag>
                <module-name>com.tarantula.game.module.ProfileModule</module-name>
            </application>
        </application-list>
    </lobby-context>
</tarantula>
```

2. Save `profile.xml` in `src/main/deploy` or `src/main/resources/appplicaton`

3. Profle module is starting duration system startup.

4. Client call 

```
    var headers = new[]
    {
        HttpCallHeaderHelper.Tag("user/profile"),
        HttpCallHeaderHelper.Token(Presence.Token),
        HttpCallHeaderHelper.Action("onLoad"),
    };
	var response = await _httpCaller.GetJson(caller, HttpCallHeaderHelper.ServicePath, headers);
    //Parse response json payload
```
