using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
        private const float FlashRate = 0.02f;//200ms
        private IntegrationManager _integrationManager;
        private float _flashRate;
        private void Start()
        {
            _flashRate = FlashRate; 
            _integrationManager = IntegrationManager.Instance;
            _integrationManager.Messenger.RegisterMessageHandler(MessageType.Sync,1, (sessionId, buffer) =>
            {
                var q = buffer.GetQuaternion();
                //Debug.Log("SYNC->"+q.w+"//"+q.x+"//"+q.y+"//"+q.z);
            });
        }

        private async void Update()
        {
            _flashRate -= Time.deltaTime;
            if (_flashRate > 0)
            {
                return;
            }
            _flashRate = FlashRate;
            using (var buffer = new DataBuffer())
            {
                buffer.PutQuaternion(transform.rotation);
                buffer.PutVector3(transform.position);
                await _integrationManager.Messenger.SendAsync(MessageType.Sync, 1, false, buffer);
            }
        }
    }
}