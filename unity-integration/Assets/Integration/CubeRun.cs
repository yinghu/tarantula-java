using GameClustering;
using TMPro;
using UnityEngine;

namespace Integration
{
    public class CubeRun : MonoBehaviour
    {
        public int sequence;
        private bool _enabled = true;
        private float _timer;
        private float _speed;
        public TMP_Text bText;
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
            if (_enabled) transform.Rotate(_speed,_speed,_speed);
        }

        private async void FixedUpdate()
        {
            _timer += Time.deltaTime;
            if (_timer < 1)
            {
                return;
            }
            _timer = 0;
            var buffer2 = new DataBuffer();
            var f = _speed < 25 ? (_speed + 1) : 3;
            bText.text = "Speed->" + _speed;
            buffer2.PutFloat(f);
            await IntegrationManager.Instance.Messenger.SendAsync(MessageType.Echo, sequence, false, buffer2);
            await IntegrationManager.Instance.Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer2);
        }
    }
}