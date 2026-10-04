package io.mastercoding.vssmart.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.data.repository.ListingRepository;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel managing active market listings, category chip filters, search queries, and item uploads.
 */
public class ListingViewModel extends AndroidViewModel {

    private final ListingRepository repository;
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>(Constants.CATEGORY_ALL);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<Resource<List<ListingModel>>> filteredListings = new MediatorLiveData<>();

    private LiveData<Resource<List<ListingModel>>> currentRepoSource;

    public ListingViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ListingRepository();
        setupListingsObserver();
    }

    private void setupListingsObserver() {
        // Whenever selectedCategory changes, fetch fresh data from repository
        LiveData<Resource<List<ListingModel>>> categoryListings = Transformations.switchMap(selectedCategory, repository::getActiveListings);

        filteredListings.addSource(categoryListings, resource -> {
            applySearchFilter(resource, searchQuery.getValue());
        });

        filteredListings.addSource(searchQuery, query -> {
            if (currentRepoSource != null && currentRepoSource.getValue() != null) {
                applySearchFilter(currentRepoSource.getValue(), query);
            }
        });

        this.currentRepoSource = categoryListings;
    }

    private void applySearchFilter(Resource<List<ListingModel>> resource, String query) {
        if (resource == null) return;

        if (resource.isSuccess() && resource.data != null) {
            String lowerQuery = query != null ? query.trim().toLowerCase() : "";
            List<ListingModel> availableItems = new ArrayList<>();
            for (ListingModel item : resource.data) {
                // Filter out SOLD or REMOVED_BY_ADMIN items so buyers only see currently available listings in real-time
                if (item.isSold() || Constants.STATUS_REMOVED_BY_ADMIN.equalsIgnoreCase(item.getStatus())) {
                    continue;
                }

                if (lowerQuery.isEmpty()) {
                    availableItems.add(item);
                } else {
                    if (item.getTitle().toLowerCase().contains(lowerQuery)
                            || item.getDescription().toLowerCase().contains(lowerQuery)
                            || item.getCategory().toLowerCase().contains(lowerQuery)
                            || item.getHostel().toLowerCase().contains(lowerQuery)) {
                        availableItems.add(item);
                    }
                }
            }
            filteredListings.setValue(Resource.success(availableItems));
        } else {
            filteredListings.setValue(resource);
        }
    }

    public LiveData<Resource<List<ListingModel>>> getListings() {
        return filteredListings;
    }

    public void setSelectedCategory(String category) {
        if (category != null && !category.equals(selectedCategory.getValue())) {
            selectedCategory.setValue(category);
        }
    }

    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<Resource<String>> uploadImage(byte[] imageBytes) {
        return repository.uploadImage(imageBytes);
    }

    public LiveData<Resource<String>> uploadImage(Uri imageUri) {
        return repository.uploadImage(imageUri);
    }

    public LiveData<Resource<String>> createListing(ListingModel listing) {
        return repository.createListing(listing);
    }

    public LiveData<Resource<Boolean>> adminRemoveListing(String listingId) {
        return repository.adminRemoveListing(listingId);
    }

    public void refreshListings() {
        String currentCat = selectedCategory.getValue();
        selectedCategory.setValue(currentCat != null ? currentCat : Constants.CATEGORY_ALL);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}
