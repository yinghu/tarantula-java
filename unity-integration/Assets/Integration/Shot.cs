using GameClustering;
using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace Integration
{
    public class Shot : MonoBehaviour
    {
        private bool _leaving;
        public TMP_Text aText;
        public TMP_Text bText;
        private void Start()
        {
            _leaving = false;
        }

        public async void Roll()
        {
            var integrationManager = IntegrationManager.Instance;
            var buffer1 = new DataBuffer();
            buffer1.PutInt(1);
            await integrationManager.Messenger.SendAsync(MessageType.Relay, 1, true, buffer1);
            var buffer2 = new DataBuffer();
            buffer2.PutInt(2);
            await integrationManager.Messenger.SendAsync(MessageType.Relay, 2, true, buffer2);
        }

        public void Check()
        {
            aText.text = "ACK->"+IntegrationManager.Instance.Messenger.PendingMessages();
            bText.text = "BYTES->"+IntegrationManager.Instance.Messenger.TotalBytes();
        }

        private void Update()
        {
            if (!_leaving)
            {
                return;
            }
            IntegrationManager.Instance.Messenger.UnregisterMessageHandler(MessageType.Leave,3);
            SceneManager.LoadScene("Main");
        }

        public async void Exit()
        {
            var integrationManager = IntegrationManager.Instance;
            integrationManager.Messenger.RegisterMessageHandler(MessageType.Leave,3, buffer =>
            {
                _leaving = true;
            });
            await integrationManager.Messenger.SendAsync(MessageType.Leave, 3, false);
        }
    }
}