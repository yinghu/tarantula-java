
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
    }

    void OnEnable(){
        Debug.Log("Bomb Run Enabled");
    }
    public void Explode(){
        networkObject.SendRpc(RPC_ON_EXPLODE,Receivers.All);
        StartCoroutine(WaitAndKill());
    }
    private IEnumerator WaitAndKill(){
        yield return new WaitForSeconds(5);
        networkObject.Destroy();
    }
    public override void OnExplode(RpcArgs args){
        Debug.Log("Exploding ...222");
        MainThreadManager.Run(() =>
		{
            GameObject ex = Instantiate(explosion,transform.position, Quaternion.identity); //1
            ex.transform.SetParent(transform);
            GetComponent<MeshRenderer>().enabled = false;       
        }); 
    }
}
