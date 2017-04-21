package com.bonzimybuddy.bonzirc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

public class ChatScrollAdapter extends RecyclerView.Adapter<ChatScrollAdapter.ViewHolder> {
    private ArrayList<String> mLines;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }

        public void addLine(String line) {
            mLines.add(line);
        }
    }

    public ChatScrollAdapter(String[] lines) {
        mLines = new ArrayList<String>(Arrays.asList(lines));
    }

    public ChatScrollAdapter() {
        mLines = new ArrayList<String>();
    }

    public void addLine(String line) {
        mLines.add(line);
        notifyItemInserted(mLines.size() - 1);
    }

    public void setLines(ArrayList<String> lines) {
        mLines = new ArrayList<String> (lines);
        notifyDataSetChanged();
    }

    public void clearLines() {
        mLines.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(mLines.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_line, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mLines.size();
    }
}
