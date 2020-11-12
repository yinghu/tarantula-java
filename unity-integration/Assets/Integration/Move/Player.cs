using System.Collections.Concurrent;
using GameClustering;
using UnityEngine;

namespace Integration.Move
{
    public class Player : MonoBehaviour
    {
        private Vector3 _target;
        private const float Speed = 3f;
        private Vector3 _end;
        private ConcurrentQueue<Vector3> _queue;
        private IntegrationManager _integrationManager;
        private void Start()
        {
            _target = transform.position;
            _end = _target;
            _queue = new ConcurrentQueue<Vector3>();
            _integrationManager = IntegrationManager.Instance;
            _integrationManager.Messenger.RegisterMessageHandler(MessageType.Relay,1, (sessionId, buffer) =>
            {
                _queue.Enqueue(buffer.GetVector3());
            });
        }

        public async void Move(Vector3 target)
        {
            _end = target;
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(target);
                await _integrationManager.Messenger.SendAsync(MessageType.Relay, 1, true, buffer);
            }
        }

        private void Update()
        {
            if (_queue.TryDequeue(out var pos))
            {
                _end = pos;
            }
        }

        private void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
        }
    }
    
}