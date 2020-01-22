
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Bomb : MonoBehaviour
{
    public GameObject explosion;
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }
    public void Explode(){
        Instantiate(explosion, transform.position, Quaternion.identity); //1
        GetComponent<MeshRenderer>().enabled = false; //2
        //transform.Find("Collider").gameObject.SetActive(false); //3
        Destroy(gameObject, 2); //4    
    }
}
