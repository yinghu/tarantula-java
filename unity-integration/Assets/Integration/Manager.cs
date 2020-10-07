using System;
using System.Collections;
using System.IO;
using System.Security.Cryptography;
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
            //await integrationManager.Service(this);
            //Debug.Log(integrationManager.Presence.ServerKey);
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
            //var decrypt = rijAlg.CreateDecryptor(rijAlg.Key, key);
            //var ret = decrypt.TransformFinalBlock(payload, 0, payload.Length);
            //Debug.Log(Encoding.UTF8.GetString(ret));
            //var mx = new MemoryStream(InboundMessage.SequenceSize);
            //var cm = new CryptoStream(m,decrypt,CryptoStreamMode.Read);
            //cm.Flush();
            //cm.FlushFinalBlock();
            //var ret = m.ToArray();
            //if (BitConverter.IsLittleEndian)
            //{
                //Array.Reverse(ret);
            //}
            //Debug.Log("MK->"+BitConverter.ToInt32(ret,0));
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
    }
}