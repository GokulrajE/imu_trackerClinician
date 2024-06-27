package com.example.clinician;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class suggetion {
    private static List<String> folderSuggestions;
    private static String A_KEY = "";
    private static String S_KEY = "";
    static BasicAWSCredentials awsCreds = new BasicAWSCredentials(A_KEY, S_KEY);
    static AmazonS3Client s3Client = new AmazonS3Client(awsCreds, com.amazonaws.regions.Region.getRegion(Regions.EU_NORTH_1));



    static List<String> getFolderSuggestions(String bucketName, String prefix) {
        Set<String> folderNames = new HashSet<>();

        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withPrefix(prefix)
                   .withDelimiter("/");

            ObjectListing objectListing;
            do {
                objectListing = s3Client.listObjects(listObjectsRequest);

                folderNames.addAll(objectListing.getCommonPrefixes());

                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(folderNames);
    }

    static void updateAutoCompleteTextView(List<String> suggestions,Context context,AutoCompleteTextView autoCompleteTextView1) {
        folderSuggestions = suggestions;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, folderSuggestions);
        autoCompleteTextView1.setAdapter(adapter);
   }
}

