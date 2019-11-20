using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
using TMPro;
public class Demo : MonoBehaviour
{
    public TextMeshProUGUI  mText;
    void Start(){
        
    }

    
    void Update()
    {
        
    }
    public async void OnStart(){
        Integration ins = Integration.Instance;

        bool suc = await ins.StartDemo();
        if(suc){
            Debug.Log("suc->"+suc);
            //TextMeshPro mText = gameObject.GetComponent<TextMeshPro>();
            mText.text = "Stop";
        }
        else{
             //TextMeshPro mText = gameObject.GetComponent<TextMeshPro>();
            mText.text = "Start";
            Debug.Log("opps=>"+ins.gec.message);
        }
    }
    public class RobotQuest{
        public string gameId { get; set; }
        public int round { get; set; }
        public string player1 { get; set; }
        public string player2 { get; set; }
        public bool started { get; set; }
        public int startCountdown { get; set; }
        public int roundCountdown { get; set; }
    }
}
