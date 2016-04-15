package hust.nursenfcclient.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;

import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.database.NurseNFCDatabaseHelper;
import hust.nursenfcclient.database.UploadDbHelper;
import hust.nursenfcclient.helps.FileHelper;
import hust.nursenfcclient.helps.WifiHelper;
import hust.nursenfcclient.init.LogInActivity;

/**
 * Created by admin on 2015/11/24.
 */
public class NetWorkHelper implements Runnable, ServicesHelper {
    public static final int CONNECT_SERVER = 1;
    public static final int TEST_CONNECT_SERVER = 2;
    public static final int CONNECT_SERVER_AND_GET_RECV = 3;
    public static final int UPLOAD_DATA_TO_SERVER = 4;

    private static final int DEFALUT_ACTION = 0;
    private static final int ACTION_MAXIUM = 4;
    private int action = DEFALUT_ACTION;

    // 服务器端口及IP地址
    public static final String DEFAULT_SERVER_IP = "115.156.187.146";
    public static final int DEFAULT_SERVER_PORT = 8088;
    private String mServerIP = DEFAULT_SERVER_IP;
    private int mServerPort = DEFAULT_SERVER_PORT;

    // socket相关参数
    private Socket socket;
    private SocketChannel socketChannel;
    private static final int DEFAULT_TIMEOUT = 6 * 1000; // 默认读取超时连接时间
    private boolean isRunning = false;

    private String mNurse_id;
    private Context mContext;
    private Handler mHandler;
    private int recv_data_precent;

    public static final String RECV_PROGRESS = "recv_progress";
    public static final String RECV_SUCCESS = "recv_success";

    private NetWorkHelper() {
        this(CONNECT_SERVER);
    }

    private NetWorkHelper(int action) {
        if (isActionValid(action))
            this.action = action;
    }

    public NetWorkHelper(Context context, String nurse_id, Handler handler, int action) {
        mContext = context;
        mNurse_id = nurse_id;
        mHandler = handler;
        if (isActionValid(action))
            this.action = action;
    }

