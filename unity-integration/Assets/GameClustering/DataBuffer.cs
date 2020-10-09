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
        private readonly bool _writeMode;
        public DataBuffer()
        {
            _memoryStream = new MemoryStream();
            _writeMode = true;
        }
        public DataBuffer(byte[] data)
        {
            _memoryStream = new MemoryStream(data);
        }

        public void PutVector3(Vector3 vector3)
        {
            CheckMode();
        }

        public Vector3 GetVector3()
        {
            return new Vector3();
        }

        public void PutByte(byte b)
        {
            CheckMode();
            _memoryStream.WriteByte(b);
        }
        public byte GetByte()
        {
            return (byte) _memoryStream.ReadByte();
        }

        public void PutFloat(float f)
        {
            CheckMode();
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
            CheckMode();
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

        private void CheckMode()
        {
            if (!_writeMode)
            {
                throw new InvalidOperationException("write operation not allowed in read mode");
            }
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