using GameClustering;
using UnityEngine;

namespace Integration.Move
{
    public class Player : ClusteringObject
    {
        
        private Vector3 _target;
        private const float Speed = 6f;
        private Vector3 _end;
      
       
        private void Start()
        {
            StartClusteringObject(buffer =>
            {
                _end = buffer.GetVector3();
            });
            _target = transform.position;
            _end = _target;
           
            Messenger.RegisterMessageHandler(MessageType.Relay,sequence,(sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                    _end.y = 1;
                });
            });
        }

        public async void Move(Vector3 target)
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(target);
                await Messenger.SendAsync(MessageType.Relay, sequence, true, buffer);
            }
        }
        
        private void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
        }
    }
    
}