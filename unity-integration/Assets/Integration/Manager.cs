using GameClustering;
using UnityEngine;

namespace Integration
{
    public class Manager : MonoBehaviour
    {
        public IntegrationManager integrationManager;

        void Start()
        {
            integrationManager.Index(this);
        }

    }
}