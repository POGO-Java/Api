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

package com.pogojava.pogojavaapi.pokegoapi.auth;

import com.pogojava.pogojavaapi.pokegoapi.exceptions.LoginFailedException;
import com.pogojava.pogojavaapi.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;

/**
 * Any Credential Provider can extend this.
 */
public abstract class CredentialProvider {

	public abstract String getTokenId() throws LoginFailedException, RemoteServerException;

	public abstract AuthInfo getAuthInfo() throws LoginFailedException, RemoteServerException;

	public abstract boolean isTokenIdExpired();
}
