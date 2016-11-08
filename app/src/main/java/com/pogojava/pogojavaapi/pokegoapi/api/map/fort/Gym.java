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

package com.pogojava.pogojavaapi.pokegoapi.api.map.fort;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;
import com.pogojava.pogojavaapi.pokegoapi.api.gym.Battle;
import com.pogojava.pogojavaapi.pokegoapi.api.pokemon.Pokemon;
import com.pogojava.pogojavaapi.pokegoapi.main.AsyncServerRequest;
import com.pogojava.pogojavaapi.pokegoapi.main.CommonRequest;
import com.pogojava.pogojavaapi.pokegoapi.util.Constant;
import com.pogojava.pogojavaapi.pokegoapi.util.MapPoint;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeAFunc;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeCallback;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Enums.TeamColorOuterClass;
import POGOProtos.Map.Fort.FortDataOuterClass.FortData;
import POGOProtos.Networking.Requests.Messages.GetGymDetailsMessageOuterClass.GetGymDetailsMessage;
import POGOProtos.Networking.Requests.RequestTypeOuterClass.RequestType;
import POGOProtos.Networking.Responses.FortDeployPokemonResponseOuterClass.FortDeployPokemonResponse;
import POGOProtos.Networking.Responses.GetGymDetailsResponseOuterClass.GetGymDetailsResponse;

public class Gym implements MapPoint {
	private FortData proto;
	private GymDetails details;
	private PokemonGo api;

	private long cachedGymUpdate = 0;

	/**
	 * Gym object.
	 *
	 * @param api   The api object to use for requests.
	 * @param proto The FortData to populate the Gym with.
	 */
	public Gym(PokemonGo api, FortData proto) {
		this.api = api;
		this.proto = proto;
		this.details = null;
	}

	public String getId() {
		return proto.getId();
	}

	public double getLatitude() {
		return proto.getLatitude();
	}

	public double getLongitude() {
		return proto.getLongitude();
	}

	public boolean getEnabled() {
		return proto.getEnabled();
	}

	public TeamColorOuterClass.TeamColor getOwnedByTeam() {
		return proto.getOwnedByTeam();
	}

	public PokemonIdOuterClass.PokemonId getGuardPokemonId() {
		return proto.getGuardPokemonId();
	}

	public int getGuardPokemonCp() {
		return proto.getGuardPokemonCp();
	}

	public long getPoints() {
		return proto.getGymPoints();
	}

	public boolean getIsInBattle() {
		return proto.getIsInBattle();
	}

	public Battle battle(Pokemon[] team) {
		return new Battle(api, team, this);
	}

	public void getDetails(PokeCallback<GymDetails> callback) {
		if (api.currentTimeMillis() - cachedGymUpdate < 60 * 1000
				&& details != null) {
				callback.onResponse(details);
			return;
		}

		cachedGymUpdate = api.currentTimeMillis();

		GetGymDetailsMessage getGymDetailsMessage = GetGymDetailsMessage
				.newBuilder()
				.setGymId(this.getId())
				.setGymLatitude(this.getLatitude())
				.setGymLongitude(this.getLongitude())
				.setPlayerLatitude(api.getLatitude())
				.setPlayerLongitude(api.getLongitude())
				.setClientVersion(Constant.APP_VERSION_STRING)
				.build();

		new AsyncServerRequest(RequestType.GET_GYM_DETAILS, getGymDetailsMessage,
				new PokeAFunc<GetGymDetailsResponse, GymDetails>() {
					@Override
					public GymDetails exec(GetGymDetailsResponse response) {
						details = new GymDetails(response);
						proto = response.getGymState().getFortData();
						return details;
					}
				}, callback, api, CommonRequest.getDefaultCheckChallenge());
	}

	/**
	 * Deploy pokemon
	 *
	 * @param pokemon The pokemon to deploy
	 * @return Result of attempt to deploy pokemon
	 */
	public FortDeployPokemonResponse.Result deployPokemon(Pokemon pokemon) {
		/*FortDeployPokemonMessage reqMsg = FortDeployPokemonMessage.newBuilder()
				.setFortId(getId())
				.setPlayerLatitude(api.getLatitude())
				.setPlayerLongitude(api.getLongitude())
				.setPokemonId(pokemon.getId())
				.build();

		ServerRequest serverRequest = new ServerRequest(RequestType.FORT_DEPLOY_POKEMON, reqMsg);
		api.getRequestHandler().sendServerRequests(serverRequest);

		try {
			return FortDeployPokemonResponse.parseFrom(serverRequest.getData()).getResult();
		} catch (InvalidProtocolBufferException e) {
			throw new RemoteServerException();
		}*/
		return null;

	}

	protected PokemonGo getApi() {
		return api;
	}

}
