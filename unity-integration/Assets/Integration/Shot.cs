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
        private int[] _pendingShot;
        private void Start()
        {
            _leaving = false;
            _timer = 0;
            _pendingShot = new int[10];
        }

        public async void Roll()
        {
            var integrationManager = IntegrationManager.Instance;
            foreach (var mid in _pendingShot)
            {
                await integrationManager.Messenger.RetryAsync(mid, true);
            }
            for (var i = 1; i < 11; i++)
            {
                using (var buffer1 = new DataBuffer())
                {
                    buffer1.PutInt(1);
                    _pendingShot[i-1]=await integrationManager.Messenger.SendAsync(MessageType.Relay, i, true, buffer1);
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
            IntegrationManager.Instance.Messenger.UnregisterMessageHandler(MessageType.Leave,3);
            SceneManager.LoadScene("Main");
        }

        private void FixedUpdate()
        {
            _timer += Time.deltaTime;
            eText.text = "TIMER->" + _timer;
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