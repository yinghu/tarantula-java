using System;
using System.IO;
using System.Security.Cryptography;
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
            Debug.Log(integrationManager.Presence.SystemId);
            Debug.Log(integrationManager.Presence.Token);
            integrationManager.Messenger.RegisterMessageHandler(1, mg =>
            {
                Debug.Log("MESSAGE->"+mg.MessageId());
                Debug.Log("CONNECTION->"+mg.ConnectionId());
            });
            await integrationManager.OnMessage();
        }

        private async void Update()
        {
        }

        public async void SendAsync()
        {
            var integrationManager = IntegrationManager.Instance;
            var key = Convert.FromBase64String(integrationManager.Presence.ServerKey);
            var m = new MemoryStream(InboundMessage.SequenceSize);
            var seq = BitConverter.GetBytes(-798);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(seq);
            }
            Debug.Log("SEQ->"+seq.Length);
            var rijAlg = new RijndaelManaged()
            {
                Key = key,
                Padding = PaddingMode.PKCS7,
                Mode = CipherMode.CBC,
                IV = key
            };
            var encryptor = rijAlg.CreateEncryptor();
            var cryptStream = new CryptoStream(m, encryptor, CryptoStreamMode.Write);
            cryptStream.Write(seq,0,seq.Length);
            cryptStream.FlushFinalBlock();
            var outboundMessage = new OutboundMessage();
            outboundMessage.Ack(true);
            outboundMessage.Type(122);
            outboundMessage.MessageId(20);
            outboundMessage.ConnectionId(201);
            var payload = Encoding.UTF8.GetBytes("Hello123456789");
            outboundMessage.Payload(payload);
            var seq1 = m.ToArray();
            Debug.Log("m->"+seq1.Length);
            outboundMessage.Sequence(seq1);
            await integrationManager.Messenger.SendAsync(outboundMessage);
        }
    }
}