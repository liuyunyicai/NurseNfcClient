package hust.nursenfcclient.helps;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by admin on 2015/11/27.
 */
// 文件在网络中传输的相关操作
public class FileHelper {

    // ================= 写入到/data/data文件夹中 =========== //
    // 向File文件中写入数据
    public void writeIntoFile(String filename, DataInputStream inputStream, Context context) {
        FileOutputStream fileOutputStream = null;
        byte[] datas = new byte[1024];
        try {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            int length;
            int sum = 0;
            Log.i("LOG_TAG", "开始向文件" + filename + "写入数据");
            while ((length = inputStream.read(datas, 0, datas.length)) > 0) {
                Log.i("LOG_TAG", "已读取文件数据" + (sum += length));
                fileOutputStream.write(datas);
            }
            Log.w("LOG_TAG", "文件" + filename + "写入数据完毕");
        } catch (IOException e) {
            Log.e("LOG_TAG", e.toString());
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
            }
        }
    }

    // 上传File文件，即读取File文件中数据
    public void ReadFromFile(String filename, DataOutputStream outputStream, Context context) {
        FileInputStream fileInputStream = null;
        byte[] datas = new byte[1024];
        try {
            fileInputStream = context.openFileInput(filename);
            int filelength = fileInputStream.available();
            int length;
            int sum = 0;

            while ((length = fileInputStream.read(datas, 0, datas.length)) > 0) {
                Log.i("LOG_TAG", "已传输：" + (((sum +=length)  / filelength) * 100) + "%");
                outputStream.write(datas, 0, length);
                outputStream.flush();
            }
        } catch (IOException e) {
            Log.e("LOG_TAG", e.toString());
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
            }
        }
    }

    // ======= 在SD卡中进行文件读写 ======== //

    // 判断SD卡是否已挂载
    public boolean isSdCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // 创建目录
    public void mkdirDirectory(String dirPath) {
        File file = new File(dirPath);

        if (!file.exists())
            file.mkdirs();
    }

    // 获得默认的应用图片文件存储路径
    private static String getDefaultDirPath() {
        String default_dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dir_path = "nursenfc" + File.separator + "photos";
        default_dirPath += File.separator + dir_path;
        return default_dirPath;
    }

    // 给定文件名，获取其在SD卡中的详细位置
    public static String getSDFileAbsolutePath (String fileName) {
        return getDefaultDirPath() + File.separator + fileName;
    }

    public void mkdirDefaultDir() {
        mkdirDirectory(getDefaultDirPath());
    }

    // 创建文件(如果文件已存在，则删除重新创建)
    public File createNewPhotoFile(String fileName) {
        mkdirDefaultDir();

        File file = new File(getDefaultDirPath() + File.separator + fileName);

        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
        }
        return file;
    }

    // ======= 写入到SD卡的文件中 ===== //
    public void writeIntoSDcardFile(String filename, DataInputStream inputStream) {
        FileOutputStream fileOutputStream = null;
        byte[] datas = new byte[1024];
        try {
            File file = createNewPhotoFile(filename);
            fileOutputStream = new FileOutputStream(file);

            int file_length = inputStream.readInt();

            int length;
            int sum = 0;
            Log.i("LOG_TAG", "开始向文件" + filename + "写入数据");
            while ((sum < file_length) && ((length = inputStream.read(datas, 0, datas.length)) > 0)) {
                sum += length;
                fileOutputStream.write(datas, 0, length);
//                Log.i("LOG_TAG", "已读取文件数据" + (sum += length));
//                Log.i("LOG_TAG", "本次写入文件数据长度:" + length);
            }
            Log.w("LOG_TAG", "文件" + filename + "写入数据完毕");
        } catch (IOException e) {
            Log.e("LOG_TAG", e.toString());
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
            }
        }
    }

    // 上传File文件，即读取File文件中数据
    public void ReadFromSDcardFile(String filename, DataOutputStream outputStream) {
        FileInputStream fileInputStream = null;
        byte[] datas = new byte[1024];
        try {
            File file = createNewPhotoFile(filename);
            fileInputStream = new FileInputStream(file);
            int filelength = fileInputStream.available();
            int length;
            int sum = 0;

            while ((length = fileInputStream.read(datas, 0, datas.length)) > 0) {
                Log.i("LOG_TAG", "已传输：" + (((sum +=length)  / filelength) * 100) + "%");
                outputStream.write(datas, 0, length);
                outputStream.flush();
            }
        } catch (IOException e) {
            Log.e("LOG_TAG", e.toString());
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
            }
        }
    }

    // 清除SD卡中的文件
    public static void clearFiles(List<String> photoUris) {
        try {
            for (String fileName : photoUris) {
                File file = new File(getDefaultDirPath() + File.separator + fileName);

                if (file.exists()) {
                    file.delete();
                }
            }
        }catch (Exception e) {}
    }
}
