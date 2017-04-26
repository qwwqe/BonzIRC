package com.bonzimybuddy.bonzirc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class ChatScrollAdapter extends RecyclerView.Adapter<ChatScrollAdapter.ViewHolder> {
    private ArrayList<String> mLines;

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.textView);
        }

        TextView getTextView() { return textView; }
    }

    ChatScrollAdapter() { mLines = new ArrayList<>(); }

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
    public int getItemCount() { return mLines.size(); }

    void addLine(String line) {
        mLines.add(line);
        notifyItemInserted(mLines.size() - 1);
    }

    void setLines(ArrayList<String> lines) {
        mLines = new ArrayList<> (lines);
        notifyDataSetChanged();
    }

    void clearLines() {
        mLines.clear();
        notifyDataSetChanged();
    }
}
