using System;
using System.Text;
using GameClustering;
using UnityEngine;

namespace Integration
{
    public class Manager : MonoBehaviour
    {
        private async void Start()
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
            var outboundMessage = new OutboundMessage();
            outboundMessage.Ack(true);
            outboundMessage.Type(122);
            outboundMessage.MessageId(20);
            outboundMessage.ConnectionId(201);
            var payload = Encoding.UTF8.GetBytes("Hello123456789");
            outboundMessage.Payload(payload);
            var inboundMessage = new InboundMessage(outboundMessage.Message());
            Debug.Log("ack->"+inboundMessage.Ack());
            Debug.Log("tid->"+inboundMessage.Type());
            Debug.Log("mid->"+inboundMessage.MessageId());
            Debug.Log("cid->"+inboundMessage.ConnectionId());
            Debug.Log("Payload->" + Encoding.UTF8.GetString(inboundMessage.Payload()));
            outboundMessage.Close();
            inboundMessage.Close();
            await integrationManager._udpCaller.Receive();
        }

        private async void Update()
        {
            var integrationManager = IntegrationManager.Instance;
            var outboundMessage = new OutboundMessage();
            outboundMessage.Ack(true);
            outboundMessage.Type(122);
            outboundMessage.MessageId(20);
            outboundMessage.ConnectionId(201);
            var payload = Encoding.UTF8.GetBytes("Hello123456789");
            outboundMessage.Payload(payload);
            await integrationManager._udpCaller.Send(outboundMessage);
        }
    }
}