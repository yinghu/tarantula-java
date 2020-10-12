using System.Collections.Generic;
using GameClustering;
using UnityEngine;

namespace Integration
{
    public class Manager : MonoBehaviour
    {
        private IntegrationManager _integrationManager;
        private const float Speed = 3.0f;
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
            _integrationManager.Messenger.RegisterMessageHandler(1,5, buffer =>
            {
                Debug.Log("point->" + buffer.GetFloat());
                Debug.Log("str->" + buffer.GetUTF8String());
                Debug.Log("point->" + buffer.GetFloat());
                Debug.Log("str->" + buffer.GetUTF8String());
                //var v = buffer.GetVector3();
                //Debug.Log("X:"+v.x);
                //Debug.Log("Y:"+v.y);
                //Debug.Log("Z:"+v.z);
                //var v1 = buffer.GetVector3();
                //Debug.Log("X:"+v1.x);
                //Debug.Log("Y:"+v1.y);
                //Debug.Log("Z:"+v1.z);
                //var v2 = buffer.GetQuaternion();
                //Debug.Log("X:"+v2.x);
                //Debug.Log("Y:"+v2.y);
                //Debug.Log("Z:"+v2.z);
                
            });
            await _integrationManager.OnMessage();
        }

        private void Update()
        {
            //using (var buffer = new DataBuffer())
            
            //{ 
                //buffer.PutVector3(transform.position);
                //buffer.PutVector3(transform.localScale);
                //buffer.PutQuaternion(transform.rotation);
                //await _integrationManager.Messenger.SendAsync(1, 4, false,buffer);
            //}
            transform.Rotate(0,Speed,0);
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
                var ck1 = new CallbackKey(1,5);
                var ck2 = new CallbackKey(1,5);
                var _dictionary = new Dictionary<CallbackKey, string>();
                _dictionary[ck1] = "123";
                _dictionary[ck2] = "234";
                Debug.Log("EQ->"+_dictionary.Count);    
                await _integrationManager.Messenger.SendAsync(1, 4, false, buffer);
            }
        }
    }
}