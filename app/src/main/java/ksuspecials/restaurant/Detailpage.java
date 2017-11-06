package ksuspecials.restaurant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import ksuspecials.getset.Foodtype;
import ksuspecials.getset.Getsetfav;
import ksuspecials.getset.Restgetset;
import ksuspecials.util.AlertDialogManager;
import ksuspecials.util.ConnectionDetector;
import ksuspecials.util.DBAdapter;

public class Detailpage extends Activity {

	private Context mContext;

	String rating, name, type, id, distance, homedistance;
	Float number;
	RatingBar rb;
	String text,urlToShare,imageurl;
	ArrayList<Restgetset> rest;
	ArrayList<Foodtype> food;
	int start = 0;
	int currentindex = 0;
	int startIndex = 0;
	ProgressDialog pd;
	ImageView img_scroll;
	Button btn_contact, btn_map, btn_review, btn_share, btn_book, btn_fvrt, btn_fvrt1;
	DBAdapter myDbHelpel;
	SQLiteDatabase db;
	Cursor cur = null;
	String id1, name1, address, longitude, latitude;
	ArrayList<Getsetfav> FileList;
	String state, state1, stateid, curlat, curlng;
	public static final String MY_PREFS_NAME = "Restaurant";
	ProgressDialog progressDialog;
	Boolean isInternetPresent = false;
	ConnectionDetector cd;
	View layout12;
	private String Error = null;
	InterstitialAd mInterstitialAd;
	boolean interstitialCanceled;
	AlertDialogManager alert = new AlertDialogManager();
	int MainPosition = 0;
	String[] separated = null;
	ScaleAnimation makeSmaller;
	ScaleAnimation makeBigger;
	ImageView imageView;

	// Image Loader

	private ImageLoader imgLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
		setContentView(R.layout.activity_detailpage);

		cd = new ConnectionDetector(getApplicationContext());

