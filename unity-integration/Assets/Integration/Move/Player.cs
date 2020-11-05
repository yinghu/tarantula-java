using UnityEngine;

namespace Integration.Move
{
    public class Player : MonoBehaviour
    {
        private Vector3 _target;
        private const float Speed = 3f;
        private Vector3 _end;

        private void Start()
        {
            _target = transform.position;
            _end = _target;
        }

        public void Move(Vector3 target)
        {
            _end = target;
        }

        private void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);       
        }
    }
    
}