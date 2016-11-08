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

package com.pogojava.pogojavaapi.pokegoapi.api.pokemon;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;
import com.pogojava.pogojavaapi.pokegoapi.api.inventory.EggIncubator;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeCallback;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import POGOProtos.Networking.Responses.UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse;

/**
 * The egg pokemon.
 */
public class EggPokemon {

	private static final String TAG = EggPokemon.class.getSimpleName();
	final PokemonGo api;
	private PokemonData proto;

	// API METHODS //

	/**
	 * Incubate this egg.
	 *
	 * @param incubator : the incubator
	 * @param callback  an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<UseItemEggIncubatorResponse> incubate(EggIncubator incubator,
															  PokeCallback<UseItemEggIncubatorResponse> callback) {
		if (incubator.isInUse()) {
			throw new IllegalArgumentException("Incubator already used");
		}
		return incubator.hatchEgg(this, callback);
	}

	/**
	 * Get the current distance that has been done with this egg
	 *
	 * @return get distance already walked
	 */
	public double getEggKmWalked() {
		if (!isIncubate())
			return 0;
		EggIncubator incubator = api.getInventories().getIncubators().get(proto.getEggIncubatorId());
		// incubator should not be null but why not eh
		if (incubator == null)
			return 0;
		else
			return proto.getEggKmWalkedTarget()
					- (incubator.getKmTarget() - api.getPlayerProfile().getStats().getKmWalked());
	}

	/**
	 * Build a EggPokemon wrapper from the proto.
	 *
	 * @param api   : current api
	 * @param proto : the prototype
	 */
	public EggPokemon(PokemonGo api, PokemonData proto) {
		this.api = api;
		if (!proto.getIsEgg()) {
			throw new IllegalArgumentException("You cant build a EggPokemon without a valid PokemonData.");
		}
		this.proto = proto;
	}

	public long getId() {
		return proto.getId();
	}

	public double getEggKmWalkedTarget() {
		return proto.getEggKmWalkedTarget();
	}

	public long getCapturedCellId() {
		return proto.getCapturedCellId();
	}

	public long getCreationTimeMs() {
		return proto.getCreationTimeMs();
	}

	public String getEggIncubatorId() {
		return proto.getEggIncubatorId();
	}

	public boolean isIncubate() {
		return proto.getEggIncubatorId().length() > 0;
	}

	@Override
	public int hashCode() {
		return proto.getPokemonId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EggPokemon) {
			EggPokemon other = (EggPokemon) obj;
			return (this.getId() == other.getId());
		}

		return false;
	}

	public void setProto(PokemonData egg) {
		proto = egg;
	}
}
