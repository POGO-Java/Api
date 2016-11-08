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

package com.pogojava.pogojavaapi.pokegoapi.api.device;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;

import java.util.Random;

import POGOProtos.Networking.Envelopes.SignatureOuterClass;

/**
 * Created by fabianterhorst on 08.08.16.
 */

public class SensorInfo {

	private SignatureOuterClass.Signature.SensorUpdate.Builder sensorInfoBuilder;

	private long timestampCreate;

	public SensorInfo() {
		sensorInfoBuilder = SignatureOuterClass.Signature.SensorUpdate.newBuilder();
	}

	/**
	 * Create a sensor info with already existing sensor infos
	 *
	 * @param sensorInfos the sensor infos interface
	 */
	public SensorInfo(SensorInfos sensorInfos) {
		this();
		sensorInfoBuilder
				.setTimestamp(sensorInfos.getTimestampSnapshot())
				.setAccelerationX(sensorInfos.getLinearAccelerationX())
				.setAccelerationY(sensorInfos.getLinearAccelerationY())
				.setAccelerationZ(sensorInfos.getLinearAccelerationZ())
				.setMagneticFieldX(sensorInfos.getMagneticFieldX())
				.setMagneticFieldY(sensorInfos.getMagneticFieldY())
				.setMagneticFieldZ(sensorInfos.getMagneticFieldZ())
				.setRotationRateX(sensorInfos.getRotationVectorX())
				.setRotationRateY(sensorInfos.getRotationVectorY())
				.setRotationRateZ(sensorInfos.getRotationVectorZ())
				.setAttitudePitch(sensorInfos.getGyroscopeRawX())
				.setAttitudeRoll(sensorInfos.getGyroscopeRawY())
				.setAttitudeYaw(sensorInfos.getGyroscopeRawZ())
				.setGravityX(sensorInfos.getGravityX())
				.setGravityY(sensorInfos.getGravityY())
				.setGravityZ(sensorInfos.getGravityZ())
				.setMagneticFieldAccuracy(3);
	}

	/**
	 * Gets the default sensor info for the given api
	 *
	 * @param api         the api
	 * @param currentTime the current time
	 * @param random      random object
	 * @return the default sensor info for the given api
	 */
	public static SignatureOuterClass.Signature.SensorUpdate getDefault(PokemonGo api, long currentTime, Random random) {
		SensorInfo sensorInfo = new SensorInfo();
		sensorInfo.getBuilder().setTimestamp(currentTime - api.getStartTime() + random.nextInt(500))
				.setAccelerationX(-0.7 + random.nextDouble() * 1.4)
				.setAccelerationY(-0.7 + random.nextDouble() * 1.4)
				.setAccelerationZ(-0.7 + random.nextDouble() * 1.4)
				.setRotationRateX(0.1 + (0.7 - 0.1) * random.nextDouble())
				.setRotationRateY(0.1 + (0.8 - 0.1) * random.nextDouble())
				.setRotationRateZ(0.1 + (0.8 - 0.1) * random.nextDouble())
				.setAttitudePitch(-1.0 + random.nextDouble() * 2.0)
				.setAttitudeRoll(-1.0 + random.nextDouble() * 2.0)
				.setAttitudeYaw(-1.0 + random.nextDouble() * 2.0)
				.setGravityX(-1.0 + random.nextDouble() * 2.0)
				.setGravityY(-1.0 + random.nextDouble() * 2.0)
				.setGravityZ(-1.0 + random.nextDouble() * 2.0)
				.setMagneticFieldAccuracy(-1)
				.setStatus(3);
		if (currentTime - sensorInfo.getTimestampCreate() > (random.nextInt(10 * 1000) + 5 * 1000)) {
			sensorInfo.setTimestampCreate(currentTime);
			return sensorInfo.getBuilder().build();
		}
		return null;
	}

	private void setTimestampCreate(long timestampCreate) {
		this.timestampCreate = timestampCreate;
	}

	/**
	 * Gets the scene info builder
	 *
	 * @return the scene info builder
	 */
	public SignatureOuterClass.Signature.SensorUpdate.Builder getBuilder() {
		return sensorInfoBuilder;
	}

	public long getTimestampCreate() {
		return timestampCreate;
	}
}
