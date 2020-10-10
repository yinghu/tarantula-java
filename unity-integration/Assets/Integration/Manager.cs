using GameClustering;
using UnityEngine;

namespace Integration
{
    public class Manager : MonoBehaviour
    {
        private IntegrationManager _integrationManager;
        private  float _smooth = 5.0f;
        private  float _tiltAngle = 60.0f;
       
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
                var v = buffer.GetVector3();
                Debug.Log("X:"+v.x);
                Debug.Log("Y:"+v.y);
                Debug.Log("Z:"+v.z);
            });
            await _integrationManager.OnMessage();
        }

        private async void Update()
        {
            var tiltAroundZ = Input.GetAxis("Horizontal") * _tiltAngle;
            var tiltAroundX = Input.GetAxis("Vertical") * _tiltAngle;

            // Rotate the cube by converting the angles into a quaternion.
            var target = Quaternion.Euler(tiltAroundX, 0, tiltAroundZ);
            var updates = Quaternion.Slerp(transform.rotation, target,  Time.deltaTime * _smooth);
            //await   _integrationManager.Messenger.SendAsync()
        }

        public async void SendAsync()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutFloat(12.98f);
                buffer.PutUTFString("Hello");
                buffer.PutFloat(3.56f);
                buffer.PutUTFString("pop");
                buffer.PutVector3(transform.position);
                await _integrationManager.Messenger.SendAsync(1, 5, false, buffer);
            }
        }
    }
}