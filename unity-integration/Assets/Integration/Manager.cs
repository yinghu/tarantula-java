using System;
using GameClustering;
using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;


namespace Integration
{
    public class Manager : MonoBehaviour
    {
        private IntegrationManager _integrationManager;
        public GameObject bPlay;
        public GameObject bExit;
        public TMP_Text bText;
        private bool _playing;
        private void Awake()
        {
            _playing = false;
        }

        private async void Start()
        {
            _integrationManager = IntegrationManager.Instance;
            if (!_integrationManager.Authenticated)
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
            await _integrationManager.Service(this);
            await _integrationManager.OnMessage();
        }

        private void Update()
        {
            if (!_playing)
            {
                return;
            }
            _integrationManager.Messenger.UnregisterMessageHandler(0,0);
            SceneManager.LoadScene("GamePlay");
        }

        public  async void Play()
        {
            _integrationManager.Messenger.RegisterMessageHandler(0,0, (buff) =>
            {
                _playing = true;
            });
            var buffer = new DataBuffer();
            buffer.PutInt(100);
            buffer.PutUTF8String(_integrationManager.Presence.Login);
            buffer.PutUTF8String(_integrationManager.Presence.Ticket);
            Debug.Log("TK->"+_integrationManager.Presence.Ticket);
            await _integrationManager.Messenger.SendAsync(0, 0, true, buffer);
        }
        public async void Exit()
        {
            if (!await _integrationManager.Logout(this))
            {
                return;
            }
            _playing = false;
        }
    }
}