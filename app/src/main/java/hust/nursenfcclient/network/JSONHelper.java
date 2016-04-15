package hust.nursenfcclient.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

/**
 * Created by admin on 2015/12/14.
 */
public class JSONHelper implements ServicesHelper{

    // 判断该NurseID是否存在
    public static boolean isNurseExisted(byte[] data_bytes) {
        boolean isSuccess = false;
        try {
            // ==== 解析结果字符串 ===== //
            String dataStr = new String(data_bytes, Charset.forName("UTF-8"));
            JSONObject jsonObject = new JSONObject(dataStr);
            JSONArray dataArray = jsonObject.getJSONArray(NURSE_INFO_TABLE_NAME);

            for (int i = 0; i < dataArray.length(); i++) {
                if (dataArray.getJSONObject(i).has(NURSE_ID))
                    isSuccess = true;
            }
        } catch (JSONException e) {
            Log.e("LOG_TAG", e.toString());
        } catch (Exception e) {
            Log.e("LOG_TAG", e.toString());
        }
        return isSuccess;

    }
}
