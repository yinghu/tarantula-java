using System;
using UnityEngine;

namespace Holee
{
    public class RequestPopup : MonoBehaviour,IMessage
    {
        private void Start()
        {
            Debug.Log("start popup");
        }

        public void OnRequest()
        {
            
        }

        public void OnMessage(MessageHeader header, MessageBuffer messageBuffer)
        {
            
        }

    }
}