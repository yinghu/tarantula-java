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
            _integrationManager.Messenger.RegisterMessageHandler(1, buffer =>
            {
                Debug.Log("point->" + buffer.GetFloat());
                Debug.Log("str->" + buffer.GetUTFString());
                Debug.Log("point->" + buffer.GetFloat());
                Debug.Log("str->" + buffer.GetUTFString());
            });
            await _integrationManager.OnMessage();
        }

        private void Update()
        {
        }

        public async void SendAsync()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutFloat(12.98f);
                buffer.PutUTFString("Hello");
                buffer.PutFloat(3.56f);
                buffer.PutUTFString("pop");
                await _integrationManager.Messenger.SendAsync(1, 5, false, buffer);
            }
        }
    }
}