/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pogojava.pogojavaapi.pokegoapi.api.player;

import android.util.Log;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;
import com.pogojava.pogojavaapi.pokegoapi.api.inventory.Item;
import com.pogojava.pogojavaapi.pokegoapi.api.inventory.ItemBag;
import com.pogojava.pogojavaapi.pokegoapi.api.inventory.Stats;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.InvalidCurrencyException;
import com.pogojava.pogojavaapi.pokegoapi.main.AsyncServerRequest;
import com.pogojava.pogojavaapi.pokegoapi.main.CommonRequest;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeAFunc;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeCallback;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import POGOProtos.Data.Player.CurrencyOuterClass;
import POGOProtos.Data.Player.EquippedBadgeOuterClass.EquippedBadge;
import POGOProtos.Data.Player.PlayerAvatarOuterClass;
import POGOProtos.Data.Player.PlayerStatsOuterClass;
import POGOProtos.Data.PlayerDataOuterClass.PlayerData;
import POGOProtos.Enums.GenderOuterClass.Gender;
import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Enums.TutorialStateOuterClass;
import POGOProtos.Inventory.Item.ItemAwardOuterClass.ItemAward;
import POGOProtos.Networking.Requests.Messages.ClaimCodenameMessageOuterClass.ClaimCodenameMessage;
import POGOProtos.Networking.Requests.Messages.EncounterTutorialCompleteMessageOuterClass.EncounterTutorialCompleteMessage;
import POGOProtos.Networking.Requests.Messages.GetPlayerMessageOuterClass.GetPlayerMessage;
import POGOProtos.Networking.Requests.Messages.LevelUpRewardsMessageOuterClass.LevelUpRewardsMessage;
import POGOProtos.Networking.Requests.Messages.MarkTutorialCompleteMessageOuterClass.MarkTutorialCompleteMessage;
import POGOProtos.Networking.Requests.Messages.SetAvatarMessageOuterClass.SetAvatarMessage;
import POGOProtos.Networking.Requests.Messages.VerifyChallenge;
import POGOProtos.Networking.Requests.RequestTypeOuterClass.RequestType;
import POGOProtos.Networking.Responses.ClaimCodenameResponseOuterClass.ClaimCodenameResponse;
import POGOProtos.Networking.Responses.EncounterTutorialCompleteResponseOuterClass.EncounterTutorialCompleteResponse;
import POGOProtos.Networking.Responses.GetPlayerResponseOuterClass.GetPlayerResponse;
import POGOProtos.Networking.Responses.LevelUpRewardsResponseOuterClass.LevelUpRewardsResponse;
import POGOProtos.Networking.Responses.MarkTutorialCompleteResponseOuterClass.MarkTutorialCompleteResponse;
import POGOProtos.Networking.Responses.SetAvatarResponseOuterClass.SetAvatarResponse;
import POGOProtos.Networking.Responses.VerifyChallengeResponseOuterClass;

public class PlayerProfile {
	private static final String TAG = PlayerProfile.class.getSimpleName();
	private final PokemonGo api;
	private final PlayerLocale playerLocale;
	private PlayerData playerData;
	private EquippedBadge badge;
	private PlayerAvatar avatar;
	private DailyBonus dailyBonus;
	private ContactSettings contactSettings;
	private Map<Currency, Integer> currencies = new EnumMap<>(Currency.class);
	private Stats stats;
	private TutorialState tutorialState;

	/**
	 * @param api the api
	 */
	public PlayerProfile(PokemonGo api) {
		this.api = api;
		this.playerLocale = new PlayerLocale();

		if (playerData == null) {
			initProfile();
			initProfile();
		}
	}

	/**
	 * Init the player profile.
	 */
	private void initProfile() {
		GetPlayerMessage getPlayerReqMsg = GetPlayerMessage.newBuilder()
				.setPlayerLocale(playerLocale.getPlayerLocale())
				.build();

		new AsyncServerRequest(RequestType.GET_PLAYER, getPlayerReqMsg,
				new PokeAFunc<GetPlayerResponse, Void>() {
					@Override
					public Void exec(GetPlayerResponse response) {
						parseData(response.getPlayerData());

						ArrayList<TutorialStateOuterClass.TutorialState> tutorialStates =
								getTutorialState().getTutorialStates();
						if (tutorialStates.isEmpty()) {
							activateAccount();
							return null;
						}

						if (!tutorialStates.contains(TutorialStateOuterClass.TutorialState.AVATAR_SELECTION)) {
							setupAvatar();
							return null;
						}

						if (!tutorialStates.contains(TutorialStateOuterClass.TutorialState.POKEMON_CAPTURE)) {
							encounterTutorialComplete();
							return null;
						}

						if (!tutorialStates.contains(TutorialStateOuterClass.TutorialState.NAME_SELECTION)) {
							claimCodeName();
							return null;
						}

						if (!tutorialStates.contains(
								TutorialStateOuterClass.TutorialState.FIRST_TIME_EXPERIENCE_COMPLETE)) {
							firstTimeExperienceComplete();
						}
						return null;
					}
				}, null, api, null);
	}

