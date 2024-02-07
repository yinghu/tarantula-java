# Summary 

## Game Service Provider module is the hook between game cluster and game over HTTP and UDP protocols

### NOTE : the coding guide is only relative with HTTP protocol.

## API Stack (important java interface files) 
### [GameServiceProvider.java](../modules/protocol/src/main/java/com/icodesoftware/protocol/GameServiceProvider.java)
### [GameContext.java](../modules/protocol/src/main/java/com/icodesoftware/protocol/GameContext.java)
### [Session.java](../modules/protocol/src/main/java/com/icodesoftware/Session.java)
### [Inventory.java](../modules/protocol/src/main/java/com/icodesoftware/Inventory.java)
### [Tournament.java](../modules/protocol/src/main/java/com/icodesoftware/Tournament.java)
### [ApplicationResource.java](../modules/protocol/src/main/java/com/icodesoftware/protocol/ApplicationResource.java)
### [Transaction.java](../modules/protocol/src/main/java/com/icodesoftware/Transaction.java)
### [ApplicationSchema.java](../modules/protocol/src/main/java/com/icodesoftware/service/ApplicationSchema.java)
### [OnAccess.java](../modules/protocol/src/main/java/com/icodesoftware/OnAccess.java)
### [ApplicationPreSetup.java](../modules/protocol/src/main/java/com/icodesoftware/service/ApplicationPreSetup.java)
### [Room.java](../modules/protocol/src/main/java/com/icodesoftware/Room.java)
### [Recoverable.java](../modules/protocol/src/main/java/com/icodesoftware/Recoverable.java)
### [RecoverableRegistry.java](../modules/protocol/src/main/java/com/icodesoftware/RecoverableRegistry.java)
### [RecoverableFactory.java](../modules/protocol/src/main/java/com/icodesoftware/RecoverableFactory.java)
### [Statistics.java](../modules/protocol/src/main/java/com/icodesoftware/Statistics.java)
### [Rating.java](../modules/protocol/src/main/java/com/icodesoftware/Rating.java)
### [TokenValidatorProvider.java](../modules/protocol/src/main/java/com/icodesoftware/service/TokenValidatorProvider.java)
### [DataStore.java](../modules/protocol/src/main/java/com/icodesoftware/DataStore.java)

## Basic Code Guide
### 1. Create persistent layer
#### 1.1 Implements RecoverableRegistry (via AbstractRecoverableListener)

```
    public class Earth8PortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 100;

    public static final int BATTLE_TRANSACTION_CID = 1;
   
    public static Earth8PortableRegistry INS;

    public Earth8PortableRegistry(){
        INS = this;
    }

    public T create(int i) {
        Recoverable pt = null;
        switch (i){
            case BATTLE_TRANSACTION_CID:
                pt = new BattleTransaction();
                break;
            default:
        }
        return (T)pt;
    }

    public int registryId() {
        return OID;
    }
}
```

#### 1.2 Implements Recoverable (via RecoverableObject)

```
public class BattleTransaction extends RecoverableObject {

    public long chapterId;
    public long stageId;

    public long[] party;

    public boolean win;

    public String TEMP_BattleStage;
    
    //class id registered on RecoverableRegistry implementation
    @Override
    public int getClassId() {
        return Earth8PortableRegistry.BATTLE_TRANSACTION_CID;
    }

   //factory id registered on RecoverableRegistry implementation
    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    //Data store write contract
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(chapterId);
        buffer.writeLong(stageId);
        buffer.writeInt(party.length);
        for(long unit : party){
            buffer.writeLong(unit);
        }
        buffer.writeBoolean(win);
        buffer.writeBoolean(disabled);

        buffer.writeUTF8(TEMP_BattleStage);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        chapterId = buffer.readLong();
        stageId = buffer.readLong();
        int size = buffer.readInt();
        party = new long[size];
        for(int i=0;i<size;i++){
            party[i]=buffer.readLong();
        }
        win = buffer.readBoolean();
        disabled = buffer.readBoolean();
        TEMP_BattleStage = buffer.readUTF8();
        return true;
    }

    //LIMIT RESPONSE SIZE FROM DEFAULT JSON-CONVERT
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("BattleId",distributionKey());
        return jsonObject;
    }

    //data validate contract
    @Override
    public boolean validate() {
        for(long unit : party){
            if(unit < 0) return false;
        }
        return true;
    }
```
#### 1.3. Save and load recoverable object in a transaction

