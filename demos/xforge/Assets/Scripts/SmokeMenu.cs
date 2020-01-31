using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
using BeardedManStudios;
using System.Collections.Generic;
using System;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class SmokeMenu : MonoBehaviour
{
	public string ipAddress;
	public ushort portNumber;
    public bool asServer;
	public GameObject networkManager = null;
	
    private NetworkManager mgr = null;
	private NetWorker server;

	private void Start(){
		ipAddress = "10.0.0.234";
		portNumber = 15937;
        Rpc.MainThreadRunner = MainThreadManager.Instance; 
        if(asServer){
            Host();
        }else{
            Connect();
        }
	}
	public void Connect(){
		UDPClient client = new UDPClient();
        client.Connect(ipAddress,portNumber);
        client.serverAccepted += (sender) =>{
			Debug.Log("accepted from server");
		};
        client.connectAttemptFailed += (sender) =>{
			Debug.Log("failed from server");
		};
		Connected(client);
	}
	public void Host(){	
        server = new UDPServer(64);			
        ((UDPServer)server).Connect(ipAddress,portNumber);
		server.playerAccepted += (player, sender) =>{
			Debug.Log("Player " + player.NetworkId + " Accepted");
		};
        server.playerConnected += (player, sender) =>{
			Debug.Log("Player " + player.NetworkId + " connected");
		};
        server.playerAuthenticated += (player, sender) =>{
			Debug.Log("Player " + player.NetworkId + " Authenticated");
		};
        server.playerDisconnected += (player, sender) =>{
			Debug.Log("Player " + player.NetworkId + " disconnected");
		};
		Connected(server);
	}
	public void Connected(NetWorker networker){
		if (!networker.IsBound){
			Debug.LogError("NetWorker failed to bind");
			return;
		}
		if (mgr == null && networkManager == null){
			Debug.LogWarning("A network manager was not provided, generating a new one instead");
			networkManager = new GameObject("Network Manager");
			mgr = networkManager.AddComponent<NetworkManager>();
		}
		else if (mgr == null){
			mgr = Instantiate(networkManager).GetComponent<NetworkManager>();
        }
        Debug.Log("IS SERVER->"+networker.IsServer);
		mgr.Initialize(networker);
		if (networker is IServer){
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex + 1);
		}
	}
    void OnApplicationQuit(){
        Debug.Log("server closed");
		if (server != null) server.Disconnect(true);
	}
}
