using System.Collections;
using System.Collections.Generic;

using System.Text;
using UnityEngine;
using UnityEngine.Networking;
using Unity.EditorCoroutines.Editor;
using UnityEditor;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
namespace Tarantula.Editor{
    public class PendingUpdate{
        public Object update;
        public bool pending;
        public PendingUpdate(Object o){
            update = o;
            pending = false;
        }
    }
    public enum PLATFORMS{Develop=0,Staging=1,Production=2}
    public enum CONFIGS{Abilities=0,Arenas=1,Robots=2}
    
    public class RQEditor : EditorWindow{
        private static JsonSerializerSettings JSON_SETTING = new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore};
        private Presence presence;
        private string header = "Select platform/config and Login";
        private string login="root";
        private string password="root";
        private PLATFORMS platforms;
        private CONFIGS configs;
        private PendingUpdate[] plist;
        
        [MenuItem ("RQ/UpdateAbilities")] 
        public static void  ShowWindow () {
            EditorWindow.GetWindow(typeof(RQEditor)).Show();
        }
        void Awake(){
            presence = null;
            plist = new PendingUpdate[0];
        }
        void OnGUI(){
            EditorGUILayout.Space();
            EditorGUILayout.LabelField(header, EditorStyles.boldLabel);
            EditorGUILayout.Space();
            if(presence==null){   
                platforms = (PLATFORMS)EditorGUILayout.EnumPopup("Platform", platforms);
                configs = (CONFIGS)EditorGUILayout.EnumPopup("Configuration", configs);
                EditorGUILayout.Space();
                login = EditorGUILayout.TextField("User Name : ",login);
                password = EditorGUILayout.PasswordField("Password : ",password);
                if(GUILayout.Button("Login")){
                    User u = new User();
                    u.login = login;
                    u.password = password;
                    if(Login(u)){
                        Object[] melee = Resources.LoadAll(configs.ToString());
                        plist = new PendingUpdate[melee.Length];
                        for(int i=0;i<melee.Length;i++){
                            plist[i] = new PendingUpdate(melee[i]);
                        }
                        header = platforms+"/"+configs;
                    }    
                }
                return;
            }
            EditorGUILayout.LabelField("Check out to synchronize on ["+platforms+"]", EditorStyles.boldLabel);
            foreach(PendingUpdate p in plist){
                p.pending = EditorGUILayout.Toggle(p.update.name,p.pending);
            }
            if(GUILayout.Button("UPDATE")){
                foreach(PendingUpdate u in plist){
                    if(u.pending){
                        header = "Updating ["+u.update.name+"] ....";
                        Sync(JsonConvert.SerializeObject(u.update,JSON_SETTING));
                    }
                }
            }  
        }
        private bool Sync(string json){
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","robot-quest/admin"),
                new Header("Tarantula-token",presence.token),
                new Header("Tarantula-action","on"+configs)
            };
            string jstr =  PostJson("/service/action",headers,json);
            Debug.Log(jstr);
            return true;
        }
        private bool Login(User u){
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","index/user"),
                new Header("Tarantula-magic-key",u.login),
                new Header("Tarantula-action","onLogin")
            };
            string jstr =  PostJson("/user/action",headers,JsonConvert.SerializeObject(u,JSON_SETTING));
            JObject jo = JObject.Parse(jstr);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                header = (string)jo.SelectToken("message");
                return suc;
            }
            JToken tk = jo.SelectToken("presence");
            presence = tk.ToObject<Presence>();
            header = "Welcome ["+presence.login+"]";
            return true;
        }
        private string PostJson(string path,Header[] headers,string json){
            string host ="http://10.0.0.234:8090";
            switch(platforms){
                case PLATFORMS.Develop:
                    host ="http://10.0.0.234:8090";
                    break;
                case PLATFORMS.Staging:
                    host ="http://10.0.0.234:8090";
                    break;
                case PLATFORMS.Production:
                    host ="http://10.0.0.234:8090";
                    break;
                //default:       
            }
            using(UnityWebRequest www = new UnityWebRequest(host+path,"POST")){
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
                www.SendWebRequest(); 
                while(!www.isDone){//check 
                    header = "Pending ....";   
                }
                string ret;
                if(www.isNetworkError || www.isHttpError) {
                    ret = ("{'successful':false,'message':'"+www.error+"'}"); 
                }
                else{
                    ret = (www.downloadHandler.text);    
                }
                return ret; 
            }
        }  
    }
}