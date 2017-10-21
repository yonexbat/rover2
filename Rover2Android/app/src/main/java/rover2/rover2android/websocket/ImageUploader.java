package rover2.rover2android.websocket;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by publi on 10/21/2017.
 */

public class ImageUploader {

    public Context context;
    public MultipartBody.Builder multipartBody;
    public OkHttpClient okHttpClient;

    public ImageUploader(Context context)
    {
        this.context = context;
        this.multipartBody = new MultipartBody.Builder();
        this.multipartBody.setType(MultipartBody.FORM);
        this.okHttpClient = new OkHttpClient();
    }

        // Add String
    public void addString(String name, String value)
    {
        this.multipartBody.addFormDataPart(name, value);
    }

    // Add Image File
    public void addFile(String name, String filePath, String fileName)
    {
        this.multipartBody.addFormDataPart(name, fileName, RequestBody.create(MediaType.parse("image/jpeg"), new File(filePath)));
    }

    public void addBinary(String name, String fileName,  byte[] data) {
        this.multipartBody.addFormDataPart(name, fileName, RequestBody.create(MediaType.parse("image/jpeg"), data));
    }

    // Execute Url
    public String execute(String url)
    {
        RequestBody requestBody = null;
        Request request = null;
        Response response = null;
        int code = 200;
        String strResponse = null;

        try
        {
            requestBody = this.multipartBody.build();
            // Set Your Authentication key here.
            request = new Request.Builder().header("Key", "Value").url(url).post(requestBody).build();

            Log.v("====== REQUEST ======",""+request);
            response = okHttpClient.newCall(request).execute();
            Log.v("====== RESPONSE ======",""+response);

            if (!response.isSuccessful())
                throw new IOException();

            code = response.networkResponse().code();

            /*
             * "Successful response from server"
             */
            if (response.isSuccessful())
            {
                strResponse =response.body().string();
            }
            else {
                strResponse = "Status: " + code;
            }
        }
        catch (Exception e)
        {
            Log.e("Exception", e.getMessage());
        }
        finally
        {
            requestBody = null;
            request = null;
            response = null;
            multipartBody = null;
            if (okHttpClient != null)
                okHttpClient = null;

            System.gc();
        }
        return strResponse;
    }
}
