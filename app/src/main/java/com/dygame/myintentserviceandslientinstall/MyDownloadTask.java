package com.dygame.myintentserviceandslientinstall;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An {@link IntentService} subclass for handling asynchronous(�D�P�B) task requests in
 * a service on a separate handler thread.
 * <p/>
 * update intent actions, extra parameters and static
 * helper methods.
 */
public class MyDownloadTask extends IntentService
{
    // choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    protected static final String ACTION_FETCH_NEW_ITEMS_TASK_1 = "com.dygame.myintentserviceandslientinstall.task";
    protected static final String ACTION_FETCH_NEW_ITEMS_TASK_2 = "com.dygame.myintentserviceandslientinstall.item";
    // parameters
    private static final String EXTRA_PARAM1 = "com.dygame.myintentserviceandslientinstall.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.dygame.myintentserviceandslientinstall.extra.PARAM2";
    //
    private ExecutorService mExec;
    private CompletionService<String> mEcs;
    //
    protected String TAG = "MyCrashHandler" ;
    protected String sDygameDownload= "http://mail.dygame.cn:8800/dygame/game_zone/dygamezone_000_mgb.apk" ;
    protected int iProgressPercent = 0 ;
    @Override
    public void onCreate()
    {
        super.onCreate();
        // 最多同時3條Thred執行,超過則等前面執行完會接下去執行
//        mExec = Executors.newFixedThreadPool(3);
//        mEcs = new ExecutorCompletionService<String>(mExec);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
 //         mExec.shutdown();
    }

