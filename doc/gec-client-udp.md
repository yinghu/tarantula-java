### CLIENT UDP API LIST
### UDP CALLBACK HOOK
``` 
    1. REGISTER PLAYER ON UDP SERVER
```
```C#
    -------- SAMPLE C# CODE ----------
    JSONObject payload = new JSONObject(JSONObject.Type.OBJECT);
    payload.AddField("command","onJoin");
    payload.AddField("systemId",login);
    payload.AddField("instanceId",conn.GetField("instanceId"));
    payload.AddField("stub",stub);
    payload.AddField("ticket",conn.GetField("ticket").str);
    Debug.Log(payload.ToString());
    byte[] onjoin = Encoding.UTF8.GetBytes(payload.ToString());
    udp.Send(onjoin,onjoin.Length,endPoint);
```