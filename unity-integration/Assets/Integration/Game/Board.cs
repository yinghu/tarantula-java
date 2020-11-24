using GameClustering;
using TMPro;
using UnityEngine;

namespace Integration.Game
{
    public class Board : MonoBehaviour
    {
        public Camera mainCamera;
        public Player[] players;
        private int _seat;
        public TMP_Text bText;
        private void Start()
        {
            
            _seat = IntegrationManager.Instance.Presence.Seat;
            //if (_seat % 2 == 1)
            //{
                //var pos = mainCamera.transform.rotation;
                //mainCamera.transform.Rotate(pos.x, pos.y, pos.z + 180);
            //}
            bText.text = ">>"+IntegrationManager.Instance.SessionId;
        }

        private void Update()
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
            players[_seat].Move(hit.point);
        }
    }
}