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

package com.pogojava.pogojavaapi.pokegoapi.util;

import android.util.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by rama on 31/08/16.
 */
public abstract class PokeCallback<T> {

	final Semaphore sem;

	private Throwable error;
	private T result;

	protected PokeCallback() {
		this.sem = new Semaphore(Integer.MAX_VALUE);
		try {
			this.sem.acquire(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public final void fire(T result) {
		try {
			this.result = result;
			onResponse(result);
		} finally {
			sem.release(Integer.MAX_VALUE);
		}
	}

	public final void fire(Throwable error) {
		try {
			this.error = error;
			onError(error);
		} finally {
			sem.release(Integer.MAX_VALUE);
		}
	}

	public void onError(Throwable error) {
		Log.e("PokeCallback", "exception", error);
	}

	public abstract void onResponse(T result);

	public PokeCallback<T> block() {
		try {
			sem.tryAcquire(Integer.MAX_VALUE, 30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fire(e);
		}
		return this;
	}

	public T getResult() {
		return result;
	}

	public Throwable getError() {
		return error;
	}
}