	/**
	 * Update profile
	 *
	 * @param callback an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<PlayerProfile> updateProfile(PokeCallback<PlayerProfile> callback) {
		GetPlayerMessage getPlayerReqMsg = GetPlayerMessage.newBuilder()
				.setPlayerLocale(playerLocale.getPlayerLocale())
				.build();

		new AsyncServerRequest(RequestType.GET_PLAYER, getPlayerReqMsg,
				new PokeAFunc<GetPlayerResponse, PlayerProfile>() {
					@Override
					public PlayerProfile exec(GetPlayerResponse response) {
						parseData(response.getPlayerData());
						return PlayerProfile.this;
					}
				}, callback, api);
		return callback;
	}

	/**
	 * Update the profile with the given player data
	 *
	 * @param playerData the data for update
	 */
	private void parseData(PlayerData playerData) {
		this.playerData = playerData;

		avatar = new PlayerAvatar(playerData.getAvatar());
		dailyBonus = new DailyBonus(playerData.getDailyBonus());
		contactSettings = new ContactSettings(playerData.getContactSettings());

		// maybe something more graceful?
		for (CurrencyOuterClass.Currency currency : playerData.getCurrenciesList()) {
			try {
				addCurrency(currency.getName(), currency.getAmount());
			} catch (InvalidCurrencyException e) {
				Log.w(TAG, "Error adding currency. You can probably ignore this.", e);
			}
		}

		// Tutorial state
		tutorialState = new TutorialState(playerData.getTutorialStateList());
	}

	public PokeCallback<Boolean> sendChallenge(String token, final PokeCallback<Boolean> callback) {
		VerifyChallenge.VerifyChallengeMessage verifyChallengeMessage = VerifyChallenge.VerifyChallengeMessage.newBuilder()
				.setToken(token)
				.build();

		new AsyncServerRequest(RequestType.VERIFY_CHALLENGE, verifyChallengeMessage,
						new PokeAFunc<VerifyChallengeResponseOuterClass.VerifyChallengeResponse, Boolean>() {
							@Override
							public Boolean exec(VerifyChallengeResponseOuterClass.VerifyChallengeResponse response) {
								return response.getSuccess();
							}
						}, callback, api);
		return callback;
	}

	/**
	 * Accept the rewards granted and the items unlocked by gaining a trainer level up. Rewards are retained by the
	 * server until a player actively accepts them.
	 * The rewarded items are automatically inserted into the players item bag.
	 *
	 * @param level    the trainer level that you want to accept the rewards for
	 * @param callback an optional callback to handle results
	 * @see PlayerLevelUpRewards
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<PlayerLevelUpRewards> acceptLevelUpRewards(int level,
			PokeCallback<PlayerLevelUpRewards> callback) {
		// Check if we even have achieved this level yet
		if (level > stats.getLevel()) {
			callback.fire(new PlayerLevelUpRewards(PlayerLevelUpRewards.Status.NOT_UNLOCKED_YET));
			return callback;
		}

		LevelUpRewardsMessage msg = LevelUpRewardsMessage.newBuilder()
				.setLevel(level)
				.build();
		new AsyncServerRequest(RequestType.LEVEL_UP_REWARDS, msg,
				new PokeAFunc<LevelUpRewardsResponse, PlayerLevelUpRewards>() {
					@Override
					public PlayerLevelUpRewards exec(LevelUpRewardsResponse response) {
						// Add the awarded items to our bag
						ItemBag bag = api.getInventories().getItemBag();
						for (ItemAward itemAward : response.getItemsAwardedList()) {
							Item item = bag.getItem(itemAward.getItemId());
							item.setCount(item.getCount() + itemAward.getItemCount());
						}

						// Build a new rewards object and return it
						return new PlayerLevelUpRewards(response);
					}
				}, callback, api);
		return callback;
	}

	/**
	 * Add currency.
	 *
	 * @param name   the name
	 * @param amount the amount
	 * @throws InvalidCurrencyException the invalid currency exception
	 */
	public void addCurrency(String name, int amount) throws InvalidCurrencyException {
		try {
			currencies.put(Currency.valueOf(name), amount);
		} catch (Exception e) {
			throw new InvalidCurrencyException();
		}
	}

	/**
	 * Gets currency.
	 *
	 * @param currency the currency
	 * @return the currency
	 */
	public int getCurrency(Currency currency) {
		return currencies.get(currency);
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}

