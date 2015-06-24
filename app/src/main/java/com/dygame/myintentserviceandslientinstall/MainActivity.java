package com.dygame.myintentserviceandslientinstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 *  Sample for use  adapter , intentService ->Download->AlarmManager ,  slient Install ,  AlarmRecevicer
 *  20140624@ slient install failed.
 *  20140624@ adapter cancel
 */
public class MainActivity extends ActionBarActivity
{
    protected String TAG = "" ;
    protected Button quitButton ;
    protected Button downloadHttpClientButton ;
    protected Button downloadURLConnectButton ;
    protected Button downloadCloseableHttpClientButton ;
    protected Button uninstallApkButton ;
    protected Button installApkButton ;
    protected MyReceiver pReceiver;//BroadcastReceiver
    protected MyPackageChangeReceiver mPackageChangeReceiver ;//接收廣播以便知道 Package已經移除完畢
    protected ListView lvDownloadTaskList ;
    protected MyAdvancedAdapter gameAdapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Uncaught Exception Handler(Crash Exception)
        MyCrashHandler pCrashHandler = MyCrashHandler.getInstance();
        pCrashHandler.init(getApplicationContext());
        TAG = pCrashHandler.getTag() ;
        //find resource
        quitButton = (Button)findViewById(R.id.button1) ;
        downloadHttpClientButton = (Button)findViewById(R.id.button2) ;
        downloadURLConnectButton = (Button)findViewById(R.id.button3);
        downloadCloseableHttpClientButton = (Button)findViewById(R.id.button4);
        uninstallApkButton = (Button)findViewById(R.id.button5) ;
        installApkButton = (Button)findViewById(R.id.button6) ;
       //lvDownloadTaskList = (ListView)findViewById(R.id.listview) ;//cancel
        //OnClickListener
        quitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        downloadHttpClientButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this,MyDownloadTask.class);
                intent.setAction("ACTION_FETCH_NEW_ITEMS_TASK_3");
                intent.putExtra("EXTRA_PARAM1", "find the apk");
                intent.putExtra("EXTRA_PARAM2", "install it");
                startService(intent);
            }
        });
        downloadURLConnectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this,MyDownloadTask.class);
                intent.setAction("ACTION_FETCH_NEW_ITEMS_TASK_4");
                intent.putExtra("EXTRA_PARAM1", "find the apk");
                intent.putExtra("EXTRA_PARAM2", "install it");
                startService(intent);
            }
        });
        downloadCloseableHttpClientButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this,MyDownloadTask.class);
                intent.setAction("ACTION_FETCH_NEW_ITEMS_TASK_5");
                intent.putExtra("EXTRA_PARAM1", "find the apk");
                intent.putExtra("EXTRA_PARAM2", "install it");
                startService(intent);
            }
        });
        uninstallApkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String sPackage = "com.dygame.myalarmmanager" ;
                Log.e(TAG,"remove package="+sPackage);
                boolean isResult = slientUninstall(sPackage);
                if (isResult == false)
                {
                    Log.e(TAG, "root權限獲取失敗，將進行普通卸載...");
                    mPackageChangeReceiver.removePackage(MainActivity.this, sPackage) ;
                }
            }
        });
        installApkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                apkInstall() ;
            }
        });
        //init
      //gameAdapter = new MyAdvancedAdapter() ;
      //lvDownloadTaskList.setAdapter(gameAdapter);//cancel
      //lvDownloadTaskList.setOnClickListener(MainActivity.this);//cancel
        //註冊廣播接收:
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.dygame.broadcast");//為BroadcastReceiver指定action，使之用於接收同action的廣播
        pReceiver = new MyReceiver();
        registerReceiver(pReceiver, intentFilter);
        //註冊廣播接收:
        mPackageChangeReceiver = new MyPackageChangeReceiver() ;
        IntentFilter intentFilterII = new IntentFilter();
        intentFilterII.addAction(Intent.ACTION_PACKAGE_ADDED) ;
        intentFilterII.addAction(Intent.ACTION_PACKAGE_REMOVED) ;
        registerReceiver(mPackageChangeReceiver, intentFilterII);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //註銷
        if (pReceiver!=null)
        {
            unregisterReceiver(pReceiver);
            pReceiver=null;
        }
        //註銷
        if (mPackageChangeReceiver!=null)
        {
            unregisterReceiver(mPackageChangeReceiver);
            mPackageChangeReceiver=null;
        }
    }

    /**
     *   要安裝的 apk 放到  assets 目錄下, 安裝時, 先將目錄下的 apk 拷貝到 SD 卡上,改名為 temp.apk,然後安裝temp.apk
     */
    public void apkInstall()
    {
        PackageInfo packageInfo;
        try
        {
            packageInfo = getPackageManager().getPackageInfo("com.dygame.myalarmmanager", 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            packageInfo = null;
        }
        //判斷是否已安裝
        if (packageInfo == null)
        {
            // 啟用安裝新線程
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.e(TAG, "未安裝進行安裝");
                    slientInstall(); // 未安裝進行安裝
                }
            }).start();
        }
        else
        {
            Log.e(TAG, "已經安裝");
        }
    }
    /**
     * 靜默安裝
     */
    public boolean slientInstall()
    {
        // 進行資源的轉移 將assets下的文件轉移到可讀寫文件目錄下
        copyAssetsFile();
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/temp.apk");
        //
        boolean result = false;
        Process process = null;
        OutputStream out = null;
        if (file.exists())
        {
            Log.e(TAG, "target apk File=" + file.toString() + ",Path=" + file.getPath());
            try
            {
                //獲取root權限，並執行命令，通過執行su產生一個具有root權限的進程，在向這個進程的寫入要執行的命令，即可達到以root權限執行命令。
                //以root權限執行命令，只在真機上測試成功，在模擬器上沒有成功過。
                process = Runtime.getRuntime().exec("su");
                out = process.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(out);
                dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n"); // 獲取文件所有權限
                // 進行靜默安裝命令
                dataOutputStream.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + file.getPath());
                //退出su
                dataOutputStream.writeBytes("exit") ;
                // 提交命令
                dataOutputStream.flush();
                // 關閉流操作
                dataOutputStream.close();
                out.close();
                //等同於下一句
                //Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c", "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + file.getPath()});
                int value = process.waitFor();
                // 安裝成功
                if (value == 0)
                {
                    Log.e(TAG, "Install success!");
                    result = true;
                }
                else if (value == 1)
                { //安裝失敗
                    Log.e(TAG, "Install Failed!");
                    result = false;
                }
                else
                { // 未知情況
                    Log.e(TAG, "unknown error！");
                    result = false;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            if (!result)
            {
                Log.e(TAG, "root權限獲取失敗，將進行普通安裝");//手機是否有root權限
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);//安裝
                intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
                startActivity(intent);
                result = true;
            }
        }
        return result;
    }
    //
    public void copyAssetsFile()
    {
        InputStream is = null;
        FileOutputStream fos = null;
        try
        {
            //path= .\yourProject\app\src\main\assets\your.Apk
            is = MainActivity.this.getAssets().open("MyAlarmManager.apk");
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/temp.apk");
            file.createNewFile();
            Log.e(TAG, "Copy Assets File="+file.toString());
            fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0)
            {
                fos.write(temp, 0, i);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 靜默卸載
     */
    public boolean slientUninstall(String packageName)
    {
        Process process = null;
        try
        {
            process = Runtime.getRuntime().exec("su");
            PrintWriter pPrintWriter = new PrintWriter(process.getOutputStream());
            pPrintWriter.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
            pPrintWriter.println("pm uninstall "+packageName);
            pPrintWriter.flush();
            pPrintWriter.close();
            int value = process.waitFor();
            return (value == 0) ? true : false ;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(process!=null)
            {
                process.destroy();
            }
        }
        return false ;
    }
    //
    /**
     * 廣播接收
     */
    public class MyReceiver  extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if ("android.intent.action.BOOT_COMPLETED".equals(action))
            {
                Log.i(TAG, "You've got mail");
            }
            if ("com.dygame.unknown".equals(action))
            {
                Log.i(TAG, "broadcast receiver action:" + action);
            }
            if ("com.dygame.alarmmanager".equals(action))
            {
                Log.i(TAG, "broadcast incoming=" + action);
            }
        }
    }

}
