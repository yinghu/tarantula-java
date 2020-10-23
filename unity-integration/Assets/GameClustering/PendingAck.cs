using System.Collections.Generic;

namespace GameClustering
{
    public class PendingAck
    {
        private readonly int[] _buffer;

        private  int _header;
        private  int _tail;
        private  readonly int _overflow;
        
        public PendingAck(int size){
            _buffer =  new int[size];
            _header = 0;
            _tail = 0;
            _overflow = size;
            for (var i = 0; i < size; i++)
            {
                _buffer[i] = 0;
            }
        }
        
        public  void Push(int t){
            if (_tail < _overflow)
            {
                _buffer[_tail++] = t;
            }
            else
            {
                //overflow
                if (_header > 0)
                {
                    //remove segment
                    _tail = _overflow - _header;
                    for (var i = 0; i < _tail; i++)
                    {
                        _buffer[i] = _buffer[_header++];
                    }
                    _header = 0;
                    _buffer[_tail++] = t;
                }
                else
                {
                    //remove first
                    for (var i = 0; i < _overflow - 1; i++)
                    {
                        _buffer[i] = _buffer[i + 1];
                    }
                    _buffer[_overflow - 1] = t;
                }
            }
        }
        
        public List<int> List(){
            var list = new List<int>();
            for(var i = 0; i < _overflow; i++){
                if(_buffer[i] != 0){
                    list.Add(_buffer[i]);
                }
            }
            return list;
        }
    }
}