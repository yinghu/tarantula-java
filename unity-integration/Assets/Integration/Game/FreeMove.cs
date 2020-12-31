using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class FreeMove : ClusteringObject
    {
        private  float _speed = 6f;
        private Vector3 _end;
       
        private void Start()
        {
            
            _end = transform.position;
        }
        
        private  void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, _speed*Time.fixedDeltaTime);
        }

        private async void OnTriggerEnter(Collider other)
        {
            if (!other.gameObject.CompareTag("pvx"))
            {
                return;
            }
            await Messenger.SendAsync(MessageType.Collision, sequence, true);
        }
        
        public override void Setup(int oid, bool owner)
        {
            base.Setup(oid,owner);
            Messenger.RegisterMessageHandler(MessageType.Move,sequence, (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                    _speed = buffer.GetFloat();
                });
            });
            Messenger.RegisterMessageHandler(MessageType.OnCollision,sequence, async (sessionId, data) =>
            {
                await Messenger.SendAsync(MessageType.Destroy, sequence, true);
            });
            Messenger.RegisterMessageHandler(MessageType.Destroy,sequence, (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    DestroyImmediate(gameObject);    
                });
            });
        }
    }
}