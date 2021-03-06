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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Inventory.EggIncubatorOuterClass;
import POGOProtos.Inventory.InventoryItemDataOuterClass;
import POGOProtos.Inventory.InventoryItemOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import POGOProtos.Networking.Responses.GetInventoryResponseOuterClass.GetInventoryResponse;

public class Inventories {

	private final PokemonGo api;
	private ItemBag itemBag;
	private PokeBank pokebank;
	private CandyJar candyjar;
	private Pokedex pokedex;
	private final ConcurrentMap<String, EggIncubator> incubators = new ConcurrentHashMap<>();
	private Hatchery hatchery;
	private long lastInventoryUpdate = 0;

	/**
	 * Creates Inventories and initializes content.
	 *
	 * @param api PokemonGo api
	 */
	public Inventories(PokemonGo api) {
		this.api = api;
		itemBag = new ItemBag(api);
		pokebank = new PokeBank();
		candyjar = new CandyJar(api);
		pokedex = new Pokedex();
		hatchery = new Hatchery(api);
	}

	/**
	 * Updates the inventories with the latest data.
	 *
	 * @param response the get inventory response
	 */
	public void updateInventories(GetInventoryResponse response) {
		for (InventoryItemOuterClass.InventoryItem inventoryItem
				: response.getInventoryDelta().getInventoryItemsList()) {
			InventoryItemDataOuterClass.InventoryItemData itemData = inventoryItem.getInventoryItemData();

			// hatchery
			if (itemData.getPokemonData().getPokemonId() == PokemonId.MISSINGNO && itemData.getPokemonData().getIsEgg()) {
				hatchery.addEgg(itemData.getPokemonData());
			}

			// pokebank
			if (itemData.getPokemonData().getPokemonId() != PokemonId.MISSINGNO) {
				pokebank.addPokemon(api, inventoryItem.getInventoryItemData().getPokemonData());
			}

			// items
			if (itemData.getItem().getItemId() != ItemId.UNRECOGNIZED
					&& itemData.getItem().getItemId() != ItemId.ITEM_UNKNOWN) {
				itemBag.addItem(itemData.getItem());
			}

			// candyjar
			if (itemData.getCandy().getFamilyId() != PokemonFamilyIdOuterClass.PokemonFamilyId.UNRECOGNIZED
					&& itemData.getCandy().getFamilyId() != PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_UNSET) {
				candyjar.setCandy(
						itemData.getCandy().getFamilyId(),
						itemData.getCandy().getCandy()
				);
			}

			// player stats
			if (itemData.hasPlayerStats()) {
				api.getPlayerProfile().setStats(new Stats(itemData.getPlayerStats()));
			}

			// pokedex
			if (itemData.hasPokedexEntry()) {
				pokedex.add(itemData.getPokedexEntry());
			}

			if (itemData.hasEggIncubators()) {
				for (EggIncubatorOuterClass.EggIncubator incubator : itemData.getEggIncubators().getEggIncubatorList()) {
					EggIncubator current = incubators.putIfAbsent(incubator.getId(), new EggIncubator(api, incubator));
					if (current != null)
						current.setProto(incubator);
				}
			}
		}

		if (response.hasInventoryDelta()
				&& response.getInventoryDelta().getNewTimestampMs() > 0) {
			lastInventoryUpdate = response.getInventoryDelta().getNewTimestampMs();
		}
	}

	public ItemBag getItemBag() {
		return itemBag;
	}

	public CandyJar getCandyjar() {
		return candyjar;
	}

	public ConcurrentMap<String, EggIncubator> getIncubators() {
		return incubators;
	}

	public long getLastInventoryUpdate() {
		return lastInventoryUpdate;
	}
}
