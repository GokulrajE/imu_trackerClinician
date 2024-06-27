package com.example.clinician;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.HashMap;

public class CustomMarkerView extends MarkerView {
    private final TextView tvContent;
    private Entry longPressedEntry;
    private HashMap<Integer, String> emojiMap;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
        emojiMap = new HashMap<>();
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Set the emoji or any other content you want
//        if (e != null && e.equals(longPressedEntry)) {
//            tvContent.setText("\uD83D\uDC4D"); // Unicode for smiley emoji
//        } else {
//            tvContent.setText(""); // Clear content if not long pressed
//        }
//        super.refreshContent(e, highlight);
        int index = (int) e.getX();
        if (emojiMap.containsKey(index)) {
            tvContent.setText(emojiMap.get(index));
        } else {
            tvContent.setText("");
        }
        super.refreshContent(e, highlight);
    }
    public void addEmoji(int index, String emoji) {
        emojiMap.put(index, emoji);
    }

    public void removeEmoji(int index) {
        emojiMap.remove(index);
    }


    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
    public void setLongPressedEntry(Entry entry) {
        if(entry != null) {
            this.longPressedEntry = entry;
        }else{
            System.out.println("entry is null");
        }
    }
}
