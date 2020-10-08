using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using GameClustering;
using UnityEngine;

namespace Integration
{
    public class Manager : MonoBehaviour
    {
        private IntegrationManager _integrationManager;
        private async void Start()
        {
            _integrationManager = IntegrationManager.Instance;
            if (!await _integrationManager.Index(this))
            {
                Debug.Log("INDEX FAILED");    
            }

            if (!await _integrationManager.Device(this))
            {
                Debug.Log("DEVICE FAILED");
            }
            Debug.Log(_integrationManager.Presence.SystemId);
            Debug.Log(_integrationManager.Presence.Token);
            Debug.Log(_integrationManager.Presence.Ticket);
            _integrationManager.Messenger.RegisterMessageHandler(1, mg =>
            {
                Debug.Log("MESSAGE->"+mg.MessageId());
                Debug.Log("CONNECTION->"+mg.ConnectionId());
            });
            await _integrationManager.OnMessage();
        }

        private async void Update()
        {
        }

        public async void SendAsync()
        {
            var payload = Encoding.UTF8.GetBytes("Hello123456789");
           
            await _integrationManager.Messenger.SendAsync(1,5,false,payload);
        }
    }
}