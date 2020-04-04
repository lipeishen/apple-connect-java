package com.apple.sdk;


import com.apple.sdk.constant.ApiConstant;
import com.apple.sdk.utils.PemUtils;
import com.apple.sdk.utils.http.HttpClientUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AppleConnectFluentClient extends AbstractAppleApiClient {

    private static Cache<String, Object> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(8)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .initialCapacity(10)
            .maximumSize(500)
            .recordStats().build();

    public AppleConnectFluentClient() {
    }

    public AppleConnectFluentClient(String privateSecPath, String issuer, String keyId) {
        super(privateSecPath, issuer, keyId);
    }

    public AppleConnectFluentClient getAppleConnectFluentClient() {
        return this;
    }

    /**
     * 用于测试独立主题下生成token
     *
     * @return
     * @throws IOException
     * @throws Exception
     */
    public String generateJWTTokenUt() throws IOException, Exception {
        /* 这个方法本地封装了的，一般可以从本地配置文件中读取，切记这个秘钥不能外露哦。
         它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个secret,
         那就意味着客户端是可以自我签发jwt了。
         */
        PrivateKey key = null; // 生成签名的时候使用的秘钥secret,
        try {
            key = generalKeyUt(this.privateSecPath(), this.privateSecAlgorithm());
        } catch (IOException e) {
            log.error("read private file error");
            throw e;
        }
        String token = genToken(key);
        return token;

    }

    /**
     * 根据用户app 管理的主体生成token
     *
     * @return
     * @throws Exception
     */
    public String generateJWTToken() throws Exception {

        /* 这个方法本地封装了的，一般可以从本地配置文件中读取，切记这个秘钥不能外露哦。
         它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个secret,
         那就意味着客户端是可以自我签发jwt了。
         */
        PrivateKey key = null; // 生成签名的时候使用的秘钥secret,
        try {
            key = generalKey(Base64.getDecoder().decode(this.key()), this.privateSecAlgorithm());
        } catch (IOException e) {
            log.error("read private file error");
            throw e;
        }
        String token = genToken(key);
        return token;

    }

    /**
     * jwt token 生成
     *
     * @param key
     * @return
     */
    private String genToken(PrivateKey key) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.ES256;
        // JWT header 生成
        Map<String, Object> headerClaims = createHeader(this);
        // JWT playload 生成
        Map<String, Object> claims = createPlayLoad(this);
         /* 下面就是在为payload添加各种标准声明和私有声明了
            这里其实就是new一个JwtBuilder，设置jwt的body
         */
        JwtBuilder builder = Jwts.builder()
                .setHeader(headerClaims)
                .addClaims(claims)
                .setExpiration((Date) claims.get("exp"))
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, key);
        String token = builder.compact();
        log.info("token:{}", token);
        cache.put(this.issuer() + "_" + this.keyId(), token);
        return token;
    }

    /**
     * 拼装jwt token header
     *
     * @param client
     * @return
     */
    private Map<String, Object> createHeader(AppleConnectFluentClient client) {
        Map<String, Object> headerClaims = Maps.newConcurrentMap();
        headerClaims.put("alg", client.algorithm());
        headerClaims.put("kid", client.keyId());
        headerClaims.put("typ", client.tokenType());
        return headerClaims;
    }

    /**
     * 拼装jwt token playLoad
     *
     * @param client
     * @return
     */
    private Map<String, Object> createPlayLoad(AppleConnectFluentClient client) {
        Map<String, Object> claims = Maps.newConcurrentMap();
        long expiration = client.tokenExpiration();
        // 生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        // 创建payload的私有声明（根据特定的业务需要添加，如果要拿这个做验证，一般是需要和jwt的接收方提前沟通好验证方式的）
        claims.put("iss", client.issuer());
        Date exp = null;
        if (expiration >= 0) {
            long expMisSec = nowMillis + expiration * 60 * 1000;
            exp = new Date(expMisSec);
            log.info("exp:{}", exp);
        }
        claims.put("exp", exp);
        claims.put("aud", client.audience());
        return claims;
    }

    /**
     * 单独测试读取私钥
     *
     * @param filePath
     * @param algorithm
     * @return
     * @throws Exception
     */
    private PrivateKey generalKeyUt(String filePath, String algorithm) throws Exception {
        // filePath = "/Users/lipeishen/Documents/appstore/AuthKey_53ZRM54M6H.p8";
        PrivateKey privateKey = PemUtils.readPrivateKeyFromFile(filePath, algorithm);
        return privateKey;
    }

    /**
     * 读取私钥
     *
     * @param key
     * @param algorithm
     * @return
     * @throws Exception
     */
    private PrivateKey generalKey(byte[] key, String algorithm) throws Exception {
        // filePath = "/Users/lipeishen/Documents/appstore/AuthKey_53ZRM54M6H.p8";
        PrivateKey privateKey = PemUtils.getPrivateKey(key, algorithm);
        return privateKey;
    }

    /**
     * 每个api 请求jwt token ,复用或者重新生成
     *
     * @param test
     * @return
     */
    private String getOrRefreshToken(boolean test) {
        String token = null;
        try {
            token = (String) cache.getIfPresent(this.issuer() + "_" + this.keyId());
            if (StringUtils.isNoneBlank(token)) {
                log.info("get token from cache:{}", token);
                return token;
            }
            if (test) {
                token = this.generateJWTTokenUt();
                log.info("test env get token :{}", token);
            } else {
                token = this.generateJWTToken();
                log.info("get token :{}", token);
            }
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("get JWT token failed", e);
            return null;
        }

    }

    /**
     * api 通用post 方法
     *
     * @param url
     * @param paramMap
     * @param headerMap
     * @param test
     * @return
     */
    public String postRequest(String url, Map<String, String> paramMap, Map<String, String> headerMap, boolean test) {
        Assert.notNull(url, "url cannot null or empty");
        String token = getOrRefreshToken(test);
        if (StringUtils.isBlank(token)) {
            token = getOrRefreshToken(test);
        }
        if (StringUtils.isBlank(token)) {
            Assert.notNull(token, "cannot get jwt token");
            return null;
        }
        if (headerMap == null) {
            headerMap = Maps.newConcurrentMap();
        }
        headerMap.put("Authorization", "Bearer " + token);
        return HttpClientUtil.executePost(url, paramMap, headerMap, "utf-8");

    }

    /**
     * api 通用 get 方法
     *
     * @param url
     * @param headerMap
     * @param test
     * @return
     */
    public String getRequest(String url, Map<String, String> headerMap, boolean test) {
        Assert.notNull(url, "url cannot null or empty");
        String token = getOrRefreshToken(test);
        if (StringUtils.isBlank(token)) {
            token = getOrRefreshToken(test);
        }
        if (StringUtils.isBlank(token)) {
            Assert.notNull(token, "cannot get jwt token");
            return null;
        }
        if (headerMap == null) {
            headerMap = Maps.newConcurrentMap();
        }
        headerMap.put("Authorization", "Bearer " + token);
        return HttpClientUtil.executeGet(url, headerMap, "utf-8");

    }

    /**
     * api 通用 delete 方法
     *
     * @param url
     * @param headerMap
     * @param test
     * @return
     */
    public String deleteRequest(String url, Map<String, String> headerMap, boolean test) {
        Assert.notNull(url, "url cannot null or empty");
        String token = getOrRefreshToken(test);
        if (StringUtils.isBlank(token)) {
            token = getOrRefreshToken(test);
        }
        if (StringUtils.isBlank(token)) {
            Assert.notNull(token, "cannot get jwt token");
            return null;
        }
        if (headerMap == null) {
            headerMap = Maps.newConcurrentMap();
        }
        headerMap.put("Authorization", "Bearer " + token);
        return HttpClientUtil.execDelete(url, headerMap, "utf-8");

    }

    /**
     * 对外api接口，获取app build 列表
     *
     * @return
     */
    public String listBuilds() {
        String url = this.baseUrl();
        url = url + ApiConstant.LIST_BUILDS;
        String result = this.getRequest(url, null, false);
        return result;
    }

    /**
     * 对外api接口，根据buildId 获取build 信息
     *
     * @param buildId
     * @return
     */
    public String readBuildInformation(String buildId) {
        String url = this.baseUrl();
        url = url + ApiConstant.READ_BUILD_INFORMATION;
        url = url.replace("{id}", buildId);
        String result = this.getRequest(url, null, false);
        return result;
    }

    /**
     * 对外api接口，根据buildId 查询特别的测试者
     *
     * @param buildId
     * @return
     */
    public String listAllIndividualTestersforBuild(String buildId) {
        String url = this.baseUrl();
        url = url + ApiConstant.LIST_ALL_INDIVIDUAL_TESTERS_FOR_A_BUILD;
        url = url.replace("{id}", buildId);
        String result = this.getRequest(url, null, false);
        return result;
    }

    /**
     * 对外api接口，获取测试者列表
     *
     * @return
     */
    public String listBetaTesters() {
        String url = this.baseUrl();
        url = url + ApiConstant.LIST_BETA_TESTERS;
        String result = this.getRequest(url, null, false);
        return result;
    }

    /**
     * 对外api接口，获取单个测试人员信息
     *
     * @param beta_tester_id
     * @return
     */
    public String readBetaTesterInformation(String beta_tester_id) {
        String url = this.baseUrl();
        url = url + ApiConstant.READ_BETA_TESTER_INFORMATION;
        url = url.replace("{id}", beta_tester_id);
        String result = this.getRequest(url, null, false);
        return result;
    }

    /**
     * 对外api接口，获取某个build 的测试组
     *
     * @param build_id
     * @return
     */
    public String getBetaGroupsByBuildId(String build_id) {
        String url = this.baseUrl();
        url = url + ApiConstant.LIST_BETA_GROUPS_BY_BUILD_ID + build_id;
        String result = this.getRequest(url, null, false);
        return result;
    }

    /**
     * 对外api接口，获取测试组中测试人员信息
     *
     * @param group_id
     * @return
     */
    public String getBetaTesterInBetaGroups(String group_id) {
        String url = this.baseUrl();
        url = url + ApiConstant.LIST_BETA_TESTER_IN_BETA_GROUPS;
        url = url.replace("{id}", group_id);
        String result = this.getRequest(url, null, false);
        return result;
    }

}
