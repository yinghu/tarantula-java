using System.Collections.Concurrent;
using GameClustering;
using UnityEngine;

namespace Integration.Spawn
{
    public class Spawn : MonoBehaviour
    {
        public Camera mainCamera;
        public Transform arena;
        public GameObject bump;
        private IntegrationManager _integrationManager;
        private ConcurrentQueue<Vector3> _targets;
        private void Start()
        {
            _targets = new ConcurrentQueue<Vector3>();
            _integrationManager = IntegrationManager.Instance;
            _integrationManager.Messenger.RegisterMessageHandler(MessageType.Spawn,1, (sessionId,buffer) =>
            {
                var target = buffer.GetVector3();
                _targets.Enqueue(target);
                //Instantiate(bump, target, Quaternion.identity, arena);
            });
        }

        private async void Update()
        {
            if (_targets.TryDequeue(out var pt))
            {
                Instantiate(bump, pt, Quaternion.identity, arena);
            }

            if (!Input.GetMouseButtonDown(0))
            {
                return;
            }
            var target = Input.mousePosition;
            var x = (target.x / Screen.width) * Screen.width;
            var y = (target.y / Screen.height) * Screen.height;
            if (!Physics.Raycast(mainCamera.ScreenPointToRay(new Vector3(x, y)), out var hit))
            {
                return;
            }
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(hit.point);
                await _integrationManager.Messenger.SendAsync(MessageType.Spawn, 1,true,buffer);
            }
            //Debug.Log("PT->"+hit.point.x+"->"+hit.point.y+">>"+hit.point.z);
            //Instantiate(bump, hit.point, Quaternion.identity, arena);
        }
        
    }
}