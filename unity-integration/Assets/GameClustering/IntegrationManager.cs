using System;
using System.Security.Cryptography;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using UnityEngine;

namespace GameClustering
{
    [CreateAssetMenu(fileName = "IntegrationManager", menuName = "GameClustering/IntegrationManager", order = 1)]
    public class IntegrationManager : ScriptableObject
    {
        private static readonly JsonSerializerSettings JsonSerializerSettings = new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore};

        public string gecHost = "localhost:8090";
        private HttpCaller _httpCaller;
        public UdpCaller _udpCaller;
        private bool _live;
        private static IntegrationManager _instance;

        public Presence Presence { get; private set; }

        [RuntimeInitializeOnLoadMethod]
        private static void _Init(){
            _instance = Resources.Load<IntegrationManager>("IntegrationManager");
            _instance.Bootstrap();    
        }

        public static IntegrationManager Instance => _instance;
        
        private void Bootstrap()
        {
            _httpCaller = new HttpCaller(gecHost);
            _udpCaller = new UdpCaller();
            _udpCaller.Connect("10.0.0.234",16393);
            _live = true;
            Debug.Log("Started manager");
        }

        public async Task<bool> Index(MonoBehaviour caller)
        {
            try{
                var headers = new []
                {
                    new Header{ Name = Header.TarantulaTag , Value = "index/user"},
                    new Header{ Name = Header.TarantulaAction ,Value = "onIndex"}
                };
                var response = await _httpCaller.GetJson(caller,"/user/action",headers);
                Debug.Log(response);
                return true;
            }
            catch(Exception ex)
            {
               Debug.Log(ex.Message);
               return false;
            }
        }
        
        public  async Task<bool> Device(MonoBehaviour caller){
            try{
                var device = new Device{ DeviceId = "ABC1235"};
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
                if (suc)
                {
                    var pt = jo.SelectToken("presence");
                    Presence = new Presence {SystemId = (string)pt.SelectToken("systemId"), Token = (string)pt.SelectToken("token")};
                }
                Debug.Log(response);
                return true;
            }
            catch(Exception ex)
            {
                Debug.Log(ex.Message);
                return false;
            }
        }

        public async Task<bool> Service(MonoBehaviour caller)
        {
            try
            {
                var headers = new[]
                {
                    new Header {Name = Header.TarantulaTag, Value = "presence/lobby"},
                    new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                    new Header {Name = Header.TarantulaAction, Value = "onPresence"}
                };
                var response = await _httpCaller.GetJson(caller, "/service/action", headers);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                if (suc)
                {
                    Presence.ServerKey = (string)jo.SelectToken("serverKey");
                }
                var aes = Aes.Create();
                
                Debug.Log(response);
                return true;
            }
            catch (Exception ex)
            {
                Debug.Log(ex.Message);
                return false;
            }
        }

        public async Task<bool> OnUDPSocketMessage(){
            try
            {
                //do receive loop
                while(_live)
                {
                    await _udpCaller.Receive();
                    //ParseInboundMessage(msg);
                }    
                return false;
            }
            catch(Exception ex)
            {
                Debug.Log(ex.Message);
                return false;
            }   
        }
        
    }
}