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

package com.pogojava.pogojavaapi.pokegoapi.api.map;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;
import com.pogojava.pogojavaapi.pokegoapi.api.map.fort.Gym;
import com.pogojava.pogojavaapi.pokegoapi.geometry.MutableInteger;
import com.pogojava.pogojavaapi.pokegoapi.geometry.S2CellId;
import com.pogojava.pogojavaapi.pokegoapi.geometry.S2LatLng;
import com.pogojava.pogojavaapi.pokegoapi.main.AsyncServerRequest;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeAFunc;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import POGOProtos.Map.Fort.FortDataOuterClass;
import POGOProtos.Map.Fort.FortTypeOuterClass;
import POGOProtos.Map.MapCellOuterClass;
import POGOProtos.Networking.Requests.Messages.GetMapObjectsMessageOuterClass.GetMapObjectsMessage;
import POGOProtos.Networking.Requests.RequestTypeOuterClass.RequestType;
import POGOProtos.Networking.Responses.GetMapObjectsResponseOuterClass;

public class Map {
	private final PokemonGo api;
	private int cellWidth = 5;
	private long lastMapUpdate;

	/**
	 * Instantiates a new Map.
	 *
	 * @param api the api
	 */
	public Map(PokemonGo api) {
		this.api = api;
		lastMapUpdate = 0;
	}

	/**
	 * Returns the cells requested.
	 *
	 * @param cellIds  List of cellId
	 * @param callback an optional callback to handle results
	 */
	private PokeCallback<MapResponse> getMapObjects(List<Long> cellIds, PokeCallback<MapResponse> callback) {
		lastMapUpdate = api.currentTimeMillis();
		GetMapObjectsMessage.Builder builder = GetMapObjectsMessage.newBuilder()
				.setLatitude(api.getLatitude())
				.setLongitude(api.getLongitude());

		for (Long cellId : cellIds) {
			builder.addCellId(cellId);
			builder.addSinceTimestampMs(0);
		}

		new AsyncServerRequest(
				RequestType.GET_MAP_OBJECTS, builder.build(),
				new PokeAFunc<GetMapObjectsResponseOuterClass.GetMapObjectsResponse, MapResponse>() {
					@Override
					public MapResponse exec(GetMapObjectsResponseOuterClass.GetMapObjectsResponse response) {
						MapResponse mapResponse = new MapResponse();
						mapResponse.mapObjectsResponse = response;

						MapObjects result = new MapObjects(api);
						for (MapCellOuterClass.MapCell mapCell : response.getMapCellsList()) {
							result.addNearbyPokemons(mapCell.getNearbyPokemonsList());
							result.addCatchablePokemons(mapCell.getCatchablePokemonsList());
							result.addWildPokemons(mapCell.getWildPokemonsList());
							result.addDecimatedSpawnPoints(mapCell.getDecimatedSpawnPointsList());
							result.addSpawnPoints(mapCell.getSpawnPointsList());

							Set<Gym> gyms = new HashSet<>();
							Set<FortDataOuterClass.FortData> pokestops = new HashSet<>();

							for (FortDataOuterClass.FortData fortData : mapCell.getFortsList()) {
								if (fortData.getType() == FortTypeOuterClass.FortType.CHECKPOINT) {
									pokestops.add(fortData);
								} else if (fortData.getType() == FortTypeOuterClass.FortType.GYM) {
									gyms.add(new Gym(api, fortData));
								}
							}

							result.addGyms(gyms);
							result.addPokestops(pokestops);
						}

						mapResponse.mapObjects = result;
						return mapResponse;
					}
				}, callback, api);
		return callback;
	}

	/**
	 * Request a MapObjects around your current location.
	 *
	 * @param callback an optional callback to handle results
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<MapResponse> getMapObjects(final PokeCallback<MapResponse> callback) {
		return getMapObjects(getDefaultCells(), callback);
	}

	/**
	 * Get a list of all the Cell Ids.
	 *
	 * @param latitude  latitude
	 * @param longitude longitude
	 * @param width     width
	 * @return List of Cells
	 */
	private List<Long> getCellIds(double latitude, double longitude, int width) {
		S2LatLng latLng = S2LatLng.fromDegrees(latitude, longitude);
		S2CellId cellId = S2CellId.fromLatLng(latLng).parent(15);

		MutableInteger index = new MutableInteger(0);
		MutableInteger jindex = new MutableInteger(0);

		int level = cellId.level();
		int size = 1 << (S2CellId.MAX_LEVEL - level);
		int face = cellId.toFaceIJOrientation(index, jindex, null);

		List<Long> cells = new ArrayList<>();

		int halfWidth = (int) Math.floor(width / 2);
		for (int x = -halfWidth; x <= halfWidth; x++) {
			if (cells.size() == 20) {
				return cells;
			}

			for (int y = -halfWidth; y <= halfWidth; y++) {
				cells.add(S2CellId.fromFaceIJ(face, index.intValue() + x * size, jindex.intValue() + y * size).parent(15).id());
			}
		}
		return cells;
	}

	public void setDefaultWidth(int width) {
		cellWidth = width;
	}

	/**
	 * Wether or not to get a fresh copy or use cache;
	 *
	 * @return true if enough time has elapsed since the last request, false otherwise
	 */
	private boolean useCache() {
		return (api.currentTimeMillis() - lastMapUpdate) < api.getSettings().getMapSettings().getMinRefresh();
	}

	public List<Long> getDefaultCells() {
		return getCellIds(api.getLatitude(), api.getLongitude(), cellWidth);
	}

	public static class MapResponse {
		public MapObjects mapObjects;
		public GetMapObjectsResponseOuterClass.GetMapObjectsResponse mapObjectsResponse;
	}
}