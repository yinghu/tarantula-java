using System.Collections;
using System.Text;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Networking;

namespace Holee
{
    public class KeyValidator : CertificateHandler
    {
        protected override bool ValidateCertificate(byte[] certificateData)
        {
            //put key validation here
            //uncomment this block to valid the certificate
            //X509Certificate2 cert = new X509Certificate2(certificateData);
            return true;
        }
    }
    
    public class Header
    {
        public const string TarantulaTag = "Tarantula-tag";
        public const string TarantulaAction = "Tarantula-action";
        public const string TarantulaMagicKey = "Tarantula-magic-key";
        public const string TarantulaToken = "Tarantula-token";
        public const string TarantulaName = "Tarantula-name";
        public const string TarantulaTournamentId = "Tarantula-tournament-id";
        public string Name { get; set; }
        public string Value { get; set; }
    }
    public class HttpCaller
    {
        private readonly string _gecHost;
        public HttpCaller(string host)
        {
            _gecHost = host;
        }

        public async Task<string> GetJson(MonoBehaviour caller,string path,Header[] headers)
        {
            using var www = new UnityWebRequest(_gecHost + path, "GET");
            www.downloadHandler = new DownloadHandlerBuffer();
            www.certificateHandler = new KeyValidator(); 
            foreach(var h in headers)
            {
                www.SetRequestHeader(h.Name,h.Value);    
            }
            www.SetRequestHeader("Accept","application/json");
            var tcs = new TaskCompletionSource<string>();
            caller.StartCoroutine(SendWebRequest(www,tcs));    
            return await tcs.Task;
        }
        
        public async Task<string> PostJson(MonoBehaviour caller,string path,Header[] headers,string json)
        {
            using var www = new UnityWebRequest(_gecHost + path, "POST");
            var payload = Encoding.UTF8.GetBytes(json.ToString());
            www.downloadHandler = new DownloadHandlerBuffer();
            www.certificateHandler = new KeyValidator(); 
            www.uploadHandler = new UploadHandlerRaw(payload);
            foreach(var h in headers)
            {
                www.SetRequestHeader(h.Name,h.Value);    
            }
            www.SetRequestHeader("Accept","application/json");
            www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
            www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);
            var tcs = new TaskCompletionSource<string>();
            caller.StartCoroutine(SendWebRequest(www,tcs));    
            return await tcs.Task;
        }  
        
        private static IEnumerator SendWebRequest(UnityWebRequest request, TaskCompletionSource<string> tcs)
        {
            yield return request.SendWebRequest();
            if(request.result == UnityWebRequest.Result.ProtocolError ) 
            {
                tcs.SetResult("{'successful':false,'message':'"+request.error+"'}"); 
            }
            else
            {
                tcs.SetResult(request.downloadHandler.text);        
            }
        }
        
    }
}