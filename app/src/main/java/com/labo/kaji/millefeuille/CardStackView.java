package com.labo.kaji.millefeuille;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kakajika on 2015/01/22.
 */
public class CardStackView extends RecyclerView {

    private final List<Card> mCardList = new ArrayList<>();
    
    public CardStackView(Context context) {
        super(context);
        init(context);
    }

    public CardStackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardStackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public CardStackView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init(context);
//    }

    private void init(Context context) {
        setClipToPadding(false);
//        setVerticalScrollBarEnabled(true);

        for (int i=0; i<10; ++i) {
            int color = Color.rgb((int) Math.floor(Math.random() * 128) + 64,
                                  (int) Math.floor(Math.random() * 128) + 64,
                                  (int) Math.floor(Math.random() * 128) + 64);
            mCardList.add(new Card(i + 1, color));
        }

        final RecyclerView.Adapter adapter = new CardAdapter(mCardList);

        setAdapter(adapter);
//        setAdapter(new AlphaInAnimationAdapter(new CardAdapter()));

//        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        setLayoutManager(new ArcLayoutManager());
//        setLayoutManager(new SlideStackLayoutManager());

        SwipeDismissRecyclerViewTouchListener touchListener =
                new SwipeDismissRecyclerViewTouchListener(
                        this,
                        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return !isLayoutRequested();
                            }
                            public void onDismiss(RecyclerView listView, int position) {
//                                for (int position : reverseSortedPositions) {
                                    mCardList.remove(position);
                                    adapter.notifyItemRemoved(position);
//                                }
                            }
                        });
        setOnTouchListener(touchListener);
        setOnScrollListener(touchListener.makeScrollListener());
    }

    public static class Card {
        public final int number;
        public final int color;
        public Card(int number, int color) {
            this.number = number;
            this.color = color;
        }
    }

    public class CardAdapter extends RecyclerView.Adapter {

        private List<Card> mList;

        public CardAdapter(List<Card> list) {
            mList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(parent.getContext(), R.layout.card, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            Card card = mList.get(position);
            ((ViewHolder) holder).colorLabel.setBackgroundColor(card.color);
            ((ViewHolder) holder).textView.setText(String.valueOf(card.number));
            ((ViewHolder) holder).colorLabel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    scrollToPosition(position);
                    smoothScrollToPosition(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView textView;
            public final View colorLabel;
            public ViewHolder(View v) {
                super(v);
                textView = (TextView)v.findViewById(R.id.text);
                colorLabel = v.findViewById(R.id.label);
            }
        }

    }

}
