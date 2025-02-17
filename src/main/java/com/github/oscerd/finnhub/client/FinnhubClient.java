/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.oscerd.finnhub.client;

import java.io.IOException;
import java.util.List;

import com.github.oscerd.finnhub.model.Candle;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oscerd.finnhub.model.CompanyProfile;
import com.github.oscerd.finnhub.model.Exchange;
import com.github.oscerd.finnhub.model.Quote;
import com.github.oscerd.finnhub.model.EnrichedSymbol;
import com.github.oscerd.finnhub.model.SymbolLookup;

public class FinnhubClient {

	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private String token;
	private ObjectMapper objectMapper;

	public FinnhubClient() {
	}

	public FinnhubClient(String token) {
		this.token = token;
		this.objectMapper = new ObjectMapper();
	}

	public FinnhubClient(String token, ObjectMapper objectMapper) {
		this.token = token;
		this.objectMapper = objectMapper;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public Quote getQuote(String symbol) throws IOException, ParseException {
		HttpGet get = new HttpGet(Endpoint.QUOTE.url() + "?token=" + token + "&symbol=" + symbol);

		String result = null;
		try (CloseableHttpResponse response = httpClient.execute(get)) {
			result = EntityUtils.toString(response.getEntity());
		}

		return objectMapper.readValue(result, Quote.class);
	}

	/**
	 *  Get the Stock Candle object for a date or a range. Set startEpoch equal to the endEpoch for one day.
	 * @param symbol Ticker symbol
	 * @param resolution Supported resolution includes 1, 5, 15, 30, 60, D, W, M.
	 * Some timeframes might not be available depending on the exchange.
	 * @param startEpoch In seconds, not milliseconds.
	 * @param endEpoch As above.
	 * @return JSON object with arrays for the close, low, high, open, volume. status is a String.
	 * @throws IOException
	 * @throws ParseException
	 */
	public Candle getCandle(String symbol, String resolution, long startEpoch, long endEpoch) throws IOException, ParseException {
		HttpGet get = new HttpGet(Endpoint.CANDLE.url() + "?token=" + token
				+ "&symbol=" + symbol.toUpperCase() + "&resolution=" + resolution + "&from=" + startEpoch + "&to=" + endEpoch);

		String result = null;
		try (CloseableHttpResponse response = httpClient.execute(get)) {
			result = EntityUtils.toString(response.getEntity());
		}

		return objectMapper.readValue(result, Candle.class);
	}

	public CompanyProfile getCompanyProfile(String symbol) throws IOException, ParseException {
		HttpGet get = new HttpGet(Endpoint.COMPANY_PROFILE.url() + "?token=" + token + "&symbol=" + symbol);

		String result = null;
		try (CloseableHttpResponse response = httpClient.execute(get)) {
			result = EntityUtils.toString(response.getEntity());
		}

		return objectMapper.readValue(result, CompanyProfile.class);
	}
	
	public List<EnrichedSymbol> getSymbols(String exchange) throws IOException, ParseException {
		HttpGet get = new HttpGet(Endpoint.SYMBOL.url() + "?token=" + token + "&exchange=" + Exchange.valueOf(exchange).code());

		String result = null;
		try (CloseableHttpResponse response = httpClient.execute(get)) {
			result = EntityUtils.toString(response.getEntity());
		}

		return objectMapper.readValue(result, new TypeReference<List<EnrichedSymbol>>(){});
	}

	public SymbolLookup searchSymbol(String query) throws IOException, ParseException {
		HttpGet get = new HttpGet(Endpoint.SYMBOL_LOOKUP.url() + "?token=" + token + "&q=" + query);

		String result = null;
		try (CloseableHttpResponse response = httpClient.execute(get)) {
			result = EntityUtils.toString(response.getEntity());
		}

		return objectMapper.readValue(result, SymbolLookup.class);
	}

	public static class Builder {

		private final FinnhubClient client;

		public Builder() {
			client = new FinnhubClient();
		}

		public Builder token(String token) {
			client.setToken(token);
			return this;
		}

		public Builder to(CloseableHttpClient httpClient) {
			client.setHttpClient(httpClient);
			return this;
		}

		public Builder mapper(ObjectMapper mapper) {
			client.setObjectMapper(mapper);
			return this;
		}

		public FinnhubClient build() {
			if (client.getObjectMapper() == null) {
				client.setObjectMapper(new ObjectMapper());
			}
			return client;
		}
	}
}
