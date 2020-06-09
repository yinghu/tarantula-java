

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
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;

public class GlobalStateManager : AdminBehavior{
    
    private GameEngineCluster integration;
   
    public GameStage gameStage;
    private bool _started;
    private bool _updated;
    protected override void NetworkStart(){
		base.NetworkStart();
        Debug.Log("network started admin");
        _Start();
        _started = true;
    }
    
    void _Start(){
        RegisterRpcCallback(RPC_ON_TIMER,_OnTimer);
        integration = GameEngineCluster.Instance; 
        integration.OnUpdating += _OnUpdating; 
        integration.OnException += (ex,msg,code)=>{
            Debug.Log(msg);
            Debug.Log(ex);
            Debug.Log(code);
        };
        if(integration.room.connection.offline){
            SceneManager.LoadSceneAsync(integration.room.arena,LoadSceneMode.Additive);
            StartCoroutine(StartCountdown());
        }
    }
    async void Update (){
        if(!_started){
            return;
        }
        if(integration.dedicated&&_updated){
            _updated = false;
            Gain[] stats = new Gain[]{
                new Gain(0,"mc",10),
                new Gain(0,"fc",2)
            };
            await integration.OnGameUpdated(this,stats);
        }
        if(integration.dedicated&&integration.room.started&&integration.room.totalJoined==0){
            integration.room.started = false;
            Gain[] stats = new Gain[]{
                new Gain(0,"mc",10),
                new Gain(0,"pc",2)
            };
            Rating[] ratings = new Rating[]{
                new Rating(0,1,70),
            };
            await integration.OnGameEnded(this,stats,ratings,(s)=>{
                _started = false;
                Debug.Log(s);
                Application.Quit();
            });
        }
        if(integration.dedicated&&(!integration.room.started)&&integration.room.totalJoined>0){
            integration.room.started = true;
            await integration.OnGameStarted(this,(ms)=>{
                Debug.Log("ready to play=>"+ms);
                Room jo = JObject.Parse(ms).Root.ToObject<Room>();
                //Debug.Log("Room=>"+jo.arena);
                integration.room.arena = jo.arena;
                integration.room.duration = jo.duration;
                integration.room.overtime = jo.overtime;
                integration.room.playerList = jo.playerList;
                //Debug.Log("Room=>"+jo.duration);
                SceneManager.LoadSceneAsync("Map",LoadSceneMode.Additive);
                StartCoroutine(StartCountdown());
            });                    
        }           
    }
    public void PlayerDied (int playerNumber)
    {
            
    }
    void _OnUpdating(RoomState st){
        if(st==RoomState.STARTING){
            gameStage.OnTimer(integration.timer.m,integration.timer.s);    
        }
        else if(st==RoomState.OVERTIME){
            //Debug.Log("Overtime run");
            gameStage.OnTimer(integration.timer.m,integration.timer.s);
        }
        else if(st==RoomState.ENDING){
            integration.OnUpdating -= _OnUpdating;
            gameStage.OnEnd();
            integration.OnClose();
            NetworkManager.Instance.Disconnect();
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex-1);    
        }
    }
    private IEnumerator StartCountdown(){
        while (integration.room.duration>0){
             //Debug.Log("Countdown: " + integration.room.duration);
             yield return new WaitForSeconds(1.0f);
             integration.room.duration--;
             //gameStage.OnLive();
             _updated = true;
             if(networkObject.IsOwner){
                networkObject.SendRpc(RPC_ON_TIMER, Receivers.All,integration.room.duration); 
                if(integration.room.duration<=30){
                    integration.room.totalJoined=0;    
                }; 
             }
         }        
    }
    private void _OnTimer(RpcArgs args){
        int timer = args.GetNext<int>();
        //Debug.Log("TIMER->"+timer);
        gameStage.OnTimer(timer);
    }

}
