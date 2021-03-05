package com.nexysquare.ddoyac.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nexysquare.ddoyac.core.BackgroundTask;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.util.BitmapUtil;
import com.nexysquare.ddoyac.util.MatConvertor;
import com.nexysquare.ddoyac.util.PermissionManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimilarityTestActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static Bitmap bmp, yourSelectedImage, bmpimg1, bmpimg2;
    private ImageView iv1, iv2;
    private TextView tv;
    private TextView descriptor_txt;
    private static String path1, path2;
    private static String text;
    private Button start, descriptor_btn;
    private static int imgNo = 0;
    private static Uri selectedImage;
    private static InputStream imageStream;
    private static long startTime, endTime;
    private static final int SELECT_PHOTO = 100;

//    private static int descriptor = DescriptorExtractor.BRISK;
    private static String descriptorType;
    private static int min_dist = 40;
    private static int min_matches = 100;


    private static String des1, des2;

//    public CompMainActivity() {
//        Log.i(TAG, "Instantiated new " + this.getClass());
//    }
    public static void open(Context context) {
        Intent intent = new Intent(context, SimilarityTestActivity.class);
        context.startActivity(intent);
    }

    public AppCompatActivity getActivity(){
        return this;
    }
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.similarity_test_main);

        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
        iv1 = (ImageView) findViewById(R.id.img1);
        iv2 = (ImageView) findViewById(R.id.img2);
        start = (Button) findViewById(R.id.button1);
        tv = (TextView) findViewById(R.id.tv);
        descriptor_btn = findViewById(R.id.button2);
        descriptor_txt = findViewById(R.id.descriptor_txt);
        run();

        checkPath();
    }

    private void checkPath(){
        PermissionManager.checkPermission(SimilarityTestActivity.this);
        String path = Environment
                .getExternalStorageDirectory()
                .getAbsolutePath()
                + "/opencv4test/Descriptor.txt";


        Log.d("file path" , path);
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        min_dist = newIntent.getExtras().getInt("min_dist");
//        descriptor = newIntent.getExtras().getInt("descriptor");
        min_matches = newIntent.getExtras().getInt("min_matches");
        run();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {

                    selectedImage = imageReturnedIntent.getData();

                    try {
                        imageStream = getContentResolver().openInputStream(
                                selectedImage);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    Bitmap resized = BitmapUtil.resizeBitmapImage(yourSelectedImage, 150);

                    if (imgNo == 1) {
                        iv1.setImageBitmap(resized);
                        path1 = selectedImage.getPath();
                        bmpimg1 = resized;
                        iv1.invalidate();
                    } else if (imgNo == 2) {
                        iv2.setImageBitmap(resized);
                        path2 = selectedImage.getPath();
                        bmpimg2 = resized;
                        iv2.invalidate();
                    }
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }



    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public void run() {
//        if (descriptor == DescriptorExtractor.BRIEF)
//            descriptorType = "BRIEF";
//        else if (descriptor == DescriptorExtractor.BRISK)
//            descriptorType = "BRISK";
//        else if (descriptor == DescriptorExtractor.FREAK)
//            descriptorType = "FREAK";
//        else if (descriptor == DescriptorExtractor.ORB)
//            descriptorType = "ORB";
//        else if (descriptor == DescriptorExtractor.SIFT)
//            descriptorType = "SIFT";
//        else if(descriptor == DescriptorExtractor.SURF)
//            descriptorType = "SURF";
//        System.out.println(descriptorType);
//        tv.setText("Select the two images to be compared.\n"+"DescriptorExtractor:"+descriptorType+"\nHamming distance between descriptors:"+min_dist+"\nMinimum number of good matches:"+min_matches);
        tv.setText("비교할 두 이미지를 선택해주세요.");
        iv1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                imgNo = 1;

            }
        });
        iv2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                imgNo = 2;
            }
        });


        descriptor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new aDesTask(getActivity()).execute();

                startTime = System.currentTimeMillis();
            }
        });


        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (bmpimg1 != null && bmpimg2 != null) {
					/*if(bmpimg1.getWidth()!=bmpimg2.getWidth()){
						bmpimg2 = Bitmap.createScaledBitmap(bmpimg2, bmpimg1.getWidth(), bmpimg1.getHeight(), true);
					}*/
                    bmpimg1 = Bitmap.createScaledBitmap(bmpimg1, 100, 100, true);
                    bmpimg2 = Bitmap.createScaledBitmap(bmpimg2, 100, 100, true);

                    Mat img1 = new Mat();
                    Utils.bitmapToMat(bmpimg1, img1);
                    Mat img2 = new Mat();
                    Utils.bitmapToMat(bmpimg2, img2);

                    Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
                    Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);
                    img1.convertTo(img1, CvType.CV_32F);
                    img2.convertTo(img2, CvType.CV_32F);
                    //Log.d("ImageComparator", "img1:"+img1.rows()+"x"+img1.cols()+" img2:"+img2.rows()+"x"+img2.cols());

                    Mat hist1 = new Mat();
                    Mat hist2 = new Mat();
                    MatOfInt histSize = new MatOfInt(180);
                    MatOfInt channels = new MatOfInt(0);
                    ArrayList<Mat> bgr_planes1= new ArrayList<Mat>();
                    ArrayList<Mat> bgr_planes2= new ArrayList<Mat>();
                    Core.split(img1, bgr_planes1);
                    Core.split(img2, bgr_planes2);

                    MatOfFloat histRanges = new MatOfFloat (0f, 180f);
                    boolean accumulate = false;
                    Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, accumulate);
                    Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
                    Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, accumulate);
                    Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());
                    img1.convertTo(img1, CvType.CV_32F);
                    img2.convertTo(img2, CvType.CV_32F);
                    hist1.convertTo(hist1, CvType.CV_32F);
                    hist2.convertTo(hist2, CvType.CV_32F);

                    double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
                    Log.d("ImageComparator", "compare: "+compare);
