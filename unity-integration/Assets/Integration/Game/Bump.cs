using GameClustering;
using UnityEngine;
using UnityEngine.UIElements;

namespace Integration.Game
{
    public class Bump : ClusteringObject
    {
        private Vector3 _prePos;
        private Quaternion _preQua;
        
        private Vector3 _curPos;
        private Quaternion _curQua;
        

        private void Start()
        {
            OnSync( buffer =>
            {
                buffer.PutVector3(transform.position);
                buffer.PutQuaternion(transform.rotation);
            },
            buffer =>
            {
                transform.position = buffer.GetVector3();
                transform.rotation = buffer.GetQuaternion();
            });
            _prePos = transform.position;
            _curPos = _prePos;
        }

        private async void Update()
        {
            if (_curPos.Equals(transform.position))
            {
                return;
            }
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(transform.position);
                buffer.PutQuaternion(transform.rotation);
                await Messenger.SendAsync(MessageType.OnSync, sequence, true);
            }
        }

        private void FixedUpdate()
        {
            _prePos = Vector3.Lerp(_prePos, _curPos, 10*Time.fixedDeltaTime);
            transform.position = _prePos;
        }
    }
}