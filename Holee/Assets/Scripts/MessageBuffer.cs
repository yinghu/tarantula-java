using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using UnityEngine;

namespace Holee
{
    public class Command
    {
        public const short Ack = 0;
        public const short Replication = 1;
        
        public const short Join = 100;
        public const short Ping = 101;
        
        //server notification
        public const short OnJoin = 200;
    }
    public interface IMessage
    {
        void OnMessage(MessageHeader header,MessageBuffer messageBuffer);
    }

    public struct MessageHeader
    {
        public bool Ack;
        public int ChannelId;
        public int SessionId;
        public int ObjectId;
        public int Sequence;
        public short CommandId;
        public short BatchSize;
        public short Batch;
        public bool Broadcasting;
        public bool Encrypted; //25

        public override string ToString()
        {
            return "HEADER->" + ChannelId + "<>" + SessionId + "<>" + ObjectId + "<>" + Sequence;
        }

    }

    public class MessageBuffer :IDisposable
    {
        public const int Size = 508;
        public const int HeaderSize = 25;
        private bool _disposed;
        private readonly MemoryStream _memoryStream;
        private readonly byte[] _tem4;
        private readonly byte[] _tem2;
        private bool _encrypted;
        private readonly UTF8Encoding _utf8Encoding;
        
        private readonly Rijndael _cipher;
        
        public MessageBuffer(Rijndael cipher) : this()
        {
            _cipher = cipher;
        }
        public MessageBuffer()
        {
            _memoryStream = new MemoryStream(new byte[Size]);
            _memoryStream.Position = 0;
            _tem4 = new byte[4];
            _tem2 = new byte[2];
            _utf8Encoding = new UTF8Encoding();
        }

        public MessageBuffer WriteHeader(MessageHeader header)
        {
            CheckAvailability(HeaderSize);
            _encrypted = header.Encrypted;
            WriteByte(header.Ack?(byte)1:(byte)0).WriteInt(header.ChannelId).WriteInt(header.SessionId);
            WriteInt(header.ObjectId).WriteInt(header.Sequence).WriteShort(header.CommandId);
            WriteShort(header.BatchSize).WriteShort(header.Batch);
            return WriteByte(header.Broadcasting?(byte)1:(byte)0).WriteByte(header.Encrypted?(byte)1:(byte)0);
        }
        public MessageHeader ReadHeader()
        {
            return new MessageHeader
            {
                Ack = ReadByte()==1,
                ChannelId =  ReadInt(),
                SessionId = ReadInt(),
                ObjectId = ReadInt(),
                Sequence = ReadInt(),
                CommandId =  ReadShort(),
                BatchSize = ReadShort(),
                Batch = ReadShort(),
                Broadcasting = ReadByte()==1,
                Encrypted = ReadByte()==1
            };
        }

        public MessageBuffer WriteVector3(Vector3 vector3)
        {
            CheckAvailability(12);
            return WriteFloat(vector3.x).WriteFloat(vector3.y).WriteFloat(vector3.z);
        }

        public Vector3 ReadVector3()
        {
            return new Vector3
            {
                x = ReadFloat(),
                y = ReadFloat(),
                z = ReadFloat()
            };
        }

        public MessageBuffer WriteQuaternion(Quaternion quaternion)
        {
            CheckAvailability(16);
            return WriteFloat(quaternion.w).WriteFloat(quaternion.x).WriteFloat(quaternion.y).WriteFloat(quaternion.z);
        }
        public Quaternion ReadQuaternion()
        {
            return new Quaternion
            {
                w = ReadFloat(),
                x = ReadFloat(),
                y = ReadFloat(),
                z = ReadFloat()
            };
        }

        public MessageBuffer WriteByte(byte data)
        {
            CheckAvailability(1);
            _memoryStream.WriteByte(data);
            return this;
        }

        public byte ReadByte()
        {
            return (byte)_memoryStream.ReadByte();
        }
        
        public MessageBuffer WriteUTF8(string data)
        {
            var bytes = _utf8Encoding.GetBytes(data);
            CheckAvailability(bytes.Length+4);
            WriteInt(bytes.Length);
            _memoryStream.Write(bytes,0,bytes.Length);
            return this;
        }

        public string ReadUTF8()
        {
            var len = ReadInt();
            var data = new byte[len];
            if (len <= 0 || len > Size - HeaderSize) throw new OverflowException();
            _memoryStream.Read(data, 0, len);
            return _utf8Encoding.GetString(data);
        }