//                    if(compare>0 && compare<1500) {
//                    if(compare>0 && compare<1500) {
//                        Toast.makeText(getApplicationContext(), "Images may be possible duplicates, verifying", Toast.LENGTH_LONG).show();
//                        new asyncTask(this).execute();
                        new aTask(getActivity()).execute();
//                        new BackgroundTask(getActivity()){
//
//                            @Override
//                            public void doInBackground() {
//
//                            }
//
//                            @Override
//                            public void onPostExecute() {
//
//                            }

//                        }.execute();
//                    }
//                    else if(compare==0)
//                        Toast.makeText(getApplicationContext(), "Images are exact duplicates", Toast.LENGTH_LONG).show();
//                    else
//                        Toast.makeText(getApplicationContext(), "Images are not duplicates", Toast.LENGTH_LONG).show();

                    Toast.makeText(getApplicationContext(),"compare : " + compare , Toast.LENGTH_LONG).show();
                    startTime = System.currentTimeMillis();
                } else
                    Toast.makeText(getApplicationContext(),
                            "You haven't selected images.", Toast.LENGTH_LONG)
                            .show();
            }
        });
    }

    private void process(){

    }

//    public abstract class BackgroundTask {
//
//        protected Activity activity;
//        public BackgroundTask(Activity activity) {
//            this.activity = activity;
//
//        }
//
//        private void startBackground() {
//            new Thread(new Runnable() {
//                public void run() {
//
//                    doInBackground();
//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//
//                            onPostExecute();
//                        }
//                    });
//                }
//            }).start();
//        }
//        public void execute(){
//            startBackground();
//        }
//
//        public abstract void doInBackground();
//        public abstract void onPostExecute();
//
//    }


    class aDesTask extends BackgroundTask {
        private Mat descriptors, dupDescriptors;
        private DescriptorMatcher matcher;
        private ProgressDialog pd;
        private MatOfDMatch matches, matches_final_mat;
        public aDesTask(Activity activity) {
            super(activity);
            pd = new ProgressDialog(activity);
            pd.setIndeterminate(true);
            pd.setCancelable(true);
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage("Processing...");
            pd.show();
        }

        @Override
        public void doInBackground() {

            try{

                matcher = DescriptorMatcher
                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);



                matches = new MatOfDMatch();

                if(des1!=null && des2 !=null){
//                    des1 = MatConvertor.matToJson(descriptors);
//                    des2 = MatConvertor.matToJson(dupDescriptors);

                    descriptors = MatConvertor.matFromJson(des1);
                    dupDescriptors = MatConvertor.matFromJson(des2);

                    matcher.match(descriptors, dupDescriptors, matches);
                    Log.d("LOG!", "Matches Size " + matches.size());
                    // New method of finding best matches
                    List<DMatch> matchesList = matches.toList();
                    List<DMatch> matches_final = new ArrayList<DMatch>();
                    for (int i = 0; i < matchesList.size(); i++) {
                        if (matchesList.get(i).distance <= min_dist) {
                            matches_final.add(matches.toList().get(i));
                        }
                    }

                    matches_final_mat = new MatOfDMatch();
                    matches_final_mat.fromList(matches_final);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPostExecute() {
            try {

                List<DMatch> finalMatchesList = matches_final_mat.toList();
                final int matchesFound = finalMatchesList.size();
                endTime = System.currentTimeMillis();
//                if (finalMatchesList.size() > min_matches)// dev discretion for
//                // number of matches to
//                // be found for an image
//                // to be judged as
//                // duplicate
//                {
//                    text = finalMatchesList.size()
//                            + " matches were found. Possible duplicate image.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = true;
//                } else {
//                    text = finalMatchesList.size()
//                            + " matches were found. Images aren't similar.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = false;
//                }
               String text = finalMatchesList.size()+ " matches were found.\nTime taken="+ (endTime - startTime) + "ms";;
                pd.dismiss();

                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

//                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
//                        activity);
//                alertDialog.setTitle("Result");
//                alertDialog.setCancelable(true);
//                LayoutInflater factory = LayoutInflater.from(activity);
//                final View view = factory.inflate(R.layout.view_image, null);
//                ImageView matchedImages = (ImageView) view
//                        .findViewById(R.id.finalImage);
//                matchedImages.setImageBitmap(bmp);
//                matchedImages.invalidate();
////                final CheckBox shouldBeDuplicate = (CheckBox) view.findViewById(R.id.checkBox);
//                TextView message = (TextView) view.findViewById(R.id.message);
//                message.setText(text);
//                alertDialog.setView(view);
////                shouldBeDuplicate
////                        .setText("These images are actually duplicates.");
//                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
//                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    class aTaskSIFT extends BackgroundTask {
        private Mat img1, img2, mask1, mask2, descriptors, dupDescriptors;
        private FastFeatureDetector detector;
        private Feature2D DescExtractor;
        private DescriptorMatcher matcher;
        private MatOfKeyPoint keypoints, dupKeypoints;
        private MatOfDMatch matches, matches_final_mat;
        private ProgressDialog pd;
        private boolean isDuplicate = false;
        List<MatOfDMatch> knnMatch;
        private String result ;
        private Scalar RED = new Scalar(255,0,0);
        private Scalar GREEN = new Scalar(0,255,0);
        public aTaskSIFT(Activity activity) {
            super(activity);
            pd = new ProgressDialog(activity);
            pd.setIndeterminate(true);
            pd.setCancelable(true);
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage("Processing...");
            pd.show();
        }

        @Override
        public void doInBackground() {
            compare();
        }

        @Override
        public void onPostExecute() {
            try {

                // DRAWING OUTPUT
//                Mat outputImg = new Mat();
//// this will draw all matches, works fine
//                Features2d.drawMatches(img1, keypoints, img2, dupKeypoints, matches, outputImg);
//                bmp = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(),
//                        Bitmap.Config.ARGB_8888);
//                Imgproc.cvtColor(outputImg, outputImg, Imgproc.COLOR_BGR2RGB);
//                Utils.matToBitmap(outputImg, bmp);


                Mat img3 = new Mat();
                MatOfByte drawnMatches = new MatOfByte();

                Features2d.drawMatchesKnn(img1, keypoints, img2, dupKeypoints, knnMatch, img3);
//                Features2d.drawMatches(img1, keypoints, img2, dupKeypoints,
//                        matches_final_mat, img3, GREEN, RED, drawnMatches, Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
                bmp = Bitmap.createBitmap(img3.cols(), img3.rows(),
                        Bitmap.Config.ARGB_8888);
                Imgproc.cvtColor(img3, img3, Imgproc.COLOR_BGR2RGB);
                Utils.matToBitmap(img3, bmp);
                List<DMatch> finalMatchesList = matches_final_mat.toList();
                final int matchesFound = knnMatch.size();
                endTime = System.currentTimeMillis();
//                if (finalMatchesList.size() > min_matches)// dev discretion for
//                // number of matches to
//                // be found for an image
//                // to be judged as
//                // duplicate
//                {
//                    text = finalMatchesList.size()
//                            + " matches were found. Possible duplicate image.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = true;
//                } else {
//                    text = finalMatchesList.size()
//                            + " matches were found. Images aren't similar.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = false;
//                }

                int matching_count = 0 ;

                for(MatOfDMatch modm : knnMatch ){
                    matching_count += modm.toList().size();
                }

                text = matching_count+ " matches were found.\nTime taken="+ (endTime - startTime) + "ms\n";
                pd.dismiss();

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        activity);
                alertDialog.setTitle("Result");
                alertDialog.setCancelable(true);
                LayoutInflater factory = LayoutInflater.from(activity);
                final View view = factory.inflate(R.layout.view_image, null);
                ImageView matchedImages = (ImageView) view
                        .findViewById(R.id.finalImage);
                matchedImages.setImageBitmap(bmp);
                matchedImages.invalidate();
//                final CheckBox shouldBeDuplicate = (CheckBox) view.findViewById(R.id.checkBox);
                TextView message = (TextView) view.findViewById(R.id.message);
                message.setText(text);
                alertDialog.setView(view);
//                shouldBeDuplicate
//                        .setText("These images are actually duplicates.");
                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
//                alertDialog.setPositiveButton("Add to logs",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int which) {
//                                File logs = new File(Environment
//                                        .getExternalStorageDirectory()
//                                        .getAbsolutePath()
//                                        + "/imageComparator/Data Logs.txt");
//                                FileWriter fw;
//                                BufferedWriter bw;
//                                try {
//                                    fw = new FileWriter(logs, true);
//                                    bw = new BufferedWriter(fw);
//                                    bw.write("Algorithm used: "
//                                            + descriptorType
//                                            + "\nHamming distance: "
//                                            + min_dist + "\nMinimum good matches: "+min_matches
//                                            +"\nMatches found: "+matchesFound+"\nTime elapsed: "+(endTime-startTime)+"seconds\n"+ path1
//                                            + " was compared to " + path2
//                                            + "\n" + "Is actual duplicate: "
//                                            + shouldBeDuplicate.isChecked()
//                                            + "\nRecognized as duplicate: "
//                                            + isDuplicate + "\n");
//                                    bw.close();
//                                    Toast.makeText(
//                                            activity,
//                                            "Logs updated.\nLog location: "
//                                                    + Environment
//                                                    .getExternalStorageDirectory()
//                                                    .getAbsolutePath()
//                                                    + "/imageComparator/Data Logs.txt",
//                                            Toast.LENGTH_LONG).show();
//                                } catch (IOException e) {
//                                    // TODO Auto-generated catch block
//                                    // e.printStackTrace();
//                                    try {
//                                        File dir = new File(Environment
//                                                .getExternalStorageDirectory()
//                                                .getAbsolutePath()
//                                                + "/imageComparator/");
//                                        dir.mkdirs();
//                                        logs.createNewFile();
//                                        logs = new File(
//                                                Environment
//                                                        .getExternalStorageDirectory()
//                                                        .getAbsolutePath()
//                                                        + "/imageComparator/Data Logs.txt");
//                                        fw = new FileWriter(logs, true);
//                                        bw = new BufferedWriter(fw);
//                                        bw.write("Algorithm used: "
//                                                + descriptorType
//                                                + "\nMinimum distance between keypoints: "
//                                                + min_dist + "\n" + path1
//                                                + " was compared to " + path2
//                                                + "\n"
//                                                + "Is actual duplicate: "
//                                                + shouldBeDuplicate.isChecked()
//                                                + "\nRecognized as duplicate: "
//                                                + isDuplicate + "\n");
//                                        bw.close();
//                                        Toast.makeText(
//                                                activity,
//                                                "Logs updated.\nLog location: "
//                                                        + Environment
//                                                        .getExternalStorageDirectory()
//                                                        .getAbsolutePath()
//                                                        + "/imageComparator/Data Logs.txt",
//                                                Toast.LENGTH_LONG).show();
//                                    } catch (IOException e1) {
//                                        // TODO Auto-generated catch block
//                                        e1.printStackTrace();
//                                    }
//
//                                }
//                            }
//                        });
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }

        void compare() {
            int min_dist = 200000;
            try {
                bmpimg1 = bmpimg1.copy(Bitmap.Config.ARGB_8888, true);
                bmpimg2 = bmpimg2.copy(Bitmap.Config.ARGB_8888, true);
                img1 = new Mat();
                img2 = new Mat();

                mask1 = new Mat();
                mask2 = new Mat();

                Utils.bitmapToMat(bmpimg1, img1);
                Utils.bitmapToMat(bmpimg2, img2);
                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2GRAY);
                detector = FastFeatureDetector.create(7);


//                KNearest.create();

//                org.opencv.features2d.AKAZE
//                DescExtractor = ORB.create();

                DescExtractor = SIFT.create();

                matcher = DescriptorMatcher
                        .create(DescriptorMatcher.BRUTEFORCE_SL2);
//                matcher = BFMatcher.create(NORM_HAMMING, true);

//                matcher = BFMatcher.create();

//                matcher = BFMatcher.knn
//                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                keypoints = new MatOfKeyPoint();
                dupKeypoints = new MatOfKeyPoint();

                descriptors = new Mat();
                dupDescriptors = new Mat();

                matches = new MatOfDMatch();
                detector.detect(img1, keypoints);
                detector.detect(img2, dupKeypoints);


//                Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
//                detector.detect(img2, dupKeypoints);
//                Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
                // Descript keypoints


//                detector.detect(mg1, keypoints);
                /**
                 * Detects keypoints and computes the descriptors
                 * @param image automatically generated
                 * @param mask automatically generated
                 * @param keypoints automatically generated
                 * @param descriptors automatically generated
                 * @param useProvidedKeypoints automatically generated
                 */

//                DescExtractor.detectAndCompute(img1, mask1, keypoints, descriptors);
//                DescExtractor.detectAndCompute(img2, mask2, dupKeypoints, dupDescriptors);

                DescExtractor.compute(img1, keypoints, descriptors);
                DescExtractor.compute(img2, dupKeypoints, dupDescriptors);

                Log.d("LOG!", "number of descriptors= " + descriptors.size());
                Log.d("LOG!",
                        "number of dupDescriptors= " + dupDescriptors.size());
                // matching descriptors

//                descriptors.getNativeObjAddr();
                des1 = MatConvertor.SerializeFromMat(descriptors);
                des2 = MatConvertor.SerializeFromMat(dupDescriptors);



                Log.d("LOG!", des1);



//                writeDescriptor(des1);
//                writeDescriptor(des2);
//                matcher.knnMatch(descriptors, dupDescriptors, matches);
                knnMatch = new ArrayList<MatOfDMatch>();
                matcher.knnMatch(descriptors, dupDescriptors, knnMatch, 2);

//                matcher.match(descriptors, dupDescriptors, matches);
//                Log.d("LOG!", "Matches Size " + matches.size());
//                Log.d("LOG!", des1);
//                // New method of finding best matches
                //List<MatOfDMatch> recheckList =  ratioCheck(_matchesList, (float)0.8);

                //Log.d("LOG!", "ratioCheck size " + recheckList.size());
//                ratioCheck(recheckList , 0.8)

                matches_final_mat = new MatOfDMatch();
//                for(MatOfDMatch mom : _matchesList){
//                    List<DMatch> matchesList = mom.toList();
//                    matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());
//
//
//                    matches_final_mat.fromList(matchesList);
//                }

                for (int i = 0 ; i < knnMatch.size() ; i++){
                    List<DMatch> matchesList =knnMatch.get(i).toList();

                    for(DMatch dm :matchesList){
                        Log.d(TAG, i + " ) distance : " + dm.distance);
                    }
                    matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());
                    knnMatch.get(i).fromList(matchesList);
                }

//                int total = matches.toList().size();
//                int matched = 0;
//                List<DMatch> matchesList = matches.toList();
//
//                for(DMatch dm : matchesList){
//                    Log.d(TAG, "distance : " + dm.distance);
//                }

//                matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());
//                matches_final_mat = new MatOfDMatch();
//                matches_final_mat.fromList(matchesList);

//                matched = matches_final_mat.toList().size();
//
//                float percentage = total!=0 ? ((float)matched / (float)total) * 100 : 0;
//
//                Log.d(TAG, "matched/total : " + matched +" / " + total + " m_per : "+ percentage + "%");
//                result = "matched/total : " + matched +" / " + total + " m_per : "+ percentage + "%";
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        private List<MatOfDMatch> crossCheck(List<DMatch> matches12, List<DMatch> matches21, List<MatOfDMatch> knn_matches) {

            List<MatOfDMatch> good_matches = new ArrayList<MatOfDMatch>();

            for(int i=0; i<matches12.size(); i++)
            {
                DMatch forward = matches12.get(i);
                DMatch backward = matches21.get(forward.trainIdx);
                if(backward.trainIdx == forward.queryIdx)
                    good_matches.add(knn_matches.get(i));   //k=2
            }

            return good_matches;
        }

        private List<MatOfDMatch> ratioCheck(List<MatOfDMatch> knn_matches, float ratio) {

            List<MatOfDMatch> good_matches = new ArrayList<MatOfDMatch>();

            for(int i=0; i<knn_matches.size(); i++)
            {
                List<DMatch> subList = knn_matches.get(i).toList();

                if(subList.size()>=2)
                {
                    Float first_distance = subList.get(0).distance;
                    Float second_distance = subList.get(1).distance;

                    if((first_distance/second_distance) <= ratio)
                        good_matches.add(knn_matches.get(i));


                }

            }

            return good_matches;
        }
    }


    class aTask extends BackgroundTask {
        private Mat img1, img2, mask1, mask2, descriptors, dupDescriptors;
        private FastFeatureDetector detector;
        private Feature2D DescExtractor;
        private DescriptorMatcher matcher;
        private MatOfKeyPoint keypoints, dupKeypoints;
        private MatOfDMatch matches, matches_final_mat;
        private ProgressDialog pd;
        private boolean isDuplicate = false;

        private String result ;
        private Scalar RED = new Scalar(255,0,0);
        private Scalar GREEN = new Scalar(0,255,0);
        public aTask(Activity activity) {
            super(activity);
            pd = new ProgressDialog(activity);
            pd.setIndeterminate(true);
            pd.setCancelable(true);
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage("Processing...");
            pd.show();
        }

        @Override
        public void doInBackground() {
            compare();
        }

        @Override
        public void onPostExecute() {
            try {

                // DRAWING OUTPUT
//                Mat outputImg = new Mat();
//// this will draw all matches, works fine
//                Features2d.drawMatches(img1, keypoints, img2, dupKeypoints, matches, outputImg);
//                bmp = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(),
//                        Bitmap.Config.ARGB_8888);
//                Imgproc.cvtColor(outputImg, outputImg, Imgproc.COLOR_BGR2RGB);
//                Utils.matToBitmap(outputImg, bmp);


                Mat img3 = new Mat();
                MatOfByte drawnMatches = new MatOfByte();
                Features2d.drawMatches(img1, keypoints, img2, dupKeypoints,
//                        matches, img3, GREEN, RED, drawnMatches, Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
                matches_final_mat, img3, GREEN, RED, drawnMatches, Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
                bmp = Bitmap.createBitmap(img3.cols(), img3.rows(),
                        Bitmap.Config.ARGB_8888);
                Imgproc.cvtColor(img3, img3, Imgproc.COLOR_BGR2RGB);
                Utils.matToBitmap(img3, bmp);
                List<DMatch> finalMatchesList = matches_final_mat.toList();
                final int matchesFound = finalMatchesList.size();
                endTime = System.currentTimeMillis();
//                if (finalMatchesList.size() > min_matches)// dev discretion for
//                // number of matches to
//                // be found for an image
//                // to be judged as
//                // duplicate
//                {
//                    text = finalMatchesList.size()
//                            + " matches were found. Possible duplicate image.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = true;
//                } else {
//                    text = finalMatchesList.size()
//                            + " matches were found. Images aren't similar.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = false;
//                }
                text = finalMatchesList.size()+ " matches were found.\nTime taken="+ (endTime - startTime) + "ms\n" + result;;
                pd.dismiss();

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        activity);
                alertDialog.setTitle("Result");
                alertDialog.setCancelable(true);
                LayoutInflater factory = LayoutInflater.from(activity);
                final View view = factory.inflate(R.layout.view_image, null);
                ImageView matchedImages = (ImageView) view
                        .findViewById(R.id.finalImage);
                matchedImages.setImageBitmap(bmp);
                matchedImages.invalidate();
//                final CheckBox shouldBeDuplicate = (CheckBox) view.findViewById(R.id.checkBox);
                TextView message = (TextView) view.findViewById(R.id.message);
                message.setText(text);
                alertDialog.setView(view);
//                shouldBeDuplicate
//                        .setText("These images are actually duplicates.");
                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
//                alertDialog.setPositiveButton("Add to logs",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int which) {
//                                File logs = new File(Environment
//                                        .getExternalStorageDirectory()
//                                        .getAbsolutePath()
//                                        + "/imageComparator/Data Logs.txt");
//                                FileWriter fw;
//                                BufferedWriter bw;
//                                try {
//                                    fw = new FileWriter(logs, true);
//                                    bw = new BufferedWriter(fw);
//                                    bw.write("Algorithm used: "
//                                            + descriptorType
//                                            + "\nHamming distance: "
//                                            + min_dist + "\nMinimum good matches: "+min_matches
//                                            +"\nMatches found: "+matchesFound+"\nTime elapsed: "+(endTime-startTime)+"seconds\n"+ path1
//                                            + " was compared to " + path2
//                                            + "\n" + "Is actual duplicate: "
//                                            + shouldBeDuplicate.isChecked()
//                                            + "\nRecognized as duplicate: "
//                                            + isDuplicate + "\n");
//                                    bw.close();
//                                    Toast.makeText(
//                                            activity,
//                                            "Logs updated.\nLog location: "
//                                                    + Environment
//                                                    .getExternalStorageDirectory()
//                                                    .getAbsolutePath()
//                                                    + "/imageComparator/Data Logs.txt",
//                                            Toast.LENGTH_LONG).show();
//                                } catch (IOException e) {
//                                    // TODO Auto-generated catch block
//                                    // e.printStackTrace();
//                                    try {
//                                        File dir = new File(Environment
//                                                .getExternalStorageDirectory()
//                                                .getAbsolutePath()
//                                                + "/imageComparator/");
//                                        dir.mkdirs();
//                                        logs.createNewFile();
//                                        logs = new File(
//                                                Environment
//                                                        .getExternalStorageDirectory()
//                                                        .getAbsolutePath()
//                                                        + "/imageComparator/Data Logs.txt");
//                                        fw = new FileWriter(logs, true);
//                                        bw = new BufferedWriter(fw);
//                                        bw.write("Algorithm used: "
//                                                + descriptorType
//                                                + "\nMinimum distance between keypoints: "
//                                                + min_dist + "\n" + path1
//                                                + " was compared to " + path2
//                                                + "\n"
//                                                + "Is actual duplicate: "
//                                                + shouldBeDuplicate.isChecked()
//                                                + "\nRecognized as duplicate: "
//                                                + isDuplicate + "\n");
//                                        bw.close();
//                                        Toast.makeText(
//                                                activity,
//                                                "Logs updated.\nLog location: "
//                                                        + Environment
//                                                        .getExternalStorageDirectory()
//                                                        .getAbsolutePath()
//                                                        + "/imageComparator/Data Logs.txt",
//                                                Toast.LENGTH_LONG).show();
//                                    } catch (IOException e1) {
//                                        // TODO Auto-generated catch block
//                                        e1.printStackTrace();
//                                    }
//
//                                }
//                            }
//                        });
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }

        void compare() {
            try {
                bmpimg1 = bmpimg1.copy(Bitmap.Config.ARGB_8888, true);
                bmpimg2 = bmpimg2.copy(Bitmap.Config.ARGB_8888, true);
                img1 = new Mat();
                img2 = new Mat();

                mask1 = new Mat();
                mask2 = new Mat();

                Utils.bitmapToMat(bmpimg1, img1);
                Utils.bitmapToMat(bmpimg2, img2);
                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2GRAY);
                detector = FastFeatureDetector.create(3);

//                KNearest.create();

//                org.opencv.features2d.AKAZE
//                DescExtractor = ORB.create();

                DescExtractor = ORB.create();


                matcher = DescriptorMatcher
                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//                matcher = BFMatcher.create(NORM_HAMMING, true);

//                matcher = BFMatcher.create();

//                matcher = BFMatcher.knn
//                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                keypoints = new MatOfKeyPoint();
                dupKeypoints = new MatOfKeyPoint();

                descriptors = new Mat();
                dupDescriptors = new Mat();

                matches = new MatOfDMatch();
                detector.detect(img1, keypoints);
                detector.detect(img2, dupKeypoints);


//                Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
//                detector.detect(img2, dupKeypoints);
//                Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
                // Descript keypoints


//                detector.detect(mg1, keypoints);
                /**
                 * Detects keypoints and computes the descriptors
                 * @param image automatically generated
                 * @param mask automatically generated
                 * @param keypoints automatically generated
                 * @param descriptors automatically generated
                 * @param useProvidedKeypoints automatically generated
                 */

//                DescExtractor.detectAndCompute(img1, mask1, keypoints, descriptors);
//                DescExtractor.detectAndCompute(img2, mask2, dupKeypoints, dupDescriptors);

                DescExtractor.compute(img1, keypoints, descriptors);
                DescExtractor.compute(img2, dupKeypoints, dupDescriptors);

                Log.d("LOG!", "number of descriptors= " + descriptors.size());
                Log.d("LOG!",
                        "number of dupDescriptors= " + dupDescriptors.size());
                // matching descriptors

//                descriptors.getNativeObjAddr();
                des1 = MatConvertor.SerializeFromMat(descriptors);
                des2 = MatConvertor.SerializeFromMat(dupDescriptors);



                Log.d("LOG!", des1);



                writeDescriptor(des1);
//                writeDescriptor(des2);
//                matcher.knnMatch(descriptors, dupDescriptors, matches);
                //List<MatOfDMatch> _matchesList = new ArrayList<MatOfDMatch>();
                //matcher.knnMatch(descriptors, dupDescriptors, _matchesList, 2);

                matcher.match(descriptors, dupDescriptors, matches);
//                Log.d("LOG!", "Matches Size " + matches.size());
//                Log.d("LOG!", des1);
//                // New method of finding best matches
                //List<MatOfDMatch> recheckList =  ratioCheck(_matchesList, (float)0.8);

                //Log.d("LOG!", "ratioCheck size " + recheckList.size());
//                ratioCheck(recheckList , 0.8)

//                for(MatOfDMatch mom : _matchesList){
//                    List<DMatch> matchesList = mom.toList();
//                    matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());
//                    matches_final_mat = new MatOfDMatch();
//                    matches_final_mat.fromList(matchesList);
//                }

                int total = matches.toList().size();
                int matched = 0;
                List<DMatch> matchesList = matches.toList();

                for(DMatch dm : matchesList){
                    Log.d(TAG, "distance : " + dm.distance);
                }

                matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());
                matches_final_mat = new MatOfDMatch();
                matches_final_mat.fromList(matchesList);

                matched = matches_final_mat.toList().size();

                float percentage = total!=0 ? ((float)matched / (float)total) * 100 : 0;

                Log.d(TAG, "matched/total : " + matched +" / " + total + " m_per : "+ percentage + "%");
                result = "matched/total : " + matched +" / " + total + " m_per : "+ percentage + "%";
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        private List<MatOfDMatch> crossCheck(List<DMatch> matches12, List<DMatch> matches21, List<MatOfDMatch> knn_matches) {

            List<MatOfDMatch> good_matches = new ArrayList<MatOfDMatch>();

            for(int i=0; i<matches12.size(); i++)
            {
                DMatch forward = matches12.get(i);
                DMatch backward = matches21.get(forward.trainIdx);
                if(backward.trainIdx == forward.queryIdx)
                    good_matches.add(knn_matches.get(i));   //k=2
            }

            return good_matches;
        }

        private List<MatOfDMatch> ratioCheck(List<MatOfDMatch> knn_matches, float ratio) {

            List<MatOfDMatch> good_matches = new ArrayList<MatOfDMatch>();

            for(int i=0; i<knn_matches.size(); i++)
            {
                List<DMatch> subList = knn_matches.get(i).toList();

                if(subList.size()>=2)
                {
                    Float first_distance = subList.get(0).distance;
                    Float second_distance = subList.get(1).distance;

                    if((first_distance/second_distance) <= ratio)
                        good_matches.add(knn_matches.get(i));


                }

            }

            return good_matches;
        }
    }



    private void writeDescriptor(String des){
        try {


            File logs = new File(Environment
                                        .getExternalStorageDirectory()
                                        .getAbsolutePath()
                                        + "/opencv4test/Descriptor4.txt");

            FileWriter fw;
            BufferedWriter bw;

            File dir = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath() + "/opencv4test/");
            dir.mkdirs();

            logs.createNewFile();

            logs = new File(
                    Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/opencv4test/Descriptor4.txt");

            fw = new FileWriter(logs, true);
            bw = new BufferedWriter(fw);

            bw.write(des + "\n");
            bw.close();



        } catch (IOException e1) {

            // TODO Auto-generated catch block
            e1.printStackTrace();

        }
    }

}