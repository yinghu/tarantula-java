

/*
 * Copyright (c) 2017 Razeware LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish, 
 * distribute, sublicense, create a derivative work, and/or sell copies of the 
 * Software in any work that is designed, intended, or marketed for pedagogical or 
 * instructional purposes related to programming, coding, application development, 
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works, 
 * or sale is expressly withheld.
 *    
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using UnityEngine.SceneManagement;
using BeardedManStudios.Forge.Networking.Unity;
public class GlobalStateManager : MonoBehaviour{
    
    private GameEngineCluster integration;
   
    private Descriptor game;
    
    public GameStage gameStage;
    
   
    
    void Start(){
        integration = GameEngineCluster.Instance; 
        game = integration.game;
        integration.OnInboundMessage += _OnStart; 
        integration.OnException += (ex,msg,code)=>{
            Debug.Log(msg);
            Debug.Log(ex);
            Debug.Log(code);
        };
    }
    void Update (){
       
    }
    public void PlayerDied (int playerNumber)
    {

    }
    public async void OnLeave(){   
        gameStage.OnEnd();
        await OnLeave(this);   
    }
    private async Task<bool> OnLeave(MonoBehaviour caller){
        Payload payload = new Payload();
        payload.command = "onLeave";
        integration.OnInboundMessage -= _OnStart;
        return await  integration.OnInstance(caller,game,payload,(ps)=>{
            NetworkManager.Instance.Disconnect();
            integration.CloseUDP();
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex-1);
        });
    }
    async void _OnStart(InboundMessage msg){
        //Debug.Log(msg.query);
        //Debug.Log(msg.instanceId);        
        //Debug.Log(msg.payload);
        if(msg.query!=null&&msg.query.Equals("onStart")){
            gameStage.OnStart(msg.payload);
            //Debug.Log("START=>>>"+msg.payload);
            //JObject jo = JObject.Parse(msg.payload);
            //integration.arena = (string)jo.SelectToken("arena");
            //integration.robotList = (JArray)jo.SelectToken("robotList");
            //matched = true;
        }
        else if(msg.query!=null&&msg.query.Equals("onTimer")){
            gameStage.OnTimer(msg.payload);
        }
        else if(msg.query!=null&&msg.query.Equals("onMove")){
            gameStage.OnMove(msg.payload);
        }
        else if(msg.query!=null&&msg.query.Equals("onEnd")){
            Debug.Log(msg.payload);
            gameStage.OnEnd();
            await OnLeave(this);
        }
        else if(msg.query!=null&&msg.query.Equals("onQuest")){
            gameStage.OnQuest(msg.payload);
        }
        else if(msg.query!=null&&msg.query.Equals("onRemove")){
            gameStage.OnRemove(msg.payload);
        }
        else{
            gameStage.OnMessage(msg);
        }
    }
    public async Task<bool> OnQuest(Payload payload){   
        payload.command = "onQuest";
        return await integration.SendOnInstance(game.applicationId,game.instanceId,payload,true);
    }
    public  async void OnSeat1(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","a"),new Header("c","5")};
        await OnQuest(payload);       
    }
    public  async void OnSeat2(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","b"),new Header("c","5")};
        await OnQuest(payload);
    }
    public  async void OnSeat3(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","c"),new Header("f","2")};
        await OnQuest(payload);
    }
    public async  void OnSeat4(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","d")};
        await OnQuest(payload);
    }
}
