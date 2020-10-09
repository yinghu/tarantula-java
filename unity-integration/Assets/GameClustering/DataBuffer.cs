using System;
using System.IO;
using System.Text;
using UnityEngine;

namespace GameClustering
{
    public class DataBuffer : IDisposable
    {
        private readonly MemoryStream _memoryStream;
        private bool _disposed;
        public DataBuffer()
        {
            _memoryStream = new MemoryStream();
        }
        public DataBuffer(byte[] data)
        {
            _memoryStream = new MemoryStream(data);
        }
        public void PutByte(byte b)
        {
            _memoryStream.WriteByte(b);
        }
        public byte GetByte()
        {
            return (byte) _memoryStream.ReadByte();
        }

        public void PutFloat(float f)
        {
            var bytes = BitConverter.GetBytes(f);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Write(bytes,0,4);
        }

        public float GetFloat()
        {
            var bytes = new byte[4];
            _memoryStream.Read(bytes, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            return BitConverter.ToSingle(bytes, 0);
        }

        public void PutUTFString(string utf)
        {
            var str = Encoding.UTF8.GetBytes(utf);
            var bytes = BitConverter.GetBytes(str.Length);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Write(bytes,0,4);
            _memoryStream.Write(str,0,str.Length);
        }
        public string GetUTFString()
        {
            var bytes = new byte[4];
            _memoryStream.Read(bytes, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }

            var sz = BitConverter.ToInt32(bytes,0);
            var str = new byte[sz];
            _memoryStream.Read(str, 0, sz);
            return Encoding.UTF8.GetString(str);
        }

        public byte[] ToArray()
        {
            return _memoryStream.ToArray();
        }

        public void Dispose() => Dispose(true);
        protected virtual void Dispose(bool disposing)
        {
            Debug.Log("release resource 3");
            if (_disposed)
            {
                return;
            }
            if (disposing)
            {
                _memoryStream?.Dispose();
            }
            _disposed = true;
        }
        ~DataBuffer() => Dispose(false);
    }
}