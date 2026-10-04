package io.mastercoding.vssmart.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.databinding.FragmentMarketBinding;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.Resource;
import io.mastercoding.vssmart.view.adapter.CategoryChipAdapter;
import io.mastercoding.vssmart.view.adapter.ListingAdapter;
import io.mastercoding.vssmart.view.detail.ListingDetailActivity;
import io.mastercoding.vssmart.viewmodel.ListingViewModel;
import java.util.List;

/**
 * Market Feed Fragment: Responsive product grid with search, category chip filters, and Glide preloading.
 */
public class MarketFragment extends Fragment implements ListingAdapter.OnListingClickListener, CategoryChipAdapter.OnCategoryClickListener {

    private FragmentMarketBinding binding;
    private ListingViewModel listingViewModel;
    private ListingAdapter listingAdapter;
    private CategoryChipAdapter categoryChipAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMarketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listingViewModel = new ViewModelProvider(requireActivity()).get(ListingViewModel.class);

        setupRecyclerView();
        setupCategoryChips();
        setupSearchInput();
        setupSwipeRefresh();
        observeListings();
    }

    private int calculateSpanCount() {
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        if (screenWidthDp >= 900) {
            return 4;
        } else if (screenWidthDp >= 600) {
            return 3;
        } else {
            return 2;
        }
    }

    private void setupRecyclerView() {
        listingAdapter = new ListingAdapter(requireContext(), this);
        binding.rvListings.setLayoutManager(new GridLayoutManager(requireContext(), calculateSpanCount()));
        binding.rvListings.setAdapter(listingAdapter);
        binding.rvListings.setHasFixedSize(true);
        binding.rvListings.setItemViewCacheSize(20);

        // Preload upcoming off-screen card images into memory/disk cache 6 items ahead
        FixedPreloadSizeProvider<ListingModel> sizeProvider = new FixedPreloadSizeProvider<>(400, 400);
        RecyclerViewPreloader<ListingModel> preloader = new RecyclerViewPreloader<>(
                Glide.with(this),
                listingAdapter,
                sizeProvider,
                6
        );
        binding.rvListings.addOnScrollListener(preloader);
    }

    private void setupCategoryChips() {
        categoryChipAdapter = new CategoryChipAdapter(Constants.CATEGORIES, Constants.CATEGORY_ALL, this);
        binding.rvCategoryChips.setAdapter(categoryChipAdapter);
    }

    private void setupSearchInput() {
        binding.etSearchListings.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                listingViewModel.setSearchQuery(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearchListings.setText("");
            listingViewModel.setSearchQuery("");
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshMarket.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefreshMarket.setOnRefreshListener(() -> {
            listingViewModel.refreshListings();
        });
    }

    private void observeListings() {
        listingViewModel.getListings().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefreshMarket.setRefreshing(false);

            if (resource.isLoading()) {
                if (listingAdapter.getItemCount() == 0) {
                    binding.progressBarMarket.setVisibility(View.VISIBLE);
                }
                binding.layoutEmptyState.setVisibility(View.GONE);
            } else if (resource.isSuccess()) {
                binding.progressBarMarket.setVisibility(View.GONE);
                List<ListingModel> items = resource.data;
                listingAdapter.setListings(items);

                if (items == null || items.isEmpty()) {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.rvListings.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyState.setVisibility(View.GONE);
                    binding.rvListings.setVisibility(View.VISIBLE);
                }
            } else if (resource.isError()) {
                binding.progressBarMarket.setVisibility(View.GONE);
                if (listingAdapter.getItemCount() == 0) {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.tvEmptyTitle.setText("Connection Error");
                    binding.tvEmptyDesc.setText(resource.message);
                }
            }
        });
    }

    @Override
    public void onCategoryClick(String category) {
        listingViewModel.setSelectedCategory(category);
    }

    @Override
    public void onListingClick(ListingModel listing) {
        Intent intent = new Intent(requireContext(), ListingDetailActivity.class);
        intent.putExtra(Constants.EXTRA_LISTING, listing);
        startActivity(intent);
    }

    public void scrollToTop() {
        if (binding != null && binding.rvListings != null) {
            binding.rvListings.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