        public MessageBuffer WriteShort(short data)
        {
            CheckAvailability(2);
            _memoryStream.Write(FromShort(data),0,2);
            return this;
        }
        public short ReadShort()
        {
            _memoryStream.Read(_tem2, 0, 2);
            return ToShort(_tem2);
        }

        public MessageBuffer WriteInt(int data)
        {
            CheckAvailability(4);
            _memoryStream.Write(FromInt(data),0,4);
            return this;
        }
        public int ReadInt()
        {
            _memoryStream.Read(_tem4, 0, 4);
            return ToInt(_tem4);
        }
        
        public MessageBuffer WriteFloat(float data)
        {
            CheckAvailability(4);
            _memoryStream.Write(FromFloat(data),0,4);
            return this;
        }
        public float ReadFloat()
        {
            _memoryStream.Read(_tem4, 0, 4);
            return ToFloat(_tem4);
        }

        public void Reset(byte[] data)
        {
            _memoryStream.Position = 0;
            CheckAvailability(data.Length);
            _memoryStream.Write(data,0,data.Length);
            _memoryStream.Position = 0;
            if (!ReadHeader().Encrypted)
            {
                _memoryStream.Position = 0;
                return;
            }
            var decrypted = Decrypt(data,HeaderSize,data.Length-HeaderSize);
            _memoryStream.Position = HeaderSize;
            _memoryStream.Write(decrypted,0,decrypted.Length);
            _memoryStream.Position = 0;
        }

        public void Reset()
        {
            _memoryStream.Position = 0;
        }
        public void Reset(int start)
        {
            _memoryStream.Position = start;
        }

        public byte[] Drain()
        {
            var len = (int)_memoryStream.Position;
            var buffer = new byte[len];
            _memoryStream.Position = 0;
            _memoryStream.Read(buffer, 0, len);
            _memoryStream.Position = 0;
            if (!_encrypted) return buffer;
            var encrypted = Encrypt(buffer,HeaderSize,len-HeaderSize);
            _memoryStream.Position = HeaderSize;
            CheckAvailability(encrypted.Length);
            _memoryStream.Write(encrypted,0,encrypted.Length);
            _memoryStream.Position = 0;
            buffer = new byte[HeaderSize + encrypted.Length];
            _memoryStream.Read(buffer, 0, buffer.Length);
            _memoryStream.Position = 0;
            return buffer;
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
        ~MessageBuffer() => Dispose(false);

        private void CheckAvailability(int size)
        {
            if (_memoryStream.Position + size <= Size) return;
            throw new OverflowException("Max message size is [" + Size + "]");
        }

        private static byte[] FromInt(int data)
        {
            var ret = BitConverter.GetBytes(data);
            if (BitConverter.IsLittleEndian) Array.Reverse(ret);
            return ret;
        }

        private static int ToInt(byte[] data)
        {
            if(BitConverter.IsLittleEndian) Array.Reverse(data);
            return BitConverter.ToInt32(data,0);
        }
        
        private static byte[] FromFloat(float data)
        {
            var ret = BitConverter.GetBytes(data);
            if(BitConverter.IsLittleEndian) Array.Reverse(ret);
            return ret;
        } 
        
        private static float ToFloat(byte[] data)
        {
            if(BitConverter.IsLittleEndian) Array.Reverse(data);
            return BitConverter.ToSingle(data,0);
        }
        
        private static byte[] FromShort(short data)
        {
            var ret = BitConverter.GetBytes(data);
            if (BitConverter.IsLittleEndian) Array.Reverse(ret);
            return ret;
        }
        
        private static short ToShort(byte[] data)
        {
            if(BitConverter.IsLittleEndian) Array.Reverse(data);
            return BitConverter.ToInt16(data,0);
        }
        private byte[] Encrypt(byte[] data,int start,int size)
        {
            var stream = new MemoryStream();
            var cryptStream = new CryptoStream(stream, _cipher.CreateEncryptor(), CryptoStreamMode.Write);
            cryptStream.Write(data,  start,size);
            cryptStream.FlushFinalBlock();
            var ret = stream.ToArray();
            stream.Dispose();
            return ret;
        }
        
        private byte[] Decrypt(byte[] data,int start,int size)
        {
            var stream = new MemoryStream();
            var cryptStream = new CryptoStream(stream, _cipher.CreateDecryptor(), CryptoStreamMode.Write);
            cryptStream.Write(data, start, size);
            cryptStream.FlushFinalBlock();
            var ret = stream.ToArray();
            stream.Dispose();
            return ret;
        }
    }
}