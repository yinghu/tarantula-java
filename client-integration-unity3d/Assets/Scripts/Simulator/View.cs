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
    
    public void OnView(){
        Debug.Log(">>>>> on view");
        Instantiate(prefab, new Vector3(0, 0, 0), Quaternion.identity,transform);
    }
}
