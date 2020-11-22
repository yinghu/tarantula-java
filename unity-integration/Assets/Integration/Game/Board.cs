using GameClustering;
using Integration.Move;
using TMPro;
using UnityEngine;

namespace Integration.Game
{
    public class Board : MonoBehaviour
    {
        private Camera _mainCamera;
        public Player[] players;
        private int _seat;
        public TMP_Text bText;
        private void Start()
        {
            _mainCamera = Camera.main;
            _seat = IntegrationManager.Instance.Presence.Seat;
            bText.text = ">>"+IntegrationManager.Instance.SessionId;
        }

        private void Update()
        {
            if (!Input.GetMouseButtonDown(0))
            {
                return;
            }
            var target = Input.mousePosition;
            if (!Physics.Raycast(_mainCamera.ScreenPointToRay(new Vector3(target.x, target.y)), out var hit))
            {
                return;
            }
            players[_seat].Move(hit.point);
        }
    }
}