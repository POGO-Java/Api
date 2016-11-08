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

package com.pogojava.pogojavaapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;
import com.pogojava.pogojavaapi.pokegoapi.api.map.Map;
import com.pogojava.pogojavaapi.pokegoapi.auth.PtcCredentialProvider;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.LoginFailedException;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.RemoteServerException;
import com.pogojava.pogojavaapi.pokegoapi.main.OnCheckChallengeRequestListener;
import com.pogojava.pogojavaapi.pokegoapi.util.PokeCallback;

import java.util.Random;

import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import okhttp3.OkHttpClient;

public class SampleActivity extends AppCompatActivity {

    private final String ACCOUNT_NAME = "";
    private final String ACCOUNT_PASS = "";

    private PokemonGo mPokemonGo;

    private TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatus = (TextView) findViewById(R.id.status);

        updateStatus("Creating provider");

        requestThred.start();
    }

    private final Thread requestThred = new Thread(new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            PtcCredentialProvider ptcCredentialProvider;

            try {
                ptcCredentialProvider = new PtcCredentialProvider(client, ACCOUNT_NAME, ACCOUNT_PASS);
            } catch (LoginFailedException | RemoteServerException e) {
                // Handle errors
                updateStatus("Error creating provider: " + e.toString());
                return;
            }

            // Unique seed for deviceId
            Random random = new Random(ACCOUNT_NAME.hashCode());
            long udid = random.nextLong();

            // These values are generally coming from Location object in Android
            double latitude = 34.0095897345215;
            double longitude = -118.49791288375856;
            double altitude = 65;
            float speed = 0;
            float accuracy = 65;

            // Create the client
            mPokemonGo = new PokemonGo(client, udid, latitude, longitude, altitude, speed, accuracy);

            // Add the checkchallenge callback
            mPokemonGo.setOnCheckChallengeRequestListener(new OnCheckChallengeRequestListener() {
                @Override
                public void onCheckChallenge(String challengeUrl) {
                    // Handle checkchallenge
                    updateStatus("Received check challenge");
                }
            });

            updateStatus("Logging in");

            // Login calls
            mPokemonGo.login(ptcCredentialProvider, new PokeCallback<Void>() {
                @Override
                public void onResponse(Void result) {
                    // We are logged in
                    updateStatus("Account logged. Requesting map object");

                    requestMapObject();
                }

                @Override
                public void onError(Throwable t) {
                    // Handle errors
                    updateStatus("Login error: " + t.toString());
                }
            });
        }

        private void requestMapObject() {
            final Map map = new Map(mPokemonGo);
            map.getMapObjects(new PokeCallback<Map.MapResponse>() {
                @Override
                public void onResponse(Map.MapResponse result) {
                    updateStatus("Map response:\n" + "Pokemon found: " + result.mapObjects.getAllCatchablePokemons().size() + "\n"
                            + "Pokestops found: " + result.mapObjects.getPokestops().size() + "\n"
                            + "Gyms found: " + result.mapObjects.getGyms().size());
                }

                @Override
                public void onError(Throwable t) {
                    // Handle errors
                    updateStatus("Error getting map object: " + t.toString());
                }
            });
        }
    });

    private void updateStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatus.setText(status);
            }
        });
    }
}
