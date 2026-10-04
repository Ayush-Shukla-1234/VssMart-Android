package io.mastercoding.vssmart.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.databinding.ItemMyListingBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for managing the user's posted listings in the "You" tab with quick Mark as Sold and Delete actions.
 */
public class MyListingsAdapter extends RecyclerView.Adapter<MyListingsAdapter.MyListingViewHolder> {

    private final List<ListingModel> items = new ArrayList<>();
    private final OnMyListingActionListener actionListener;

    public interface OnMyListingActionListener {
        void onListingClick(ListingModel listing, int position);
        void onToggleSold(ListingModel listing, int position);
        void onDeleteListing(ListingModel listing, int position);
    }

    public MyListingsAdapter(OnMyListingActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public static final java.util.Comparator<ListingModel> LISTING_COMPARATOR = (item1, item2) -> {
        if (item1 == null && item2 == null) return 0;
        if (item1 == null) return 1;
        if (item2 == null) return -1;

        boolean isSold1 = "SOLD".equalsIgnoreCase(item1.getStatus()) || !item1.isAvailable() || item1.isSold();
        boolean isSold2 = "SOLD".equalsIgnoreCase(item2.getStatus()) || !item2.isAvailable() || item2.isSold();

        // 1. Prioritize AVAILABLE over SOLD
        if (isSold1 != isSold2) {
            return isSold1 ? 1 : -1; // AVAILABLE items come first
        }

        // 2. Secondary sort: Newest first based on timestamp/createdAt
        java.util.Date date1 = item1.getCreatedAt() != null ? item1.getCreatedAt() : item1.getTimestamp();
        java.util.Date date2 = item2.getCreatedAt() != null ? item2.getCreatedAt() : item2.getTimestamp();
        if (date1 != null && date2 != null) {
            return date2.compareTo(date1);
        } else if (date1 != null) {
            return -1;
        } else if (date2 != null) {
            return 1;
        }
        return 0;
    };

    public void setItems(List<ListingModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
            java.util.Collections.sort(items, LISTING_COMPARATOR);
        }
        notifyDataSetChanged();
    }

    public void updateItemSoldStatus(int position, boolean isSold) {
        if (position >= 0 && position < items.size()) {
            ListingModel item = items.get(position);
            item.setSold(isSold);
            item.setStatus(isSold ? ListingModel.STATUS_SOLD : ListingModel.STATUS_AVAILABLE);
            item.setAvailable(!isSold);
            java.util.Collections.sort(items, LISTING_COMPARATOR);
            notifyDataSetChanged();
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void insertItem(int position, ListingModel item) {
        if (position >= 0 && position <= items.size() && item != null) {
            items.add(position, item);
            java.util.Collections.sort(items, LISTING_COMPARATOR);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public MyListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyListingBinding binding = ItemMyListingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MyListingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListingViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyListingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingBinding binding;

        public MyListingViewHolder(ItemMyListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListingModel item) {
            if (item == null) return;

            binding.tvMyItemTitle.setText(item.getTitle());
            binding.tvMyItemPrice.setText(item.getFormattedPrice());
            binding.tvMyItemSubtitle.setText(item.getCategory() + " • " + item.getCampusLocationString());

            // Sold Status Visual Styling
            if (item.isSold()) {
                binding.getRoot().setAlpha(0.70f);
                binding.tvMyItemStatus.setText("Sold Out");
                binding.tvMyItemStatus.setBackgroundResource(R.drawable.bg_sold_badge);
                binding.tvMyItemStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.on_primary));

                // Disable "Mark as Sold" button for already sold items
                binding.btnToggleSold.setEnabled(false);
                binding.btnToggleSold.setText("Sold Out");
                binding.btnToggleSold.setAlpha(0.5f);

                // Keep Delete button active
                binding.btnDeleteListing.setEnabled(true);
                binding.btnDeleteListing.setAlpha(1.0f);
            } else {
                binding.getRoot().setAlpha(1.0f);
                binding.tvMyItemStatus.setText("AVAILABLE");
                binding.tvMyItemStatus.setBackgroundResource(R.drawable.bg_category_badge);
                binding.tvMyItemStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary));

                // Enable "Mark as Sold" button
                binding.btnToggleSold.setEnabled(true);
                binding.btnToggleSold.setText(R.string.mark_as_sold);
                binding.btnToggleSold.setAlpha(1.0f);

                // Keep Delete button active
                binding.btnDeleteListing.setEnabled(true);
                binding.btnDeleteListing.setAlpha(1.0f);
            }

            // Image (Aggressive Disk Caching & Smooth Cross-fade)
            binding.ivItemThumb.setVisibility(android.view.View.VISIBLE);
            String itemUrl = item.resolveImageUrl();
            if (itemUrl != null && !itemUrl.trim().isEmpty()) {
                com.bumptech.glide.request.RequestOptions options = new com.bumptech.glide.request.RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                        .downsample(com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.CENTER_INSIDE)
                        .override(300, 300)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder);

                Glide.with(binding.ivItemThumb.getContext())
                        .load(itemUrl)
                        .apply(options)
                        .thumbnail(
                                Glide.with(binding.ivItemThumb.getContext())
                                        .load(itemUrl)
                                        .override(50, 50)
                                        .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(150))
                        )
                        .centerCrop()
                        .into(binding.ivItemThumb);
            } else {
                binding.ivItemThumb.setImageResource(R.drawable.ic_image_placeholder);
            }

            // Click Listeners
            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (actionListener != null && pos != RecyclerView.NO_POSITION) {
                    actionListener.onListingClick(item, pos);
                }
            });

            binding.btnToggleSold.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (actionListener != null && pos != RecyclerView.NO_POSITION) {
                    actionListener.onToggleSold(item, pos);
                }
            });

            binding.btnDeleteListing.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (actionListener != null && pos != RecyclerView.NO_POSITION) {
                    actionListener.onDeleteListing(item, pos);
                }
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull MyListingViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView.getContext()).clear(holder.binding.ivItemThumb);
        holder.binding.ivItemThumb.setImageDrawable(null);
    }
}
