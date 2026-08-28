package com.smartcalllogmanager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

public class theCaller {
    public  void  AddNumToCallLog(ContentResolver resolver , String strNum, int type, long timeInMiliSecond)
    {
        while(strNum.contains("-"))
        {
            strNum =strNum.substring(0,strNum.indexOf('-')) + strNum.substring(strNum.indexOf('-')+1,strNum.length());
        }

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, strNum);
        values.put(CallLog.Calls.DATE, timeInMiliSecond);
        values.put(CallLog.Calls.DURATION, 0);
        values.put(CallLog.Calls.TYPE, type);
        values.put(CallLog.Calls.NEW, 1);
        Log.d("AddToCallLog", "Inserting call log placeholder for " + strNum);

        if(resolver != null)
        {
            resolver.insert(CallLog.Calls.CONTENT_URI, values);
        }
        //getContentResolver().delete(url, where, selectionArgs)
    }


    public void DeleteNumFromCallLog(ContentResolver resolver, String strNum)
    {
        try
        {
            String strUriCalls = "content://call_log/calls";
            Uri UriCalls = Uri.parse(strUriCalls);
            //Cursor c = res.query(UriCalls, null, null, null, null);
            if(resolver != null )
            {
                resolver.delete(UriCalls, CallLog.Calls.NUMBER +"=?",new String[]{ strNum});
            }
        }
        catch(Exception e)
        {
            e.getMessage();
        }
    }
}
