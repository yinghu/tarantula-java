### CLIENT UDP API LIST
### UDP CALLBACK HOOK
``` 
    1. REGISTER PLAYER ON UDP SERVER
       PAYLOAD : {commnad:"onJoin",instanceId:{instanceId},systemId:{user_login},stub:{session_stub},ticket:{ticket}}
```
```C#
    -------- SAMPLE C# CODE ----------
    JSONObject payload = new JSONObject(JSONObject.Type.OBJECT);
    payload.AddField("command","onJoin");
    payload.AddField("systemId","abc123");
    payload.AddField("instanceId","BDS01/fd3032b0fbac4fb6adde79b0f366f80c/2");
    payload.AddField("stub",2);
    payload.AddField("ticket","access timed ticket");
    byte[] onjoin = Encoding.UTF8.GetBytes(payload.ToString());
    udp.Send(onjoin,onjoin.Length,endPoint);
```

```
    2. REGISTER INCOMING MESSAGE CALLBACK ON UDP
        ------ SAMPLE C# CODE ----- 
        for(;;)
        {
            Byte[] buff = udp.Receive(ref endPoint);
            string json = Encoding.ASCII.GetString(buff);
            int ix = json.IndexOf("{");
            string lb = json.Substring(0,ix);
            int ix = e.Data.IndexOf("{");
            string lb = e.Data.Substring(0,ix);
            JSONObject payload = new JSONObject(e.Data.Substring(ix));        
            Action<JSONOBJECT> callback = callbackMapping[lb](payload);
        }
```
```C#
       
```

```
    3. FORWARD PLAYER'S MESSAGE
    PAYLOAD : {commnad:"onMessage",instanceId:{instanceId},systemId:{user_login},stub:{session_stub},ticket:{ticket}} 
```
```C#
    ---------- SAMPLE C# CODE -------------
    JSONObject payload = new JSONObject(JSONObject.Type.OBJECT);
    payload.AddField("command","onMessage");
    payload.AddField("label","position");
    payload.AddField("systemId","BDS01/fd3032b0fbac4fb6adde79b0f366f80c");
    payload.AddField("instanceId","BDS01/fd3032b0fbac4fb6adde79b0f366f80c/3");
    JSONObject data = new JSONObject(JSONObject.Type.OBJECT);
    data.AddField("x",10);
    data.AddField("y",50);
    data.AddField("z",-40);
    payload.AddField("data",data);
    byte[] mf = Encoding.UTF8.GetBytes(payload.ToString());
    udp.Send(mf,mf.Length,endPoint);
```