using UnityEngine;

namespace Integration.Spawn
{
    public class Bump : MonoBehaviour
    {
        private float _speed;
        private void Start()
        {
            _speed = 3f;
        }
        
        private void Update()
        {
            transform.Rotate(_speed+1,_speed,_speed-1);    
        }
    }
}