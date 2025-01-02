package com.areaSoundsWhitelist;

import com.google.inject.Provides;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.client.callback.ClientThread;


import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Area Sounds Whitelist"
)
public class AreaSoundsWhitelistPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private AreaSoundsWhitelistConfig config;

	private Set<Integer> soundsToWhitelist;

	@Override
	protected void startUp() throws Exception
	{
		soundsToWhitelist = new HashSet<>();
		soundsToWhitelist.clear();

		String[] ids = config.whitelist().split(",");
		for (int i = 0; i < ids.length; i++) {
			if (ids[i].equals("")) {
				continue;
			}
			soundsToWhitelist.add(Integer.parseInt(ids[i]));
		}
	}

	@Override
	public void shutDown()
	{
		soundsToWhitelist.clear();
	}

	@Subscribe(priority = -2) // priority -2 to run after music plugin
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();

		// on map load mute ambient sounds
		if (gameState == GameState.LOGGED_IN)
		{
			if (config.muteAmbient()) {
				client.getAmbientSoundEffects().clear();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		soundsToWhitelist.clear();
		String[] ids = config.whitelist().split(",");
		for (int i = 0; i < ids.length; i++) {
			soundsToWhitelist.add(Integer.parseInt(ids[i]));
		}
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed areaSoundEffectPlayed) {
		int soundId = areaSoundEffectPlayed.getSoundId();
		if (!soundsToWhitelist.contains(soundId)) {
			areaSoundEffectPlayed.consume();
		}

	}

	@Provides
	AreaSoundsWhitelistConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AreaSoundsWhitelistConfig.class);
	}
}