```
     BattleTransaction battleTransaction = new BattleTransaction();
     //fills data or passing data
     
     //save transaction
     Transaction transaction = gameContext.applicationSchema().transaction();
     boolean created = transaction.execute(ctx->{
        ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
        DataStore dataStore = setup.onDataStore("battle");
        return dataStore.create(battleTransaction);
     });
     
     //saved if created is true or false/excetion threw
     
     //load transaction
     BattleTransaction battleLoaded = new BattleTransaction();
     //set distributionId
     battleLoaded.distributionId(battleTransaction.distributionId());
     Transaction loadTransaction = gameContext.applicationSchema().transaction();
     boolean loaded = loadTransaction.execute(ctx->{
        ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
        DataStore dataStore = setup.onDataStore("battle");
        return dataStore.load(battleTransaction);
     });
     
     //loaded if loaded is true or false/excetion threw    
```

#### 1.4. Create a queryable object ( key index : 1 to many with a label string tag)
##### 1.4.1 Associate an index string to label attribute this.onEdge = true.
##### 1.4.2 Mark it as index to this.onEdge = true.
##### 1.4.3 Can be setting label and onEdge in runtime
##### 1.4.4 Associate the owner key on calling ownerKey(SnowflakeKey.from(playerId));
##### 1.4.5 Owner key itself may not be persistent 
```
    public class PlayerAction extends RecoverableObject implements OnAccess {

        public static final String LABEL = "inbox";
        public boolean completed;
        public PlayerAction(){
            this.onEdge = true;
            this.label = LABEL;
        }
    
        public PlayerAction(String name,boolean completed){
            this();
            this.name = name;
            this.completed = completed;
        }
        @Override
        public int getFactoryId() {
            return Earth8PortableRegistry.OID;
        }
        @Override
        public int getClassId() {
            return Earth8PortableRegistry.PLAYER_ACTION_CID;
        }
    
        @Override
        public boolean write(DataBuffer buffer) {
            buffer.writeUTF8(name);
            buffer.writeBoolean(completed);
            return true;
        }
    
        //Data store read contract
        @Override
        public boolean read(DataBuffer buffer) {
            name = buffer.readUTF8();
            completed = buffer.readBoolean();
            return true;
        }
    
        @Override
        public JsonObject toJson(){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(name,completed);
            return jsonObject;
        }
    }
    //Save object with the playerId 
    PlayerAction playerAction = new PlayerAction("ShippingFormCompleted",true);
    playerAction.ownerKey(SnowflakeKey.from(playerId)); 
    Transaction transaction = gameContext.applicationSchema().transaction();
    boolean created = transaction.execute(ctx->{
        ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
        DataStore dataStore = setup.onDataStore("player_action");
        return dataStore.create(playerAction);
    });
```
#### 1.5. Implements QueryFactory to query a set of objects associated with the owner
```
    public class PlayerActionQuery implements RecoverableFactory<PlayerAction> {

    private long systemId;
    public PlayerActionQuery(long systemId){
        this.systemId = systemId;
    }
    @Override
    public PlayerAction create() {
        return new PlayerAction();
    }

    @Override
    public String label() {
        return PlayerAction.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
```

#### 1.6. Put query in the transaction 
```
     List<PlayerAction> inbox = new ArrayList<>();
     long playerId = 10000;
     gameContext.applicationSchema().transaction().execute(ctx->{
        DataStore dataStore = ctx.onDataStore("player_action");
        dataStore.list(new PlayerActionQuery(playerId)).forEach(playerAction -> {
            inbox.add(playerAction);
        });
        return true;
     });
```

