using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
using BeardedManStudios;
using System.Collections.Generic;
using System;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class ForgeMenu : MonoBehaviour
{
	public bool asServer;
	public GameObject networkManager = null;
	
    private NetworkManager mgr = null;
	private NetWorker server;
    private UDPClient client;
   
	private void Start(){
		Rpc.MainThreadRunner = MainThreadManager.Instance; 
	}
	public void Connect(string ipAddress,ushort portNumber){
        client = new UDPClient();
        client.Connect(ipAddress,portNumber);
        client.serverAccepted += (sender) =>{
			Debug.Log("accepted from server");
		};
        client.connectAttemptFailed += (sender) =>{
			Debug.Log("failed from server");
		};
		Connected(client);
	}
	public void Host(string ipAddress,ushort portNumber){	
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
    public void Disconnect(){
        if(asServer){
            server.Disconnect(true);    
        }
        else{
            client.Disconnect(true);
        }
    }
	private void Connected(NetWorker networker){
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
    }   
    void OnApplicationQuit(){
        Debug.Log("server closed");
		if (server != null) server.Disconnect(true);
	}
}
