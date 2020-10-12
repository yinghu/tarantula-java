using System;

namespace GameClustering
{
    public class CallbackKey : IEquatable<CallbackKey>
    {
        private readonly int _type;
        private readonly int _sequence;
        public CallbackKey(int type,int sequence)
        {
            _type = type;
            _sequence = sequence;
        }

        public override int GetHashCode() {
            return _type+_sequence;
        }
        public override bool Equals(object obj) {
            return Equals(obj as CallbackKey);
        }
        public bool Equals(CallbackKey obj) {
            return obj != null && obj._type == _type && obj._sequence == _sequence;
        }
    }
}