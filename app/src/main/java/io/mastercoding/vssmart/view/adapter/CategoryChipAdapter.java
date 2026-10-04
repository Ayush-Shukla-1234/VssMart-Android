package io.mastercoding.vssmart.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.databinding.ItemCategoryChipBinding;

/**
 * Adapter for the horizontal category filter chips in the marketplace feed.
 */
public class CategoryChipAdapter extends RecyclerView.Adapter<CategoryChipAdapter.CategoryViewHolder> {

    private final String[] categories;
    private String selectedCategory;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryChipAdapter(String[] categories, String initialSelection, OnCategoryClickListener listener) {
        this.categories = categories;
        this.selectedCategory = initialSelection != null ? initialSelection : categories[0];
        this.listener = listener;
    }

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryChipBinding binding = ItemCategoryChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories[position];
        holder.bind(category, category.equalsIgnoreCase(selectedCategory));
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.length : 0;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryChipBinding binding;

        public CategoryViewHolder(ItemCategoryChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String category, boolean isSelected) {
            binding.tvCategoryName.setText(category);

            if (isSelected) {
                binding.chipContainer.setCardBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary)
                );
                binding.chipContainer.setStrokeWidth(0);
                binding.tvCategoryName.setTextColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.on_primary)
                );
            } else {
                binding.chipContainer.setCardBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.surface_light)
                );
                binding.chipContainer.setStrokeWidth(2);
                binding.chipContainer.setStrokeColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.divider_stroke)
                );
                binding.tvCategoryName.setTextColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.text_primary)
                );
            }

            binding.chipContainer.setOnClickListener(v -> {
                if (listener != null) {
                    selectedCategory = category;
                    notifyDataSetChanged();
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}
