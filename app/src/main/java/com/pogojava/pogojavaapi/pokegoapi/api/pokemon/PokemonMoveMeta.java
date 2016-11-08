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

import POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;

public class PokemonMoveMeta {

	private PokemonMove move;
	private PokemonType type;
	private int power;
	private int accuracy;
	private double critChance;
	private int time;
	private int energy;

	public void setMove(PokemonMove move) {
		this.move = move;
	}

	public void setType(PokemonType type) {
		this.type = type;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public void setCritChance(double critChance) {
		this.critChance = critChance;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}
}
