using GameClustering;
using UnityEngine;

namespace Integration
{
    public class Manager : MonoBehaviour
    {
        async void Start()
        {
            var integrationManager = IntegrationManager.Instance;
            if (!await integrationManager.Index(this))
            {
                Debug.Log("INDEX FAILED");    
            }

            if (!await integrationManager.Device(this))
            {
                Debug.Log("DEVICE FAILED");
            }
        }

    }
}