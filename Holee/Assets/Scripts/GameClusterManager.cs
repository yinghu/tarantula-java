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
        
        public string Tag { get; private set; }

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
                    new Header {Name = Header.TarantulaTag, Value = "holee/mmk"},
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
                Tag = (string)jo.SelectToken("tag");
                var list = (JArray)jo.SelectToken("room").SelectToken("onList");
                foreach (var je in list)
                {
                    var sid = (string)je.SelectToken("systemId");
                    if (sid.Equals(Presence.SystemId))
                    {
                        SessionId = (int)je.SelectToken("seat");
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                Exception = ex;
                return false;
            }    
        }
        
        public  async Task<bool> Leave(MonoBehaviour caller){
            try{
                var headers = new []
                {
                    new Header{ Name = Header.TarantulaTag,Value = Tag},
                    new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                    new Header{ Name = Header.TarantulaAction, Value = "onLeave"}
                };
                var response = await _httpCaller.GetJson(caller,"/service/action",headers);
                var jo = JObject.Parse(response);
                return (bool)jo.SelectToken("successful");
            }
            catch(Exception ex)
            {
                Exception = ex;
                return false;
            }
        }
    }
}