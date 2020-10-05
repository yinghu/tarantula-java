using System;
using System.Collections;
using System.Text;
using System.Threading.Tasks;
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
            Debug.Log(integrationManager.Presence.SystemId);
            Debug.Log(integrationManager.Presence.Token);
            await integrationManager.Service(this);
            Debug.Log(integrationManager.Presence.ServerKey);
            //var outboundMessage = new OutboundMessage();
            //outboundMessage.Ack(true);
            //outboundMessage.Type(122);
            //outboundMessage.MessageId(20);
            //outboundMessage.ConnectionId(201);
            //var payload = Encoding.UTF8.GetBytes("Hello123456789");
            //outboundMessage.Payload(payload);
            //var inboundMessage = new InboundMessage(outboundMessage.Message());
            //Debug.Log("ack->"+inboundMessage.Ack());
            //Debug.Log("tid->"+inboundMessage.Type());
            //Debug.Log("mid->"+inboundMessage.MessageId());
            //Debug.Log("cid->"+inboundMessage.ConnectionId());
            //Debug.Log("Payload->" + Encoding.UTF8.GetString(inboundMessage.Payload()));
            //outboundMessage.Close();
            //inboundMessage.Close();
            //StartCoroutine(Send());
            //await integrationManager.OnUDPSocketMessage();
        }

        private async void Update()
        {
        }
        private IEnumerator Send()
        {
            yield return new WaitForSeconds(5);
            var integrationManager = IntegrationManager.Instance;
            var outboundMessage = new OutboundMessage();
            outboundMessage.Ack(true);
            outboundMessage.Type(122);
            outboundMessage.MessageId(20);
            outboundMessage.ConnectionId(201);
            var payload = Encoding.UTF8.GetBytes("Hello123456789");
            outboundMessage.Payload(payload);
            Task.FromResult(integrationManager._udpCaller.Send(outboundMessage));
            StartCoroutine(Send());
        }
    }
}