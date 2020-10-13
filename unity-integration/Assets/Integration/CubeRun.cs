using GameClustering;
using UnityEngine;

namespace Integration
{
    public class CubeRun : MonoBehaviour
    {
        public int sequence;
        private bool _enabled = true;

        private void Start()
        {
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(1,sequence, (buffer) =>
            {
                _enabled = !enabled;
                Debug.Log("CallId ->"+buffer.GetInt()+"//"+_enabled);
            });
        }

        private void Update()
        {
            if (_enabled) transform.Rotate(0,3,0);
        }
    }
}