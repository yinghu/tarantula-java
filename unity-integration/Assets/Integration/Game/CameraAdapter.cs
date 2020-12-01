using UnityEngine;

namespace Integration.Game
{
    public class CameraAdapter : MonoBehaviour
    {
        
        private const float Speed = 2f;
        private Vector3 _end;
        private float _xOffset;
        private float _zOffset;

        private void Start()
        {
            _end = transform.position;
            _xOffset = _end.x;
            _zOffset = _end.z;
        }

        public void Adapt(Vector3 point)
        {
            var pos = transform.position;
            _end.x = point.x + (_xOffset);
            if (_end.x < -7.4f)
            {
                _end.x = -7.4f;
            }
            else if (_end.x > 7.4f)
            {
                _end.x = 7.4f;
            }
            _end.y = pos.y;
            _end.z = point.z + (_zOffset);
            if (_end.z < -21.7f)
            {
                _end.z = -21.7f;
            }
            else if (_end.z > -13.4f)
            {
                _end.z = -13.4f;
            }
        }
        private void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
        }
    }
}