		isInternetPresent = cd.isConnectingToInternet();
		// check for Internet status
		if (isInternetPresent) {
			// Internet Connection is Present
			// make HTTP requests
			// showAlertDialog(getApplicationContext(), "Internet Connection",
			// "You have internet connection", true);

			imgLoader = new ImageLoader(Detailpage.this);
			initialize();
			getintent();
			new getList().execute();
			new getrestaufulldetail().execute();
			new getfoodtypedetail().execute();
		} else {
			// Internet connection is not present
			// Ask user to connect to Internet
			RelativeLayout rl_back = (RelativeLayout) findViewById(R.id.rl_back);
			if (rl_back == null) {
				Log.d("second", "second");
				RelativeLayout rl_dialoguser = (RelativeLayout) findViewById(R.id.rl_infodialog);

				layout12 = getLayoutInflater().inflate(R.layout.connectiondialog, rl_dialoguser, false);

				rl_dialoguser.addView(layout12);
				rl_dialoguser.startAnimation(AnimationUtils.loadAnimation(Detailpage.this, R.anim.popup));
				Button btn_yes = (Button) layout12.findViewById(R.id.btn_yes);
				btn_yes.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						finish();
					}
				});
			}
			// showAlertDialog(getApplicationContext(),
			// "No Internet Connection",
			// "You don't have internet connection.", false);

		}
	}
	private void getintent() {
		// TODO Auto-generated method stub
		Intent iv = getIntent();
		rating = iv.getStringExtra("rating");
		name = iv.getStringExtra("name");
		type = iv.getStringExtra("foodtype");
		id = iv.getStringExtra("id");
		state1 = iv.getStringExtra("state");
		curlat = iv.getStringExtra("curlatitude");
		curlng = iv.getStringExtra("curlongitude");
		homedistance = iv.getStringExtra("distance");
		Log.d("type", "" + type);

		number = Float.parseFloat(rating);
		Log.d("number", "" + number);
		rb.setRating(number);
		mContext = this;
	}

	private void initialize() {
		// TODO Auto-generated method stub

		FileList = new ArrayList<Getsetfav>();
		rest = new ArrayList<Restgetset>();
		food = new ArrayList<Foodtype>();
		btn_fvrt = (Button) findViewById(R.id.btn_fvrt);
		btn_fvrt1 = (Button) findViewById(R.id.btn_fvrt1);
		rb = (RatingBar) findViewById(R.id.rate1);
	}

	// Image View Flipper

	class CustomPagerAdapter extends PagerAdapter {

		Context mContext;
		LayoutInflater mLayoutInflater;

		public CustomPagerAdapter(Context context) {
			mContext = context;
			mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return separated.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((RelativeLayout) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View itemView = mLayoutInflater.inflate(R.layout.image_pager, container, false);

			imageView = (ImageView) itemView.findViewById(R.id.image_page_fliper);

			/*
			 * Animation anim = AnimationUtils.loadAnimation(Detailpage.this,
			 * R.anim.rotate_in); Animation anim1 =
			 * AnimationUtils.loadAnimation(Detailpage.this, R.anim.rotate_out);
			 * 
			 * // imageView.setAnimation(anim); AnimationSet s = new
			 * AnimationSet(false);// false mean dont share // interpolators
			 * s.addAnimation(anim); s.addAnimation(anim1);
			 * 
			 * imageView.startAnimation(s);
			 */
			imageView.setImageResource(R.drawable.detail_page_img);
			imageurl = getResources().getString(R.string.liveurl) + "uploads/" + separated[position];
			Log.e("imageurl", imageurl);
			//imgLoader.DisplayImage(imageurl.replace(" ", "%20"), imageView);
			Picasso.with(Detailpage.this)
					.load(imageurl)
					.into(imageView);
			container.addView(itemView);

			return itemView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((RelativeLayout) object);
		}
	}

	// class for detail of foodtype
	public class getfoodtypedetail extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			URL hp = null;
			try {

				hp = new URL(getString(R.string.liveurl) + "foodtype.php?value=" + id);
				// hp = new URL(
				// "http://192.168.1.106/restourant/foodtype.php?value="
				// + id);
				Log.d("URL", "" + hp);
				URLConnection hpCon = hp.openConnection();
				hpCon.connect();
				InputStream input = hpCon.getInputStream();
				Log.d("input", "" + input);

				BufferedReader r = new BufferedReader(new InputStreamReader(input));

				String x = "";
				x = r.readLine();
				String total = "";

				while (x != null) {
					total += x;
					x = r.readLine();
				}
				Log.d("URL", "" + total);
				JSONObject jObject = new JSONObject(total);
				JSONArray j = jObject.getJSONArray("Foodtype");
				// JSONArray j = new JSONArray(total);

				Log.d("URL1", "" + j);
				for (int i = 0; i < j.length(); i++) {

					JSONObject Obj;
					Obj = j.getJSONObject(i);

					Foodtype temp = new Foodtype();
					temp.setImage_id(Obj.getString("id"));
					temp.setTbl_images(Obj.getString("food_type"));

					food.add(temp);
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO: handle exception
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			Log.d("img1111", "img");
			int start = 0;
			Foodtype food12 = food.get(start);
			Typeface tf = Typeface.createFromAsset(Detailpage.this.getAssets(), "fonts/Redressed.ttf");
			String type = food12.getTbl_images();

			final String[] separateddata = type.split(",");

			Log.d("split", "" + (separateddata.length));

			LinearLayout ll = (LinearLayout) findViewById(R.id.ll_hsv);
			android.widget.LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(260, 80, 1.0f);
			param.gravity = Gravity.CENTER;

			// programatically add button in horizontal scroll view
			Button[] btn = new Button[separateddata.length];
			for (int i = 0; i < separateddata.length; i++) {
				btn[i] = new Button(getApplicationContext());
				btn[i].setText(separateddata[i].toString());
				btn[i].setTypeface(tf);
				btn[i].setTextColor(Color.parseColor("#ffffff"));

				if (i % 2 == 0) {
					btn[i].setBackgroundResource(R.drawable.first_food_bg);
				} else {
					btn[i].setBackgroundResource(R.drawable.second_food_bg);
				}

				btn[i].setTextSize(14);

				btn[i].setLayoutParams(param);

				param.setMargins(5, 0, 5, 0);

				ll.addView(btn[i]);

			}

		}

	}

	// restaurant full detail class
	public class getrestaufulldetail extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(Detailpage.this);
			progressDialog.setMessage(getString(R.string.loading));
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			getdetailforNearMe();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			if (Error != null) {
				RelativeLayout rl_back = (RelativeLayout) findViewById(R.id.rl_back);
				if (rl_back == null) {
					Log.d("second", "second");
					RelativeLayout rl_dialoguser = (RelativeLayout) findViewById(R.id.rl_infodialog);

					layout12 = getLayoutInflater().inflate(R.layout.json_dilaog, rl_dialoguser, false);

					rl_dialoguser.addView(layout12);
					rl_dialoguser.startAnimation(AnimationUtils.loadAnimation(Detailpage.this, R.anim.popup));
					Button btn_yes = (Button) layout12.findViewById(R.id.btn_yes);
					btn_yes.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							finish();
						}
					});
				}

			} else {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();

					setdata();
				}
			}

		}

	}

	private void setdata() {
		// TODO Auto-generated method stub

		final Restgetset temp_Obj3 = rest.get(start);

		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

		if (prefs.getString("android_id", null) != null) {
			state = prefs.getString("android_id", null);

		} else {
			state = "off";
		}

		stateid = prefs.getString("stateid", null);

		Log.d("stateid", "" + stateid);
		Log.d("id", "" + id);
		Log.d("state", "" + state);
		/*
		 * if (state.equals("on") && stateid.equals(id)) {
		 * btn_fvrt1.setVisibility(View.VISIBLE);
		 * btn_fvrt.setVisibility(View.INVISIBLE); } else {
		 * btn_fvrt.setVisibility(View.VISIBLE);
		 * btn_fvrt1.setVisibility(View.INVISIBLE); }
		 */

		// set custom font
		Typeface tf = Typeface.createFromAsset(Detailpage.this.getAssets(), "fonts/gautami.ttf");
		Typeface tf1 = Typeface.createFromAsset(Detailpage.this.getAssets(), "fonts/OpenSans-Regular.ttf");
		Typeface tf2 = Typeface.createFromAsset(Detailpage.this.getAssets(), "fonts/calibri.ttf");
		// rb.setRating(Float.parseFloat(temp_Obj3.getRatting()));

		// on click of favourite button
		btn_fvrt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				state = "on";
				// stateid = temp_Obj3.getId();
				SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
				editor.putString("android_id", "" + state);
				editor.putString("stateid", "" + id);
				editor.commit();
				btn_fvrt1.setVisibility(View.VISIBLE);
				btn_fvrt.setVisibility(View.INVISIBLE);
				myDbHelpel = new DBAdapter(Detailpage.this);
				try {
					myDbHelpel.createDataBase();
				} catch (IOException io) {
					throw new Error("Unable TO Create DataBase");
				}
				try {
					myDbHelpel.openDataBase();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				db = myDbHelpel.getWritableDatabase();
				ContentValues values = new ContentValues();

				values.put("id", temp_Obj3.getId());
				values.put("name", temp_Obj3.getName());
				values.put("address", temp_Obj3.getAddress());
				values.put("latitude", temp_Obj3.getLatitude());
				values.put("longitude", temp_Obj3.getLongitude());
				values.put("distance", "" + homedistance);

				db.insert("favourite", null, values);

				myDbHelpel.close();
			}
		});

		btn_fvrt1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				state = "off";
				SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
				editor.putString("android_id", "" + state);
				editor.commit();
				btn_fvrt.setVisibility(View.VISIBLE);
				btn_fvrt1.setVisibility(View.INVISIBLE);

				DBAdapter myDbHelper = new DBAdapter(Detailpage.this);
				myDbHelper = new DBAdapter(Detailpage.this);
				try {
					myDbHelper.createDataBase();
				} catch (IOException e) {

					e.printStackTrace();
				}

				try {

					myDbHelper.openDataBase();

				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}

				int i = 1;
				db = myDbHelper.getWritableDatabase();

				cur = db.rawQuery("Delete from favourite where id=" + temp_Obj3.getId() + ";", null);
				if (cur.getCount() != 0) {
					if (cur.moveToFirst()) {
						do {
							Getsetfav obj = new Getsetfav();
							id1 = cur.getString(cur.getColumnIndex("id"));

							name1 = cur.getString(cur.getColumnIndex("name"));
							address = cur.getString(cur.getColumnIndex("address"));

							latitude = cur.getString(cur.getColumnIndex("latitude"));
							longitude = cur.getString(cur.getColumnIndex("longitude"));
							distance = cur.getString(cur.getColumnIndex("distance"));

							obj.setName(name1);
							obj.setAddress(address);
							obj.setLatitude(latitude);
							obj.setId(id1);
							obj.setLongitude(longitude);
							obj.setDistance(distance);

							FileList.add(obj);

						} while (cur.moveToNext());
					}
				}
				cur.close();
				db.close();
				myDbHelper.close();
			}
		});

		// click of book table button
		btn_book = (Button) findViewById(R.id.btn_book);
		btn_book.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainPosition = 1;
				if (getString(R.string.Detail_show_ads).equals("yes")) {
					if (interstitialCanceled) {

					} else {

						if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
						} else {

							ContinueIntent();
						}
					}
				} else {
					ContinueIntent();
				}
			}
		});

		// on click of vido button
		btn_contact = (Button) findViewById(R.id.btn_video);
		btn_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				 * Intent iv = new Intent(Detailpage.this, Videopage.class);
				 * iv.putExtra("videoid", "" + temp_Obj3.getVideo());
				 * startActivity(iv);
				 */

				String uri = "tel:" + temp_Obj3.getPhone_no();
				Intent i = new Intent(Intent.ACTION_CALL);
				i.setData(Uri.parse(uri));
				startActivity(i);

			}
		});

		// on click of map button
		btn_map = (Button) findViewById(R.id.btn_map);
		btn_map.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("mapcl","yes");
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?saddr=" + curlat + "," + curlng
								+ "&daddr=" + temp_Obj3.getLatitude() + "," + temp_Obj3.getLongitude()));
				intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
				startActivity(intent);
				/*MainPosition = 2;
				if (getString(R.string.Detail_show_ads).equals("yes")) {
					if (interstitialCanceled) {

					} else {

						if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
						} else {

							ContinueIntent();
						}
					}
				} else {
					ContinueIntent();
				}*/

			}
		});

		// on click of review button
		btn_review = (Button) findViewById(R.id.btn_review);
		btn_review.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainPosition = 3;
				if (getString(R.string.Detail_show_ads).equals("yes")) {
					if (interstitialCanceled) {

					} else {

						if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
						} else {

							ContinueIntent();
						}
					}
				} else {
					ContinueIntent();
				}
			}
		});

		TextView txt_header = (TextView) findViewById(R.id.txt_header);
		txt_header.setText(temp_Obj3.getName());
		txt_header.setTypeface(tf);

		TextView txt_add = (TextView) findViewById(R.id.txt_add);
		txt_add.setText(temp_Obj3.getAddress() + " " + temp_Obj3.getZipcode());
		txt_add.setTypeface(tf1);

		TextView txt_phone = (TextView) findViewById(R.id.txt_phone);
		txt_phone.setText(temp_Obj3.getPhone_no());
		txt_phone.setTypeface(tf2);

		TextView txt_timemf = (TextView) findViewById(R.id.txt_timemf);
		txt_timemf.setText(temp_Obj3.getTime());
		txt_timemf.setTypeface(tf);

		TextView txt_totalreview = (TextView) findViewById(R.id.txt_totalreview);
		txt_totalreview.setText(getString(R.string.totalrv) + temp_Obj3.getTotalreview());
		txt_totalreview.setTypeface(tf1);

		String tempimg = temp_Obj3.getImages();
		Log.d("img", "" + tempimg);

		separated = tempimg.split(",");
		Log.d("img123", "" + separated.length);

		CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(Detailpage.this);
		ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mCustomPagerAdapter);
		text=Html.fromHtml(temp_Obj3.getName())+"\n"+ Html.fromHtml(temp_Obj3.getAddress())+"\n"+"Email: " + Html.fromHtml(temp_Obj3.getEmail()) + "\n"
				+"Contact: "+Html.fromHtml(temp_Obj3.getPhone_no())+"\n"+
				"https://play.google.com/store/apps/details?id=" + Detailpage.this.getPackageName();
		btn_share = (Button) findViewById(R.id.btn_share);
		btn_share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				/*Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/" + "download");
				Intent share = new Intent(android.content.Intent.ACTION_SEND);
				share.setType("text/plain");
				share.setType("image/jpeg");
				share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				share.putExtra(Intent.EXTRA_SUBJECT, "Restaurant");
				share.putExtra(Intent.EXTRA_STREAM, imageUri);
				share.putExtra(Intent.EXTRA_TEXT,
						"https://play.google.com/store/apps/details?id=" + Detailpage.this.getPackageName() + "\n"
								+ "Email: " + temp_Obj3.getEmail() + "\n" + "Address: " + temp_Obj3.getAddress());
				startActivity(Intent.createChooser(share, getString(R.string.sharelink)));*/

				AlertDialog alertDialog = new AlertDialog.Builder(Detailpage.this).create();

				alertDialog.setTitle("Share");

				alertDialog.setMessage("Share restaurant information with");

				// share on gmail,hike etc

				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "More",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {

								// ...
								try {
									text= Html.fromHtml(temp_Obj3.getName())+"\n"+ Html.fromHtml(temp_Obj3.getAddress())+"\n"+
											"https://play.google.com/store/apps/details?id=" + Detailpage.this.getPackageName();

									urlToShare = "https://play.google.com/store/apps/details?id="
											+ Detailpage.this.getPackageName();
									String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;

									Uri bmpUri = getLocalBitmapUri(imageView);

									Intent share = new Intent(android.content.Intent.ACTION_SEND);
									share.setType("text/plain");
									share.setType("image/*");

									share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
									share.putExtra(Intent.EXTRA_SUBJECT, "Place Finder");

									share.putExtra("android.intent.extra.TEXT", text);
									share.putExtra(Intent.EXTRA_STREAM, bmpUri);

									startActivity(Intent.createChooser(share, "Share"));
								} catch (NullPointerException e) {
									// TODO: handle exception
								}

							}
						});

				// share on whatsapp

				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"Whatsapp",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {

								// ...
								try {
									Uri bmpUri = getLocalBitmapUri(imageView);
									Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
									whatsappIntent.setType("text/plain");
									whatsappIntent.setType("image/*");
									whatsappIntent.setPackage("com.whatsapp");

									whatsappIntent.putExtra(Intent.EXTRA_TEXT, text);
									whatsappIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
									Detailpage.this.startActivity(whatsappIntent);
								} catch (NullPointerException e) {
									// TODO: handle exception
								} catch (android.content.ActivityNotFoundException ex) {
									Toast.makeText(Detailpage.this, "Whatsapp have not been installed.", Toast.LENGTH_LONG)
											.show();
								}

							}
						});

				final String image =rest.get(0).getImages().replace(" ","%20");
				// share on facebook

				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Facebook",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								text=Html.fromHtml(temp_Obj3.getAddress())+"\n"+"Email: " + Html.fromHtml(temp_Obj3.getEmail()) + "\n"
										+"Contact: "+Html.fromHtml(temp_Obj3.getPhone_no());
								String link="https://play.google.com/store/apps/details?id=" + Detailpage.this.getPackageName();
								FacebookSdk.sdkInitialize(getApplicationContext());
								ShareDialog shareDialog = new ShareDialog(Detailpage.this);
								if (ShareDialog.canShow(ShareLinkContent.class)) {
									ShareLinkContent linkContent = new ShareLinkContent.Builder()
											.setContentTitle(rest.get(0).getName())
											.setContentDescription(text)
											.setContentUrl(Uri.parse(link))
											.setImageUrl(Uri.parse(imageurl))

											.build();
									shareDialog.show(linkContent);

								}
							}
						});
				alertDialog.show();
			}
		});

		// on click imageview

	}
	public Uri getLocalBitmapUri(ImageView imageView) {
		// Extract Bitmap from ImageView drawable
		Drawable drawable = imageView.getDrawable();
		Bitmap bmp = null;
		if (drawable instanceof BitmapDrawable) {
			bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
		} else {
			return null;
		}
		// Store image to default external storage directory
		Uri bmpUri = null;
		try {

			File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
					"share_image_" + System.currentTimeMillis() + ".png");
			FileOutputStream out = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 10, out);
			out.close();
			bmpUri = Uri.fromFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bmpUri;
	}
	private void getdetailforNearMe() {
		// TODO Auto-generated method stub

		URL hp = null;
		try {

			hp = new URL(getString(R.string.liveurl) + "restaurantdetail.php?value=" + id);

			Log.d("URL", "" + hp);
			URLConnection hpCon = hp.openConnection();
			hpCon.connect();
			InputStream input = hpCon.getInputStream();
			Log.d("input", "" + input);

			BufferedReader r = new BufferedReader(new InputStreamReader(input));

			String x = "";
			x = r.readLine();
			String total = "";

			while (x != null) {
				total += x;
				x = r.readLine();
			}
			Log.d("URLTOTAL", "" + total);
			JSONObject jObject = new JSONObject(total);
			JSONArray j = jObject.getJSONArray("Restaurant");
			// JSONArray j = new JSONArray(total);

			Log.d("URL1", "" + j);
			for (int i = 0; i < j.length(); i++) {

				JSONObject Obj;
				Obj = j.getJSONObject(i);

				Restgetset temp = new Restgetset();
				temp.setName(Obj.getString("name"));
				temp.setId(Obj.getString("id"));
				temp.setAddress(Obj.getString("address"));
				temp.setLatitude(Obj.getString("latitude"));
				temp.setLongitude(Obj.getString("longitude"));
				temp.setRatting(Obj.getString("ratting"));
				temp.setZipcode(Obj.getString("zipcode"));
				temp.setEmail(Obj.getString("email"));
				temp.setVegtype(Obj.getString("vegtype"));
				temp.setTime(Obj.getString("time"));
				temp.setPhone_no(Obj.getString("phone_no"));
				temp.setImages(Obj.getString("images"));
				temp.setVideo(Obj.getString("video"));
				temp.setTotalreview(Obj.getString("totalreview"));
				// temp.setDistance(Obj.getString("distance"));
				rest.add(temp);
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Error = e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Error = e.getMessage();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Error = e.getMessage();
		} catch (NullPointerException e) {
			// TODO: handle exception
			Error = e.getMessage();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detailpage, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();

		interstitialCanceled = false;
		CallNewInsertial();

	}

	private void CallNewInsertial() {
		cd = new ConnectionDetector(Detailpage.this);

		if (!cd.isConnectingToInternet()) {
			alert.showAlertDialog(Detailpage.this, "Internet Connection Error",
					"Please connect to working Internet connection", false);
			return;
		} else {
			// AdView mAdView = (AdView) findViewById(R.id.adView);
			// AdRequest adRequest = new AdRequest.Builder().build();
			// mAdView.loadAd(adRequest);
			if (getString(R.string.Detail_show_ads).equals("yes")) {
				Log.d("ad", "" + getString(R.string.Home_show_ads));
				mInterstitialAd = new InterstitialAd(this);
				mInterstitialAd.setAdUnitId(getString(R.string.insertial_ad_key));

				requestNewInterstitial();

				mInterstitialAd.setAdListener(new AdListener() {

					@Override
					public void onAdClosed() {
						ContinueIntent();
					}

				});
			} else {
				// ContinueIntent();
			}
		}
	}

	private void ContinueIntent() {
		Restgetset temp_Obj3 = rest.get(start);
		if (MainPosition == 1) {

			//Intent iv = new Intent(Detailpage.this, Booktable.class);
			//iv.putExtra("email", "" + temp_Obj3.getEmail());
			//iv.putExtra("contact", "" + temp_Obj3.getPhone_no());
			//startActivity(iv);
		} else if (MainPosition == 2) {
		/*	Intent iv = new Intent(Detailpage.this, MainActivity.class);
			iv.putExtra("lat", "" + temp_Obj3.getLatitude());
			iv.putExtra("lng", "" + temp_Obj3.getLongitude());
			iv.putExtra("nm", "" + temp_Obj3.getName());
			iv.putExtra("ad", "" + temp_Obj3.getAddress());
			iv.putExtra("id", "" + temp_Obj3.getId());
			iv.putExtra("rate", "" + rating);
			iv.putExtra("curlat", "" + curlat);
			iv.putExtra("curlng", "" + curlng);
			startActivity(iv);*/
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse("http://maps.google.com/maps?saddr=" + curlat + "," + curlng
							+ "&daddr=" + temp_Obj3.getLatitude() + "," + temp_Obj3.getLongitude()));
			intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		} else if (MainPosition == 3) {
			Intent iv = new Intent(getApplicationContext(), Review.class);
			iv.putExtra("id", "" + temp_Obj3.getId());
			startActivity(iv);
		}
	}

	private void requestNewInterstitial() {
		AdRequest adRequest = new AdRequest.Builder().build();

		mInterstitialAd.loadAd(adRequest);

	}

	@Override
	public void onPause() {
		mInterstitialAd = null;
		interstitialCanceled = true;
		super.onPause();
	}

	private class getList extends AsyncTask<String, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub

			FileList.clear();
			DBAdapter myDbHelper = new DBAdapter(Detailpage.this);
			myDbHelper = new DBAdapter(Detailpage.this);
			try {
				myDbHelper.createDataBase();
			} catch (IOException e) {

				e.printStackTrace();
			}

			try {

				myDbHelper.openDataBase();

			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}

			int i = 1;
			db = myDbHelper.getReadableDatabase();

			try {
				cur = db.rawQuery("select * from favourite where id=" + id + ";", null);
				Log.d("SIZWA", "" + cur.getCount());
				if (cur.getCount() != 0) {
					if (cur.moveToFirst()) {
						do {
							Getsetfav obj = new Getsetfav();
							id1 = cur.getString(cur.getColumnIndex("id"));

							name = cur.getString(cur.getColumnIndex("name"));
							address = cur.getString(cur.getColumnIndex("address"));

							latitude = cur.getString(cur.getColumnIndex("latitude"));
							longitude = cur.getString(cur.getColumnIndex("longitude"));
							distance = cur.getString(cur.getColumnIndex("distance"));
							obj.setName(name);
							obj.setAddress(address);
							obj.setLatitude(latitude);
							obj.setId(id1);
							obj.setLongitude(longitude);
							obj.setDistance(distance);

							FileList.add(obj);

						} while (cur.moveToNext());

					}

				}

				cur.close();
				db.close();
				myDbHelper.close();
			} catch (Exception e) {
				// TODO: handle exception
			}

			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {

			if (FileList.size() == 0) {
				// Toast.makeText(getApplicationContext(),
				// getString(R.string.norecord), Toast.LENGTH_LONG).show();
				btn_fvrt.setVisibility(View.VISIBLE);
				btn_fvrt1.setVisibility(View.INVISIBLE);
			} else {

				btn_fvrt1.setVisibility(View.VISIBLE);
				btn_fvrt.setVisibility(View.INVISIBLE);

			}

		}
	}

}
