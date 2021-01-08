using GameClustering;
using UnityEngine;

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
            _preQua = transform.rotation;
            _curQua = _preQua;
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
            //_prePos = Vector3.Lerp(_prePos, _curPos, 10*Time.fixedDeltaTime);
            //_curQua = Quaternion.Slerp(_preQua,_curQua,0.15f);
            //transform.rotation = _curQua;
            //transform.position = _prePos;
        }

        public void OnBump(bool isMaster)
        {
            if (isMaster)
            {
                gameObject.AddComponent<Rigidbody>();
            }
            //_collider.isTrigger = true;
            Debug.Log("bumping->"+isMaster);
        }
    }
}