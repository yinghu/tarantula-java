
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class Bomb : MonoBump
{
    public GameObject explosion;
    
    void Start()
    {
        
    }
    void OnEnable(){
        Debug.Log("enable bomb");
    }
    public void Explode(){
        networkObject.SendRpc(RPC_ON_EXPLODE,Receivers.Others);
        Debug.Log("Exploding ... 111");
        StartCoroutine(WaitAndKill());
    }
    private IEnumerator WaitAndKill(){
        yield return new WaitForSeconds(2);
        networkObject.Destroy();
    }
    public override void OnExplode(RpcArgs args){
        Debug.Log("Exploding ...222");
        MainThreadManager.Run(() =>
		{
            Instantiate(explosion, transform.position, Quaternion.identity); //1
            GetComponent<MeshRenderer>().enabled = false;       
        }); 
    }
}
