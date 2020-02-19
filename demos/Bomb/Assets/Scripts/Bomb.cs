
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

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
        Instantiate(explosion, transform.position, Quaternion.identity); //1
        GetComponent<MeshRenderer>().enabled = false; //2
        //transform.Find("Collider").gameObject.SetActive(false); //3
        Destroy(gameObject, 2); //4    
    }
}
