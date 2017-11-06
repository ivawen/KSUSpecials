package ksuspecials.restaurant;

import android.app.Activity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Login extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    EditText edt_email,edt_pwd;
    Button login,register,btnfacebook,google;
    String email,password;
    String userid, username, emailid, imageprofile, Error;
    public static final String MY_PREFS_NAME = "Restaurant";
    private static String APP_ID = "1687498654867590";
    // Instance of Facebook Class
    private Facebook facebook;
    private static final String FACEBOOK_PERMISSION = "publish_stream";
    private AsyncFacebookRunner mAsyncRunner;
    String name,imagefb,ppic;
    String FILENAME = "AndroidSSO_data";
    private SharedPreferences mPrefs;
    String key;
    private GoogleApiClient mGoogleApiClient;
    String personname,personPhotoUrl,personemail;
    private boolean mSignInClicked;
    private static final int RC_SIGN_IN = 0;
    private boolean mIntentInProgress;
    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_login);

        buildGoogleApiClient();

        facebook = new Facebook(APP_ID);
        mAsyncRunner = new AsyncFacebookRunner(facebook);

        edt_email=(EditText)findViewById(R.id.edt_mail);
        edt_pwd=(EditText)findViewById(R.id.edt_pwd);

        login=(Button)findViewById(R.id.login);
        register=(Button)findViewById(R.id.register);
        btnfacebook=(Button)findViewById(R.id.facebook);
        google=(Button)findViewById(R.id.google);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv=new Intent(Login.this,Register.class);
                startActivity(iv);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                key="login";
                email=edt_email.getText().toString();
                password=edt_pwd.getText().toString();
                if (email!=null)
                {
                    if (password!=null)
                    {
                        new getlogin().execute();
                    }else
                    {
                        edt_pwd.setError("enter password");
                    }
                }else
                {
                    edt_email.setError("Enter Email");
                }
            }
        });

        btnfacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                key="facebook";
                loginToFacebook();
                getProfileInformation();
            }
        });


        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                key="google";
                mGoogleApiClient.connect();
                signInWithGplus();
                getProfileInformation1();
            }
        });

    }

    private void buildGoogleApiClient() {
        // TODO Auto-generated method stub
        mGoogleApiClient = new GoogleApiClient.Builder(Login.this).addConnectionCallbacks(Login.this)
                .addOnConnectionFailedListener(Login.this).addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();

        }
    }

    /**
     * Method to resolve any signin errors
     */
    private void resolveSignInError() {
        if (mGoogleApiClient.isConnected()) {
            //getProfileInformation1();
        } else {
            if (mConnectionResult.hasResolution()) {
                try {
                    mIntentInProgress = true;
                    mConnectionResult.startResolutionForResult(Login.this, RC_SIGN_IN);

                } catch (IntentSender.SendIntentException e) {
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }

    }

    // get user profile from google plus

    private void getProfileInformation1() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                personname = currentPerson.getDisplayName();
                personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                personemail = Plus.AccountApi.getAccountName(mGoogleApiClient);

                Log.d("mydata", "Name: " + personname + ", plusProfile: " + personGooglePlusProfile + ", email: "
                        + personemail + ", Image: " + personPhotoUrl);
                Log.d("image", "" + personPhotoUrl);

                if (personname != null) {
                    if (personemail != null) {
                        personname = personname.replace(" ", "%20");
                        personemail = personemail.replace(" ", "%20");
                        personPhotoUrl = personPhotoUrl.replace("?sz=50", "");
                        new getlogin().execute();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "Person information is null", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getProfileInformation() {
        Bundle bundle = new Bundle();
        bundle.putString("fields", "id,name,first_name,last_name,email,picture,gender,birthday,work");
        mAsyncRunner.request("me", bundle, new AsyncFacebookRunner.RequestListener() {
            @Override
            public void onComplete(String response, Object state) {
                mPrefs = getPreferences(MODE_PRIVATE);
                String app_id = mPrefs.getString("access_id", null);
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("fields", "id,name,first_name,last_name,email,picture,gender,birthday,work");
                    Log.d("bundle", facebook.request("me", bundle));
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Log.d("Profile", response);
                String json = response;
                try {
                    JSONObject profile = new JSONObject(json);

                    // getting name of the user
                    name = profile.getString("name");
                    // getting email of the user
                    imagefb = profile.getString("id");
                    email = profile.getString("email");
                    JSONObject picture = profile.getJSONObject("picture");
                    JSONObject data = picture.getJSONObject("data");
                    ppic = data.getString("url");
                    Log.d("ppic", "" + ppic);
                    Log.d("fbimage", "" + imagefb);
                    Log.d("fbname", "" + name);
                    if (name != null) {
                        if (ppic != null) {
                            name = name.replace(" ", "%20");

                            imagefb = "https://graph.facebook.com/" + imagefb + "/picture?type=large";
                            email = email.replace(" ", "%20");
                            new getlogin().execute();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
            }

            @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        });

    }

    public void loginToFacebook() {
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);

        if (access_token != null) {
            facebook.setAccessToken(access_token);

        }

        if (expires != 0) {
            facebook.setAccessExpires(expires);

        }

        if (!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] { "email", "publish_actions" },Facebook.FORCE_DIALOG_AUTH, new Facebook.DialogListener() {

                @Override
                public void onCancel() {
                    // Function to handle cancel event
                    Log.d("hello", "hello");
                }

                @Override
                public void onComplete(Bundle values) {


                    Log.d("hello", "hello1");
                    Log.d("accesstoken", "" + facebook.getAccessToken());
                    Log.d("accesstokenexp", "" + facebook.getAccessExpires());
                    Log.d("accesstokenid", "" + facebook.getAppId());

                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.putString("access_id", facebook.getAppId());
                    editor.commit();
                    getProfileInformation();
                }

                @Override
                public void onError(DialogError error) {
                    // Function to handle error
                    Log.d("hello", "hello2");
                }

                @Override
                public void onFacebookError(FacebookError fberror) {
                    // Function to handle Facebook errors
                    Log.d("hello", "hello3");
                }

            });
        }
        Log.d("login", "login");


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
                if (key.equals("login")) {
                    hp = new URL(getString(R.string.liveurl) + "app_userlogin.php?email=" + email + "&password="
                            + password);
                }else if (key.equals("facebook"))
                {
                    hp = new URL(getString(R.string.liveurl) + "adduser.php?login_with=facebook&email=" + email + "&name="
                            + name+"&image="+imagefb);
                }else if (key.equals("google"))
                {
                    hp = new URL(getString(R.string.liveurl) + "adduser.php?login_with=google&email=" + personemail + "&name="
                            + personname+"&image="+personPhotoUrl);
                }
                // hp = new URL(
                // "http://192.168.1.106/restourant/adduser.php?username="
                // + createusername + "&&email=" + createusermail);

                Log.e("userurl", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("input", "" + input);

                BufferedReader r = new BufferedReader(new InputStreamReader(input));

                String x = "";
                // x = r.readLine();
                String total = "";

                while (x != null) {
                    total += x;
                    x = r.readLine();
                }
                Log.e("totalid", "" + total);
                JSONArray jarr = new JSONArray(total);

                JSONObject Obj;
                Obj =jarr.getJSONObject(0);

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
            } catch (MalformedURLException e)
            {
                Error=e.getMessage();
            } catch (IOException e)
            {
                Error=e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            if (Error!=null) {

                Toast.makeText(Login.this, "Email id or password is incorrect", Toast.LENGTH_LONG).show();
            } else  {
                Log.e("datafb",""+userid+username+emailid+imageprofile);
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("score", "" + userid);
                editor.putString("username", "" + username);

                editor.putString("emailid", "" + emailid);
                editor.putString("picture", "" + imageprofile);

                editor.commit();
                Intent iv = new Intent(Login.this, Home.class);
                startActivity(iv);
                Toast.makeText(Login.this, "Register Successful..", Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* try {
            mGoogleApiClient.connect();
        } catch (IllegalStateException e) {
            // TODO: handle exception
        }*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;

        //Toast.makeText(this, "User is Connect", Toast.LENGTH_LONG).show();

        // Update the UI after signin
        updateUI(true);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        updateUI(false);
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            getProfileInformation1();

            // btnSignIn.setVisibility(View.GONE);
            // btnSignOut.setVisibility(View.VISIBLE);
            // btnRevokeAccess.setVisibility(View.VISIBLE);
            // llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            // btnSignIn.setVisibility(View.VISIBLE);
            // btnSignOut.setVisibility(View.GONE);
            // btnRevokeAccess.setVisibility(View.GONE);
            // llProfileLayout.setVisibility(View.GONE);
        }
    }
}