### 2. Send http response via Session object
#### 2.1 Send response early if no need to check call result
#### 2.1 Process is allowed after response.
```
    session.write(JsonUtil.toSimpleResponse(true,"call response first").getBytes());
    //client side receive json {Successful:true,Message:"call response first"}
    //continute to process other operations after session response
```

### 3. Inventory Operation 
#### 3.1 Get inventory via ApplicationPreSetup.

```
    long playerId = 1000;
    String gemType = "gem";
    long inventoryId = 2000;
    ApplicationPreSetup applicationPreSetup = gameContext.applicationSchema().applicationPreSetup(); 
    List<Inventory> inventories = applicationPreSetup.inventory(playerId);
    Inventory typedInventory = applicationPreSetup.inventory(playerId,gemType);
    Inventory inventory = applicationPreSetup.inventory(inventoryId);
```

#### 3.2 Inventory Listener callback per inventory grant operation

```
    public void onInventory(ApplicationPreSetup applicationPreSetup,Inventory inventory, Inventory.Stock stock){
        //proccess game data based item configs.
        //if failed throw exception to rollback                            
    }
```

#### 3.3 Grant resource via ResourceRedeemer

```
     ApplicationResource resource; //from resource listener callback cache
     ApplicationResource.Redeemer redeemer = gameContext.redeemer(session);
     //grant resournce into player's inventory in the transaction context
     redeemer.redeem(applicationPreSetup,resource);
```

### 3.4 Remove stock (inventory item) from inventory
```
    long stockId = 1234;
    Inventory inventory = applicationPreSetup.inventory(session.distributionId(),"Hero");
    Inventory.Stock removed = inventory.stock(stockId);
    if(removed!=null){
        inventory.removeStock(removed);
    }
```

### 4. Tournament Operation

```
    Tournament tournament; //from tournament listener callback cache
    double score = 10;
    double credits = 0; //could be > 0 for credits base race
    tournament.register(session).update(session,(entry)->{
        entry.score(score,credits);
    });
```

### 5. Metrics Operation

```
    gameContext.onMetrics("totalBattle",1);
```

### 6. Send Event Out Via External Webhook
#### 6.1 Do not call webhook on callback thread. 
```
    TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
    gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
        webhook.upload(ANALYTICS_QUERY,new BattleEndTransaction(session, battleTransaction.distributionId(), payload).toBytes()))
    );
```

## Put All Together
### GameServiceProvider implementation guide over HTTP [Earth8GameServiceProvider.java](../modules/earth8/src/main/java/com/perfectday/games/earth8/Earth8GameServiceProvider.java)
### Contract Callback Methods To Exchange Data Between System And Module

### 1. Setup game context
#### 1.1 Setup system call is called once per game cluster instance launch time.
#### 1.2 Cache gameContext instance on module instance scope.
#### 1.3 Register recoverable registry if module needs to persistent data
#### 1.4 Register All system level listeners such as InventoryListener, TournamentListener, ResourceListener.
#### 1.5 Other instance scope initializations 

```
    private GameContext gameContext
    public void setup(GameContext gameContext){
        this.gameContext = gameContext; //cache reference
        this.gameContext.registerTournamentListener(this); //register tournament lisetener
        this.gameContext.recoverableRegistry(new Earth8PortableRegistry<>());//register persistence registry
    }
```
### 2. OnJoined callback
#### 2.1 Called once per player's join HTTP call after HTTP response.
#### 2.2 Session object is the async http request wrapper with a distributionId same as the systemId on client presence.
#### 2.3 Room object is the distributed game room the player's join call.

```
     public void onJoined(Session session,Room room) {
        //use room.distributionId/key as game session id
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY,new ServerConnectTransaction(session).toString().getBytes()))
        );
    }
```

### NOTES : 
### * Following 3, 4, 5 callbacks are core callbacks for game logic. 
### * Each callback must use session object to write HTTP response to player's request and only write once per call.
### * UpdateGame callback is available player's login session.
### * StartGame and EndGame is only available after OnJoined session call.
### * All 3,4,5 callbacks are called with multiple times during login session or game session.
### * Do not put heavy operations on those calls instead of dispatching heavy operations on the scheduled thread pool.
### * Unless needed never use try-catch block. System catches all exceptions.
### * Never let invalid request calls pass through without throwing exceptions
### * Http request post content is passed on those callbacks as byte array. The content is same as player's post content.
### * Payload could be anything. Current client implementation is using JSON protocol.

