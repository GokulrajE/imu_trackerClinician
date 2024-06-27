package com.example.clinician;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.arch.core.executor.ArchTaskExecutor;


import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import android.content.Context.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;


import org.w3c.dom.Comment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class S3Uploader {
    static String dir = "imuble";
    private static String A_KEY = "";
    private static String S_KEY = "";

    private static final String BUCKET_NAME = "clinicianappbucket";
    static BasicAWSCredentials awsCreds = new BasicAWSCredentials(A_KEY, S_KEY);
    static AmazonS3Client s3Client = new AmazonS3Client(awsCreds);



    public static List<String> fetchComments(String bucketname, String key) throws IOException {
        List<String> comments = new ArrayList<>();
        boolean fileexist = s3Client.doesObjectExist(bucketname,key);
        if(fileexist) {
            S3Object object = s3Client.getObject(new GetObjectRequest(bucketname, key));
            S3ObjectInputStream inputStream = object.getObjectContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] comment = line.split(",");
                String commettext = comment[0];
                boolean isread = Boolean.parseBoolean(comment[1]);
                String formatedstring = commettext+"("+(isread?"read":"unread")+")";


                comments.add(formatedstring);
            }
            reader.close();
        }

            return comments;

    }

    static void addComment(String bucketName, String fileName, String newComment, Context context) {

        StringBuilder existingComments = new StringBuilder();
        boolean fileexist = s3Client.doesObjectExist(bucketName,fileName);
        if (fileexist) {

            try {
                S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, fileName));
                S3ObjectInputStream inputStream = object.getObjectContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    existingComments.append(line).append("\n");
                }
                reader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

                // Step 2: Append the new comment
                existingComments.append(newComment).append("\n");

                // Step 3: Upload the updated comments file back to S3
                try {
                    String tem = "Comment.csv";
                    File tempFile = new File(context.getFilesDir(), tem);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(existingComments.toString().getBytes());
                    fos.close();
                    TransferNetworkLossHandler transferNetworkLossHandler = TransferNetworkLossHandler.getInstance(context);
                    TransferUtility transferUtility = TransferUtility.builder()
                            .context(context.getApplicationContext())
                            .defaultBucket(BUCKET_NAME)
                            .s3Client(s3Client)
                            .build();

                    // TransferUtility transferUtility = s3Client.getTransferUtility(this);
                    TransferObserver uploadObserver = transferUtility.upload(bucketName, fileName, tempFile);

                    uploadObserver.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (state == TransferState.COMPLETED) {
                                Log.d("S3Upload", "Comment upload completed!");

                            } else if (state == TransferState.FAILED) {
                                Log.e("S3Upload", "Comment upload failed!");
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            float percentDone = ((float) bytesCurrent / bytesTotal) * 100;
                            Log.d("S3Upload", "Progress: " + percentDone + "%");
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

    }
    public static void addemojidata(String bucketName, String fileName, StringBuilder emojidata, Context context){

        try {
            String tem = "Comment.csv";
            File tempFile = new File(context.getFilesDir(), tem);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(emojidata.toString().getBytes());
            fos.close();
            TransferNetworkLossHandler transferNetworkLossHandler = TransferNetworkLossHandler.getInstance(context);
            TransferUtility transferUtility = TransferUtility.builder()
                    .context(context.getApplicationContext())
                    .defaultBucket(BUCKET_NAME)
                    .s3Client(s3Client)
                    .build();

            // TransferUtility transferUtility = s3Client.getTransferUtility(this);
            TransferObserver uploadObserver = transferUtility.upload(bucketName, fileName, tempFile);

            uploadObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state == TransferState.COMPLETED) {
                        Log.d("S3Upload", "Comment upload completed!");

                    } else if (state == TransferState.FAILED) {
                        Log.e("S3Upload", "Comment upload failed!");
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDone = ((float) bytesCurrent / bytesTotal) * 100;
                    Log.d("S3Upload", "Progress: " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static List<EmojiEntry> readDataFromCSV(String bucketName,String fileName) {
        boolean fileexist = s3Client.doesObjectExist(bucketName, fileName);
        List<EmojiEntry> entries = new ArrayList<>();

            if (fileexist) {

                try {
                    S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, fileName));
                    S3ObjectInputStream inputStream = object.getObjectContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] tokens = line.split(",");
                        float x = Float.parseFloat(tokens[0]);
                        float y = Float.parseFloat(tokens[1]);
                        String emoji = tokens.length > 2 ? tokens[2] : "";
                        entries.add(new EmojiEntry(x, y, emoji));
                    }
                    reader.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        return entries;
    }





}

