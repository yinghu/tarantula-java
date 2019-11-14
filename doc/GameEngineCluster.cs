using System.Text;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using System.Security.Cryptography.X509Certificates;

using UnityEngine;
using UnityEngine.Networking;


namespace PerfectDay.GameEngineCluster{
    
    public class GecHttpClient{
        
        private string GEC_HOST = "localhost:8090";
        public GecHttpClient(string host){
            GEC_HOST = host; 
            
        }
        
        public async Task<string> GetJson(MonoBehaviour caller,string path,Header[] headers){
            using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+path,"GET")){
                www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
                www.certificateHandler = new KeyValidator(); 
                foreach(Header h in headers){
                    www.SetRequestHeader(h.name,h.value);    
                }
                www.SetRequestHeader("Accept","application/json");
                var tcs = new TaskCompletionSource<string>();
                caller.StartCoroutine(SendWebRequest(www,tcs));    
                return await tcs.Task;
            }
        }
        public async Task<string> PostJson(MonoBehaviour caller,string path,Header[] headers,string json){
            using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+path,"POST")){
                byte[] payload = Encoding.UTF8.GetBytes(json.ToString());
                www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
                www.certificateHandler = new KeyValidator(); 
                www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
                foreach(Header h in headers){
                    www.SetRequestHeader(h.name,h.value);    
                }
                www.SetRequestHeader("Accept","application/json");
                www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
                www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);
                var tcs = new TaskCompletionSource<string>();
                caller.StartCoroutine(SendWebRequest(www,tcs));    
                return await tcs.Task;
            }
        }  
        private static IEnumerator SendWebRequest(UnityWebRequest request, TaskCompletionSource<string> tcs){
			yield return request.SendWebRequest();
			if(request.isNetworkError || request.isHttpError) {
                //pass json format error to caller
                tcs.SetResult("{'successful':false,'message':'"+request.error+"'}"); 
            }
            else{
                tcs.SetResult(request.downloadHandler.text);        
            }
		}
    }
    public class KeyValidator : CertificateHandler{

        protected override bool ValidateCertificate(byte[] certificateData){
            //put key validation here
            return true;
        }
    }
    public class Header{
        public string name { get; set; }
        public string value { get; set; }
        public Header(string name,string value){
            this.name = name;
            this.value = value;
        }
    }
    public class Device{
        public string deviceId { get; set; }
    }
    public class User{
        public string login { get; set; }
        public string nickname { get; set; }
        public string emailAddress { get; set; }
        public string password { get; set; }
    }
    public class Presence{
        public bool successful { get; set; }
        public string systemId { get; set; }
        public int stub { get; set; }
        public string token { get; set; }
        public string ticket { get; set; }
        public string balance { get; set; }
        public string login { get; set; }
    }
    public class Connection{
        public string command { get; set; }
        public int code { get; set; }
        public int timestamp { get; set; }
        public int sequence { get; set; }
        public bool successful { get; set; }
        public string path { get; set; }
        public string protocol { get; set; }
        public string host { get; set; }
        public string type { get; set; }
        public string serverId { get; set; }
        public bool secured { get; set; }
        public int port { get; set; }
    }
    public class Descriptor{
        public bool singleton { get; set; }
        public int deployCode { get; set; }
        public string type { get; set; }
        public string typeId { get; set; }
        public string category { get; set; }
        public int capacity { get; set; }
        public string name { get; set; }
        public string description { get; set; }
        public string applicationId { get; set; }
        public int accessMode { get; set; }
        public double entryCost { get; set; }
        public string entryCostAsString { get; set; }
        public bool tournamentEnabled { get; set; }
        public bool disabled { get; set; }
        public bool resetEnabled { get; set; }
        public int timerOnModule { get; set; }
        public int runtimeDuration { get; set; }
        public int runtimeDurationOnInstance { get; set; }
        public string tag { get; set; }
        public string icon { get; set; }
        public string viewId { get; set; }
        public string responseLabel { get; set; }
    }
    public class ArenaZone{
        public string name { get; set; }
        public int rank { get; set; }
        public string description { get; set; }
        public bool enabled { get; set; }
        public List<Arena> list { get; set; }
    }
    public class Arena{
        public string name { get; set; }
        public int level { get; set; }
        public string description { get; set; }
        public bool enabled { get; set; }
    }
}
