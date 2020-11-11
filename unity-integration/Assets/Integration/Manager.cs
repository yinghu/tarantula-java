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
        private string _sceneName;
        private void Awake()
        {
            _playing = false;
        }

        private async void Start()
        {
            _integrationManager = IntegrationManager.Instance;
            if (_integrationManager.Authenticated)
            {
                bText.text = "game over";
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
            _integrationManager.OnJoinedEvent -= Join;
            SceneManager.LoadScene(_sceneName);
        }

        public async void Play()
        {
            if (!_integrationManager.Authenticated)
            {
                if (!await _integrationManager.Device(this))
                {
                    bText.text = _integrationManager.Exception.Message;
                    return;
                }
            }
            _sceneName = "Game";
            _integrationManager.OnJoinedEvent += Join;
            await _integrationManager.Lobby(this);
        }

        public  async void Roll()
        {
            if (!_integrationManager.Authenticated)
            {
                if (!await _integrationManager.Device(this))
                {
                    bText.text = _integrationManager.Exception.Message;
                    return;
                }
            }
            _sceneName = "Roll";
            _integrationManager.OnJoinedEvent += Join;
            if (!await _integrationManager.Join(this))
            {
                bText.text = _integrationManager.Exception.Message;
            }
        }
        public  async void Spawn()
        {
            if (!_integrationManager.Authenticated)
            {
                if (!await _integrationManager.Device(this))
                {
                    bText.text = _integrationManager.Exception.Message;
                    return;
                }
            }
            _sceneName = "Spawn";
            _integrationManager.OnJoinedEvent += Join;
            if (!await _integrationManager.Join(this))
            {
                bText.text = _integrationManager.Exception.Message;
            }
        }
        public  async void Move()
        {
            if (!_integrationManager.Authenticated)
            {
                if (!await _integrationManager.Device(this))
                {
                    bText.text = _integrationManager.Exception.Message;
                    return;
                }
            }
            _sceneName = "Move";
            _integrationManager.OnJoinedEvent += Join;
            if (!await _integrationManager.Join(this))
            {
                bText.text = _integrationManager.Exception.Message;
            }
        }
        public  async void Collide()
        {
            if (!_integrationManager.Authenticated)
            {
                if (!await _integrationManager.Device(this))
                {
                    bText.text = _integrationManager.Exception.Message;
                    return;
                }
            }
            _sceneName = "Collide";
            _integrationManager.OnJoinedEvent += Join;
            if (!await _integrationManager.Join(this))
            {
                bText.text = _integrationManager.Exception.Message;
            }
        }
        public async void Exit()
        {
            if (!await _integrationManager.Logout(this))
            {
                return;
            }
            bText.text = "play again";
            _playing = false;
            //_integrationManager.Messenger.Disconnect();
        }

        private void Join(int sessionId)
        {
            Debug.Log("Session id->"+sessionId);
            _playing = true;
        }
    }
}