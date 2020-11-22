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
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(MessageType.Relay,sequence, (sessionId,buffer) =>
            {
                _enabled = !_enabled;
            });
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(MessageType.Spawn,sequence, (sessionId,data) =>
            {
                using (var buffer = new DataBuffer(data))
                {
                    _speed = buffer.GetFloat();
                }
            });
        }

        private void Update()
        {
            if (_enabled) transform.Rotate(_speed,_speed,_speed);
        }

        private async void FixedUpdate()
        {
            _timer += Time.fixedDeltaTime; //20 ms per frame
            if (_timer < 0.04) 
            {
                return;
            }
            _timer = 0;
            using (var buffer2 = new DataBuffer())
            {
                var f = _speed < 25 ? (_speed+1) :(3);
                buffer2.PutFloat(f);
                buffer2.PutInt(IntegrationManager.Instance.Messenger.Sequence());
                await IntegrationManager.Instance.Messenger.SendAsync(MessageType.Spawn, sequence, false, buffer2);
            }
        }
    }
}