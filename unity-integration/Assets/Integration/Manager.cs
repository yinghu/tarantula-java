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
        private static bool _created;

        private void Awake()
        {
            DontDestroyOnLoad(gameObject);
            if (_created)
            { 
                Destroy(gameObject);
            }
            else
            {
                _created = true;
            }
            bPlay.SetActive(true);
            bExit.SetActive(false);
        }

        private async void Start()
        {
            bText.text = "Start";
            _integrationManager = IntegrationManager.Instance;
            if (!await _integrationManager.Index(this))
            {
                bText.text = _integrationManager.Exception.Message;
            }
            if (!await _integrationManager.Device(this))
            {
                bText.text = _integrationManager.Exception.Message;
            }
            bText.text = _integrationManager.Presence.SystemId;
            await _integrationManager.Service(this);
            await _integrationManager.OnMessage();
        }
        
        public  async void Play()
        {
            var buffer = new DataBuffer();
            buffer.PutUTF8String(_integrationManager.Presence.Ticket);
            await _integrationManager.Messenger.SendAsync(0, 0, true, buffer);
            SceneManager.LoadScene("GamePlay");
            bPlay.SetActive(false);
            bExit.SetActive(true);
        }
        public  void Exit()
        {
            SceneManager.LoadScene("Main");
            bExit.SetActive(false);
            bPlay.SetActive(true);
        }
    }
}