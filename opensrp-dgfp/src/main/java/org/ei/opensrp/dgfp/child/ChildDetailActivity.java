package org.ei.opensrp.dgfp.child;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.dgfp.R;
import org.ei.opensrp.domain.Alert;


import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ImageCache;
import util.ImageFetcher;

import static org.ei.opensrp.util.StringUtil.humanize;

/**
 * Created by raihan on 5/11/15.
 */
public class ChildDetailActivity extends Activity {

    //image retrieving
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private static int mImageThumbSize;
    private static int mImageThumbSpacing;

    private static ImageFetcher mImageFetcher;




    //image retrieving

    public static CommonPersonObjectClient ChildClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.child_detail_activity);
        TextView name = (TextView) findViewById(R.id.childid);
        TextView fathersname = (TextView) findViewById(R.id.fathersname);
        TextView mothersname = (TextView) findViewById(R.id.mothersname);

        TextView age = (TextView) findViewById(R.id.age);
        TextView jivitahhid = (TextView) findViewById(R.id.jivitahhid);
        TextView godhhid = (TextView) findViewById(R.id.gobhhid);
        TextView village = (TextView) findViewById(R.id.village);

        ImageButton back = (ImageButton) findViewById(org.ei.opensrp.R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AllCommonsRepository allchildRepository = Context.getInstance().allCommonsRepositoryobjects("members");
        CommonPersonObject childobject = allchildRepository.findByCaseID(ChildClient.entityId());
        AllCommonsRepository motherrep = Context.getInstance().allCommonsRepositoryobjects("members");
        final CommonPersonObject mcaremotherObject = motherrep.findByCaseID(childobject.getRelationalId());


        name.setText(humanize((ChildClient.getDetails().get("FWBNFCHILDNAME") != null ? ChildClient.getDetails().get("FWBNFCHILDNAME") : "").replace("+", "_")));
        fathersname.setText(Html.fromHtml(getString(R.string.child_details_fathers_name_label) + "<b> " + humanize((ChildClient.getDetails().get("FWHUSNAME") != null ? ChildClient.getDetails().get("FWHUSNAME") : "")) + "</b>"));
        mothersname.setText(Html.fromHtml(getString(R.string.child_details_mothers_name_label) +"<b> "+ humanize((ChildClient.getColumnmaps().get("FWWOMFNAME") != null ? ChildClient.getColumnmaps().get("FWWOMFNAME") : ""))+ "</b>"));

        age.setText(Html.fromHtml(getString(R.string.elco_age_label) +"<b> "+ age(ChildClient) + " days "+ "</b>"));
        godhhid.setText(Html.fromHtml(getString(R.string.hhid_gob_elco_label) +"<b> "+ (ChildClient.getColumnmaps().get("GOBHHID")!=null?ChildClient.getColumnmaps().get("GOBHHID"):"")+ "</b>"));
        jivitahhid.setText(Html.fromHtml(getString(R.string.hhiid_jivita_elco_label)+"<b> "+(ChildClient.getColumnmaps().get("JiVitAHHID")!=null?ChildClient.getColumnmaps().get("JiVitAHHID"):"")+ "</b>"));
        village.setText(Html.fromHtml(getString(R.string.elco_details_mauza) + "<b> " + humanize((ChildClient.getDetails().get("mauza") != null ? ChildClient.getDetails().get("mauza") : "").replace("+", "_"))+ "</b>"));
        String type_of_delivery = ChildClient.getDetails().get("FWPNC1DELTYPE") != null ? ChildClient.getDetails().get("FWPNC1DELTYPE") : "";






//       checkEncc1view(ChildClient);
//       checkEncc2view(ChildClient);
//        checkEncc3view(ChildClient);
//        checktempView(ChildClient);
       doolay(ChildClient);
        assign_text_to_givenView(ChildClient,(TextView)findViewById(R.id.critical_disease_problem),"Diseases_Prob");
        assign_text_to_givenView(ChildClient,(TextView)findViewById(R.id.referred),"Has_Referred");
        assign_text_to_givenView(ChildClient,(TextView)findViewById(R.id.vaccination_history),"Vaccines");

//        final ImageView householdview = (ImageView) findViewById(R.id.householdprofileview);
//
//        if (ChildClient.getDetails().get("profilepic") != null) {
//            setImagetoHolder(ChildDetailActivity.this, ChildClient.getDetails().get("profilepic"), householdview, R.mipmap.woman_placeholder);
//        }



    }

    private Long age(CommonPersonObjectClient ancclient) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date edd_date = format.parse(ancclient.getDetails().get("FWBNFDOB")!=null?ancclient.getDetails().get("FWBNFDOB"):"");
            Calendar thatDay = Calendar.getInstance();
            thatDay.setTime(edd_date);

            Calendar today = Calendar.getInstance();

            long diff = today.getTimeInMillis() - thatDay.getTimeInMillis();

            long days = diff / (24 * 60 * 60 * 1000);

            return days;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }
    private void doolay(CommonPersonObjectClient ancclient) {

        TextView edd = (TextView)findViewById(R.id.date_of_outcome);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date edd_date = format.parse(ancclient.getDetails().get("FWBNFDOB")!=null?ancclient.getDetails().get("FWBNFDOB"):"");
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(edd_date);
            edd_date.setTime(calendar.getTime().getTime());
            edd.setText(format.format(edd_date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }




    private void assign_text_to_givenView(CommonPersonObjectClient ecclient,TextView tview,String detailvariable) {
        String text = ecclient.getDetails().get(detailvariable)!=null?ecclient.getDetails().get(detailvariable):"N/A";
        tview.setText(text);
    }








    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    static final int REQUEST_TAKE_PHOTO = 1;
   static ImageView mImageView;
    static File currentfile;
    static String bindobject;
    static String entityid;
    private void dispatchTakePictureIntent(ImageView imageView) {
        mImageView = imageView;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                currentfile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            String imageBitmap = (String) extras.get(MediaStore.EXTRA_OUTPUT);
//            Toast.makeText(this,imageBitmap,Toast.LENGTH_LONG).show();
            HashMap <String,String> details = new HashMap<String,String>();
            details.put("profilepic",currentfile.getAbsolutePath());
            saveimagereference(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
        }
    }
    public void saveimagereference(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
//                Elcoclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
    }
    public static void setImagetoHolder(Activity activity,String file, ImageView view, int placeholder){
        mImageThumbSize = 300;
        mImageThumbSpacing = Context.getInstance().applicationContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);


        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, IMAGE_CACHE_DIR);
             cacheParams.setMemCacheSizePercent(0.50f); // Set memory cache to 25% of app memory
        mImageFetcher = new ImageFetcher(activity, mImageThumbSize);
        mImageFetcher.setLoadingImage(placeholder);
        mImageFetcher.addImageCache(activity.getFragmentManager(), cacheParams);
//        Toast.makeText(activity,file,Toast.LENGTH_LONG).show();
        mImageFetcher.loadImage("file:///"+file,view);

//        Uri.parse(new File("/sdcard/cats.jpg")






//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(file, options);
//        view.setImageBitmap(bitmap);
    }
}