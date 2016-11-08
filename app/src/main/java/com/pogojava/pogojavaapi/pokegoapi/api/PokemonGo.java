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

package com.pogojava.pogojavaapi.pokegoapi.api;

import android.content.Context;

import com.pogojava.pogojavaapi.pokegoapi.api.device.ActivityStatus;
import com.pogojava.pogojavaapi.pokegoapi.api.device.DeviceInfo;
import com.pogojava.pogojavaapi.pokegoapi.api.device.LocationFixes;
import com.pogojava.pogojavaapi.pokegoapi.api.device.SensorInfo;
import com.pogojava.pogojavaapi.pokegoapi.api.inventory.Inventories;
import com.pogojava.pogojavaapi.pokegoapi.api.map.Map;
import com.pogojava.pogojavaapi.pokegoapi.api.player.PlayerProfile;
import com.pogojava.pogojavaapi.pokegoapi.api.settings.Settings;
import com.pogojava.pogojavaapi.pokegoapi.auth.CredentialProvider;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.LoginFailedException;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.RemoteServerException;
import com.pogojava.pogojavaapi.pokegoapi.main.AsyncServerRequest;
import com.pogojava.pogojavaapi.pokegoapi.main.CommonRequest;
import com.pogojava.pogojavaapi.pokegoapi.main.OnCheckChallengeRequestListener;
import com.pogojava.pogojavaapi.pokegoapi.main.RequestHandler;
import com.pogojava.pogojavaapi.pokegoapi.util.ClientInterceptor;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeCallback;
import com.pogojava.pogojavaapi.pokegoapi.util.SystemTimeImpl;
import com.pogojava.pogojavaapi.pokegoapi.util.Time;

import java.util.Random;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;
import POGOProtos.Networking.Envelopes.SignatureOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import okhttp3.OkHttpClient;


public class PokemonGo {
	private final Time mTime;
	private long mStartTime;
	private final byte[] mSessionHash;
	RequestHandler mRequestHandler;
	private PlayerProfile mPlayerProfile;
	private Inventories mInventories;
	private double mLatitude;
	private double mLongitude;
	private double mAltitude;
	private float mAccuracy;
	private CredentialProvider mCredentialProvider;
	private Settings mSettings;
	private Map mMap;
	private long mSeed;
	private LocationFixes mLocationFixes;
	private float mDistance;
	private float mSpeed;
	private long mLastLockFix = 0;
	private boolean mIsInCheckChallenge = false;

	private OnCheckChallengeRequestListener onCheckChallengeRequestListener;

	/**
	 * Instantiates a new Pokemon go.
	 *
	 * @param client the http client
	 * @param seed   the seed to generate same device
	 */
	public PokemonGo(OkHttpClient client, long seed, double latitude, double longitude,
					 double altitude, float speed, float accuracy) {
		this.mTime = new SystemTimeImpl();
		this.mSeed = seed;
		mSessionHash = new byte[16];
		new Random().nextBytes(mSessionHash);
		client = client.newBuilder()
				.addNetworkInterceptor(new ClientInterceptor())
				.build();
		mRequestHandler = new RequestHandler(this, client);
		mMap = new Map(this);
		mLongitude = longitude;
		mLatitude = latitude;
		mAltitude = altitude;
		mAccuracy = accuracy;
		mSpeed = speed;
	}

	/**
	 * Login user with the provided provider
	 *
	 * @param credentialProvider the credential provider
	 * @param callback           the callback that will return this instance or errors once login
	 *                           process is fully completed
	 *
	 * @return callback passed as argument
	 */
	public PokeCallback<Void> login(CredentialProvider credentialProvider, PokeCallback<Void> callback) {
		if (credentialProvider == null) {
			throw new NullPointerException("Credential Provider is null");
		}
		mCredentialProvider = credentialProvider;
		mStartTime = currentTimeMillis();
		mSettings = new Settings(this);
		mInventories = new Inventories(this);
		return initialize(callback);
	}

	/**
	 * Reproduce the login calls made by the official client and return a callback with login errors
	 * or the current instance of PokemonGo once initialized
	 *
	 * @param callback the callback
	 *
	 * @return callback passed as argument
	 */
	private PokeCallback<Void> initialize(final PokeCallback<Void> callback) {
		mPlayerProfile = new PlayerProfile(this);

		/*
			new AsyncServerRequest(RequestTypeOuterClass.RequestType.DOWNLOAD_REMOTE_CONFIG_VERSION,
				CommonRequest.getDefaultDownloadRemoteConfigVersionRequest(), null, null, this);

			new AsyncServerRequest(RequestTypeOuterClass.RequestType.GET_ASSET_DIGEST,
				CommonRequest.getDefaultGetAssetDigestMessageRequest(), null, callback, PokemonGo.this);
		*/

		callback.onResponse(null);

		return callback;
	}

