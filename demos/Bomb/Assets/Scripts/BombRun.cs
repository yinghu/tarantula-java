
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BombRun : MonoBomb
{
    public GameObject explosion;
   
    void Awake(){
        GameObject stg = GameObject.Find("Stage");
        transform.SetParent(stg.transform);
        RegisterRpcCallback(RPC_ON_EXPLODE,_OnExplode);
    }
    
    void OnEnable(){
        //Debug.Log("Bomb Run Enabled");
    }
    public void Explode(){
        if(networkObject.IsOwner){
            Debug.Log("Let explode");
            networkObject.SendRpc(RPC_ON_EXPLODE,Receivers.All);
        }
    }
    private IEnumerator WaitAndKill(){
        yield return new WaitForSeconds(1);
        networkObject.Destroy();
    }
    public void _OnExplode(RpcArgs args){
        GameObject ex = Instantiate(explosion,transform.position, Quaternion.identity); //1
        ex.transform.SetParent(transform);
        GetComponent<MeshRenderer>().enabled = false;       
        StartCoroutine(WaitAndKill());
    }
}
