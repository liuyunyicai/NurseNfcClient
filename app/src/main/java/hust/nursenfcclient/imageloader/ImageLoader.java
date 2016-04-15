package hust.nursenfcclient.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import hust.nursenfcclient.MainActivity;

/**
 * Created by admin on 2016/2/24.
 */
public class ImageLoader {
    private static final String LOG_TAG = "LOG_TAG";

    // 图片缓存到内存中
    private LruCache<String, Bitmap> mLruCache;
    private int MAX_CACHE_SIZE; // 最大内存缓存大小(KB)
    // 图片缓存到本地
    private DiskLruCache mDiskLruCache;
    private boolean isDiskCacheUseful; // 判断磁盘缓存是否可用；
    private static final int MAX_DISKCACHE_SIZE = 50 * 1024 * 1024; // 最大磁盘缓存大小为50M
    private static final String DISK_CACHE_NAME = "nursenfc";
    private static final int DISK_CACHE_INDEX = 0;

    // 构造函数
    public ImageLoader(Context context) {
        // LruCache初始化
        initLruCache();
        // 初始化DiskLruCache
        initDiskLruCache(context);
    }

    // 初始化一个ImageLoader实例
    public static ImageLoader build(Context context) {
        return new ImageLoader(context);
    }


    // *****************  图片加载业务逻辑  **************//
    private Bitmap loadBitmap(String url, int picWidth, int picHeight) {
        // 先尝试从内存缓存中获取Bitmap
        Bitmap bitmap = getBitmapFromCacheUseUrl(url);

        if (bitmap == null) { // 如果获取失败
            // 如果系统允许磁盘缓存，则尝试从磁盘缓存中获取
            if (mDiskLruCache != null) {
                bitmap = getBitmapFromDiskCache(url, picWidth, picHeight);
                // 如果磁盘缓存中不存在，则尝试直接从网络中获取,并将获取到的Bitmap添加到缓存中
                if (bitmap == null) {
                    bitmap = getBitmapFromUrlAddtoDisk(url, picWidth, picHeight);
                }
            } else { // 如果不存在磁盘缓存，则直接从网络中获取
                bitmap = getBitmapFromUrl(url);
            }
        }
        return bitmap;
    }


    // *********************  LruCache缓存的使用 ******************************* //
    // ====== LruCache的使用 ==== //
    // LruCache初始化
    private void initLruCache() {
        // 获取最大内存缓存大小
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        MAX_CACHE_SIZE = maxMemory / 8; // 定义为应用最大缓存的1/8
        // 初始化LruCache
        mLruCache = new LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024; // 单位为KB
            }
        };
    }

    // 从缓存中获取到Bitmap
    private Bitmap getBitmapFromCache(String key) {
        return mLruCache.get(key);
    }

    // 根据URL从内存缓存中获取Bitmap
    private Bitmap getBitmapFromCacheUseUrl(String url) {
        return getBitmapFromCache(hashKeyFormUrl(url));
    }

    // 添加到缓存中
    private void addBitmapToMemCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {// 先判断是否已经存在
            mLruCache.put(key, bitmap);
        }
    }


    // ===== DiskLruCache的使用 ===== //
    // 初始化DiskLruCache
    private void initDiskLruCache(Context context) {
        File diskFile = getDiskCacheDir(context, DISK_CACHE_NAME);
        if (!diskFile.exists())
            diskFile.mkdirs();

        if (isCacheSpaceEnough(diskFile)) {
            try {
                mDiskLruCache = DiskLruCache.open(diskFile, 1, 1, MAX_DISKCACHE_SIZE);
            } catch (IOException e) {
                Log.e(LOG_TAG, "create DislruCache Error:" + e.toString());
            }
        }
    }

    // 从磁盘中获取Bitmap
    private Bitmap getBitmapFromDiskCache(String url, int picWidth, int picHeight) {
        if (mDiskLruCache == null)
            return null;
        String key = hashKeyFormUrl(url);
        Bitmap bitmap = null;

        // DisLruCache获取图片缓存的方法
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                FileInputStream inputStream = (FileInputStream)snapshot.getInputStream(DISK_CACHE_INDEX);
                FileDescriptor descriptor = inputStream.getFD();
                bitmap = ImageHelper.decodeSampledBitmapFromFileDescriptor(descriptor, picWidth, picHeight);

                // 注意将获取到的bitmap添加到内存缓存中
                if (bitmap != null) {
                    addBitmapToMemCache(key, bitmap);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "getBitmapFromDiskCache Error:" + e.toString());
        }
        return bitmap;
    }

    // 将Bitmap添加到磁盘缓存中
    private Bitmap getBitmapFromUrlAddtoDisk(String url, int picWidth, int picHeight) {
        if (mDiskLruCache == null)
            return null;
        String key = hashKeyFormUrl(url);

        try {
            // 向DiskLruCache中添加Bitmap需要使用Editor
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                // 从网络中下载图片并写入到Disk中
                if (downloadBitmapToDisk(url, outputStream))
                    editor.commit();  // 注意完成后要commit
                else
                    editor.abort();   // 撤销操作使用abort
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "addBitmapToDiskCache Error:" + e.toString());
        }
        // 从本地中获取图片
        return getBitmapFromDiskCache(url, picWidth, picHeight);
    }

    // 从网络中获取Bitmap数据流，写入到DiskLruCache中
    private boolean downloadBitmapToDisk(String url, OutputStream outputStream) {
        HttpURLConnection connection = null;
        BufferedInputStream in = null;
        BufferedOutputStream out =null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            in = new BufferedInputStream(connection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int res;
            while ((res = in.read()) != -1) {
                out.write(res);
            }
            return true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "downloadBitmapToDisk Error:" + e.toString());
        } finally {
            if (connection != null)
                connection.disconnect();
            closeStream(in);
            closeStream(out);
        }
        return false;
    }

    // ======= 从网络中直接获取Bitmap ======= //
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private Bitmap getBitmapFromUrl(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
            // 注意将获取到的bitmap添加到内存缓存中
            if (bitmap != null) {
                addBitmapToMemCache(hashKeyFormUrl(urlString), bitmap);
            }
        } catch (final IOException e) {
            Log.e(LOG_TAG, "Error in downloadBitmap: " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            closeStream(in);
        }
        return bitmap;
    }


    // ================== 辅助类 ================== //
    private String hashKeyFormUrl(String url) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    // 关闭输入输出流
    private void closeStream(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    // 获取磁盘缓存路径
    private File getDiskCacheDir(Context context, String dirName) {
        boolean isAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String dirPath;
        if (isAvailable) {
            dirPath = context.getApplicationContext().getExternalCacheDir().getPath();
        } else {
            dirPath = context.getApplicationContext().getCacheDir().getPath();
        }

        dirPath += File.separator + dirName;
        return new File(dirPath);
    }

    // 判断磁盘是否有足够的空间缓存图片数据
    private boolean isCacheSpaceEnough(File diskFile) {
        long usefulSpace = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            usefulSpace = diskFile.getUsableSpace();
        } else {
            final StatFs stats = new StatFs(diskFile.getPath());
            usefulSpace =  (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        }
        return usefulSpace > MAX_DISKCACHE_SIZE;
    }
}