	public enum Currency {
		STARDUST, POKECOIN;
	}

	/**
	 * Gets raw player data proto
	 *
	 * @return Player data
	 */
	public PlayerData getPlayerData() {
		return playerData;
	}

	/**
	 * Gets avatar
	 *
	 * @return Player Avatar object
	 */
	public PlayerAvatar getAvatar() {
		return avatar;
	}

	/**
	 * Gets daily bonus
	 *
	 * @return DailyBonus object
	 */
	public DailyBonus getDailyBonus() {
		return dailyBonus;
	}

	/**
	 * Gets contact settings
	 *
	 * @return ContactSettings object
	 */
	public ContactSettings getContactSettings() {
		return contactSettings;
	}

	/**
	 * Gets a map of all currencies
	 *
	 * @return map of currencies
	 */
	public Map<Currency, Integer> getCurrencies() {
		return currencies;
	}

	/**
	 * Gets player stats
	 *
	 * @return stats API objet
	 */
	public Stats getStats() {
		if (stats == null) {
			return new Stats(PlayerStatsOuterClass.PlayerStats.newBuilder().build());
		}
		return stats;
	}

	/**
	 * Gets tutorial states
	 *
	 * @return TutorialState object
	 */
	public TutorialState getTutorialState() {
		return tutorialState;
	}

	/**
	 * Internal call for account activation
	 */
	private void activateAccount() {
		activateAccount(new PokeCallback<PlayerProfile>() {
			@Override
			public void onResponse(PlayerProfile result) {
				setupAvatar();
			}
		});
	}

	/**
	 * Set the account to legal screen in order to receive valid response
	 *
	 * @param callback an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<PlayerProfile> activateAccount(PokeCallback<PlayerProfile> callback) {
		return markTutorial(TutorialStateOuterClass.TutorialState.LEGAL_SCREEN, callback);
	}

	/**
	 * Internal call for avatar setup
	 */
	private void setupAvatar() {
		setupAvatar(new PokeCallback<SetAvatarResponse.Status>() {
			@Override
			public void onResponse(SetAvatarResponse.Status result) {
				encounterTutorialComplete();
			}
		});
	}

	/**
	 * Setup an avatar for the current account
	 *
	 * @param callback an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<SetAvatarResponse.Status> setupAvatar(PokeCallback<SetAvatarResponse.Status> callback) {
		Random random = new Random();

		final PlayerAvatarOuterClass.PlayerAvatar.Builder playerAvatarBuilder =
				PlayerAvatarOuterClass.PlayerAvatar.newBuilder();
		final boolean female = random.nextInt(100) % 2 == 0;
		if (female) {
			playerAvatarBuilder.setGender(Gender.FEMALE);
		}

		playerAvatarBuilder.setSkin(random.nextInt(PlayerAvatar.getAvailableSkins()))
				.setHair(random.nextInt(PlayerAvatar.getAvailableHair()))
				.setEyes(random.nextInt(PlayerAvatar.getAvailableEyes()))
				.setHat(random.nextInt(PlayerAvatar.getAvailableHats()))
				.setShirt(random.nextInt(PlayerAvatar.getAvailableShirts(female ? Gender.FEMALE : Gender.MALE)))
				.setPants(random.nextInt(PlayerAvatar.getAvailablePants(female ? Gender.FEMALE : Gender.MALE)))
				.setShoes(random.nextInt(PlayerAvatar.getAvailableShoes()))
				.setBackpack(random.nextInt(PlayerAvatar.getAvailableShoes()));

		final SetAvatarMessage setAvatarMessage = SetAvatarMessage.newBuilder()
				.setPlayerAvatar(playerAvatarBuilder.build())
				.build();

		new AsyncServerRequest(RequestType.SET_AVATAR, setAvatarMessage,
				new PokeAFunc<SetAvatarResponse, SetAvatarResponse.Status>() {
					@Override
					public SetAvatarResponse.Status exec(SetAvatarResponse response) {
						parseData(response.getPlayerData());

						if (response.getStatus() == SetAvatarResponse.Status.SUCCESS) {
							markTutorial(TutorialStateOuterClass.TutorialState.AVATAR_SELECTION, null);
						}

						new AsyncServerRequest(RequestType.GET_ASSET_DIGEST,
								CommonRequest.getDefaultGetAssetDigestMessageRequest(), null, null, api);

						return response.getStatus();
					}
				}, callback, api);
		return callback;
	}

	/**
	 * Internal call to encounter tutorial complete
	 */
	private void encounterTutorialComplete() {
		encounterTutorialComplete(new PokeCallback<EncounterTutorialCompleteResponse.Result>() {
			@Override
			public void onResponse(EncounterTutorialCompleteResponse.Result result) {
				claimCodeName();
			}
		});
	}

