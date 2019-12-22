using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Board : MonoBehaviour{
    public GameObject redPlayer;
    public GameObject greenPlayer;
    public GameObject redDeck;
    public GameObject greenDeck;
    void Start(){
        
    }

    void Update(){
        
    }
    
    public void OnView(){
        
        GameObject r = Instantiate(redPlayer,redDeck.transform.position, Quaternion.identity,redDeck.transform);
        Setup r1 = r.GetComponent<Setup>();
        r1._Setup();
        GameObject g = Instantiate(greenPlayer,greenDeck.transform.position, Quaternion.identity,greenDeck.transform);
        Setup g1 = g.GetComponent<Setup>();
        g1._Setup();   
    }
}
