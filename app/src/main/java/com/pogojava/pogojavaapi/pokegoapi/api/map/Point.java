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

import com.pogojava.pogojavaapi.pokegoapi.util.MapPoint;

import POGOProtos.Map.SpawnPointOuterClass;

public class Point implements MapPoint {
	private double longitude;
	private double latitude;

	public Point(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Point(SpawnPointOuterClass.SpawnPoint spawnpoint) {
		this.latitude = spawnpoint.getLatitude();
		this.longitude = spawnpoint.getLongitude();
	}

	@Override
	public double getLatitude() {
		return latitude;
	}

	@Override
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
}
