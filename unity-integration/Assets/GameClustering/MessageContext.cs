using System;
using System.Collections.Generic;
using UnityEngine;

namespace GameClustering
{
    public class MessageContext : MonoBehaviour
    {
        private static MessageContext _instance;
        private Queue<MainCaller> _queue;
        private object _lock;
        private void Awake()
        {
            if (_instance == null){
                _lock = new object();
                lock (_lock)
                { 
                    _queue = new Queue<MainCaller>(); 
                }
                _instance = this;
                DontDestroyOnLoad(gameObject);
            } else {
                Destroy(this);
            }
        }

        private void Update()
        {
            MainCaller mainCaller;
            lock (_lock)
            {
                if (_queue.Count <= 0)
                {
                    return;
                }
                mainCaller = _queue.Dequeue();
            }
            if (mainCaller.Data != null)
            {
                using (var buffer = new DataBuffer(mainCaller.Data))
                {
                    mainCaller.Caller.Invoke(buffer);
                }
            }
            else
            {
                using (var buffer = new DataBuffer())
                {
                    mainCaller.Caller.Invoke(buffer);
                }
            }
        }
        
        public static MessageContext Instance => _instance;

        public void Execute(byte[] data,Action<DataBuffer> action)
        {
            lock (_lock)
            {
                _queue.Enqueue(new MainCaller {Data = data, Caller = action});
            }
        }
        public void Execute(Action<DataBuffer> action)
        {
            lock (_lock)
            { 
                _queue.Enqueue(new MainCaller {Caller = action});
            }
        }
    }

    internal class MainCaller
    {
        public byte[] Data { set; get; }
        public Action<DataBuffer> Caller { set; get; }
        
    }
}