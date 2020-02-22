
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Bomb : MonoBehaviour
{
    
    void FixedUpdate(){
        transform.localScale = transform.localScale*(1+Time.fixedDeltaTime/1000);  
    }
}
