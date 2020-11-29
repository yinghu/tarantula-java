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
        public void PutVector2(Vector2 vector2)
        {
            CheckMode();
            WritePrimitiveBytes(BitConverter.GetBytes(vector2.x));
            WritePrimitiveBytes(BitConverter.GetBytes(vector2.y));
        }

        public Vector2 GetVector2()
        {
            return new Vector2
            {
                x = GetFloat(),
                y = GetFloat()
            };
        }
        public void PutVector3(Vector3 vector3)
        {
            CheckMode();
            WritePrimitiveBytes(BitConverter.GetBytes(vector3.x));
            WritePrimitiveBytes(BitConverter.GetBytes(vector3.y));
            WritePrimitiveBytes(BitConverter.GetBytes(vector3.z));
        }

        public Vector3 GetVector3()
        {
            return new Vector3
            {
                x = GetFloat(),
                y = GetFloat(),
                z = GetFloat()
            };
        }

        public void PutVector4(Vector4 vector4)
        {
            CheckMode();
            WritePrimitiveBytes(BitConverter.GetBytes(vector4.w));
            WritePrimitiveBytes(BitConverter.GetBytes(vector4.x));
            WritePrimitiveBytes(BitConverter.GetBytes(vector4.y));
            WritePrimitiveBytes(BitConverter.GetBytes(vector4.z));
        }

        public Vector4 GetVector4()
        {
            return new Vector4
            {
                w = GetFloat(),
                x = GetFloat(),
                y = GetFloat(),
                z = GetFloat() 
            };
        }
        
        public Quaternion GetQuaternion()
        {
            return new Quaternion
            {
                w = GetFloat(),
                x = GetFloat(),
                y = GetFloat(),
                z = GetFloat() 
            };
        }
        public void PutQuaternion(Quaternion quaternion)
        {
            CheckMode();
            WritePrimitiveBytes(BitConverter.GetBytes(quaternion.w));
            WritePrimitiveBytes(BitConverter.GetBytes(quaternion.x));
            WritePrimitiveBytes(BitConverter.GetBytes(quaternion.y));
            WritePrimitiveBytes(BitConverter.GetBytes(quaternion.z));
        }

        public void PutColor(Color color)
        {
            CheckMode();
            WritePrimitiveBytes(BitConverter.GetBytes(color.r));
            WritePrimitiveBytes(BitConverter.GetBytes(color.g));
            WritePrimitiveBytes(BitConverter.GetBytes(color.b));
            WritePrimitiveBytes(BitConverter.GetBytes(color.a));
        }
        public Color GetColor()
        {
            return new Color
            {
                r = GetFloat(),
                g = GetFloat(),
                b = GetFloat(),
                a = GetFloat() 
            };
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
        public void PutInt(int i)
        {
            CheckMode();
            WritePrimitiveBytes(BitConverter.GetBytes(i));
        }

        public int GetInt()
        {
            var bytes = new byte[4];
            _memoryStream.Read(bytes, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            return BitConverter.ToInt32(bytes, 0);
        }
        public void PutFloat(float f)
        {
            CheckMode();
            WritePrimitiveBytes(BitConverter.GetBytes(f));
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

        public void PutUTF8String(string utf)
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
        public string GetUTF8String()
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
        
        private void WritePrimitiveBytes(byte[] bytes)
        {
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Write(bytes,0,bytes.Length);
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