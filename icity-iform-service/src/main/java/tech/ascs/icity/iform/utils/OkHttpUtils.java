package tech.ascs.icity.iform.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.Option;
import tech.ascs.icity.iform.api.model.ResponseResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OkHttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtils.class);

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    public static ResponseResult execute(Request request){
        ResponseResult responseResult = new ResponseResult();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            responseResult.setCode(response.code());
            responseResult.setMessage(response.message());
            if (response.body() != null) {
                Map<String, Object> result =  jsonToMap(response.body().string());
                if(result == null){
                    throw new IFormException("业务触发服务异常，请稍后再试");
                }
                responseResult.setResult(result);
                int code = new BigDecimal(String.valueOf(result.get("code"))).intValue();
                if( code != 200){
                    responseResult.setCode(code);
                    responseResult.setMessage((String)result.get("message"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setCode(403);
            responseResult.setMessage(e.getMessage().substring(0,e.getMessage().length() > 1024 ? 1024 : e.getMessage().length()));
        }
        return responseResult;
    }

    public static Map<String, Object> jsonToMap(String str_json) {
        Map<String, Object> res = null;
        if(StringUtils.isBlank(str_json)){
            return res;
        }
        try {
            Gson gson = new Gson();
            res = gson.fromJson(str_json, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return res;
    }

    public static String mapToJson(Map<String,Object> map){
        if(map == null){
            return "";
        }
        return JSONObject.toJSONString(map);
    }


    public static ResponseResult doGet(String url, Map<String, Object> queries){
        StringBuffer sb = new StringBuffer(url);
        sb.append(getParamString(queries));

        //创建一个Request
        final Request request = new Request.Builder()
                .url(url)
                .build();
        return  execute(request);
    }

    public static ResponseResult doPost(String url, Map<String,Object> map){
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mapToJson(map));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return execute(request);
    }


    public static ResponseResult doPut(String url, Map<String,Object> map){
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mapToJson(map));
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();
        return execute(request);
    }
    public static ResponseResult doDelete(String url, Map<String,Object> map){
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mapToJson(map));
        Request request = new Request.Builder()
                .url(url)
                .delete(requestBody)
                .build();
        return execute(request);
    }

    private static String getParamString(Map<String, Object> queries){
        StringBuffer sb = new StringBuffer("");
        if (queries != null && queries.keySet().size() > 0) {
            boolean firstFlag = true;
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, Object>) iterator.next();
                if (firstFlag) {
                    sb.append("?" + entry.getKey() + "=" + entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&" + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
        return sb.toString();
    }

    /**
     * get
     * @param url     请求的url
     * @param queries 请求的参数，在浏览器？后面的数据，没有可以传null
     * @return
     */
    public static ResponseResult doHeader(String url, Map<String, Object> queries) {
        String responseBody = "";
        StringBuffer sb = new StringBuffer(url);
        sb.append(getParamString(queries));
       /* Request.Builder builder = new Request.Builder();
        if (queries != null && queries.keySet().size() > 0) {
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map<String, String> map = (Map<String, String>)iterator.next();
                List<String> list = new ArrayList<>(map.keySet());
                builder.addHeader(list.get(0), map.get(list.get(0)));
            }
        }*/
        Request request = new Request.Builder()
                .url(sb.toString())
                .build();
        return execute(request);
    }

    public static void downloadFile(String fileUrl, String path, String fileName) {
        File file = new File(path+File.separator+fileName);
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            Request request = new Request.Builder().url(fileUrl).build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            byte[] buf = new byte[2048];
            int len = 0;
            is = response.body().byteStream();
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IFormException("下载文件过程中失败：" + e.getMessage(), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
