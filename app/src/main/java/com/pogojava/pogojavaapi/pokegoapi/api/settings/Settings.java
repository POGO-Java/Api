package com.pogojava.pogojavaapi.pokegoapi.api.settings;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;

import POGOProtos.Networking.Responses.DownloadSettingsResponseOuterClass.DownloadSettingsResponse;

/**
 * Created by rama on 27/07/16.
 */
public class Settings {

	private final PokemonGo api;

	/**
	 * Settings for various parameters on map
	 *
	 * @return MapSettings instance.
	 */
	private final MapSettings mapSettings;

	/**
	 * Settings for various parameters during levelup
	 *
	 * @return LevelUpSettings instance.
	 */
	private final LevelUpSettings levelUpSettings;

	/**
	 * Settings for various parameters during levelup
	 *
	 * @return LevelUpSettings instance.
	 */
	private final FortSettings fortSettings;

	/**
	 * Settings for various parameters during levelup
	 *
	 * @return LevelUpSettings instance.
	 */
	private final InventorySettings inventorySettings;

	/**
	 * Settings for showing speed warnings
	 *
	 * @return GpsSettings instance.
	 */
	private final GpsSettings gpsSettings;

	/**
     * Settings for hash
     *
     * @return String hash.
     */
	private String hash;

	/**
	 * Settings object that hold different configuration aspect of the game.
	 * Can be used to simulate the real app behaviour.
	 *
	 * @param api api instance
	 */
	public Settings(PokemonGo api) {
		this.api = api;
		this.mapSettings = new MapSettings();
		this.levelUpSettings = new LevelUpSettings();
		this.fortSettings = new FortSettings();
		this.inventorySettings = new InventorySettings();
		this.gpsSettings = new GpsSettings();
		this.hash = "";
	}

	/**
	 * Updates settings latest data.
	 *
	 * @param response the settings download response
	 */
	public void updateSettings(DownloadSettingsResponse response) {
		if (response.getSettings().hasMapSettings()) {
			mapSettings.update(response.getSettings().getMapSettings());
		}
		if (response.getSettings().hasLevelSettings()) {
			levelUpSettings.update(response.getSettings().getInventorySettings());
		}
		if (response.getSettings().hasFortSettings()) {
			fortSettings.update(response.getSettings().getFortSettings());
		}
		if (response.getSettings().hasInventorySettings()) {
			inventorySettings.update(response.getSettings().getInventorySettings());
		}
		if (response.getSettings().hasGpsSettings()) {
			gpsSettings.update(response.getSettings().getGpsSettings());
		}
		this.hash = response.getHash();
	}

	public String getHash() {
		return hash;
	}

	public MapSettings getMapSettings() {
		return mapSettings;
	}

	public FortSettings getFortSettings() {
		return fortSettings;
	}
}
