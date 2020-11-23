
using UnityEngine;

namespace Integration.Game
{
    public class Bullet : MonoBehaviour
    {
        private Rigidbody _rigidbody;

        private void Start()
        {
            _rigidbody = GetComponent<Rigidbody>();
        }

        private void Update()
        {
            _rigidbody.AddForce(transform.forward*1000);
        }
    }
}