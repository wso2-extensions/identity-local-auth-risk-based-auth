/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.adaptive.authentication.riskscore.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.adaptive.authentication.riskscore.exception.RiskScoreCalculationException;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Handle the Http connection
 */
public class ConnectionHandler {
    private static final Log log = LogFactory.getLog(ConnectionHandler.class);
    private HttpClientConnectionManager connectionManager;
    private HttpClient httpClient;
    private HttpResponse httpResponse;
    private HttpPost httpPost;

    public ConnectionHandler() {
        connectionManager = new BasicHttpClientConnectionManager();
        httpClient = null;
        try {
            httpClient = HttpClientBuilder.create()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null,
                            new TrustSelfSignedStrategy()).build())).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error("Failed to establish a secure connection. " + e.getMessage(), e);
        }
        httpResponse = null;
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(1000)
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .build();
        httpPost = new HttpPost(RiskScoreConstants.URL);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-type", "application/json");

    }

    /**
     * send the authentication request data to the IS analytics and obtain the risk score
     *
     * @param context   Authentication context
     * @param timestamp timestamp of the authentication request
     * @return riskScore riskScore value for the authentication request
     */
    public int calculateRiskScore(AuthenticationContext context, String timestamp, String remoteIp) throws
            RiskScoreCalculationException {

        String username = context.getSubject().getUserName();
        String userStoreDomain = context.getSubject().getUserStoreDomain();
        String tenantDomain = context.getSubject().getTenantDomain();

//        String timestamp = String.valueOf(System.currentTimeMillis());
        int riskScore = -1;

        //define the request body of the rest API call
        String requestBody = "{" +
                "\"username\": \"" + username + "\"," +
                "\"userStoreDomain\": \"" + userStoreDomain + "\"," +
                "\"tenantDomain\": \"" + tenantDomain + "\"," +
                "\"remoteIp\": \"" + remoteIp + "\"," +
                "\"timestamp\": \"" + timestamp + "\"}";

        StringEntity input = null;
        try {
            input = new StringEntity(requestBody);
            input.setContentType("application/json");
            httpPost.setEntity(input);
        } catch (UnsupportedEncodingException e) {
            throw new RiskScoreCalculationException("Failed to initialize http request body. ", e, riskScore);
        }

        //execute the API call and obtain the result
        try {
            httpResponse = httpClient.execute(httpPost);
        } catch (IOException e) {
            throw new RiskScoreCalculationException("Failed to connect with the server. ", e, riskScore);
//            log.error("Could not get the risk score from the server. " + e.getMessage());
        } finally {
            connectionManager.shutdown();
        }
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String responseString = EntityUtils.toString(httpResponse.getEntity());
                RiskScoreDTO riskScoreDTO = mapper.readValue(responseString, RiskScoreDTO.class);
                riskScore = riskScoreDTO.getScore();
            } catch (IOException e) {
                throw new RiskScoreCalculationException("Failed to get risk score from reponse. ", e, riskScore);
            }
        } else {
            throw new RiskScoreCalculationException("HTTP error code : " + httpResponse.getStatusLine().getStatusCode() , riskScore);
        }
        return riskScore;
    }

}

