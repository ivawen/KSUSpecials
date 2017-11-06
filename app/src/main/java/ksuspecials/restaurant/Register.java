package ksuspecials.restaurant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Register extends Activity {

    public static final String MY_PREFS_NAME = "Restaurant";
    private static int RESULT_LOAD_IMAGE = 1;
    ImageView img_profile;
    String picturepath, responseStr;
    EditText edt_name, edt_email, edt_pwd;
    Button register;
    String name, email, pwd;
    ProgressDialog progressDialog;
    String userid, username, emailid, imageprofile, Error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_register);

        edt_name = (EditText) findViewById(R.id.edt_name);
        edt_email = (EditText) findViewById(R.id.edt_mail);
        edt_pwd = (EditText) findViewById(R.id.edt_pwd);

        img_profile = (ImageView) findViewById(R.id.img_profile);
        img_profile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = edt_name.getText().toString();
                email = edt_email.getText().toString();
                pwd = edt_pwd.getText().toString();
                if (picturepath != null) {
                    if (name != null) {
                        if (email != null) {
                            if (pwd != null) {
                                new PostDataAsyncTask().execute();
                            } else {
                                edt_pwd.setError("Enter Password");
                            }
                        } else {
                            edt_email.setError("Enter Email");
                        }
                    } else {
                        edt_name.setError("Enter Name");
                    }
                } else {
                    Toast.makeText(Register.this, "Please select image", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public class PostDataAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // do stuff before posting data
            progressDialog = new ProgressDialog(Register.this);
            progressDialog.setMessage("Loading..");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {


                postdata();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String lenghtOfFile) {
            // do stuff after posting data
            Log.d("successful", "successful");
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                new getlogin().execute();
            }

        }
    }

    private void postdata() {
        // TODO Auto-generated method stub
        HttpClient httpClient = new DefaultHttpClient();
        HttpEntity entity;
        //try {


        entity = MultipartEntityBuilder.create().addTextBody("email", "" + email, ContentType.create("text/plain", MIME.UTF8_CHARSET)).addTextBody("name", "" + name, ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .addTextBody("password", "" + pwd
                        , ContentType.create("text/plain", MIME.UTF8_CHARSET)).addBinaryBody("file", new File(picturepath),
                        ContentType.create("application/octet-stream"), "filename")
                .build();


        HttpPost httpPost = new HttpPost(getString(R.string.liveurl) + "adduser.php");

        httpPost.setEntity(entity);
        HttpResponse response = null;

        try {
            response = httpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpEntity result = response.getEntity();
        if (result != null) {

            // String responseStr = "";
            try {
                responseStr = EntityUtils.toString(result).trim();
                //responseStr= URLEncoder.encode(responseStr,"UTF-8");
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.e("Response", "Response: " + responseStr);


        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturepath = cursor.getString(columnIndex);

            Log.d("picturepath", "" + picturepath);
            cursor.close();


            img_profile.setImageBitmap(decodeFile(picturepath));
        }

    }

    public Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }


    public class getlogin extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {

            // TODO Auto-generated method stub

            URL hp = null;
            try {


                JSONArray jarr = new JSONArray(responseStr);

                JSONObject Obj;
                Obj = jarr.getJSONObject(0);

                userid = Obj.getString("id");
                username = Obj.getString("name");
                emailid = Obj.getString("email");
                imageprofile = Obj.getString("image");


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
                Error = e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            if (Error != null) {

                Toast.makeText(Register.this, "Email id or username is already exists", Toast.LENGTH_LONG).show();
            } else {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("score", "" + userid);
                editor.putString("username", "" + username);

                editor.putString("emailid", "" + emailid);
                editor.putString("picture", "" + imageprofile);

                editor.commit();
                Intent iv = new Intent(Register.this, Home.class);
                startActivity(iv);
                Toast.makeText(Register.this, "Register Successful..", Toast.LENGTH_LONG).show();

            }
        }
    }
}