	/**
	 * Calculate distance between two points in meters
	 *
	 * @param sourceLat      source latitude
	 * @param sourceLng      source longitude
	 * @param destinationLat destination latitude
	 * @param destinationLng destination longitude
	 * @return the distance in meters
	 */
	private static float distFrom(double sourceLat, double sourceLng, double destinationLat, double destinationLng) {
		double earthRadius = 6371000; //meters
		destinationLat = Math.toRadians(destinationLat - sourceLat);
		destinationLng = Math.toRadians(destinationLng - sourceLng);
		double valueA = Math.sin(destinationLat / 2) * Math.sin(destinationLat / 2)
				+ Math.cos(Math.toRadians(sourceLat)) * Math.cos(Math.toRadians(destinationLat))
				* Math.sin(destinationLng / 2) * Math.sin(destinationLng / 2);
		double valueC = 2 * Math.atan2(Math.sqrt(valueA), Math.sqrt(1 - valueA));
		return (float) (earthRadius * valueC);
	}

	/**
	 * Hash the given string
	 *
	 * @param string string to hash
	 * @return the hashed long
	 */
	private static long hash(String string) {
		long upper = ((long) string.hashCode()) << 32;
		int len = string.length();
		StringBuilder dest = new StringBuilder(len);

		for (int index = (len - 1); index >= 0; index--) {
			dest.append(string.charAt(index));
		}
		long lower = ((long) dest.toString().hashCode()) - ((long) Integer.MIN_VALUE);
		return upper + lower;
	}

	/**
	 * Fetches valid AuthInfo
	 *
	 * @return AuthInfo object
	 * @throws LoginFailedException  when login fails
	 * @throws RemoteServerException When server fails
	 */
	public AuthInfo getAuthInfo()
			throws LoginFailedException, RemoteServerException {
		return mCredentialProvider.getAuthInfo();
	}

	/**
	 * Sets location.
	 *
	 * @param latitude  the latitude
	 * @param longitude the longitude
	 * @param altitude  the altitude
	 * @param accuracy  the accuracy
	 */
	public void setLocation(double latitude, double longitude, double altitude, float accuracy) {
		if (latitude != mLatitude || longitude != mLongitude) {
			if (mLatitude != Double.NaN) {
				mDistance += distFrom(mLatitude, mLongitude, latitude, longitude);
			}
		}
		setLatitude(latitude);
		setLongitude(longitude);
		setAltitude(altitude);
		setAccuracy(accuracy);
	}

	/**
	 * Sets location.
	 *
	 * @param latitude  the latitude
	 * @param longitude the longitude
	 * @param altitude  the altitude
	 */
	public void setLocation(double latitude, double longitude, double altitude) {
		setLocation(latitude, longitude, altitude, 10);
	}

	/**
	 * Get speed
	 *
	 * @return speed in m/s
	 */
	public float getSpeed() {
		if (mSpeed != Float.NaN) {
			return mSpeed;
		}
		return mDistance / mStartTime * 1000;
	}

	public long currentTimeMillis() {
		return mTime.currentTimeMillis();
	}

	/**
	 * Validates and sets a given latitude value
	 *
	 * @param value the latitude
	 * @throws IllegalArgumentException if value exceeds +-90
	 */
	public void setLatitude(double value) {
		if (value > 90 || value < -90) {
			throw new IllegalArgumentException("latittude can not exceed +/- 90");
		}
		mLatitude = value;
	}

	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	public void setAccuracy(float accuracy) {
		mAccuracy = accuracy;
	}

	/**
	 * Validates and sets a given longitude value
	 *
	 * @param value the longitude
	 * @throws IllegalArgumentException if value exceeds +-180
	 */
	public void setLongitude(double value) {
		if (value > 180 || value < -180) {
			throw new IllegalArgumentException("longitude can not exceed +/- 180");
		}
		mLongitude = value;
	}

	/**
	 * Gets the map API
	 *
	 * @return the map
	 * @throws IllegalStateException if location has not been set
	 */
	public Map getMap() {
		if (this.mLatitude == Double.NaN) {
			throw new IllegalStateException("Attempt to get map without setting location first");
		}
		return mMap;
	}

	public void setOnCheckChallengeRequestListener(OnCheckChallengeRequestListener onCheckChallengeRequestListener) {
		this.onCheckChallengeRequestListener = onCheckChallengeRequestListener;
	}

	public OnCheckChallengeRequestListener getOnCheckChallengeRequestListener() {
		return onCheckChallengeRequestListener;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public double getAltitude() {
		return mAltitude;
	}

	public float getAccuracy() {
		return mAccuracy;
	}

	public long getSeed() {
		return mSeed;
	}

	public boolean isInCheckChallenge() {
		return mIsInCheckChallenge;
	}

	public PlayerProfile getPlayerProfile() {
		return mPlayerProfile;
	}

	public Settings getSettings() {
		return mSettings;
	}

	public Inventories getInventories() {
		return mInventories;
	}

	public LocationFixes getLocationFixes() {
		return mLocationFixes;
	}

	public void setLocationFixes(LocationFixes locationFixes) {
		mLocationFixes = locationFixes;
	}

	public void setLastLockFix(long lastLockFix) {
		mLastLockFix = lastLockFix;
	}

	public void setInCheckChallend(boolean inChallenge) {
		mIsInCheckChallenge = inChallenge;
	}

	public byte[] getSessionHash() {
		return mSessionHash;
	}

	public long getStartTime() {
		return mStartTime;
	}

	public RequestHandler getRequestHandler() {
		return mRequestHandler;
	}
}
