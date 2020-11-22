using System;
using System.Collections.Concurrent;
using UnityEngine;

namespace GameClustering
{
    public class MessageContext : MonoBehaviour
    {
        private static MessageContext _instance;
        private ConcurrentQueue<MainCaller> _queue;
        private void Awake()
        {
            if (_instance == null){
                _queue = new ConcurrentQueue<MainCaller>();
                _instance = this;
                DontDestroyOnLoad(gameObject);
            } else {
                Destroy(this);
            }
        }

        private void Update()
        {
            if (!_queue.TryDequeue(out var mainCaller))
            {
                return;
            }
            mainCaller.Caller.Invoke(new DataBuffer(mainCaller.Data));
        }
        
        public static MessageContext Instance => _instance;
        
        public void Execute(byte[] data, Action<DataBuffer> action)
        {
            _queue.Enqueue(new MainCaller {Data = data, Caller = action});
        }
    }

    internal class MainCaller
    {
        public byte[] Data { set; get; }
        public Action<DataBuffer> Caller { set; get; }

    }
}