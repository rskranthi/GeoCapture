package kranthi.com.geocapture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class cameraActivity extends ActionBarActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    static final int CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE = 1777;
    private Bitmap selectedImage;
    AlertDialog alert;
    ImageView imageView;
    TextView gpsTextView;
    TextView cityTextView;
    private Boolean selectedFromGallery;
    //    EditText latitudeEditText;
//    EditText longitudeEditText;
//    LinearLayout infoPanel;
    private MyLocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        imageView=(ImageView)findViewById(R.id.mainImageView);
        gpsTextView=(TextView)findViewById(R.id.gpsTextView);
        cityTextView=(TextView)findViewById(R.id.cityTextView);
        Typeface font = Typeface.createFromAsset(getAssets(), "Bebas Neue.ttf");
        gpsTextView.setTypeface(font);
        cityTextView.setTypeface(font);
        locationListener=new MyLocationListener(getApplicationContext());
//        infoPanel=(LinearLayout)findViewById(R.id.infoPanel);

        selectedFromGallery=getIntent().getBooleanExtra("selectedFromGallery", false);
        if(selectedFromGallery){
            Uri selectedImageUri=Uri.parse(getIntent().getStringExtra("selectedImageFileName"));
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imageView.setImageBitmap(selectedImage);
                File path = Environment.getExternalStorageDirectory();
                String fileName=getFilename(selectedImageUri);
                File file = new File(path + "/GeoCapture/",fileName);

                ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                GPSalertBuilder(exif);
                gpsTextView.setText(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)+", "+exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
                cityTextView.setText(exif.getAttribute("UserComment"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else{
            Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
        }
    }
    private void GPSalertBuilder(ExifInterface exif){
        String city=exif.getAttribute("UserComment");
        if(city==null){
            city="Not Found";
        }
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("City:"+city+"\nLatitude: "+exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)+"\nLongitude: "+exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)+"\nLatitude Ref: "+exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)+"\nLongitude Ref: "+exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF))
                .setCancelable(false)
                .setTitle("GPS data")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                    }
                }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }
    public String getFilename(Uri uri)
    {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    public void takephotoOnClick(View v){
        /*// create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        //imageView.setImageURI(fileUri);*/
        LocationManager locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        else{
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
        }
    }
    private void buildAlertMessageNoGps() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                //Toast.makeText(this, "Image Captured", Toast.LENGTH_LONG).show();
                //Get our saved file into a bitmap object:
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
                Bitmap bitmap = decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);
                imageView.setImageBitmap(bitmap);


                Location loc=locationListener.getLocation();
                if(loc!=null){
                    Double latitude=loc.getLatitude();
                    Double longitude=loc.getLongitude();
                    Log.i("GPS:", "Latitude:" + latitude + " Longitude:" + longitude);
                    String city=locationListener.getCity();
                    gpsTextView.setText(String.valueOf(latitude)+", "+String.valueOf(longitude));
                    if(city!=null){
                        cityTextView.setText(city);
                    }
                    SaveImage(bitmap,latitude,longitude,city);
                    /*latitudeEditText.setText("Latitude:       " + String.valueOf(latitude));
                    longitudeEditText.setText("Longitude:   "+String.valueOf(longitude));

                    if(city!=null){
                        addCityToInfoPanel(city);
                    }*/
                }
                else{
                    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("The location was not recieved");
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
                    alertDialog.setIcon(R.drawable.cameraicon);
                    alertDialog.show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("The Image was not formed correctly, please try again");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                    }
                });
                alertDialog.setIcon(R.drawable.cameraicon);
                alertDialog.show();
            }
        }
    }

    private void SaveImage(Bitmap finalBitmap,Double latitude,Double longitude,String city) {

        File path = Environment.getExternalStorageDirectory();
        OutputStream fOutputStream = null;
        File file = new File(path + "/GeoCapture/", "capture"+System.currentTimeMillis()+".jpg");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            fOutputStream = new FileOutputStream(file);

            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();

            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            Log.i("EXIF:","Latitude"+String.valueOf(latitude)+" Longitude"+String.valueOf(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPS.convert(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,GPS.latitudeRef(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,GPS.convert(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,GPS.longitudeRef(longitude));
            if(city!=null){
                exif.setAttribute("UserComment",city);
            }
            exif.saveAttributes();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
            return;
        }
    }
//    private void addCityToInfoPanel(String city) {
//        EditText cityEditText=new EditText(this);
//        if(infoPanel.getChildCount()<3){
//            infoPanel.addView(cityEditText,0);
//            cityEditText.setText("City:              "+city);
//            cityEditText.setFocusable(false);
//            cityEditText.setClickable(false);
//            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(dpToPx(25),dpToPx(10),dpToPx(25),dpToPx(0));
//            cityEditText.setLayoutParams(layoutParams);
//        }
//        else{
//            cityEditText=(EditText)infoPanel.getChildAt(0);
//            cityEditText.setText("City:              "+city);
//        }
//
//    }

    private static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    //super.onActivityResult(requestCode, resultCode, data);
    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyPhotoApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onDestroy() {
        if (alert != null) {
            alert.dismiss();
        }
        super.onDestroy();
    }
}
