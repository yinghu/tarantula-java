using System;
using UnityEngine;

namespace Holee
{
    public class PlayerCollider : MonoBehaviour
    {
        
        private void Start()
        {
            Debug.Log("Start collider object");
        }

        private void OnTriggerEnter(Collider other)
        {
            if (!other.gameObject.tag.Equals("Obs")) return;
            var replication = other.gameObject.GetComponent<Replication>();
            replication.OnTrigger();
        }
        
        private void OnCollisionEnter(Collision other)
        {
            Debug.Log("hit player from 1->"+other.gameObject.tag);
        }
    }
}