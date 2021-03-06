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

package com.pogojava.pogojavaapi.pokegoapi.main;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pogojava.pogojavaapi.pokegoapi.api.PokemonGo;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.LoginFailedException;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.RemoteServerException;
import com.pogojava.pogojavaapi.pokegoapi.util.Signature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import POGOProtos.Networking.Envelopes.AuthTicketOuterClass.AuthTicket;
import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope;
import POGOProtos.Networking.Envelopes.ResponseEnvelopeOuterClass.ResponseEnvelope;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestHandler implements Runnable {
	private final PokemonGo mApi;
	private OkHttpClient mClient;
	private Random mRandom;
	private String mApiEndpoint;

	private final Thread mAsyncHttpThread;
	private final BlockingQueue<AsyncServerRequest> mWorkQueue = new LinkedBlockingQueue<>();
	private static AtomicLong IDS = new AtomicLong(System.currentTimeMillis());

	private ExecutorService mDecoupler = Executors.newCachedThreadPool();

	/**
	 * Instantiates a new Request handler.
	 *
	 * @param api    the api
	 * @param client the client
	 */
	public RequestHandler(PokemonGo api, OkHttpClient client) {
		mApi = api;
		mClient = client;
		mRandom = new Random();
		mApiEndpoint = ApiSettings.API_ENDPOINT;

		mAsyncHttpThread = new Thread(this, "Async HTTP Thread");
		mAsyncHttpThread.setDaemon(true);
		mAsyncHttpThread.start();
	}

	/**
	 * This method is called internally from the AsyncServerRequest and is the unique method
	 * to queue a request. Response is provided through proper callbacks built in the constructor
	 * of each request
	 *
	 * @param asyncServerRequest Request to make
	 */
	protected void sendRequest(final AsyncServerRequest asyncServerRequest) {
		mWorkQueue.offer(asyncServerRequest);
	}

	/**
	 * Sends multiple ServerRequests in a thread safe manner.
	 *
	 * @param serverRequests list of ServerRequests to be sent
	 * @throws RemoteServerException the remote server exception
	 * @throws LoginFailedException  the login failed exception
	 */
	private AuthTicket internalSendServerRequests(AuthTicket authTicket, InternalServerRequest... serverRequests)
			throws RemoteServerException, LoginFailedException {
		AuthTicket newAuthTicket = authTicket;

		if (serverRequests.length == 0) {
			return authTicket;
		}

		if (mApi.isInCheckChallenge()) {
			return authTicket;
		}

		RequestEnvelope.Builder builder = RequestEnvelope.newBuilder();
		resetBuilder(builder, authTicket);

		for (InternalServerRequest serverRequest : serverRequests) {
			builder.addRequests(serverRequest.getRequest());
		}

		Signature.setSignature(mApi, builder);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		RequestEnvelope request = builder.build();
		try {
			request.writeTo(stream);
		} catch (IOException e) {
			// Failed to write request to bytearray ouput stream. This should never happen
		}

		RequestBody body = RequestBody.create(null, stream.toByteArray());
		okhttp3.Request httpRequest = new okhttp3.Request.Builder()
				.url(mApiEndpoint)
				.post(body)
				.build();

		try (Response response = mClient.newCall(httpRequest).execute()) {
			if (response.code() != 200) {
				throw new RemoteServerException("Got a unexpected http code : " + response.code());
			}

			ResponseEnvelope responseEnvelop;
			try (InputStream content = response.body().byteStream()) {
				responseEnvelop = ResponseEnvelope.parseFrom(content);
			} catch (IOException e) {
				// retrieved garbage from the server
				throw new RemoteServerException("Received malformed response : " + e);
			}

			if (responseEnvelop.getApiUrl() != null && responseEnvelop.getApiUrl().length() > 0) {
				mApiEndpoint = "https://" + responseEnvelop.getApiUrl() + "/rpc";
			}

			if (responseEnvelop.hasAuthTicket()) {
				newAuthTicket = responseEnvelop.getAuthTicket();
			}

			if (responseEnvelop.getStatusCode() == ResponseEnvelope.StatusCode.INVALID_AUTH_TOKEN) {
				throw new LoginFailedException(String.format("Invalid Auth status code recieved, token not refreshed? %s %s",
						responseEnvelop.getApiUrl(), responseEnvelop.getError()));
			} else if (responseEnvelop.getStatusCode() == ResponseEnvelope.StatusCode.REDIRECT) {
				// 53 means that the api_endpoint was not correctly set, should be at this point, though, so redo the request
				return internalSendServerRequests(newAuthTicket, serverRequests);
			} else if (responseEnvelop.getStatusCode() == ResponseEnvelope.StatusCode.BAD_REQUEST) {
				throw new RemoteServerException("Your account may be banned! please try from the official client.");
			}

			/**
			 * map each reply to the numeric response,
			 * ie first response = first request and send back to the requests to toBlocking.
			 * */
			int count = 0;
			for (ByteString payload : responseEnvelop.getReturnsList()) {
				InternalServerRequest serverReq = serverRequests[count];
				/**
				 * TODO: Probably all other payloads are garbage as well in this case,
				 * so might as well throw an exception and leave this loop */
				if (payload != null) {
					serverReq.handleData(payload);
				}
				count++;
			}
		} catch (IOException e) {
			throw new RemoteServerException(e);
		} catch (RemoteServerException e) {
			// catch it, so the auto-close of resources triggers, but don't wrap it in yet another RemoteServer Exception
			throw e;
		}
		return newAuthTicket;
	}

	private void resetBuilder(RequestEnvelope.Builder builder, AuthTicket authTicket)
			throws LoginFailedException, RemoteServerException {
		builder.setStatusCode(2);
		builder.setRequestId(getRequestId());
		//builder.setAuthInfo(api.getAuthInfo());
		if (authTicket != null
				&& authTicket.getExpireTimestampMs() > 0
				&& authTicket.getExpireTimestampMs() > mApi.currentTimeMillis()) {
			builder.setAuthTicket(authTicket);
		} else {
			builder.setAuthInfo(mApi.getAuthInfo());
		}

		int lastLocFix = mRandom.nextInt(1800 - 149) + 149;
		builder.setMsSinceLastLocationfix(lastLocFix);

		builder.setLatitude((mApi.getLatitude()));
		builder.setLongitude(mApi.getLongitude());
		builder.setAccuracy((double) mApi.getAccuracy());
	}

	private Long getRequestId() {
		return IDS.incrementAndGet();
	}

	@Override
	public void run() {
		RequestBody body = RequestBody.create(null, RequestEnvelope.newBuilder().build().toByteArray());
		okhttp3.Request versionRequest = new okhttp3.Request.Builder()
				.url(ApiSettings.API_ENDPOINT_VERSION)
				.post(body)
				.build();
		try {
			mClient.newCall(versionRequest).execute();
		} catch (IOException e) {
		}

		AsyncServerRequest<GeneratedMessage, Object> request = null;
		AuthTicket authTicket = null;
		while (true) {
			try {
				request = mWorkQueue.take();
			} catch (Throwable ignored) {
				// Ignore
			}

			if (request == null) {
				continue;
			}

			ArrayList<InternalServerRequest> serverRequests = new ArrayList<>();

			serverRequests.add(new InternalServerRequest(request.getType(), request.getRequest()));

			for (InternalServerRequest extra : request.getBoundedRequests()) {
				serverRequests.add(extra);
			}

			final InternalServerRequest[] arrayServerRequests =
					serverRequests.toArray(new InternalServerRequest[serverRequests.size()]);

			try {
				authTicket = internalSendServerRequests(authTicket, arrayServerRequests);
				final AsyncServerRequest<GeneratedMessage,Object> current = request;
				mDecoupler.execute(new Runnable() {
					@Override
					public void run() {
						try {
							current.fire(arrayServerRequests[0].getData());
						} catch (InvalidProtocolBufferException e) {
							current.fire(e);
						}

						// Assuming all the bunded requests are commons
						if (arrayServerRequests.length > 1) {
							for (int i = 1; i != arrayServerRequests.length; i++) {
								try {
									CommonRequest.parse(mApi, arrayServerRequests[i].getType(),
											arrayServerRequests[i].getData());
								} catch (InvalidProtocolBufferException e) {
									//TODO: notify error even in case of common requests?
								}
							}
						}
					}
				});

				continue;
			} catch (RemoteServerException | LoginFailedException e) {
				request.fire(e);
				continue;
			} finally {
				try {
					Thread.sleep(350);
				} catch (InterruptedException ignored) {
					// Ignore
				}
			}
		}
	}
}
