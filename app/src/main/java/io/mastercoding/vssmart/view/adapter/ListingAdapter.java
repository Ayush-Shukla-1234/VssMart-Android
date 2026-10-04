package io.mastercoding.vssmart.view.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.databinding.ItemListingBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * High-performance adapter for rendering product listings in a responsive marketplace grid.
 * Implements Glide ListPreloader for zero-stutter 60/120 FPS scrolling.
 */
public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder>
        implements ListPreloader.PreloadModelProvider<ListingModel> {

    private final Context context;
    private final List<ListingModel> listingList = new ArrayList<>();
    private final OnListingClickListener listener;

    private static final RequestOptions GLIDE_OPTIONS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .format(DecodeFormat.PREFER_RGB_565)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .override(400, 400)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder);

    public interface OnListingClickListener {
        void onListingClick(ListingModel listing);
    }

    public ListingAdapter(Context context, OnListingClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setListings(List<ListingModel> newList) {
        listingList.clear();
        if (newList != null) {
            listingList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < listingList.size()) {
            listingList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeItem(ListingModel listing) {
        if (listing == null || listing.getId() == null) return;
        for (int i = 0; i < listingList.size(); i++) {
            if (listing.getId().equals(listingList.get(i).getId())) {
                removeItem(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListingBinding binding = ItemListingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ListingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        holder.bind(listingList.get(position));
    }

    @Override
    public int getItemCount() {
        return listingList.size();
    }

    // ListPreloader.PreloadModelProvider implementation
    @NonNull
    @Override
    public List<ListingModel> getPreloadItems(int position) {
        if (position >= 0 && position < listingList.size()) {
            return Collections.singletonList(listingList.get(position));
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull ListingModel item) {
        String url = item.resolveImageUrl();
        if (url == null || url.trim().isEmpty() || context == null) {
            return null;
        }
        return Glide.with(context)
                .load(url)
                .apply(GLIDE_OPTIONS);
    }

    class ListingViewHolder extends RecyclerView.ViewHolder {
        private final ItemListingBinding binding;

        public ListingViewHolder(ItemListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListingModel item) {
            if (item == null) return;

            // 1. High-Performance Glide Image Loading
            binding.ivProduct.setVisibility(View.VISIBLE);
            String url = item.resolveImageUrl();

            if (url != null && !url.trim().isEmpty()) {
                Glide.with(binding.ivProduct.getContext())
                        .load(url)
                        .apply(GLIDE_OPTIONS)
                        .thumbnail(
                                Glide.with(binding.ivProduct.getContext())
                                        .load(url)
                                        .override(50, 50)
                                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        )
                        .centerCrop()
                        .into(binding.ivProduct);
            } else {
                binding.ivProduct.setImageResource(R.drawable.ic_image_placeholder);
            }

            // 2. Product Details
            binding.tvTitle.setText(item.getTitle());
            binding.tvPrice.setText(item.getFormattedPrice());
            binding.tvCategoryBadge.setText(item.getCategory());
            binding.tvLocation.setText(item.getCampusLocationString());

            // 3. Original Price & Discount Handling
            if (item.getOriginalPrice() > item.getPrice() && item.getOriginalPrice() > 0) {
                binding.tvOriginalPrice.setVisibility(View.VISIBLE);
                binding.tvOriginalPrice.setText(item.getFormattedOriginalPrice());
                binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                int discount = item.getDiscountPercent();
                if (discount > 0) {
                    binding.tvDiscountBadge.setVisibility(View.VISIBLE);
                    binding.tvDiscountBadge.setText(discount + "% OFF");
                } else {
                    binding.tvDiscountBadge.setVisibility(View.GONE);
                }
            } else {
                binding.tvOriginalPrice.setVisibility(View.GONE);
                binding.tvDiscountBadge.setVisibility(View.GONE);
            }

            // 4. Sold Status Handling
            if (item.isSold()) {
                binding.layoutSoldOverlay.setVisibility(View.VISIBLE);
            } else {
                binding.layoutSoldOverlay.setVisibility(View.GONE);
            }

            binding.cardListing.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onListingClick(item);
                }
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull ListingViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView.getContext()).clear(holder.binding.ivProduct);
        holder.binding.ivProduct.setImageDrawable(null);
    }
}
