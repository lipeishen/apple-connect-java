package com.apple.sdk.utils;

import com.apple.sdk.common.response.Code;
import com.apple.sdk.common.response.Result;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by lipeishen on 2020/02/20.
 */
public class JsonUtil {

    private ObjectMapper objectMapper = new ObjectMapper()
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    /**
     * 对象转json
     *
     * @param value
     *
     * @return
     *
     * @throws IOException
     */
    public String getJsonStr(String value) throws IOException {
        Result resultVO = objectMapper.readValue(value, Result.class);
        if (Code.SUCCESS_CODE != resultVO.getCode() || null == resultVO.getResult()) {
            return null;
        }
        return objectMapper.writeValueAsString(resultVO.getResult());
    }

    /**
     * 根据json 还原对象信息
     *
     * @param jsonStr
     * @param t
     * @param <T>
     *
     * @return
     *
     * @throws Exception
     */
    public <T> T deserializeJsonStr(String jsonStr, Class<T> t) throws Exception {
        if (null != jsonStr) {
            return objectMapper.readValue(jsonStr, t);
        }
        return null;
    }

}