    // === 检测Action是否合法 ==== //
    private boolean isActionValid(int action) {
        return (action > DEFALUT_ACTION) && (action <= ACTION_MAXIUM);
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            // 要先检查WIFI是否打开
            WifiHelper.checkAndOPenWifiWithNoHint(mContext);
            switch (action) {
                case CONNECT_SERVER_AND_GET_RECV:
                    connectServerAndGetRecv();
                    break;
                case UPLOAD_DATA_TO_SERVER:
                    upLoadDataToServer();
                    break;
            }
        }
        Log.i("LOG_TAG", "子线程已退出");
    }

    private void connectServer() throws IOException{
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(MainActivity.SHAREDPR_NAME, Context.MODE_PRIVATE);
        String serverIp = sharedPreferences.getString(MainActivity.SHARED_SERVER_IP, MainActivity.DEFAULT_SERVER_IP);
        String serverPort = sharedPreferences.getString(MainActivity.SHARED_SERVER_PORT, MainActivity.DEFAULT_SERVER_PORT);

        socket = new Socket();
        socket.connect(new InetSocketAddress(serverIp, Integer.valueOf(serverPort)), DEFAULT_TIMEOUT);
    }

    // ==== 连接Server ===== //
    private boolean connectServerAndGetRecv() {
        boolean isConnected = false;
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;

        boolean isDone = false;

        try {
            Log.i("LOG_TAG", "开始连接服务器");
//            socketChannel = SocketChannel.open();
//            socketChannel.connect(new InetSocketAddress(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT));
//            socket = socketChannel.socket();
////            socket.setSoTimeout(DEFAULT_TIMEOUT);
            connectServer();
            if (socket != null) {
                Log.i("LOG_TAG", "连接到服务器");
                isConnected = true;

                inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                // 发送下载数据请求
                sendDownloadRequest(outputStream);

                while (true) {
                    int service_type = inputStream.readInt();
                    if (service_type > 0) {
                        Log.i("LOG_TAG", "读取到数据" + service_type);

                        int isSuccess = inputStream.readInt();  // 获取操作是否成功
                        int dataLength = inputStream.readInt(); // 读取字段长度
                        byte[] data_bytes = new byte[dataLength];

                        recv_data_precent = 0;
                        int i = 0;
                        int last_i = 0;
                        byte temp_data;
                        // ===== 获得服务器中数据库中相关数据 ==== //
                        Log.i("LOG_TAG", "获得数据长度" + dataLength);
                        while ((i < dataLength) && ((temp_data = (byte) inputStream.read()) != -1)) {
                            Log.i("LOG", "获得数据" + i);
                            data_bytes[i++] = temp_data;

                            if (i - last_i >= 500) {
                                last_i = i;
                                recv_data_precent = (int) ((double)i / dataLength * 100);

                                // 向Activity发送消息
                                Message msg = mHandler.obtainMessage(LogInActivity.PUBLISH_PROGRESS);
                                Bundle data = new Bundle();
                                data.putInt(RECV_PROGRESS, recv_data_precent);
                                msg.setData(data);
                                mHandler.sendMessage(msg);
                            }
                        }

                        Log.i("LOG_TAG", "接收完数据");
                        // 发送下载数据完毕的消息
                        Message msg = mHandler.obtainMessage(LogInActivity.DOWNLOAD_DB_POST);
                        Bundle data = new Bundle();
                        data.putBoolean(RECV_SUCCESS, i == dataLength);
                        msg.setData(data);
                        mHandler.sendMessage(msg);
                        Log.w("LOG_TAG", new String(data_bytes, Charset.forName("UTF-8")));

                        // 如果该护士工号错误，则直接退出
                        if (!JSONHelper.isNurseExisted(data_bytes)) {
                            mHandler.sendEmptyMessage(LogInActivity.WRONG_NURSE_ID);
                            break;
                        }

                        // ====== 将数据写入到SQlite数据库中 ===== //
                        NurseNFCDatabaseHelper nurseNFCDatabaseHelper = NurseNFCDatabaseHelper.getInstance(mContext.getApplicationContext());
                        // 插入数据前注意清空数据
                        nurseNFCDatabaseHelper.clearAllTableDatas();

                        nurseNFCDatabaseHelper.initDataFromServer(data_bytes);
                        Log.w("LOG_TAG", "数据写入到SQlite数据库中");

                        // ==从数据库中查询数据 (用以测试) ==== //
                        StringBuffer stringBuffer = new StringBuffer();
                        nurseNFCDatabaseHelper.getDataFromDbToUpLoad(stringBuffer);
                        Log.w("LOG_TAG", "获取到SQlite数据库中的数据" + stringBuffer.toString());

                        // ======= 请求下载图片 ===== //
                        List<String> photoUris = nurseNFCDatabaseHelper.getPhotosUri();
                        // 向主线程发送下载图片消息
                        msg = mHandler.obtainMessage(LogInActivity.GET_IMAGE_EXCUTE);
                        msg.obj = photoUris.size();
                        mHandler.sendMessage(msg);

                        int count = 0;
                        for (String photoUri : photoUris) {
                            Log.i("LOG_TAG", "请求下载图片" + photoUri);
                            sendGetImage(outputStream, photoUri);

                            int ack_type = inputStream.readInt();
                            if (ack_type == GET_IMAGE_ACK) {
                                new FileHelper().writeIntoSDcardFile(photoUri, inputStream);
//                                new FileHelper().createNewPhotoFile(photoUri);
                            }

                            msg = mHandler.obtainMessage(LogInActivity.GET_IMAGE_PROGRESS);
                            msg.obj = (++ count);
                            mHandler.sendMessage(msg);
                        }

                        isDone = true;
                        break;
                    }
                }
            } else {
                Log.e("LOG_TAG", "服务器连接失败");
            }

        } catch (UnknownHostException e) {
            Log.e("LOG_TAG", e.toString());
        } catch (IOException e) {
            Log.e("LOG_TAG", e.toString());
        } catch (JSONException e) {
            Log.e("LOG_TAG", e.toString());
        } catch (Exception e) {
            Log.e("LOG_TAG", e.toString());
        } finally {
            // 发送结果线程
            Message msg = mHandler.obtainMessage(LogInActivity.POST_EXCUTE);
            msg.obj = isDone;
            mHandler.sendMessage(msg);

            // 关闭线程
            isRunning = false;
            // 回收资源
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
            }
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e) {
            }
            try {
//                if (socketChannel != null)
//                    socketChannel.close();
                if (socket != null)
                    socket.close();
            } catch (Exception e) {
            }

        }
        return isDone;
    }

    // ==== 连接Server并上传数据 ==== //
    private boolean upLoadDataToServer() {
        boolean isConnected = false;
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;

        boolean isDone = false;

        try {
            Log.i("LOG_TAG", "开始连接服务器");
//            socketChannel = SocketChannel.open();
//            socketChannel.connect(new InetSocketAddress(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT));
//
//            socket = socketChannel.socket();
//            socket.setSoTimeout(DEFAULT_TIMEOUT);
            connectServer();

            if (socket != null) {
                Log.i("LOG_TAG", "连接到服务器");
                isConnected = true;

                inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                // ==== 获取需要上传的数据 ==== //
                String dataStr = UploadDbHelper.getInstance(mContext).getUpLoadData().toString();

                byte[] datas = dataStr.getBytes(Charset.forName("utf-8"));

                byte[] testbytes = "Hello".getBytes(Charset.forName("utf-8"));
                // ==== 上传数据到服务器 ==== //
                outputStream.writeInt(NURSE_UPLOAD);
                outputStream.writeInt(datas.length);
                Log.i("LOG_TAG", "" + datas.length);
                outputStream.write(datas, 0, datas.length);
                outputStream.flush();
                Log.i("LOG_TAG", "" + "数据已发送");

                mHandler.sendEmptyMessage(MainActivity.UPLOAD_DATA_TO_SERVER_SUCCESS);

                // 等待接收ACK
                while (true) {
                    int service_type = inputStream.readInt();
                    if (service_type == NURSE_UPLOAD_ACK) {
                        Log.i("LOG_TAG", "读取到上传响应" + service_type);

                        int isSuccess = inputStream.readInt();  // 获取操作是否成功
                        isDone = isSuccess == ACK_SUCCESS;
                        break;
                    }
                }
            } else {
                Log.e("LOG_TAG", "服务器连接失败");
            }

        } catch (UnknownHostException e) {
            Log.e("LOG_TAG", e.toString());
        } catch (IOException e) {
            Log.e("LOG_TAG", e.toString());
        }  catch (Exception e) {
            Log.e("LOG_TAG", e.toString());
        } finally {
            // 关闭线程
            isRunning = false;
            // 向主线程传递消息
            mHandler.sendEmptyMessage(isDone ? MainActivity.UPLOAD_SUCCESS : MainActivity.UPLOAD_FAIL);
            // 回收资源
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
            }
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e) {
            }
            try {
//                if (socketChannel != null)
//                    socketChannel.close();
                if (socket != null)
                    socket.close();
            } catch (Exception e) {
            }

        }
        return isDone;
    }

    // ==== 发送请求下载请求字符串 ====== //
    private void sendDownloadRequest(DataOutputStream outputStream) throws IOException, JSONException {
        sendRequest(outputStream, NURSE_DOWNLOAD, NURSE_ID, mNurse_id);
    }

    // ==== 发送请求下载图片文件字符串 ==== //
    private void sendGetImage(DataOutputStream outputStream, String imagePath) throws IOException, JSONException {
        sendRequest(outputStream, GET_IMAGE, IMAGE_PATH, imagePath);
    }

    // 发送请求字符串
    private void sendRequest(DataOutputStream outputStream, int request_id, String param, String param_value) throws IOException, JSONException {
        outputStream.writeInt(request_id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(param, param_value);
        byte[] send_bytes = jsonObject.toString().getBytes(Charset.forName("UTF-8"));
        outputStream.writeInt(send_bytes.length);
        outputStream.write(send_bytes);
        outputStream.flush();
        Log.i("LOG_TAG", "发送请求数据");
    }


    // 网络参数设置
    public void setServerParams(String serverIP, int serverPort) {
        mServerIP = serverIP;
        mServerPort = serverPort;
    }

}
