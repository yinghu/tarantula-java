using System;
using UnityEngine;

namespace GameClustering
{
    [CreateAssetMenu(fileName = "IntegrationManager", menuName = "GameClustering/IntegrationManager", order = 1)]
    public class IntegrationManager : ScriptableObject
    {
        public string gecHost = "localhost:8090";
        private HttpCaller _httpCaller;
        private static IntegrationManager _instance;
        [RuntimeInitializeOnLoadMethod]
        private static void _Init(){
            _instance = Resources.Load<IntegrationManager>("IntegrationManager");
            _instance.Bootstrap();    
        }

        public static IntegrationManager Instance => _instance;
        private void Bootstrap()
        {
            _httpCaller = new HttpCaller(gecHost);
            Debug.Log("Started manager");
        }

        public async void Index(MonoBehaviour caller)
        {
            Debug.Log("Indexing");
            try{
                var headers = new Header[]{
                    new Header{ Name = "Tarantula-tag", Value = "index/user"},
                    new Header{ Name = "Tarantula-action",Value = "onIndex"}
                };
                var response = await _httpCaller.GetJson(caller,"/user/action",headers);
                Debug.Log(response);
            }catch(Exception ex){
               Debug.Log(ex.Message);
            }
        }
    }
}