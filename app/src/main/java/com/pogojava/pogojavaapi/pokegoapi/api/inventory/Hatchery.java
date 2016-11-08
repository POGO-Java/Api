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

package com.pogojava.pogojavaapi.pokegoapi.api.inventory;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;
import com.pogojava.pogojavaapi.pokegoapi.api.pokemon.EggPokemon;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import POGOProtos.Data.PokemonDataOuterClass;

public class Hatchery {
	private final ConcurrentMap<Long, EggPokemon> eggs = new ConcurrentHashMap<>();

	private PokemonGo api;

	public Hatchery(PokemonGo api) {
		this.api = api;
	}

	/**
	 * Add an egg to inventory, if absent, update it if it's already there.
	 *
	 * @param egg data of the eggs
	 */
	public void addEgg(PokemonDataOuterClass.PokemonData egg) {
		EggPokemon current = eggs.putIfAbsent(egg.getId(), new EggPokemon(api, egg));
		if (current != null) {
			current.setProto(egg);
		}
	}
}
