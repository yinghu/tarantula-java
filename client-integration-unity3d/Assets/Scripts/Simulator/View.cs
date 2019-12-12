using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class View : MonoBehaviour{
    
    public GameObject prefab;
    
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }
    
    public void OnView(string name,Vector3 v){
        Vector3 vc = Camera.main.ScreenToWorldPoint(v);
        vc.z = 0;
        GameObject clone = Instantiate(prefab,vc, Quaternion.identity,transform);
        clone.name = name;
    }
}
