using GameClustering;
using UnityEngine;

namespace Integration
{
    public class Manager : MonoBehaviour
    {
        public IntegrationManager integrationManager;

        async void Start()
        {
            await integrationManager.Index(this);
            await integrationManager.Device(this);
        }

    }
}