package com.zz.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.CharSetUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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
     * @param url
     * @param params
     * @param clazz
     * @return
     */
    public static final Optional get(String url, Map<String,Object> params,
                                  Class clazz){
        return get(url,params,null,clazz);
    }

    /**
     * get请求
     * @param url
     * @param params
     * @param headers
     * @param clazz
     * @return
     */
    public static final Optional get(String url, Map<String,Object> params,
                                     Map<String,String> headers, Class clazz){
        HttpResponse response;
        try {
            response = getForObject(url,params,headers);
            return Optional.of(covertResponseData(response,clazz));
        } catch (IOException e) {
            logger.error("get [{}] error,params is [{}]",url,params,e);
        }
        return Optional.empty();
    }


    /**
     * post请求
     * @param url
     * @param params
     * @param clazz
     * @return
     */
    public static final Optional post(String url, Map<String,Object> params,
                                      Class clazz){
        return post(url,params,null,clazz);
    }

    /**
     * post请求
     * @param url
     * @param params
     * @param headers
     * @param clazz
     * @return
     */
    public static final Optional post(String url, Map<String,Object> params,
                                      Map<String,String> headers, Class clazz){
        HttpResponse response;
        try {
            response = postForObject(url, params, headers);
            return Optional.of(covertResponseData(response,clazz));
        } catch (IOException e) {
            logger.error("post [{}] error,params is [{}]",url,params,e);
        }
        return Optional.empty();
    }

    /**
     * delete请求
     * @param url
     * @param params
     * @param clazz
     * @return
     */
    public static final Optional del(String url, Map<String,Object> params,
                                  Class clazz){
        return del(url, params, null, clazz);
    }

    /**
     * delete请求
     * @param url
     * @param params
     * @param headers
     * @param clazz
     * @return
     */
    public static final Optional del(String url, Map<String,Object> params,
                                     Map<String,String> headers, Class clazz){
        HttpResponse response;
        try {
            response = deleteForObject(url, params, headers);
            return Optional.of(covertResponseData(response,clazz));
        } catch (IOException e) {
            logger.error("delete [{}] error,params is [{}]",url,params,e);
        }
        return Optional.empty();
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

    private static HttpResponse postForObject(String url, Map<String, Object> params,
                                              Map<String, String> headers) throws IOException {
        HttpPost post = new HttpPost(url);

        //参数
        List<BasicNameValuePair> data = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                data.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }

        //header
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }
        }
        post.setEntity(new UrlEncodedFormEntity(data, Charsets.UTF_8.name()));
        return sendRequest(post, "Post", url);
    }

    private static HttpResponse getForObject(String url, Map<String, Object> params,
                                             Map<String, String> headers) throws IOException {
        url = parseParameterOfUrl(url, params);
        HttpGet get = new HttpGet(url);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                get.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return sendRequest(get, "Get", url);
    }

    private static HttpResponse deleteForObject(String url, Map<String, Object> params,
                                                Map<String, String> headers) throws IOException {
        url = parseParameterOfUrl(url, params);

        HttpDelete delete = new HttpDelete(url);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                delete.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return sendRequest(delete, "Delete", url);
    }

    /**
     * 发送请求，记录日志
     */
    private static HttpResponse sendRequest(HttpRequestBase httpRequest, String method, String url) throws IOException {
        long start = System.currentTimeMillis();
        HttpResponse response = getHttpClient().execute(httpRequest);
        long cost = System.currentTimeMillis() - start;
        logger.debug("Http {} cost={}ms, statusCode={}, url={}", method, cost,
                response.getStatusLine().getStatusCode(), url);
        return response;
    }


    public static CloseableHttpClient getHttpClient() {
        return HttpUtilHelper.httpClient;
    }

    /**
     * url参数拼接
     * @param url
     * @param params
     * @return
     */
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
                    tmp.append(URLEncoder.encode(entry.getValue().toString(), Charsets.UTF_8.name()));
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

    /**
     * Http连接池
     */
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
            connManager.setMaxTotal(CM_MAX_TOTAL);
            connManager.setDefaultMaxPerRoute(CM_MAX_PER_ROUTER);
            return connManager;
        }
    }
}
