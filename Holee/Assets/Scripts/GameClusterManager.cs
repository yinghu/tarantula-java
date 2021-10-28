using System;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using UnityEngine;

namespace Holee
{
    public class Device
    {
        public string DeviceId { get; set; }
    }
    public class Presence
    {
        public string SystemId { set; get; }
        public string Token { set; get; }
        public string Ticket { set; get; }
        public string Login { set; get; }
        public int Stub { set; get; }
    }
    public class GameClusterManager
    {
        
        private static readonly JsonSerializerSettings JsonSerializerSettings = new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore};
        private readonly HttpCaller _httpCaller;
        public Presence Presence { get; private set; }
        public Exception Exception { get; private set; }
        private readonly string _deviceId;
        public string ServerKey { get; private set; }
        public int SessionId { get; private set; }

        public GameClusterManager()
        {
            _httpCaller = new HttpCaller("http://10.0.0.192:8090");
            _deviceId = SystemInfo.deviceUniqueIdentifier;
        }

        public  async Task<bool> Device(MonoBehaviour caller){
            try{
                var device = new Device{ DeviceId = _deviceId};
                var headers = new []
                {
                    new Header{ Name = Header.TarantulaTag,Value = "index/user"},
                    new Header{ Name = Header.TarantulaMagicKey, Value = device.DeviceId},
                    new Header{ Name = Header.TarantulaAction, Value = "onDevice"}
                };
                var json = JsonConvert.SerializeObject(device,JsonSerializerSettings);
                var response = await _httpCaller.PostJson(caller,"/user/action",headers,json);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                if (!suc) return false;
                var pt = jo.SelectToken("presence");
                Presence = new Presence
                {
                    SystemId = (string)pt.SelectToken("systemId"),
                    Token = (string)pt.SelectToken("token"),
                    Ticket = (string)pt.SelectToken("ticket"),
                    Login =  (string)pt.SelectToken("login"),
                    Stub = (int)pt.SelectToken("stub")
                };
                return true;
            }
            catch(Exception ex)
            {
                Exception = ex;
                return false;
            }
        }
        
         public async Task<bool> Join(MonoBehaviour caller)
        {
            try
            {
                var headers = new[]
                {
                    new Header {Name = Header.TarantulaTag, Value = "tossup/mmk"},
                    new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                    new Header {Name = Header.TarantulaAction, Value = "onPlay"}
                };
                //var page = new Payload{Headers = new []{new Header{ Name = "page",Value = "1"}}};
                //var json = JsonConvert.SerializeObject(page,JsonSerializerSettings);
                var response = await _httpCaller.GetJson(caller, "/service/action", headers);
                Debug.Log(response);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                if (!suc)
                {
                    Exception = new Exception((string)jo.SelectToken("message"));
                    return false;
                }
                ServerKey = (string)jo.SelectToken("serverKey");
                var list = (JArray)jo.SelectToken("room").SelectToken("list");
                foreach (var je in list)
                {
                    var sid = (string)je.SelectToken("systemId");
                    if (sid.Equals(Presence.SystemId))
                    {
                        SessionId = (int)je.SelectToken("seat");
                    }
                }
                /**Room = new Room
                {
                    //Id = (string) jo.SelectToken("roomId"),
                    Tag = (string) jo.SelectToken("tag"),
                    Seat = (int)jo.SelectToken("seat"),
                    Capacity = (int)jo.SelectToken("capacity"),
                    Arena = (string) jo.SelectToken("arena"),
                    Offline =   (bool) jo.SelectToken("offline")
                };
                if (Room.Offline)
                {
                    return true;
                }

                var pc = jo.SelectToken("connection");
                var connection = new Connection
                {
                    ConnectionId = (int)pc.SelectToken("connectionId"),
                    Type =  (string)pc.SelectToken("type"),
                    Host = (string)pc.SelectToken("host"),
                    Port = (int)pc.SelectToken("port"),
                    Secured = (bool)pc.SelectToken("secured")
                };
                Presence.Ticket = (string)jo.SelectToken("ticket");
                Connect(connection,(string) jo.SelectToken("serverKey"));
                using (var buffer = new DataBuffer())
                {
                    buffer.PutInt(Presence.Stub);
                    buffer.PutUTF8String(Presence.Login);
                    buffer.PutUTF8String(Presence.Ticket);
                    buffer.PutInt(Room.Seat);
                    await Messenger.SendAsync(MessageType.Join, 0, true, buffer);
                }**/
                return true;
            }
            catch (Exception ex)
            {
                Exception = ex;
                return false;
            }    
        }
    }
}