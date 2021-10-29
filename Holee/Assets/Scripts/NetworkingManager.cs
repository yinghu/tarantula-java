using System;
using System.Net;
using System.Net.Sockets;
using UnityEngine;

namespace Holee
{
    public delegate void OnReceived(byte[] message);
    public static class NetworkingManager
    {
        public static event OnReceived OnReceived;

        private const string Host = "10.0.0.192";//"34.75.132.16";
        private const int Port = 11933;

        private static readonly UdpClient UdpClient;
        private static IPEndPoint _ipEndPoint;
        
        static NetworkingManager()
        {
            _ipEndPoint = new IPEndPoint(IPAddress.Parse(Host), Port);
            UdpClient = new UdpClient();
            UdpClient.Connect(_ipEndPoint);
            UdpClient.BeginReceive(ReceiveCallback, null);
            Debug.Log("Starting udp client");
        }
        
        public static void Send(byte[] payload,int length)
        {
            UdpClient.BeginSend(payload,length, SendCallback,null);
        }

        private static void SendCallback(IAsyncResult asyncResult)
        {
            UdpClient.EndSend(asyncResult);
        }
        
        private static void ReceiveCallback(IAsyncResult asyncResult)
        {
            var ret = UdpClient.EndReceive(asyncResult, ref _ipEndPoint);
            OnReceived?.Invoke(ret);
            UdpClient.BeginReceive(ReceiveCallback, null);
        }

        public static void Close()
        {
            UdpClient.Close();
            UdpClient.Dispose();
        }
        
    }
}