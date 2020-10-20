using GameClustering;
using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;


namespace Integration
{
    public class Manager : MonoBehaviour
    {
        private IntegrationManager _integrationManager;
       
        public TMP_Text bText;
        private bool _playing;
        private void Awake()
        {
            _playing = false;
        }

        private async void Start()
        {
            _integrationManager = IntegrationManager.Instance;
            if (_integrationManager.Authenticated)
            {
                return;
            }
            if (!await _integrationManager.Index(this))
            {
                bText.text = _integrationManager.Exception.Message;
                return;
            }
            if (!await _integrationManager.Device(this))
            {
                bText.text = _integrationManager.Exception.Message;
                return;
            }
            bText.text = _integrationManager.Presence.SystemId;
        }

        private void Update()
        {
            if (!_playing)
            {
                return;
            }
            _integrationManager.Messenger.UnregisterMessageHandler(MessageType.Join,0);
            SceneManager.LoadScene("GamePlay");
        }

        public  async void Play()
        {
            if (!_integrationManager.Authenticated)
            {
                if (!await _integrationManager.Device(this))
                {
                    bText.text = _integrationManager.Exception.Message;
                }
            }
            _integrationManager.Messenger.RegisterMessageHandler(MessageType.Join,0, buff=>
            {
                _playing = buff.GetUTF8String().Equals("accepted");
            });
            await _integrationManager.Ticket(this);
            var buffer = new DataBuffer();
            buffer.PutInt(_integrationManager.Presence.Stub);
            buffer.PutUTF8String(_integrationManager.Presence.Login);
            buffer.PutUTF8String(_integrationManager.Presence.Ticket);
            await _integrationManager.Messenger.SendAsync(MessageType.Join, 0, true, buffer);
        }
        public async void Exit()
        {
            if (!await _integrationManager.Logout(this))
            {
                return;
            }
            bText.text = "play again";
            _playing = false;
            _integrationManager.Messenger.Disconnect();
        }
    }
}