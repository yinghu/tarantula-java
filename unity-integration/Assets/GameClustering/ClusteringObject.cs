using System;
using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
        private Action<DataBuffer> _action;
        private byte[] _data;
        public int sequence;
        protected void _Start()
        {
            _action = null;
            _data = null;
            Debug.Log("START");
        }

        private void Update()
        {
            if (_action == null)
            {
                return;
            }
            using (var buffer = new DataBuffer(_data))
            {
                _action.Invoke(buffer);
            }
            _action = null;
        }
        //dispatch call from messaging thread into unity main thread. 
        protected void Dispatch(byte[] data,Action<DataBuffer> action)
        {
            _data = data;
            _action = action;
        }
    }
}