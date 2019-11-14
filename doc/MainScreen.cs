using System;
using System.Collections;
using UnityEngine;

public class MainScreen : BaseMainMenuScreen
{
	public MainMenuNew MainMenu;
	public Transform SettingsRoot;
	public TutorialUI TutorialUI;
	public MatchmakingScreen MatchmakingScreen;

	private async void OnEnable()
	{
		SettingsRoot.gameObject.SetActive(false);
		TutorialUI.gameObject.SetActive(false);
		SetHeaderVisibilityDelegate?.Invoke(true);
		MatchmakingScreen.gameObject.SetActive(false);

		LoadingManager.Instance.Show();
		var localPlayerData = await PlayerDataManager.Instance.LoadLocalPlayerDataAsync();
		await ChestManager.Instance.InitializePlayerDataAsync();
		await Migration.MigrateDataAsync(localPlayerData);
		await TaskUtility.WaitUntil(() => !NetworkManager.Instance.InRoom || PlayFabManager.Instance.IsOffline);
		LoadingManager.Instance.Hide();
        //await GecNetworkManager.Instance.Index();
	}

	public void OnClickBattle()
	{
		StartCoroutine(ShowTutorialCoroutine(async () =>
		{
			if (NetworkManager.Instance.IsOffline)
			{
				LoadingManager.Instance.Show();
				await NetworkManager.Instance.StartOfflineMode();
				LoadingManager.Instance.Hide();
				AppManager.Instance.GotoGameplay();
			}
			else
			{
				MatchmakingScreen.Show();
			}
		}));
	}

	public void OnClickToggleSettings()
	{
		SettingsRoot.gameObject.SetActive(!SettingsRoot.gameObject.activeSelf);
	}

	public void OnClickLogout()
	{
		PlayFabManager.Instance.Logout();
		SettingsRoot.gameObject.SetActive(false);
		MainMenu.ShowWelcomeContent();
	}

	private IEnumerator ShowTutorialCoroutine(Action callback)
	{
		if (DebugOptions.Instance.GetOption<bool>(DebugOptions.Key.ShowTutorial))
		{
			TutorialUI.gameObject.SetActive(true);
			yield return TutorialUI.ShowAsync();
			TutorialUI.gameObject.SetActive(false);
		}

		callback?.Invoke();
	}
}