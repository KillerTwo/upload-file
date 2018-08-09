package org.lwt.serverupload.tools;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonUtil {
	/**
	 * 	�������ݵ�json�ַ���ΪMap����
	 * 
	 * 
	 * 	@param jsonStr	json�ַ���
	 * 	@return	Map,	��json�ַ��������õ���map����
	 */
	public static Map<String, Object> getMapFromJson(String jsonStr){
		//Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		//map = gson.fromJson(jsonStr, new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType());
		
		JSONObject  jsonObject = JSONObject.parseObject(jsonStr);
	    //json����תMap
	    map = (Map<String,Object>)jsonObject;
		
		return map;
	}
	/**
	 * 	��map����ת��Ϊһ��json�ַ���
	 * 
	 * 	@param map	��ת����map����
	 * 	@return		����map ��Ӧ��json�ַ���
	 */
	public static String getJsonFromMap(Map<String, Object> map) {
		/*Gson gson = new Gson();
		String jsonStr = gson.toJson(map);*/
		String jsonStr = JSON.toJSONString(map);
		return jsonStr;
	}
	
}
