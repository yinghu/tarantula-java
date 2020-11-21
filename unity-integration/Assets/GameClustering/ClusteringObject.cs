using System;
using System.Collections.Concurrent;
using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
        private IntegrationManager _integrationManager;
        private ConcurrentQueue<Vector3> _queue;

        protected void _Start()
        {
           _integrationManager = IntegrationManager.Instance;
           _queue = new ConcurrentQueue<Vector3>();
           Debug.Log("START");
        }

        private void Update()
        {
            
        }

        protected void _Register(int type,int sequence,Action<int,DataBuffer> action)
        {
            _integrationManager.Messenger.RegisterMessageHandler(type,sequence,action);
        }
    }
}