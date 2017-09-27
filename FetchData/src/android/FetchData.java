package cordova.plugin.fetch.data;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;

/**
 * This class echoes a string called from JavaScript.
 */
public class FetchData extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("readAllSMS")) {
            this.getSMSDetails(callbackContext);
            return true;
        } else if (action.equals("readCallLogs")) {
            this.getAllCallLogs(callbackContext);
            return true;
        } else if (action.equals("readContacts")) {
            this.readContacts(callbackContext);
            return true;
        } 
        return false;
    }

    private void getSMSDetails(CallbackContext callbackContext) throws JSONException {
        JSONArray messages = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            StringBuffer stringBuffer = new StringBuffer();
            Uri uri = Uri.parse("content://sms");
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            Cursor cursor = this.cordova.getActivity().getContentResolver().query(uri, null, null, null, null);

            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    JSONObject message = new JSONObject();
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            .toString();
                    String number = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                            .toString();
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                            .toString();
                    Date smsDayTime = new Date(Long.valueOf(date));
                    String dateFormatted = formatter.format(smsDayTime);
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                            .toString();
                    String typeOfSMS = null;
                    switch (Integer.parseInt(type)) {
                        case 1:
                            typeOfSMS = "INBOX";
                            break;

                        case 2:
                            typeOfSMS = "SENT";
                            break;

                        case 3:
                            typeOfSMS = "DRAFT";
                            break;
                    }

                    stringBuffer.append("\nPhone Number:--- " + number + " \nMessage Type:--- "
                            + typeOfSMS + " \nMessage Date:--- " + smsDayTime
                            + " \nMessage Body:--- " + body);
                    stringBuffer.append("\n----------------------------------");
                    message.put("messageFrom", number);
                    message.put("messageType", typeOfSMS);
                    message.put("messageDate", dateFormatted);
                    message.put("messageBody", body);
                    messages.put(message);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            obj.put("status", Boolean.TRUE);
            obj.put("messages", messages);
            callbackContext.success(obj);
        } catch (Exception ex) {
            obj.put("status", Boolean.FALSE);
            callbackContext.error(obj);
        }
    }

    private void getAllCallLogs(CallbackContext callbackContext) throws JSONException {
        // reading all data in descending order according to DATE
        JSONArray calls = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
            Uri callUri = Uri.parse("content://call_log/calls");
            Cursor cur = this.cordova.getActivity().getContentResolver().query(callUri, null, null, null, strOrder);
            // loop through cursor
            while (cur.moveToNext()) {
                String callNumber = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                String callName = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
                String callDate = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DATE));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                String dateString = formatter.format(new Date(Long.parseLong(callDate)));
                String callType = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.TYPE));
                String isCallNew = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NEW));
                String duration = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DURATION));
                JSONObject call = new JSONObject();
                call.put("callNumber", callNumber);
                call.put("callName", callName);
                call.put("callDate", dateString);
                call.put("callType", callType);
                call.put("callNew", isCallNew);
                call.put("callDuration", duration);
                calls.put(call);
            }
            cur.close();
            obj.put("status", Boolean.TRUE);
            obj.put("calls", calls);
            callbackContext.success(obj);
        } catch (Exception ex) {
            obj.put("status", Boolean.FALSE);
            callbackContext.error(obj);
        }
    }

    public void readContacts(CallbackContext callbackContext) throws JSONException {
        JSONArray contacts = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            ContentResolver cr = this.cordova.getActivity().getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
					JSONObject contact = new JSONObject();
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        System.out.println("name : " + name + ", ID : " + id);

                        // get the phone number
						String phone = "";
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            phone = pCur.getString(
                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            System.out.println("phone" + phone);
                        }
						contact.put("contactName", name);
						contact.put("contactNumber", phone);
						contacts.put(contact);
                        pCur.close();
                    }
                }
            }
            obj.put("status", Boolean.TRUE);
			obj.put("contacts", contacts);
            callbackContext.success(obj);
        } catch (Exception ex) {
            obj.put("status", Boolean.FALSE);
            callbackContext.error(obj);
        }
    }
}
