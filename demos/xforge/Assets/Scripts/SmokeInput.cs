using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SmokeInput : MonoBehaviour
{
    public SmokeRun smokeRun;
    void Start()
    {
        
    }
    void Update(){
        if (Input.GetMouseButtonDown(0)) {
            RaycastHit hit;
            if (Physics.Raycast(Camera.main.ScreenPointToRay(Input.mousePosition), out hit)) {
                smokeRun.OnRun(hit.point);
            }   
        }
    }
}