### 3. StartGame callback
##### 3.1 Called per player's request via OnStartGame http request on joined lobby

```
    public void startGame(Session session, byte[] payload) throws Exception{
        //parse payload if client HTTP POST 
        //process payload
        //send response back
        //pocess after response           
    } 
```

### 4. UpdateGame callback
##### 4.1 Called per player's request via OnStartGame http request after player's login

```
    public void updateGame(Session session, byte[] payload) throws Exception{
        //parse payload if client HTTP POST 
        //process payload
        //send response back
        //pocess after response
    } 
```

### 5. EndGame callback
##### 5.1 Called per player's request via OnStartGame http request on joined lobby.

```
    public void endGame(Session session, byte[] payload) throws Exception{
        //parse payload if client HTTP POST 
        //process payload
        //send response back
        //pocess after response
    } 
```

### 6. OnLeft callback
#### 6.1 Called once per player's leave HTTP call or system controlled timeout.

```
    public void onLeft(Session session) {
        //can do nothing       
    }
```

### 7. OnGameEvent callback

```
    public <T extends OnAccess> void onGameEvent(T event){
        var eventType = (String)event.property(OnAccess.TYPE_ID);
        if(eventType != null && eventType.equals("onAction"))
        {
            var data = (byte[])event.property(OnAccess.PAYLOAD);
            var jsonData = JsonUtil.parse(data);
            var playerId = JsonUtil.getJsonLong(jsonData, "playerId", 0);
            if(playerId>0){
                gameContext.applicationSchema().transaction().execute(ctx->{
                    DataStore playerActionStore = ctx.onDataStore("player_action");
                    PlayerAction playerAction = new PlayerAction("ShippingFormCompleted",true);
                    playerAction.ownerKey(SnowflakeKey.from(playerId));
                    return playerActionStore.create(playerAction);
                });
            }
        }
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY, new ServerMetadataTransaction(event).toBytes())
        ));
    }

```

### 8. Inbox callback
#### 8.1 Pending enhancement needed to meet more use cases
```
    public <T extends OnAccess> List<T> inbox(Session session){
        List<OnAccess> inbox = new ArrayList<>();
        gameContext.applicationSchema().transaction().execute(ctx->{
            DataStore dataStore = ctx.onDataStore("player_action");
            dataStore.list(new PlayerActionQuery(session.distributionId())).forEach(playerAction -> {
                inbox.add(playerAction);
            });
            return true;
        });
        return (List<T>)inbox;
    }

```
### 9. TournamentListener callbacks

```
    @Override
    public void tournamentStarted(Tournament tournament) {
        gameContext.log("Tournament started : "+tournament.distributionId()+" : "+tournament.name()+" : "+tournament.type()+" : "+tournament.global(),OnLog.WARN);
        tournamentIndex.put(tournament.distributionId(),tournament);
    }

    @Override
    public void tournamentClosed(Tournament tournament) {
        gameContext.log("Tournament closed : "+tournament.distributionId()+" : "+tournament.name()+" : "+tournament.type()+" : "+tournament.global(),OnLog.WARN);
        tournamentIndex.remove(tournament.distributionId());
    }
    @Override
    public void tournamentEnded(Tournament tournament) {
        tournamentIndex.remove(tournament.distributionId());
    }
```

### 10. ResourceListener callbacks

```
    public void onApplicationResourceRegistered(ApplicationResource resource){
        this.resourceIndex.put(resource.name(),resource);
        this.gameContext.log(resource.name()+" registered",OnLog.INFO);
    }
    public void onApplicationResourceReleased(ApplicationResource resource){
        this.resourceIndex.remove(resource.name());
        this.gameContext.log(resource.name()+" released",OnLog.INFO);
    }
    
```