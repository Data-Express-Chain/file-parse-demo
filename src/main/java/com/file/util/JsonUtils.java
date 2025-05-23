package com.file.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.file.constant.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonUtils {
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
    /**
     * serialize a class instance to Json String.
     *
     * @param object the class instance to serialize
     * @param <T> the type of the element
     * @return JSON String
     */
    public static <T> String convertObjectToJson(T object) {
        Writer write = new StringWriter();
        try {
            OBJECT_MAPPER.writeValue(write, object);
        } catch (Exception e) {
            // log.error("[OnError]convertObjectToJson failed", e);
			log.error("OnError|{}|||||{}", ErrorCode.JSON_CONVERT_EXCEPTION.getCode(), "convertObjectToJson failed", e);
            throw new RuntimeException(e);
        } 
        return write.toString();
    }
    
    public static <T> List<T> convertJsonToObjectList(String json) {
    	List<T> tList = null;
    	
    	try {
			tList = OBJECT_MAPPER.readValue(json, new TypeReference<ArrayList<T>>(){});
		} catch (Exception e) {
			// log.error("[OnError]convertJsonToObjectList failed", e);
			log.error("OnError|{}|||||{}", ErrorCode.JSON_CONVERT_EXCEPTION.getCode(), "convertJsonToObjectList failed", e);
            throw new RuntimeException(e);
		} 
    	
    	return tList;
    }
    
    public static <T> T convertJsonToObject(String json, Class<T> clazz) {
    	T t = null;
    	try {
			t = OBJECT_MAPPER.readValue(json, clazz);
		} catch (Exception e) {
			// log.error("[OnError]convertJsonToObject failed", e);
			log.error("OnError|{}|||||{}", ErrorCode.JSON_CONVERT_EXCEPTION.getCode(), "convertJsonToObject failed", e);
            throw new RuntimeException(e);
		}
    	return t;
    }

    public static JsonNode convertJsonToJsonNode(String json) {
        JsonNode jsonNode = null;
        try {
            jsonNode = OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            // log.error("[OnError]convertJsonToJsonNode failed", e);
            log.error("OnError|{}|||||{}", ErrorCode.JSON_CONVERT_EXCEPTION.getCode(), "convertJsonToJsonNode failed", e);
            throw new RuntimeException(e);
        }
        return jsonNode;
    }

}
