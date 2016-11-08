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

import POGOProtos.Enums.PokemonFamilyIdOuterClass.PokemonFamilyId;
import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;

public class PokemonMeta {
	private String templateId;
	private PokemonFamilyId family;
	private PokemonClass pokemonClass;
	private PokemonType type2;
	private double pokedexHeightM;
	private double heightStdDev;
	private int baseStamina;
	private double cylRadiusM;
	private double baseFleeRate;
	private int baseAttack;
	private double diskRadiusM;
	private double collisionRadiusM;
	private double pokedexWeightKg;
	private MovementType movementType;
	private PokemonType type1;
	private double collisionHeadRadiusM;
	private double movementTimerS;
	private double jumpTimeS;
	private double modelScale;
	private String uniqueId;
	private int baseDefense;
	private int attackTimerS;
	private double weightStdDev;
	private double cylHeightM;
	private int candyToEvolve;
	private double collisionHeightM;
	private double shoulderModeScale;
	private double baseCaptureRate;
	private PokemonId parentId;
	private double cylGroundM;
	private PokemonMove[] quickMoves;
	private PokemonMove[] cinematicMoves;
	private int number;

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public void setPokemonClass(PokemonClass pokemonClass) {
		this.pokemonClass = pokemonClass;
	}

	public PokemonFamilyId getFamily() {
		return family;
	}

	public int getBaseStamina() {
		return baseStamina;
	}

	public double getBaseCaptureRate() {
		return baseCaptureRate;
	}

	public int getCandyToEvolve() {
		return candyToEvolve;
	}

	public double getBaseFleeRate() {
		return baseFleeRate;
	}

	public int getBaseAttack() {
		return baseAttack;
	}

	public int getBaseDefense() {
		return baseDefense;
	}

	public PokemonId getParentId() {
		return parentId;
	}

	public void setFamily(PokemonFamilyId family) {
		this.family = family;
	}

	public void setType2(PokemonType type2) {
		this.type2 = type2;
	}

	public void setPokedexHeightM(double pokedexHeightM) {
		this.pokedexHeightM = pokedexHeightM;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setHeightStdDev(double heightStdDev) {
		this.heightStdDev = heightStdDev;
	}

	public void setBaseStamina(int baseStamina) {
		this.baseStamina = baseStamina;
	}

	public void setCylRadiusM(double cylRadiusM) {
		this.cylRadiusM = cylRadiusM;
	}

	public void setBaseFleeRate(double baseFleeRate) {
		this.baseFleeRate = baseFleeRate;
	}

	public void setDiskRadiusM(double diskRadiusM) {
		this.diskRadiusM = diskRadiusM;
	}

	public void setPokedexWeightKg(double pokedexWeightKg) {
		this.pokedexWeightKg = pokedexWeightKg;
	}

	public void setType1(PokemonType type1) {
		this.type1 = type1;
	}

	public void setBaseAttack(int baseAttack) {
		this.baseAttack = baseAttack;
	}

	public void setCollisionRadiusM(double collisionRadiusM) {
		this.collisionRadiusM = collisionRadiusM;
	}

	public void setMovementType(MovementType movementType) {
		this.movementType = movementType;
	}

	public void setCollisionHeadRadiusM(double collisionHeadRadiusM) {
		this.collisionHeadRadiusM = collisionHeadRadiusM;
	}

	public void setMovementTimerS(int movementTimerS) {
		this.movementTimerS = movementTimerS;
	}

	public void setJumpTimeS(double jumpTimeS) {
		this.jumpTimeS = jumpTimeS;
	}

	public void setModelScale(double modelScale) {
		this.modelScale = modelScale;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public void setBaseDefense(int baseDefense) {
		this.baseDefense = baseDefense;
	}

	public void setAttackTimerS(int attackTimerS) {
		this.attackTimerS = attackTimerS;
	}

	public void setWeightStdDev(double weightStdDev) {
		this.weightStdDev = weightStdDev;
	}

	public void setCylHeightM(double cylHeightM) {
		this.cylHeightM = cylHeightM;
	}

	public void setCandyToEvolve(int candyToEvolve) {
		this.candyToEvolve = candyToEvolve;
	}

	public void setCinematicMoves(PokemonMove[] cinematicMoves) {
		this.cinematicMoves = cinematicMoves;
	}

	public void setQuickMoves(PokemonMove[] quickMoves) {
		this.quickMoves = quickMoves;
	}

	public void setCollisionHeightM(double collisionHeightM) {
		this.collisionHeightM = collisionHeightM;
	}

	public void setShoulderModeScale(double shoulderModeScale) {
		this.shoulderModeScale = shoulderModeScale;
	}

	public void setBaseCaptureRate(double baseCaptureRate) {
		this.baseCaptureRate = baseCaptureRate;
	}

	public void setParentId(PokemonId parentId) {
		this.parentId = parentId;
	}

	public void setCylGroundM(double cylGroundM) {
		this.cylGroundM = cylGroundM;
	}
}
