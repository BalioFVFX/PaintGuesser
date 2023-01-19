package com.paintguesser.view;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paintguesser.App;
import com.paintguesser.R;
import com.paintguesser.databinding.ActivityHistoryBinding;
import com.paintguesser.databinding.ItemGameHistoryBinding;
import com.paintguesser.network.socket.DrawData;
import com.paintguesser.network.socket.client.ClientDrawDataTransformer;
import com.paintguesser.persistance.GameHistory;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<GameHistory> data = new ArrayList<>();
        private final HistoryItemClickListener listener;

        public Adapter(HistoryItemClickListener listener) {
            setHasStableIds(true);
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            return new ViewHolder(
                    ItemGameHistoryBinding.inflate(inflater, parent, false),
                    listener);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        public void update(List<GameHistory> data) {
            final List<GameHistory> old = new ArrayList<>(this.data);

            this.data.clear();
            this.data.addAll(data);

            DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return old.size();
                }

                @Override
                public int getNewListSize() {
                    return data.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return old.get(oldItemPosition).id == data.get(newItemPosition).id;
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    GameHistory oldHistory = old.get(oldItemPosition);
                    GameHistory newHistory = data.get(newItemPosition);

                    return oldHistory.canvasData.equals(newHistory.canvasData) &&
                            oldHistory.player.equals(newHistory.player) &&
                            oldHistory.timestamp == newHistory.timestamp;
                }
            }).dispatchUpdatesTo(this);
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {

            private final ItemGameHistoryBinding binding;
            private final HistoryItemClickListener listener;

            public ViewHolder(@NonNull ItemGameHistoryBinding binding, HistoryItemClickListener listener) {
                super(binding.getRoot());
                this.binding = binding;
                this.listener = listener;
            }

            public void bind(GameHistory gameHistory) {
                binding.tvPlayerType.setText(
                        binding.getRoot().getContext().getString(R.string.role) + ": " +
                                gameHistory.player);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(gameHistory.timestamp);

                String date = DateFormat.getDateInstance().format(calendar.getTime());
                binding.tvDate.setText(
                        binding.getRoot().getContext().getString(R.string.date) + ": " +
                                date);
                binding.tvRating.setText(
                        binding.getRoot().getContext().getString(R.string.rating) + ": " +
                                gameHistory.rating);

                binding.tvRival.setText(
                        binding.getRoot().getContext().getString(R.string.rival) + ": " +
                                gameHistory.rivalUsername
                );

                binding.tvGuess.setText("Guess: " + gameHistory.guess);

                binding.getRoot().setOnClickListener(v -> listener.onClick(gameHistory));
            }
        }
    }

    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.drawingView.setPlayable(false);
        final ClientDrawDataTransformer transformer = new ClientDrawDataTransformer();

        final Adapter adapter = new Adapter(history -> {
            binding.recyclerView.setVisibility(View.GONE);
            binding.drawingView.setVisibility(View.VISIBLE);
            binding.buttonHide.setVisibility(View.VISIBLE);

            binding.drawingView.update(transformer.transform(history.canvasData));
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        binding.buttonHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.recyclerView.setVisibility(View.VISIBLE);

                binding.drawingView.setVisibility(View.GONE);
                binding.drawingView.update(new DrawData(new ArrayList<>(), 0, 0));
                binding.buttonHide.setVisibility(View.GONE);
            }
        });

        App.persistence.loadGameHistories(data -> {
            if (data.size() == 0) {
                binding.tvNoData.setVisibility(View.VISIBLE);
            } else {
                adapter.update(data);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.drawingView.getVisibility() == View.VISIBLE) {
            binding.drawingView.setVisibility(View.GONE);
            binding.buttonHide.setVisibility(View.GONE);

            binding.recyclerView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}