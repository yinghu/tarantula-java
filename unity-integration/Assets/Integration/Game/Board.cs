using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using GameClustering;
using TMPro;
using UnityEngine;

namespace Integration.Game
{
    public class Board : ClusteringObject
    {
        public CameraAdapter cameraAdapter;
        public Camera mainCamera;
        private Player[] _players;
        private int _seat;
        public TMP_Text bText;
        public GameObject[] types;
        private Vector3 _lastPosition;
        private const int FreeMoveTypeId = 4;
        private Dictionary<int,GameObject> _gameObjects;

        private int _outbound;
        private int _inbound;
        private int _timer;
        private async void Start()
        {
            StartClusteringObject(  async buffer =>
            {
                foreach (var gmo in _gameObjects.Values)
                {
                    using (var batchBuffer = new DataBuffer())
                    {
                        var fm = gmo.GetComponent<ClusteringObject>();
                        batchBuffer.PutInt(fm.typeId);
                        batchBuffer.PutInt(fm.sequence);
                        batchBuffer.PutVector3(fm.transform.position);
                        await Messenger.SendAsync(MessageType.OnSync, sequence, true, batchBuffer);
                    }
                }
            }, buffer =>
            {
                var tid = buffer.GetInt();
                var oid = buffer.GetInt();
                var gm = Instantiate(types[tid],buffer.GetVector3(),Quaternion.identity);
                gm.GetComponent<ClusteringObject>().Setup(oid,false);
            });
            _gameObjects = new Dictionary<int, GameObject>();
            _lastPosition = types[FreeMoveTypeId].transform.position;
            Messenger.RegisterMessageHandler(MessageType.Spawn,sequence, (sessionId, data) =>
            {
                MessageContext.Instance.Execute(data, buffer =>
                {
                    var tid = buffer.GetInt();
                    switch (tid)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            //spawn player
                            var pid = buffer.GetInt();
                            var pm = Instantiate(types[tid]);
                            var player = pm.GetComponent<Player>();
                            player.Setup(pid,sessionId==Manager.SessionId);
                            if (player.master)
                            {
                                _players[tid] = player;
                                _gameObjects[pid] = pm;
                            }
                            break;
                        case FreeMoveTypeId:
                            var oid = buffer.GetInt();
                            var pos = buffer.GetVector3();
                            var freeMove = types[tid];
                            pos.y = freeMove.transform.position.y;
                            var fm = Instantiate(freeMove, pos, Quaternion.identity);
                            var fmc = fm.GetComponent<ClusteringObject>();
                            fmc.Setup(oid, sessionId == Manager.SessionId);
                            if (fmc.master)
                            {
                                _gameObjects[oid] = fm;
                            }
                            break;
                    }
                });    
            });
            _seat = Manager.Room.Seat;
            _players = new Player[Manager.Room.Capacity];
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(_seat);
                buffer.PutInt(Messenger.Sequence());
                await Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }
            //if (_seat % 2 == 1)
            //{
                //var pos = mainCamera.transform.rotation;
                //mainCamera.transform.Rotate(pos.x, pos.y, pos.z + 180);
            //}
            bText.text = "SessionId->"+Manager.SessionId;
            _outbound = Messenger.TotalOutbound();
            _inbound = Messenger.TotalInbound();
            _timer = 1;
            InvokeRepeating(nameof(Print), 1.0f, 1.0f);
        }

        private async void Update()
        {
            if (!Input.GetMouseButtonDown(0))
            {
                return;
            }
            var target = Input.mousePosition;
            if (!Physics.Raycast(mainCamera.ScreenPointToRay(new Vector3(target.x, target.y)), out var hit))
            {
                return;
            }
            _lastPosition = hit.point;
            _players[_seat].Move(hit.point);
            cameraAdapter.Adapt(hit.point);
            await OnFreeMove();
        }

        private void Print()
        {
            _timer++;
            var rate1 = Messenger.TotalOutbound() - _outbound;
            _outbound = Messenger.TotalOutbound();
            var rate2 = Messenger.TotalInbound() - _inbound;
            _inbound = Messenger.TotalInbound();
            bText.text = "Retries->" + Messenger.TotalRetries() +
                         "\nPending->" + Messenger.PendingMessages() +
                         "\nOutbound->" + Messenger.TotalOutbound() +
                         "\nInbound->" + Messenger.TotalInbound() +
                         "\nTotal Bytes->" + Messenger.TotalBytes() +
                         "\nOutbound Rate->" + rate1 + 
                         "\nInbound Rate->" + rate2 +
                         "\nTimer->"+_timer;
        }

        private async Task OnFreeMove()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(FreeMoveTypeId);
                buffer.PutInt(Messenger.Sequence());
                buffer.PutVector3(_lastPosition);
                await Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }
        }

        public bool Remove(int oid)
        {
            return _gameObjects.Remove(oid);
        }
    }
}