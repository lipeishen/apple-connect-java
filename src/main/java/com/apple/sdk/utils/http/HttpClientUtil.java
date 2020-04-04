package com.apple.sdk.utils.http;

import com.apple.sdk.common.exception.BusinessException;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final int TIMEOUT = 5000;
    private static PoolingHttpClientConnectionManager cm = null;
    private static RequestConfig requestConfig = null;

    private static URI getURI(String url) throws MalformedURLException, URISyntaxException {
        if (url == null) {
            throw new BusinessException(500, "url is null");
        }
        if (!url.startsWith("http") && !url.startsWith("https")) {
            throw new BusinessException(500, "url is not invadate");
        }
        URL tmp = new URL(url);
        URI uri = new URI(tmp.getProtocol(), tmp.getUserInfo(), tmp.getHost(), tmp.getPort(), tmp.getPath(),
                tmp.getQuery(), null);
        return uri;
    }

    public static String getRealUrl(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        HttpContext httpContext = new BasicHttpContext();
        try {
            HttpGet httpGet = new HttpGet(getURI(url));
            // 将HttpContext对象作为参数传给execute()方法,则HttpClient会把请求响应交互过程中的状态信息存储在HttpContext中
            response = httpClient.execute(httpGet, httpContext);
            // 获取重定向之后的主机地址信息,即"http://127.0.0.1:8088"
            HttpHost targetHost = (HttpHost) httpContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            // 获取实际的请求对象的URI,即重定向之后的"/blog/admin/login.jsp"
            HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(HttpCoreContext.HTTP_REQUEST);
            return targetHost.toString() + realRequest.getURI().toString();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeHttpResponse(response);
            closeHttpClient(httpClient);
        }
        return null;
    }

    private static Header[] assemblyHeader(Map<String, String> headers) {
        Header[] allHeader = new BasicHeader[headers.size()];
        int i = 0;
        for (String str : headers.keySet()) {
            allHeader[i] = new BasicHeader(str, headers.get(str));
            i++;
        }
        return allHeader;
    }

    public static String executePost(String url, Map<String, String> params,
                                     Map<String, String> headers, String charSet) {

        // 采用绕过验证的方式处理https请求
        SSLContext sslcontext = null;
        try {
            sslcontext = createIgnoreVerifySSL();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClients.custom().setConnectionManager(connManager);
        // 创建自定义的httpclient对象
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).build();
        CloseableHttpResponse response = null;
        ByteArrayOutputStream os = null;

        try {
            HttpPost post = new HttpPost(getURI(url));

            if (headers != null) {
                post.setHeaders(assemblyHeader(headers));
            }

            // 设置HTTP POST请求参数必须用NameValuePair对象
            List<NameValuePair> lst = new ArrayList<NameValuePair>();
            for (Entry<String, String> entry : params.entrySet()) {
                lst.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            // 设置HTTP POST请求参数
            UrlEncodedFormEntity posEntity = new UrlEncodedFormEntity(lst, charSet);
            if (logger.isDebugEnabled()) {
                os = new ByteArrayOutputStream();
                posEntity.writeTo(os);
                logger.debug("url:{},content:{}", url, os.toString());
            }
            post.setEntity(posEntity);
            post.setConfig(requestConfig);
            long time1 = System.currentTimeMillis();
            response = httpClient.execute(post);
            long time2 = System.currentTimeMillis();
            logger.debug("executeHttpClientPost cost " + (time2 - time1));
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, charSet);
        } catch (ConnectTimeoutException cne) {
            logger.error("request url is {} time out!", url);
            return "timeout";
        } catch (Exception e) {
            logger.error(e.getMessage() + "url is:" + url, e);
            return "error";
        } finally {
            closeOutputStream(os);
            closeHttpResponse(response);
            closeHttpClient(httpClient);
        }
    }

    public static String executePost(String url, Map<String, String> params, String charSet) {
        return executePost(url, params, null, charSet);
    }

    public static String executeGet(String url, String charSet) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try {
            HttpGet getHtml = new HttpGet(getURI(url));
            getHtml.setConfig(requestConfig);

            response = httpClient.execute(getHtml);
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity, charSet);
            logger.info("http get result url:{},result:{},", url, html);
            return html;
        } catch (Exception e) {
            logger.error(e.getMessage() + " when url is:" + url);
            return null;
        } finally {
            closeHttpResponse(response);
            closeHttpClient(httpClient);
        }
    }

    public static String executeGet(String url, Map<String, String> headerMap, String charSet) {
        if (headerMap == null) {
            return executeGet(url, charSet);
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try {
            HttpGet get = new HttpGet(getURI(url));
            get.setConfig(requestConfig);
            if (null != headerMap && headerMap.size() > 0) {
                get.setHeaders(assemblyHeader(headerMap));
            }

            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity, charSet);
            return html;
        } catch (Exception e) {
            logger.error(e.getMessage() + " when url is:" + url);
            return null;
        } finally {
            closeHttpResponse(response);
            closeHttpClient(httpClient);
        }
    }

    // 去掉所有ssl验证
    public static String postBody(String url, String postBody, Map<String, String> header, String charsetName) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String content = "";
        BufferedReader red = null;

        try {
            HttpPost httppost = new HttpPost(getURI(url));
            if (header != null && !header.isEmpty()) {
                for (Entry<String, String> entry : header.entrySet()) {
                    httppost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            if (postBody == null) {
                postBody = "";
            }
            HttpEntity postEntity = new StringEntity(postBody, charsetName);
            httppost.setEntity(postEntity);
            httppost.setConfig(requestConfig);

            response = httpClient.execute(httppost);
            HttpEntity entity = response.getEntity();
            StringBuilder sb = new StringBuilder();
            red = new BufferedReader(new InputStreamReader(entity.getContent(), charsetName));
            String line;
            while ((line = red.readLine()) != null) {
                sb.append(line + "\n");
            }
            EntityUtils.consume(entity);
            content = sb.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeReader(red);
            closeHttpResponse(response);
            closeHttpClient(httpClient);
        }
        return content;
    }

    // post 请求
    public static byte[] post(String url, byte[] bytes) throws IOException {
        HttpClientConfig config = HttpClientConfig.newBuilder().build();
        return post(config, url, bytes);
    }

    /**
     * @param url
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] post(HttpClientConfig config,
                              String url, byte[] bytes) throws IOException {
        HttpPost post = new HttpPost(url);
        try {
            // 配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(config.getSocketTimeOut())
                    .setConnectTimeout(config.getConnectionTimeOut())
                    .setConnectionRequestTimeout(config.getConnectionRequestTimeOut())
                    .setExpectContinueEnabled(false).build();
            post.setConfig(requestConfig);

            post.setEntity(new ByteArrayEntity(bytes));
            CloseableHttpResponse response = HttpClients.createDefault().execute(post);
            try {
                HttpEntity entity = response.getEntity();
                try {
                    return entity == null ? null : EntityUtils.toByteArray(entity);
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } finally {
                closeHttpResponse(response);
            }
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * 发送post请求
     *
     * @param url         地址
     * @param requestBody 请求体
     * @param headers     头
     * @return 返回
     * @throws IOException 异常
     */
    public static String doJsonPost(String url, String requestBody, Map<String, String> headers)
            throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        HttpResponse response = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("request to [{}], requestBody=[{}]", new Object[]{url, requestBody});
            }
            // 添加header
            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 处理参数body
            post.setEntity(new StringEntity(requestBody, Consts.UTF_8));
            response = httpclient.execute(post);
            HttpEntity entity = response.getEntity();
            String respStr;
            if (response.getStatusLine().getStatusCode() == 200) {
                respStr = EntityUtils.toString(entity);
            } else {
                respStr = response.getStatusLine().toString();
            }
            EntityUtils.consume(entity);
            return respStr;
        } finally {
            closeHttpClient(httpclient);
            post.releaseConnection();
        }
    }

    public static String postRequest(String url) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);

            response = httpclient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            } else {
                return response.getStatusLine().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeHttpResponse(response);
            closeHttpClient(httpclient);
        }
        return "404";
    }

    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    public static String getImage(String url, String charSet) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        ByteArrayOutputStream outStream = null;
        InputStream inputStream = null;

        try {
            HttpGet getHtml = new HttpGet(getURI(url));
            getHtml.setConfig(requestConfig);

            response = httpClient.execute(getHtml);
            HttpEntity entity = response.getEntity();
            inputStream = entity.getContent();
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(outStream.toByteArray());
        } catch (Exception e) {
            logger.error(e.getMessage() + " when url is:" + url);
            return null;
        } finally {

            closeInputStream(inputStream);
            closeOutputStream(outStream);
            closeHttpResponse(response);
            closeHttpClient(httpClient);

        }
    }

    /**
     * 发送 http put 请求，参数以原生字符串进行提交
     *
     * @param url
     * @param encode
     * @return
     */
    public static String execPutRaw(String url, String stringJson, Map<String, String> headers, String encode) {
        if (encode == null) {
            encode = "utf-8";
        }
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPut httpput = new HttpPut(url);

        // 设置header
        httpput.setHeader("Content-type", "application/json");
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpput.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 组织请求参数
        StringEntity stringEntity = new StringEntity(stringJson, encode);
        httpput.setEntity(stringEntity);
        String content = null;
        CloseableHttpResponse httpResponse = null;
        try {
            // 响应信息
            httpResponse = closeableHttpClient.execute(httpput);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeHttpResponse(httpResponse);
            closeHttpClient(closeableHttpClient);
        }

        return content;
    }

    /**
     * 发送http delete请求
     */
    public static String execDelete(String url, Map<String, String> headers, String encode) {
        if (encode == null) {
            encode = "utf-8";
        }
        String content = null;
        // since 4.3 不再使用 DefaultHttpClient
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
        HttpDelete httpdelete = new HttpDelete(url);
        // 设置header
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpdelete.setHeader(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = closeableHttpClient.execute(httpdelete);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeHttpResponse(httpResponse);
        }

        return content;
    }

    public static void closeHttpClient(CloseableHttpClient client) {
        try {
            // 关闭连接、释放资源
            if (null != client) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeHttpResponse(CloseableHttpResponse response) {
        try {
            // 关闭连接、释放资源
            if (null != response) {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeInputStream(InputStream inputStream) {
        try {
            // 关闭连接、释放资源
            if (null != inputStream) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeOutputStream(OutputStream outputStream) {
        try {
            // 关闭连接、释放资源
            if (null != outputStream) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeReader(Reader reader) {
        try {
            // 关闭连接、释放资源
            if (null != reader) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
