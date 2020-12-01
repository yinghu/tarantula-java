
using UnityEngine;

namespace Integration.Game
{
    public class Bullet : MonoBehaviour
    {
        private const float Speed = 0.15f;
        private Vector3 _shootDirection;
        
        private void Update()
        {
            
            transform.Translate(_shootDirection * Speed);
        }

        public void Fire(Vector3 origin,Vector3 shootDirection)
        {
            transform.position = origin;
            _shootDirection = shootDirection;
        }
    }
}