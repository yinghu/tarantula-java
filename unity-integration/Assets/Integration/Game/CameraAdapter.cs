using UnityEngine;

namespace Integration.Game
{
    public class CameraAdapter : MonoBehaviour
    {
        public float xRight = -7.4f;
        public float xLeft = 7.4f;
        public float zTop = -21.7f;
        public float zBottom = -13.4f;
        
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
            if (_end.x < xRight)
            {
                _end.x = xRight;
            }
            else if (_end.x > xLeft)
            {
                _end.x = xLeft;
            }
            _end.y = pos.y;
            _end.z = point.z + (_zOffset);
            if (_end.z < zTop)
            {
                _end.z = zTop;
            }
            else if (_end.z > zBottom)
            {
                _end.z = zBottom;
            }
        }
        private void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
        }
    }
}