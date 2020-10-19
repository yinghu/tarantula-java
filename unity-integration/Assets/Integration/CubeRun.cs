using GameClustering;
using UnityEngine;

namespace Integration
{
    public class CubeRun : MonoBehaviour
    {
        public int sequence;
        private bool _enabled = true;
        private float _timer;
        private float _speed;
        private void Start()
        {
            _timer = 0;
            _speed = 3;
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(MessageType.Relay,sequence, (buffer) =>
            {
                _enabled = !_enabled;
            });
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(MessageType.Echo,sequence, (buffer) =>
            {
                _speed = buffer.GetFloat();
            });
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(MessageType.Spawn,sequence, (buffer) =>
            {
                _speed = buffer.GetFloat();
            });
        }

        private void Update()
        {
            if (_enabled) transform.Rotate(0,_speed,0);
        }

        private async void FixedUpdate()
        {
            _timer += Time.deltaTime;
            if (_timer < 0.5)
            {
                return;
            }
            _timer = 0;
            var buffer2 = new DataBuffer();
            buffer2.PutFloat(_speed.Equals(3)?6:3);
            await IntegrationManager.Instance.Messenger.SendAsync(MessageType.Echo, sequence, false, buffer2);
            await IntegrationManager.Instance.Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer2);
        }
    }
}