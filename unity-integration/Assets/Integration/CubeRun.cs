using GameClustering;
using TMPro;
using UnityEngine;

namespace Integration
{
    public class CubeRun : MonoBehaviour
    {
        public int sequence;
        private bool _enabled = true;
        public TMP_Text bText;
        private void Start()
        {
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(MessageType.Echo,sequence, (buffer) =>
            {
                _enabled = !_enabled;
                bText.text= ("CallId ->"+buffer.GetInt()+"//"+_enabled);
            });
        }

        private void Update()
        {
            if (_enabled) transform.Rotate(0,3,0);
        }
    }
}