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
                Debug.Log("INDEX FAILED"); 
                bText.text = _integrationManager.Exception.Message;
            }

            if (!await _integrationManager.Device(this))
            {
                Debug.Log("DEVICE FAILED");
                bText.text = _integrationManager.Exception.Message;
            }
            bText.text = _integrationManager.Presence.SystemId;
            await _integrationManager.Service(this);
            //Debug.Log(_integrationManager.Presence.SystemId);
            //Debug.Log(_integrationManager.Presence.Token);
            //Debug.Log(_integrationManager.Presence.Ticket);
            await _integrationManager.OnMessage();
        }
        
        public  void Play()
        {
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