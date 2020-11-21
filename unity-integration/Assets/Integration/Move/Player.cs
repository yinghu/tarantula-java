using System.Collections.Generic;
using GameClustering;
using UnityEngine;

namespace Integration.Move
{
    public class Player : ClusteringObject
    {
        public int sequence = 1;
        private Vector3 _target;
        private const float Speed = 3f;
        private Vector3 _end;
      
        private IntegrationManager _integrationManager;
        private void Start()
        {
            _Start();
            _target = transform.position;
            _end = _target;
            _Register(MessageType.Relay,sequence,(sessionId, buffer) =>
            {
                _end = buffer.GetVector3();
            });
            _integrationManager = IntegrationManager.Instance;
        }

        public async void Move(Vector3 target)
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(target);
                await _integrationManager.Messenger.SendAsync(MessageType.Relay, sequence, true, buffer);
            }
        }
        
        private void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
        }
    }
    
}