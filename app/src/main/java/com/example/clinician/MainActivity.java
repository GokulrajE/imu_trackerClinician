package com.example.clinician;


import static com.example.clinician.S3Uploader.dir;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static String A_KEY = "";
    private static String S_KEY = "";
    private static final String BUCKET_NAME = "clinicianappbucket";
    static BasicAWSCredentials awsCreds = new BasicAWSCredentials(A_KEY, S_KEY);
    static AmazonS3Client s3Client = new AmazonS3Client(awsCreds, com.amazonaws.regions.Region.getRegion(Regions.EU_NORTH_1));
    boolean isfolder;

   private  String username;
    private LineChart mChart;
    private LineChart mChart1;
    TextView date;
    private LineChart mChart2;
    float mXValue=0;
    AutoCompleteTextView autoCompleteTextView;
    List<String> labels = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    List<String> timelabel = new ArrayList<>();
    TextView load;
    List<ILineDataSet> dataSets1;
    List<ILineDataSet> dataSets2;
    List<EmojiEntry> emojiEntries = new ArrayList<>();
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    suggetion suggetionclass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        Button retrive = findViewById(R.id.retrive_file_button);

        load = findViewById(R.id.loading);
        mChart = findViewById(R.id.line_chart);
        setup();
        mChart1 = findViewById(R.id.line_chart1);
        setupmc1();
        mChart2 = findViewById(R.id.line_chart2);
        setupmc2();
        date = findViewById(R.id.date);
         autoCompleteTextView = findViewById(R.id.name);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String prefix = s.toString();
               // System.out.println(prefix);
                try  {
                    fetchFolderSuggestions(BUCKET_NAME,prefix);
                }catch (NullPointerException e){
                    System.out.println(e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });


        EditText editText1 = findViewById(R.id.name1);
        Button addcomments = findViewById(R.id.commentbt);
        RecyclerView recyclerView = findViewById(R.id.reccyleview);

        addcomments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()){
                    load.setText("uploading...");
                    boolean isread = false;
                    String commend = String.valueOf(editText1.getText());
                    String cmt = commend+","+isread;
                               if(!commend.isEmpty()) {
                                   Executor executor = Executors.newSingleThreadExecutor();
                                   executor.execute(() -> {
                                       S3Uploader.addComment(BUCKET_NAME, "gokul1/Comment.csv", cmt, MainActivity.this);
                                       runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               load.setText(" ");
                                           }
                                       });

                                   });
                               }else{
                                   System.out.println("enter your comment");
                               }
                               editText1.setText("");


                }

            }
        });
        TextView viewcomment = findViewById(R.id.viwecomment);
        viewcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()){
                    load.setText("loading...");
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        try {
                            List<String> comments = S3Uploader.fetchComments(BUCKET_NAME,"gokul1/Comment.csv");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    load.setText("");
                                    showCommentsDialog(MainActivity.this,comments);
                                }
                            });

                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    });

                }
            }
        });

        retrive.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isfolder = true;

                        if(isNetworkAvailable()) {
                            String patientname = String.valueOf(autoCompleteTextView.getText());
                            if (!patientname.isEmpty()) {
                                setUsername(patientname);
                                load.setText("Loading");
                                Executor executor = Executors.newSingleThreadExecutor();
                                executor.execute(() -> {
                                    emojiEntries.clear();
                                    emojiEntries = S3Uploader.readDataFromCSV(BUCKET_NAME,getUsername()+"emojidata.csv");
                                    System.out.println(emojiEntries);
                                    List<Entry>[] chartdata = fetchfromcsv(patientname);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                                    if(isfolder) {
                                                        load.setText("");
                                                        addDataPointToGraph(labels, chartdata, mChart);
                                                        usage(labels, chartdata[2], mChart2);
                                                    }
                                                    else{
                                                        load.setText("Retry");
                                            }
                                        }
                                    });
                                });

                            }else{
                                Toast.makeText(getApplicationContext(), "please Enter patient Name or ID", Toast.LENGTH_LONG).show();
                            }


                        }else{
                            Enablenetwork();
                        }
                    }
                }
        );
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);


    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    public void showCommentsDialog(Context context, List<String> comments) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Comments");

        // Create an ArrayAdapter to display the comments
        System.out.println(comments);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,comments) ;

        builder.setAdapter(adapter, null);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    void fetchFolderSuggestions(String bucketName, String prefix) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //System.out.println("strat");
            List<String> result = suggetion.getFolderSuggestions(bucketName, prefix);
           // System.out.println(result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!result.isEmpty()) {
                        suggetion.updateAutoCompleteTextView(result, MainActivity.this,autoCompleteTextView);

                    }else{
                        System.out.println("reult is empty");
                    }
                }
            });
        });
    }
    public void emojiupdate(){

        if(isNetworkAvailable()){
            load.setText("updating..");
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(()->{

                StringBuilder emojidata = new StringBuilder();
                for(EmojiEntry emojiEntry:emojiEntries){
                    float x = emojiEntry.getX();
                    float y = emojiEntry.getY();
                    String emoji = emojiEntry.getEmoji();
                    String data = x+","+y+","+emoji;
                    emojidata.append(data).append("\n");
                }
                S3Uploader.addemojidata(BUCKET_NAME,getUsername()+"emojidata.csv",emojidata,MainActivity.this);

            });
        }
    }


    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activenetworkkinfo = connectivityManager.getActiveNetworkInfo();
        return activenetworkkinfo!= null && activenetworkkinfo.isConnectedOrConnecting();
    }
    private void Enablenetwork(){
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }
    private List<Entry>[] fetchfromcsv(String foldername){
        List<Entry> entries1 = new ArrayList<>();
        List<Entry> entries2 = new ArrayList<>();
        List<Entry> entries4 = new ArrayList<>();
        try {

            ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix(foldername);
            ListObjectsV2Result result;
            result = s3Client.listObjectsV2(request);
            //System.out.println(result);
            if(!result.getObjectSummaries().isEmpty()) {
                List<S3ObjectSummary> lobjects = result.getObjectSummaries();
               // System.out.println(lobjects);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        load.setText("Loading...");
                    }
                });
                do {

                    for (S3ObjectSummary s3ObjectSummary : lobjects) {
                        String key = s3ObjectSummary.getKey();
                       // System.out.println(key);
                        keys.add(key);
                        S3Object s3Object = s3Client.getObject(BUCKET_NAME, key);
                        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(objectInputStream));
                        String line;
                        float calcval1 = 0;
                        float calcval2 = 0;

                        float sumValue1 = 0;
                        float sumValue2 = 0;
                        try {
                            while ((line = reader.readLine()) != null) {

                                String[] parts = line.split(",");
                                sumValue1 += Double.parseDouble(parts[1]);
                                sumValue2 += Double.parseDouble(parts[2]);
                            }
                            reader.close();
                            calcval1 = sumValue1 / 60;
                            calcval2 = sumValue2 / 60;
                            //System.out.println(calcval1 + ":" + calcval2);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        String[] parts = key.split("/");
                        String dates[] = parts[1].split("-");
                        String dateafter = dates[0]+"/"+dates[1];
                        //System.out.println(dateafter);
                        Entry entry1 = new Entry(mXValue,calcval1);
                        Entry entry2 = new Entry(mXValue,calcval2);
                        for(EmojiEntry emojiEntry:emojiEntries){
                            if(emojiEntry.getX()==entry1.getX() && emojiEntry.getY()==entry1.getY()){
                                String emoji = emojiEntry.getEmoji();
                                System.out.println(emojiEntry.getX()+","+emojiEntry.getY()+emoji);
                                System.out.println(emoji);
                                Drawable drawable = EmojiDrawableHelper.getEmojiDrawable(this,emoji);
                                entry1.setIcon(drawable);

                            }
                            if(emojiEntry.getX()==entry2.getX()&&emojiEntry.getY()==entry2.getY()){

                                String emoji = emojiEntry.getEmoji();
                                System.out.println(emojiEntry.getX()+","+emojiEntry.getY()+emoji);
                                Drawable drawable = EmojiDrawableHelper.getEmojiDrawable(this,emoji);
                                System.out.println(emoji);
                                entry2.setIcon(drawable);
                            }
                        }
                        entries1.add(entry1);
                        entries2.add(entry2);
                        double sum;
                        sum = (calcval1 - calcval2)/(calcval1 + calcval2);
                        if(Double.isNaN(sum)){
                            sum = 0.0;
                        }
                        entries4.add(new Entry(mXValue, (float) sum));
                        //System.out.println(sum);
                        labels.add(dateafter);
                        mXValue++;
                    }
                } while (result.isTruncated());
            }else{
                isfolder = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Invalid Patient Name or ID", Toast.LENGTH_LONG).show();

                    }
                });

            }
        } catch(RuntimeException e){
            System.out.println(e.getMessage());
        }
        return new List[]{entries1,entries2,entries4};

    }
    String convertStringtoMinutes(String datestr){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            Date date = sdf.parse(datestr);
            if(date != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                long minute = calendar.get(Calendar.MINUTE);
                long sec = calendar.get(Calendar.SECOND);
                String time = minute+":"+sec;
                return time;
            }
        }catch ( ParseException e){
            e.printStackTrace();
        }

        return" ";
    }
    public List<Entry>[] getdaydata(String key) throws IOException {
        List<Entry> entries3 = new ArrayList<>();
        List<Entry> entries4 = new ArrayList<>();

        float mvalue = 0;
        S3Object s3Object = s3Client.getObject(BUCKET_NAME, key);
        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(objectInputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String parts[] = line.split(",");
                String DateString = parts[0];
                String time = convertStringtoMinutes(DateString);
                float  larm = Float.parseFloat(parts[1]);
                float  rarm = Float.parseFloat(parts[2]);
                entries3.add(new Entry(mvalue,larm));
                entries4.add(new Entry(mvalue,rarm));
                timelabel.add(time);
                mvalue ++;
            }


        }catch (IOException e){
            System.out.println(e.getMessage());
        }

        return new List[]{entries3,entries4};

    }
    public void  usage(List<String> lable ,List<Entry> entry,LineChart chart){
        LineDataSet dataSet1 = new LineDataSet(entry, " ");
        dataSet1.setDrawValues(false);
        dataSet1.setDrawCircles(true);
        dataSet1.setColor(Color.RED);
        dataSet1.setCircleColor(Color.GREEN);
        dataSet1.setCircleRadius(5f);
        dataSet1.setLineWidth(4f);
        dataSet1.setCircleColorHole(Color.BLACK);
        List<ILineDataSet> dataSets1 = new ArrayList<>();
        dataSets1.add(dataSet1);
        LineData lineData = new LineData();
        lineData.addDataSet(dataSet1);
        LimitLine limitLine = new LimitLine(0f," ");
        limitLine.setLineWidth(2f);
        limitLine.enableDashedLine(10f,10f,0f);
        limitLine.setLineColor(Color.BLACK);
        try{
            if(chart!= null) {
                chart.setData(lineData);
                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new XAxisValueFormatter(lable)); // Set custom X-axis labels
                YAxis yAxis = chart.getAxisLeft();
                chart.setVisibleXRangeMaximum(10);
                chart.moveViewToX(0);
                yAxis.addLimitLine(limitLine);
                yAxis.setDrawLimitLinesBehindData(true);
                chart.notifyDataSetChanged();
                chart.invalidate(); // Refresh chart
            }else{
                System.out.println("mchart is nulls");
            }
        }catch (NullPointerException e){
            e.printStackTrace();

        }

    }
    public void addDataPointToGraph(List<String>labels1, List<Entry> entries[],LineChart chart) {
        LineDataSet dataSet1 = new LineDataSet(entries[0], "l-Arm");
        LineDataSet dataSet2 = new LineDataSet(entries[1], "r-Arm");
        dataSet1.setLineWidth(2f);
        dataSet1.setColor(Color.rgb(135,206,235));
        dataSet1.setCircleColor(Color.BLACK);
        dataSet1.setCircleColorHole(Color.WHITE);
        dataSet1.setDrawCircles(true);
        dataSet1.setDrawValues(false);
        dataSet2.setColor(Color.rgb(255,165,0));
        dataSet2.setCircleColor(Color.GREEN);
        dataSet2.setLineWidth(2f);
        dataSet2.setDrawCircles(true);
        dataSet2.setCircleColorHole(Color.BLACK);
        dataSet2.setDrawValues(false);
         dataSets1= new ArrayList<>();
         dataSets2  = new ArrayList<>();
        dataSets1.add(dataSet1);
        dataSets2.add(dataSet2);
        dataSet1.setDrawIcons(true);
        LineData lineData = new LineData();
        lineData.addDataSet(dataSet1);
        lineData.addDataSet(dataSet2);


        try{
            if(chart!= null) {
                chart.setData(lineData);
                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new XAxisValueFormatter(labels1));// Set custom X-axis labels
                chart.setVisibleXRangeMaximum(10);
                chart.moveViewToX(0);
                chart.setExtraOffsets(10,10,10,10);
                chart.invalidate(); // Refresh chart
            }else{
                System.out.println("mchart is nulls");
            }
        }catch (NullPointerException e){
            e.printStackTrace();

        }

    }
    private void setup(){

        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        //mChart.setDrawGridBackground(true);
        mChart.setPinchZoom(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(true);
        mChart.setBackgroundColor(Color.rgb(255,255,255));

        XAxis xAxis = mChart.getXAxis();
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getAxisRight().setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        System.out.println("setup");
        mChart.animateX(1500);
        CustomMarkerView markerView = new CustomMarkerView(this,R.layout.marker_view);
        mChart.setMarker(markerView);

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                float xvalue = e.getX();
                String key = keys.get((int) xvalue);
                //System.out.println(key);
                String[] parts = key.split("/");
                String dates[] = parts[1].split("\\.");
                String date1 = dates[0];
               // System.out.println(date1);

                load.setText("Loading...");
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    try {
                        List<Entry>[] chartdata2 = getdaydata(key);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                load.setText("");
                                date.setText("Date:"+date1);
                                addDataPointToGraph(timelabel,chartdata2, mChart1);


                            }
                        });
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }

                });

            }

            @Override
            public void onNothingSelected() {

            }
        });

        mChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

                Highlight h = mChart.getHighlightByTouchPoint(me.getX(), me.getY());
                if (h != null) {
                    Entry entry = mChart.getEntryByTouchPoint(me.getX(),me.getY());
                    if(entry != null){
                        showEmojiSelectionDialog(entry);
                    }
                }
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
            }
            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }
            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }
            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });

        mChart.invalidate();
    }
    private void showEmojiSelectionDialog(final Entry entry) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_emoji_selection, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        // Set click listeners for each emoji
        dialogView.findViewById(R.id.emoji1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEntryWithEmoji(entry,   "👍");
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.emoji2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEntryWithEmoji(entry, "🌟");
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.emoji3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEntryWithEmoji(entry, "👏");
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.emoji4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEntryWithEmoji(entry, "✨");
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.emoji5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEntryWithEmoji(entry, "⭐");
                dialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeEmojiFromEntry(entry);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void removeEmojiFromEntry(Entry entry) {
        entry.setIcon(null);

        // Find and remove the corresponding EmojiEntry
        for (int i = 0; i < emojiEntries.size(); i++) {
            EmojiEntry emojiEntry = emojiEntries.get(i);
            if (emojiEntry.getX() == entry.getX() && emojiEntry.getY() == entry.getY()) {
                emojiEntries.remove(i);
                break;
            }
        }
        System.out.println(emojiEntries);
        emojiupdate();

        mChart.invalidate(); // Refresh chart to remove the icon
    }

    private void updateEntryWithEmoji(Entry entry, String emoji) {
        Drawable drawable = EmojiDrawableHelper.getEmojiDrawable(this,emoji);
        entry.setIcon(drawable);
        boolean entryupdated = false;
        System.out.println(entry.getX()+","+entry.getY());
        if(!emojiEntries.isEmpty()) {
            for (EmojiEntry emojiEntry : emojiEntries) {
                if (emojiEntry.getX() == entry.getX() && emojiEntry.getY() == entry.getY()) {
                    emojiEntry.setEmoji(emoji);
                    entryupdated = true;
                    break;
                }
            }
        }
        if(!entryupdated){
            emojiEntries.add(new EmojiEntry(entry.getX(),entry.getY(),emoji));
        }
        System.out.println(emojiEntries);
        emojiupdate();
        mChart.invalidate();
    }
    private void setupmc1(){

        mChart1.getDescription().setEnabled(false);
        mChart1.setTouchEnabled(true);
        mChart1.setDragEnabled(true);
        mChart1.setPinchZoom(true);
        mChart1.setScaleXEnabled(true);
        mChart1.setScaleYEnabled(true);
        mChart1.setBackgroundColor(Color.rgb(255,255,255));
        XAxis xAxis = mChart1.getXAxis();
        mChart1.setVisibleXRangeMaximum(10);
        mChart1.moveViewToX(0);
        mChart1.getXAxis().setDrawGridLines(false);
        mChart1.getAxisLeft().setDrawGridLines(false);
        mChart1.getAxisRight().setDrawGridLines(false);
        mChart1.getAxisRight().setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        System.out.println("setup");
        mChart1.animateX(1500);
        mChart1.invalidate();
    }
    private  void setupmc2(){
        mChart2.getDescription().setEnabled(false);
        mChart2.setTouchEnabled(true);
        mChart2.setDragEnabled(true);
        mChart2.setPinchZoom(true);
        mChart2.setScaleXEnabled(true);
        mChart2.setScaleYEnabled(true);
        mChart2.setBackgroundColor(Color.rgb(255,255,255));
        XAxis xAxis = mChart2.getXAxis();
        mChart2.setVisibleXRangeMaximum(10);
        mChart2.moveViewToX(0);
        mChart2.getXAxis().setDrawGridLines(true);
        mChart2.getAxisLeft().setDrawGridLines(true);
        mChart2.getAxisRight().setDrawGridLines(false);
        mChart2.getAxisRight().setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        System.out.println("setup");
        mChart2.animateX(1500);
        mChart2.invalidate();

    }

}