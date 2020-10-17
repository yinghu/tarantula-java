using System;
using GameClustering;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace Integration
{
    public class Shot : MonoBehaviour
    {
        private bool _leaving;

        private void Start()
        {
            _leaving = false;
        }

        public async void Roll()
        {
            var integrationManager = IntegrationManager.Instance;
            var buffer1 = new DataBuffer();
            buffer1.PutInt(1);
            await integrationManager.Messenger.SendAsync(MessageType.Echo, 1, false, buffer1);
            var buffer2 = new DataBuffer();
            buffer2.PutInt(2);
            await integrationManager.Messenger.SendAsync(MessageType.Echo, 2, false, buffer2);
            Debug.Log("Shooting ...");
        }

        private void Update()
        {
            if (!_leaving)
            {
                return;
            }
            IntegrationManager.Instance.Messenger.UnregisterMessageHandler(MessageType.Leave,3);
            SceneManager.LoadScene("Main");
        }

        public async void Exit()
        {
            var integrationManager = IntegrationManager.Instance;
            integrationManager.Messenger.RegisterMessageHandler(MessageType.Leave,3, ibuffer =>
            {
                _leaving = true;
            });
            var buffer = new DataBuffer();
            buffer.PutInt(1);
            await integrationManager.Messenger.SendAsync(MessageType.Leave, 3, false,buffer);
        }
    }
}