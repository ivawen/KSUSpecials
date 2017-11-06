package ksuspecials.restaurant;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Profile extends Activity {
    String username,emailid,picture;
    public static final String MY_PREFS_NAME = "Restaurant";
    ImageView imag_profile;
    EditText edt_name,edt_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        // check user is created or not

        username = prefs.getString("username", null);
        emailid = prefs.getString("emailid", null);
        picture = prefs.getString("picture", null);

        edt_mail=(EditText)findViewById(R.id.edt_mail);
        edt_name=(EditText)findViewById(R.id.edt_name);
        imag_profile=(ImageView)findViewById(R.id.img_profile);

        edt_mail.setText(emailid);
        edt_name.setText(username);

        Picasso.with(Profile.this)
                .load(picture)
                .into(imag_profile);
    }
}
