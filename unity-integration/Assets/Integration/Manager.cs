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
            await _integrationManager.OnMessage();
        }
        
        public async void SendAsync()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutFloat(12.98f);
                buffer.PutUTF8String("Hello");
                buffer.PutFloat(3.56f);
                buffer.PutUTF8String("pop");
                buffer.PutVector3(transform.position);
                await _integrationManager.Messenger.SendAsync(1, 1, false, buffer);
                await _integrationManager.Messenger.SendAsync(1, 2, false, buffer);
            }
        }
    }
}