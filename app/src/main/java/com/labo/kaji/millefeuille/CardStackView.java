package com.labo.kaji.millefeuille;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.labo.kaji.millefeuille.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kakajika
 * @since 2015/01/22
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

    public void addCard() {
        int color = Color.rgb((int) Math.floor(Math.random() * 128) + 64,
                (int) Math.floor(Math.random() * 128) + 64,
                (int) Math.floor(Math.random() * 128) + 64);
        mCardList.add(new Card(color));
        if (getAdapter() != null) {
            getAdapter().notifyItemInserted(mCardList.size() - 1);
        }
    }

    private void init(Context context) {
        setClipToPadding(false);

        for (int i=0; i<10; ++i) {
            addCard();
        }

        final RecyclerView.Adapter adapter = new CardAdapter(mCardList);
        setAdapter(adapter);

        setLayoutManager(new ArcLayoutManager());

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                        makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
            }

            @Override
            public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                switch (actionState) {
                    case ItemTouchHelper.ACTION_STATE_DRAG:
                    case ItemTouchHelper.ACTION_STATE_SWIPE:
                        viewHolder.itemView.setAlpha(0.5f);
                        break;
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.animate().alpha(1.0f);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
                final int srcPos = viewHolder.getAdapterPosition();
                final int dstPos = target.getAdapterPosition();
                mCardList.add(dstPos, mCardList.remove(srcPos));
                adapter.notifyItemMoved(srcPos, dstPos);
                return true;
            }

            @Override
            public void onSwiped(ViewHolder viewHolder, int direction) {
                final int pos = viewHolder.getAdapterPosition();
                mCardList.remove(pos);
                adapter.notifyItemRemoved(pos);
            }
        });
        itemTouchHelper.attachToRecyclerView(this);
    }

    public static class Card {
        private static int sSerialNumber = 0;
        public final int number;
        public final int color;
        public Card(int color) {
            this.number = ++sSerialNumber;
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