	/**
	 * Encounter tutorial complete. In other words, catch the first Pokémon
	 *
	 * @param callback an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<EncounterTutorialCompleteResponse.Result> encounterTutorialComplete(
			PokeCallback<EncounterTutorialCompleteResponse.Result> callback) {
		Random random = new Random();
		int pokemonId = random.nextInt(4);

		final EncounterTutorialCompleteMessage.Builder encounterTutorialCompleteBuilder =
				EncounterTutorialCompleteMessage.newBuilder()
						.setPokemonId(pokemonId == 1 ? PokemonId.BULBASAUR :
								pokemonId == 2 ? PokemonId.CHARMANDER : PokemonId.SQUIRTLE);

		new AsyncServerRequest(
				RequestType.ENCOUNTER_TUTORIAL_COMPLETE, encounterTutorialCompleteBuilder.build(),
				new PokeAFunc<EncounterTutorialCompleteResponse, EncounterTutorialCompleteResponse.Result>() {
					@Override
					public EncounterTutorialCompleteResponse.Result exec(EncounterTutorialCompleteResponse response) {
						if (response.getResult() == EncounterTutorialCompleteResponse.Result.SUCCESS) {
							updateProfile(null);
						}

						return response.getResult();
					}
				}, callback, api);
		return callback;
	}

	/**
	 * Internal call to claim a codename
	 */
	private void claimCodeName() {
		claimCodeName(new PokeCallback<ClaimCodenameResponse.Status>() {
			@Override
			public void onResponse(ClaimCodenameResponse.Status result) {
				firstTimeExperienceComplete();
			}
		});
	}

	/**
	 * Setup an user name for our account
	 *
	 * @param callback an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<ClaimCodenameResponse.Status> claimCodeName(
			final PokeCallback<ClaimCodenameResponse.Status> callback) {
		ClaimCodenameMessage claimCodenameMessage = ClaimCodenameMessage.newBuilder()
				.setCodename(randomCodenameGenerator())
				.build();

		new AsyncServerRequest(
				RequestType.CLAIM_CODENAME, claimCodenameMessage,
				new PokeAFunc<ClaimCodenameResponse, ClaimCodenameResponse.Status>() {
					@Override
					public ClaimCodenameResponse.Status exec(ClaimCodenameResponse response) {
						String updatedCodename = null;
						if (response.getStatus() != ClaimCodenameResponse.Status.SUCCESS) {
							if (response.getUpdatedPlayer().getRemainingCodenameClaims() > 0) {
								claimCodeName(callback);
							}
						} else {
							updatedCodename = response.getCodename();
							parseData(response.getUpdatedPlayer());
						}

						if (updatedCodename != null) {
							markTutorial(TutorialStateOuterClass.TutorialState.NAME_SELECTION, null);
						}
						return response.getStatus();
					}
				}, callback, api);

		updateProfile(null);
		return callback;
	}

	/**
	 * Internal call to mark the first time experience complete
	 */
	private void firstTimeExperienceComplete() {
		firstTimeExperienceComplete(null);
	}

	/**
	 * The last step, mark the last tutorial state as completed
	 *
	 * @param callback an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<PlayerProfile> firstTimeExperienceComplete(PokeCallback<PlayerProfile> callback) {
		markTutorial(TutorialStateOuterClass.TutorialState.FIRST_TIME_EXPERIENCE_COMPLETE, callback);
		return callback;
	}

	/**
	 * Mark the tutorial state as complete
	 *
	 * @param state    the tutorial state
	 * @param callback an optional callback to handle results
	 */
	private PokeCallback<PlayerProfile> markTutorial(TutorialStateOuterClass.TutorialState state,
													 PokeCallback<PlayerProfile> callback) {
		final MarkTutorialCompleteMessage tutorialMessage = MarkTutorialCompleteMessage.newBuilder()
				.addTutorialsCompleted(state)
				.setSendMarketingEmails(false)
				.setSendPushNotifications(false).build();

		new AsyncServerRequest(RequestType.MARK_TUTORIAL_COMPLETE, tutorialMessage,
				new PokeAFunc<MarkTutorialCompleteResponse, PlayerProfile>() {
					@Override
					public PlayerProfile exec(MarkTutorialCompleteResponse response) {
						parseData(response.getPlayerData());
						return PlayerProfile.this;
					}
				}, callback, api);
		return callback;
	}

	private static String randomCodenameGenerator() {
		final String a = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		final SecureRandom r = new SecureRandom();
		final int l = new Random().nextInt(15 - 10) + 10;
		StringBuilder sb = new StringBuilder(l);
		for (int i = 0; i < l; i++) {
			sb.append(a.charAt(r.nextInt(a.length())));
		}
		return sb.toString();
	}
}