using System.Threading.Tasks;
using GameClustering;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace Integration
{
    public class Exit : MonoBehaviour
    {
        private bool _leaving;
        private void Start()
        {
            _leaving = false;
            IntegrationManager.Instance.OnLeftEvent += Leave;
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
        public async void OnExit()
        {
            var integrationManager = IntegrationManager.Instance;
            if (await integrationManager.Leave(this))
            {
                Debug.Log("Left room");
            }
            else
            {
                Debug.Log("end room");
                _leaving = true;
            }
        }

        public void ExitOnEnd()
        {
            _leaving = true;
        }

        private void Leave(int sessionId)
        {
            _leaving = true;
        }
    }
}