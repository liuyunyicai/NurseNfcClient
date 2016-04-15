package hust.nursenfcclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;

import com.daimajia.numberprogressbar.NumberProgressBar;

/**
 * Created by admin on 2015/11/20.
 */
public class TestActivity extends Activity {

    private NumberProgressBar download_progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_pop_layout);

        download_progress_bar = $(R.id.download_progress_bar);
        new DownloadAsyncTask().execute();
    }

    private class DownloadAsyncTask extends AsyncTask<Void, Integer, Void> {
        private int count = 0;
        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (count < 100) {
                    Thread.sleep(50);
                    publishProgress(++count);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            download_progress_bar.setProgress(values[0]);
        }
    }

    private <T> T $(int resId) {
        return (T) findViewById(resId);
    }
}
