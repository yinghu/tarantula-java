using System;
using System.Threading.Tasks;
using PlayFab.ClientModels;
using UnityEngine;

public class PersistedData<T>
{
	public bool IsInitialized { get; private set; }
	
	private string playFabId;
	private string dataKey;
	private UserDataPermission permission;
	private T cachedData;
	private T initialData;

	private int changeBlockCounter;
	private bool changeBlockWriteRequested;

	public T Value
	{
		get => cachedData;
		set => cachedData = value;
	}

	public PersistedData(string playFabId, string dataKey, T initialData, UserDataPermission permission)
	{
		this.playFabId = playFabId;
		this.dataKey = dataKey;
		this.IsInitialized = false;
		this.cachedData = initialData;
		this.initialData = initialData;
		this.permission = permission;
	}

	public async Task<T> ReadAsync()
	{
		if (IsInitialized) return cachedData;
		
		var storedData = await PlayFabManager.Instance.GetPlayerData<T>(playFabId, dataKey);
		if (!storedData.HasData)
		{
			await WriteAsync(initialData);
		}
		else
		{
			cachedData = storedData.Data;
		}
		IsInitialized = true;
		return cachedData;
	}

	public async Task WriteAsync()
	{
		await WriteAsync(Value);
	}

	public async Task WriteAsync(T data)
	{
		if (changeBlockCounter > 0)
		{
			changeBlockWriteRequested = true;
			return;
		}
		if (playFabId != AccountInfoManager.Instance.LocalUser.Id)
		{
			Debug.LogWarning($"[PlayFab] Trying to write data {dataKey} for non-local user {playFabId}");
			return;
		}
		cachedData = data;
		//await PlayFabManager.Instance.SetPlayerData(dataKey, cachedData, permission);
	}

	public async Task ResetAsync()
	{
		await WriteAsync(initialData);
	}

	public void ChangeBlock(Action block)
	{
		changeBlockCounter++;
		block.Invoke();
		if (--changeBlockCounter == 0 && changeBlockWriteRequested)
		{
			WriteAsync();
		}
	}
}