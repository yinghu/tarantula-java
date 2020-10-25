using GameClustering;
using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace Integration
{
    public class Shot : MonoBehaviour
    {
        private bool _leaving;
        private float _timer;
        public TMP_Text aText;
        public TMP_Text bText;
        public TMP_Text cText;
        public TMP_Text dText;
        public TMP_Text eText;
        public TMP_Text fText;
        public TMP_Text gText;
        private float _timeout;
        private void Start()
        {
            _leaving = false;
            _timer = 0;
            _timeout = 0;
        }

        public async void Roll()
        {
            var integrationManager = IntegrationManager.Instance;
            for (var i = 1; i < 11; i++)
            {
                using (var buffer1 = new DataBuffer())
                {
                    buffer1.PutInt(1);
                    await integrationManager.Messenger.SendAsync(MessageType.Relay, i, true, buffer1);
                }
            }
        }

        public void Check()
        {
            aText.text = "ACK->"+IntegrationManager.Instance.Messenger.PendingMessages();
            var _i = IntegrationManager.Instance.Messenger.TotalInbound();
            var _O = IntegrationManager.Instance.Messenger.TotalOutbound();
            bText.text = "IN->"+_i;
            cText.text = "OUT->"+_O;
            dText.text = "BYTES->"+IntegrationManager.Instance.Messenger.TotalBytes();
            fText.text = "RATE->"+((_i+_O)/_timer);
            gText.text = "RETRIES->" + IntegrationManager.Instance.Messenger.TotalRetries();
        }

        private void Update()
        {
            if (!_leaving)
            {
                return;
            }
            IntegrationManager.Instance.OnLeftEvent -= Leave;
            SceneManager.LoadScene("Main");
        }

        private async void FixedUpdate()
        {
            _timer += Time.fixedDeltaTime;
            _timeout += Time.fixedDeltaTime;
            eText.text = "TIMER->" + _timer;
            if (_timeout < 0.2) //100 ms
            {
                return;
            }
            _timeout = 0;
            await IntegrationManager.Instance.Messenger.RetryAsync();
        }

        public async void Exit()
        {
            var integrationManager = IntegrationManager.Instance;
            integrationManager.OnLeftEvent += Leave;
            await integrationManager.Leave();
        }

        private void Leave(int sessionId)
        {
            _leaving = true;
        }
    }
}