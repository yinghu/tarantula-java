using UnityEngine;

namespace Integration.Collide
{
    public class Collide : MonoBehaviour
    {
        private void OnCollisionEnter(Collision other)
        {
            Debug.Log(other.gameObject.tag);
        }
    }
}