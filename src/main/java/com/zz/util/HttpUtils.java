package com.zz.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Http工具类
 * @author zhourui
 * @date 2017.6.5
 */
public class HttpUtils implements HttpConstant{

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    /**
     * get请求
     * @return
     */
    public static final <T> T get(String url, Map<String,Object> params,
                                  Class<T> clazz){
        HttpResponse response;
        try {
            response = getForObject(url,params,null);
            return covertResponseData(response,clazz);
        } catch (IOException e) {
            logger.error("get [{}] error,params is [{}]",url,params,e);
        }
        return null;
    }

    /**
     * post请求
     */
    public static final <T> T post(String url, Map<String,Object> params,
                                   Class<T> clazz){
        HttpResponse response;
        try {
            response = postForObject(url, params,null);
            return covertResponseData(response,clazz);
        } catch (IOException e) {
            logger.error("get [{}] error,params is [{}]",url,params,e);
        }
        return null;
    }

    /**
     * delete请求
     */
    public static final <T> T del(String url, Map<String,Object> params,
                                  Class<T> clazz){
        HttpResponse response;
        try {
            response = postForObject(url, params,null);
            return covertResponseData(response,clazz);
        } catch (IOException e) {
            logger.error("get [{}] error,params is [{}]",url,params,e);
        }
        return null;
    }

    private static final <T> T covertResponseData(HttpResponse response,Class<T> clazz) throws IOException {
        String responseData;
        if(response.getStatusLine().getStatusCode() == SUCCESS_CODE){
            responseData = EntityUtils.toString(response.getEntity());
            return JSONObject.parseObject(responseData, clazz);
        }else if(response.getStatusLine().getStatusCode() == ERROR_CODE){
            String errorReason = EntityUtils.toString(response.getEntity());
            logger.error("get [{}] error, and reason is  : {}",errorReason);
        }
        return null;
    }


    private static HttpResponse postForObject(String url, Map<String, Object> params, Map<String, String> headers)
            throws IOException {
        HttpPost post = new HttpPost(url);

        List<BasicNameValuePair> data = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                data.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }

        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }
        }
        long start = System.currentTimeMillis();
        post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
        HttpResponse response = getHttpClient().execute(post);
        long cost = System.currentTimeMillis() - start;
        logger.debug("Http Post cost={}ms, statusCode={}, url={}，params={}", cost, response.getStatusLine()
                .getStatusCode(), url, sb);
        return response;
    }

    private static HttpResponse getForObject(String url, Map<String, Object> params, Map<String, String> headers)
            throws IOException {
        url = parseParameterOfUrl(url, params);
        HttpGet get = new HttpGet(url);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                get.addHeader(entry.getKey(), entry.getValue());
            }
        }
        long start = System.currentTimeMillis();
        HttpResponse response = getHttpClient().execute(get);
        long cost = System.currentTimeMillis() - start;
        logger.debug("Http Get cost={}ms, statusCode={}, url={}", cost, response.getStatusLine().getStatusCode(), url);
        return response;
    }

    private static HttpResponse deleteForObject(String url, Map<String, Object> params, Map<String, String> headers)
            throws IOException {
        url = parseParameterOfUrl(url, params);

        HttpDelete delete = new HttpDelete(url);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                delete.addHeader(entry.getKey(), entry.getValue());
            }
        }

        long start = System.currentTimeMillis();
        HttpResponse response = getHttpClient().execute(delete);
        long cost = System.currentTimeMillis() - start;
        logger.debug("Http Delete cost={}ms, statusCode={}, url={}", cost, response.getStatusLine().getStatusCode(),
                url);
        return response;
    }


    public static CloseableHttpClient getHttpClient() {
        return HttpUtilHelper.httpClient;
    }

    private static class HttpUtilHelper{
        private static CloseableHttpClient httpClient;
        static {
            httpClient = createHttpClient();
        }
        private static CloseableHttpClient createHttpClient(){
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(SOCKET_TIME_OUT)
                    .setConnectTimeout(CONNECT_TIME_OUT).build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(getPoolConnManager())
                    .setDefaultRequestConfig(requestConfig).build();

            return httpClient;
        }

        private static PoolingHttpClientConnectionManager getPoolConnManager(){
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(50);
            connManager.setDefaultMaxPerRoute(5);
            return connManager;
        }
    }

    public static String parseParameterOfUrl(String url, Map<String, Object> params) {
        StringBuilder tmp = new StringBuilder(url);
        if (params != null && params.size() > 0) {
            tmp.append("?");
            Set<Map.Entry<String, Object>> entries = params.entrySet();
            for (Iterator<Map.Entry<String, Object>> iterator = entries.iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Object> entry = iterator.next();
                tmp.append(entry.getKey());
                tmp.append("=");
                try {
                    tmp.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (iterator.hasNext()) {
                    tmp.append("&");
                }
            }
        }
        return tmp.toString();
    }
}
