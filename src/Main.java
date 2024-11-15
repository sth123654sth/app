import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.IOException;

public class Main {
    public static void main(String[] args){
        System.out.println("Hello");
        String url = "http://localhost:1234/v1/chat/completions";
        String jsonPayload = "{\r\n" + //
                        "    \"model\": \"meta-llama-3.1-8b-instruct:3\",\r\n" + //
                        "    \"messages\": [\r\n" + //
                        "      { \"role\": \"system\", \"content\": \"As a default, provide responses in 正體中文 unless specified otherwise . \" },\r\n" + //
                        "      { \"role\": \"user\", \"content\": \"What day is it today?\" }\r\n" + //
                        "    ],\r\n" + //
                        "    \"temperature\": 0.7,\r\n" + //
                        "    \"max_tokens\": -1,\r\n" + //
                        "    \"stream\": false\r\n" + //
                        "}";

        String response = sendHttpPostRequest(url, jsonPayload);
        System.out.println("Response: " + response);
         // 解析JSON响应，提取content字段的值
         JSONObject jsonResponse = new JSONObject(response);
         String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
 
         // 输出content内容
         System.out.println("Content: " + content);
    }
    /**
    * 發送HTTP POST請求並將JSON數據作為請求體發送
    *
    * @param url         請求的URL
    * @param jsonPayload JSON格式的數據
    * @return 呈現的字符串表示
    */
    public static String sendHttpPostRequest(String url, String jsonPayload) {
        // 創建一個默認的HttpClient實例，用於執行HTTP請求
        CloseableHttpClient httpClient = HttpClients.createDefault();
   
        // 創建一個HttpPost對象，指定目標URL
        HttpPost httpPost = new HttpPost(url);

        try {
            // 設置請求頭，指定數據格式為JSON
            httpPost.setHeader("Content-type", "application/json");

            // 將JSON數據設置到請求體中
            StringEntity entity = new StringEntity(jsonPayload, "UTF-8");
            httpPost.setEntity(entity);

            // 發送HTTP POST請求並獲取響應
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                 // 取得回應狀態碼
               int statusCode = response.getStatusLine().getStatusCode();
               if (statusCode == 200) {
                   // 將回應體轉換為字符串並返回
                   return EntityUtils.toString(response.getEntity());
               } else {
                   throw new RuntimeException("HTTP請求失敗，狀態碼: " + statusCode);
               }
            } finally {
                // 關閉響應
                response.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("發生錯誤，發送HTTP POST請求時", e);
        } finally {
            // 關閉HttpClient
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
