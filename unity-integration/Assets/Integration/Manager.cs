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

            await _integrationManager.Service(this);
            Debug.Log(_integrationManager.Presence.SystemId);
            Debug.Log(_integrationManager.Presence.Token);
            Debug.Log(_integrationManager.Presence.Ticket);
            _integrationManager.Messenger.RegisterMessageHandler(1, mg =>
            {
                Debug.Log("MESSAGE ID->"+mg.MessageId());
                Debug.Log("CONNECTION ID->"+mg.ConnectionId());
                Debug.Log("PAYLOAD->"+Encoding.UTF8.GetString(mg.Payload()));
            });
            await _integrationManager.OnMessage();
        }

        private void Update()
        {
        }

        public async void SendAsync()
        {
            var payload = Encoding.UTF8.GetBytes("Hello123456789");
            await _integrationManager.Messenger.SendAsync(1,5,false,payload);
        }
    }
}