    /**
     * Starts this service to perform action_ooxx with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // helper method
    public static void startActionFoo(Context context, String param1, String param2)
    {
        Intent intent = new Intent(context, MyDownloadTask.class);
        intent.setAction(ACTION_FETCH_NEW_ITEMS_TASK_1);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to performaction_ooxx with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // helper method
    public static void startActionBaz(Context context, String param1, String param2)
    {
        Intent intent = new Intent(context, MyDownloadTask.class);
        intent.setAction(ACTION_FETCH_NEW_ITEMS_TASK_2);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public MyDownloadTask()
    {
        super("MyDownloadTask");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            // 打印出處理intent所用的線程的ID
            long id = Thread.currentThread().getId();
//                long time = intent.getLongExtra("time", 0);
            Date date = new Date(System.currentTimeMillis());
            // 打印出每個請求對應的觸發時間
            DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss");
            Log.i(TAG, " onHandleIntent() in thread id: " + id + " , downloading..." + formatter.format(date));
            //
            final String action = intent.getAction();
            if (ACTION_FETCH_NEW_ITEMS_TASK_1.equals(action))
            {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);

            }
            else if (ACTION_FETCH_NEW_ITEMS_TASK_2.equals(action))
            {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);

            }
            else if ("ACTION_FETCH_NEW_ITEMS_TASK_3".equals(action))
            {
                gameDownload() ;
            }
            else if ("ACTION_FETCH_NEW_ITEMS_TASK_4".equals(action))
            {
                downloadURLConnection(sDygameDownload) ;
            }
            else if ("ACTION_FETCH_NEW_ITEMS_TASK_5".equals(action))
            {
                DownloadCloseableHttpClient() ;
            }
        }
    }

    public long gameDownload()
    {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(sDygameDownload);
        Log.i(TAG, "HttpClient , Url =" + sDygameDownload);

        HttpResponse response = null;
        long count = 0;
        String m_sAPKSaveDir = "" ;
        String s_sAPKFileName = "dygamezone.apk";
        if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            m_sAPKSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "dygame";
        }
        else
        {
 //           m_sAPKSaveDir = inContext.getFilesDir().getAbsolutePath();
        }
        File file = new File(m_sAPKSaveDir,s_sAPKFileName);;	//apk檔
        boolean bResumeDownload = false	;		//繼續下載
        long lFileLength = file.length();
        if (file.exists())
        {
            Log.i("DownloadHttpFile=",file.getAbsolutePath() + " file Exists");
            if(file.delete())
            {
                Log.i("DownloadHttpFile=",file.getAbsolutePath() + " file delete");
            }
        }

        try
        {
            response = client.execute(get);
        }
        catch (Exception e)
        {
            count = -1;
            e.printStackTrace();
            Log.e(TAG, "DownloadHttpFile , error : " + e.toString());
        }
        int statusCode = response.getStatusLine().getStatusCode();
        Log.i(TAG,"response=client.execute(get) , statusCode=" + statusCode);
        if((statusCode!=200)&&(statusCode!=201)&&(statusCode!=202)&&(statusCode!=206))
        {
            return -1;
        }
        File Folder = new File(m_sAPKSaveDir);//	路徑資料夾
        if(Folder.exists())
        {
            String[] list = Folder.list();
            for(int cn = 0; cn < list.length ; cn++)			// 把清單列出來
            {
                Log.e(TAG,"let's check list.."+cn+"," +  list[cn]);
            }
        }
        if (file.exists())
        {
            Log.i(TAG,"DownloadHttpFile_II"+file.getAbsolutePath() + " file Exists");
            if(file.delete())
            {
                Log.i(TAG,"DownloadHttpFile_II"+file.getAbsolutePath() + " file delete");
            }
        }
        HttpEntity entity = response.getEntity();
        long length = entity.getContentLength();
        Log.i(TAG, "FileSize = " + length);

        try
        {
            InputStream is = entity.getContent();
            if (is == null)
            {
                Log.e(TAG, "DownloadGameZoneTask::DownloadHttpFile , is == null");
                return 0;
            }
            RandomAccessFile fileOutputStream = new RandomAccessFile(file, "rw");
            byte[] buf = new byte[1024];
            int ch = -1;

            while ((ch = is.read(buf)) != -1)
            {
                fileOutputStream.write(buf, 0, ch);
                count += ch;
                //show progress
                int progress = (int)(count*100/length);
                if (iProgressPercent == progress)
                {

                }
                else
                {
                    iProgressPercent = progress;
                    Log.i(TAG, "DownloadHttpFile progress=" + progress);
                }
            }
            Log.i(TAG,"DownloadHttpFile total bytes: " + count);

            if (fileOutputStream != null)
            {
                fileOutputStream.close();
            }
            Process process = Runtime.getRuntime().exec("chmod 777 " + file.getAbsolutePath());
            process.waitFor();
        }
        catch (Exception e)
        {
            count = -1;
            e.printStackTrace();
            Log.e("dygamezone", "DownloadGameZoneTask::DownloadHttpFile , error : " + e.toString());
        }

        return count;
    }

    public Bitmap downloadBitmap(String url)
    {
        // initilize the default HTTP client object
        final DefaultHttpClient client = new DefaultHttpClient();

        //forming a HttoGet request
        final HttpGet getRequest = new HttpGet(url);
        try
        {
            HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK)
            {
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                InputStream inputStream = null;
                try
                {
                    // getting contents from the stream
                    inputStream = entity.getContent();
                    // decoding stream data back into image Bitmap that android understands
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                }
                finally
                {
                    if (inputStream != null)
                    {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        }
        catch (Exception e)
        {
            // You Could provide a more explicit error message for IOException
            getRequest.abort();
            Log.e("ImageDownloader", "Something went wrong while" + " retrieving bitmap from " + url + e.toString());
        }
        return null;
    }

    // Download Music File from Internet
    protected String downloadURLConnection(String s_url)
    {
        int count = -1;
        try
        {
            URL url = new URL(s_url);
            URL url2 = new URL("http://api.androidhive.info/progressdialog/hive.jpg") ;
            URLConnection conection = url2.openConnection();
            conection.connect();
            // Get Music file length
            // this will be useful so that you can show a typical 0-100% progress bar
            int lenghtOfFile = conection.getContentLength();
            Log.i(TAG,"DownloadURLConnection fileSize="+lenghtOfFile);
            // download the file
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url2.openStream(),8192);
            //method 2
            InputStream input2 = conection.getInputStream() ;
            // Output stream to write file in SD card
            String sPath = Environment.getExternalStorageDirectory().getPath() + "/" + "dygame" +  "/" +  "game.apk" ;
            OutputStream output = new FileOutputStream(sPath) ;
            Log.i(TAG,"DownloadURLConnection file="+sPath);
            byte data[] = new byte[1024];
            long total = 0;
//                while ((count = input.read(data)) != -1)
            //while ((count = input.read(data)) != -1)
            while(total < lenghtOfFile)
            {
                if(input.available()>0 == false)
                {
                    continue;
                }
                else
                {
                    Thread.sleep(200);
                }
                count = input.read(data);
                total += count;
                // Publish the progress which triggers onProgressUpdate method
                int progress = (int)(total*100/lenghtOfFile);
                if (iProgressPercent == progress)
                {

                }
                else
                {
                    iProgressPercent = progress;
                    Log.i(TAG, "DownloadURLConnection progress=" + progress);
                }
                // Write data to file
                output.write(data, 0, count);
            }
            Log.i(TAG,"DownloadURLConnection done="+count);
            // Flush output
            output.flush();
            // Close streams
            output.close();
            input.close();
        }
        catch (Exception e)
        {
            Log.e(TAG,"Error:" + e.getMessage());
        }
        return null;
    }

    public void DownloadCloseableHttpClient()
    {

    }
}
