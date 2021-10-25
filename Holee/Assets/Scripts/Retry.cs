using System.Collections.Generic;
using UnityEngine;

namespace Holee
{
    public class Retry : MonoBehaviour
    {
        private Dictionary<string,RetryData> _pendingAckMessage;
        private float _timer;
        private void Start()
        {
            Debug.Log("Start Retry");
            _pendingAckMessage = new Dictionary<string,RetryData>();
        }
        
        private void FixedUpdate()
        {
            _timer += Time.deltaTime;//20ms per frame
            if (_timer < 0.2) return; 
            foreach (var keyValue in _pendingAckMessage)
            {
                Debug.Log("Retry->"+keyValue.Key);
                var retry = keyValue.Value;
                retry.Retries--;
                NetworkingManager.Send(retry.Data,retry.Data.Length);
                if (retry.Retries <= 0)
                {
                    _pendingAckMessage.Remove(keyValue.Key);
                }
            }
            _timer = 0;
        }

        public void PendingAck(string key, byte[] data,int retries)
        {
            _pendingAckMessage[key] = new RetryData { Retries = retries,Data = data};
        }

        public bool Ack(string key)
        {
            return _pendingAckMessage.Remove(key);
        }

        public void OnAck(MessageBuffer messageBuffer)
        {
            for (var i = 0; i < 10; i++)
            {
                var ack = messageBuffer.ReadHeader();
                Ack(ack.ToString());
            }
        }

        internal struct RetryData
        {
            public int Retries;
            public byte[] Data;
        }

    }
}