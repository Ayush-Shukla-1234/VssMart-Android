package io.mastercoding.vssmart.view.main;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.data.model.UserModel;
import io.mastercoding.vssmart.databinding.FragmentAddListingBinding;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.ImageCompressionHelper;
import io.mastercoding.vssmart.utils.SharedPrefManager;
import io.mastercoding.vssmart.utils.WhatsAppHelper;
import io.mastercoding.vssmart.viewmodel.ListingViewModel;
import io.mastercoding.vssmart.viewmodel.UserViewModel;

/**
 * Add Listing Fragment allowing students to post pre-owned essentials with modern Photo Picker
 * and reactive pre-filling of latest campus hostel, room, and WhatsApp details.
 */
public class AddListingFragment extends Fragment {

    private FragmentAddListingBinding binding;
    private ListingViewModel listingViewModel;
    private UserViewModel userViewModel;
    private Uri selectedImageUri;
    private SharedPrefManager prefManager;
    private boolean isSubmitting = false;

    // Modern Android Photo Picker Contract (No broad storage permissions required)
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    displaySelectedImage(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listingViewModel = new ViewModelProvider(requireActivity()).get(ListingViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        prefManager = SharedPrefManager.getInstance(requireContext());

        setupDropdowns();
        observeUserProfile();
        loadUserProfileAndPrefill();
        setupPhotoPicker();
        setupSubmitButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfileAndPrefill();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadUserProfileAndPrefill();
        }
    }

    private void loadUserProfileAndPrefill() {
        // 1. Load from local cache immediately
        UserModel cachedUser = prefManager.getCachedUser();
        if (cachedUser != null) {
            populateUserFields(cachedUser);
        }

        // 2. Fetch fresh user data from shared UserViewModel / Firestore
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : prefManager.getUserId();

        if (!uid.isEmpty() && userViewModel != null) {
            userViewModel.getUserProfile(uid).observe(getViewLifecycleOwner(), resource -> {
                if (resource != null && resource.isSuccess() && resource.data != null) {
                    UserModel user = resource.data;
                    prefManager.saveUser(user);
                    populateUserFields(user);
                }
            });
        }
    }

    private void observeUserProfile() {
        if (userViewModel != null) {
            userViewModel.getCurrentUserProfileLiveData().observe(getViewLifecycleOwner(), resource -> {
                if (resource != null && resource.isSuccess() && resource.data != null) {
                    populateUserFields(resource.data);
                }
            });
        }
    }

    private void setupDropdowns() {
        // Categories Dropdown
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.item_categories,
                android.R.layout.simple_dropdown_item_1line
        );
        binding.actvCategory.setAdapter(categoryAdapter);
        String[] categoryOptions = getResources().getStringArray(R.array.item_categories);
        if (categoryOptions.length > 0) {
            binding.actvCategory.setText(categoryOptions[0], false);
        }

        // Conditions Dropdown
        ArrayAdapter<CharSequence> conditionAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.item_conditions,
                android.R.layout.simple_dropdown_item_1line
        );
        binding.actvCondition.setAdapter(conditionAdapter);
        String[] conditionOptions = getResources().getStringArray(R.array.item_conditions);
        if (conditionOptions.length > 0) {
            binding.actvCondition.setText(conditionOptions[0], false);
        }
    }

    private void populateUserFields(UserModel user) {
        if (user == null || binding == null) return;

        if (user.getHostel() != null && !user.getHostel().isEmpty()) {
            binding.etHostel.setText(user.getHostel());
        }

        if (user.getRoom() != null && !user.getRoom().isEmpty()) {
            binding.etRoom.setText(user.getRoom());
        }

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String digits = user.getPhone().replaceAll("[^0-9]", "");
            if (digits.length() > 10) {
                digits = digits.substring(digits.length() - 10);
            }
            binding.etWhatsApp.setText(digits);
        }
    }

    private void setupPhotoPicker() {
        binding.cardPhotoPicker.setOnClickListener(v -> {
            if (isSubmitting) return;
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
    }

    private void displaySelectedImage(Uri uri) {
        binding.layoutPhotoPlaceholder.setVisibility(View.GONE);
        binding.ivSelectedPhoto.setVisibility(View.VISIBLE);
        binding.tvChangePhoto.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.ivSelectedPhoto);
    }

    private void setupSubmitButton() {
        binding.btnPostListing.setOnClickListener(v -> validateAndSubmitListing());
    }

    private void validateAndSubmitListing() {
        if (isSubmitting) {
            return;
        }

        if (selectedImageUri == null) {
            Snackbar.make(binding.getRoot(), R.string.error_image_required, Snackbar.LENGTH_LONG).show();
            return;
        }

        String title = binding.etItemTitle.getText() != null ? binding.etItemTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            binding.tilItemTitle.setError(getString(R.string.error_required_field));
            return;
        }
        binding.tilItemTitle.setError(null);

        String priceStr = binding.etSellingPrice.getText() != null ? binding.etSellingPrice.getText().toString().trim() : "";
        if (priceStr.isEmpty()) {
            binding.tilSellingPrice.setError(getString(R.string.error_required_field));
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                binding.tilSellingPrice.setError("Price must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilSellingPrice.setError("Invalid price format");
            return;
        }
        binding.tilSellingPrice.setError(null);

        double parsedOrigPrice = 0;
        String origPriceStr = binding.etOriginalPrice.getText() != null ? binding.etOriginalPrice.getText().toString().trim() : "";
        if (!origPriceStr.isEmpty()) {
            try {
                parsedOrigPrice = Double.parseDouble(origPriceStr);
            } catch (NumberFormatException ignored) {}
        }
        final double originalPrice = parsedOrigPrice;

        String category = binding.actvCategory.getText().toString().trim();
        if (category.isEmpty() || "Select Category".equalsIgnoreCase(category)) {
            binding.tilCategory.setError("Please select a category");
            return;
        }
        binding.tilCategory.setError(null);

        String condition = binding.actvCondition.getText().toString().trim();
        if (condition.isEmpty() || "Select Condition".equalsIgnoreCase(condition)) {
            binding.tilCondition.setError("Please select item condition");
            return;
        }
        binding.tilCondition.setError(null);

        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        String hostel = binding.etHostel.getText() != null ? binding.etHostel.getText().toString().trim() : "";
        if (hostel.isEmpty()) {
            binding.tilHostel.setError(getString(R.string.error_required_field));
            return;
        }
        binding.tilHostel.setError(null);

        String room = binding.etRoom.getText() != null ? binding.etRoom.getText().toString().trim() : "";

        String whatsapp = binding.etWhatsApp.getText() != null ? binding.etWhatsApp.getText().toString().trim() : "";
        if (!WhatsAppHelper.isValidPhoneNumber(whatsapp)) {
            binding.tilWhatsApp.setError(getString(R.string.invalid_phone_error));
            return;
        }
        binding.tilWhatsApp.setError(null);

        // Validate authenticated user session
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Snackbar.make(binding.getRoot(), "Please sign in to post an item.", Snackbar.LENGTH_LONG).show();
            return;
        }

        final String sellerId = currentUser.getUid();
        final String sellerName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : prefManager.getUserName();

        // Lock button and start progress
        isSubmitting = true;
        showPostingProgress(true);

        // Compress image in background worker before uploading to Firebase Storage
        android.content.Context appContext = requireContext().getApplicationContext();
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            byte[] compressedBytes = ImageCompressionHelper.compressImage(appContext, selectedImageUri);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (compressedBytes == null || compressedBytes.length == 0) {
                    isSubmitting = false;
                    showPostingProgress(false);
                    Snackbar.make(binding.getRoot(), "Failed to process image. Please select another photo.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                listingViewModel.uploadImage(compressedBytes).observe(getViewLifecycleOwner(), uploadResource -> {
                    if (uploadResource == null) return;

                    if (uploadResource.isSuccess() && uploadResource.data != null) {
                        String imageUrl = uploadResource.data;

                        // Build ListingModel
                        ListingModel newListing = new ListingModel();
                        newListing.setSellerId(sellerId);
                        newListing.setSellerName(sellerName);
                        newListing.setTitle(title);
                        newListing.setDescription(description);
                        newListing.setPrice(price);
                        newListing.setOriginalPrice(originalPrice);
                        newListing.setCategory(category);
                        newListing.setCondition(condition);
                        newListing.setHostel(hostel);
                        newListing.setRoomNo(room);
                        newListing.setWhatsappPhone(whatsapp);
                        newListing.setImageUrl(imageUrl);
                        newListing.setSold(false);

                        // Save profile updates to prefs
                        prefManager.updateCampusDetails(hostel, room, whatsapp);

                        listingViewModel.createListing(newListing).observe(getViewLifecycleOwner(), createResource -> {
                            if (createResource == null) return;

                            if (createResource.isSuccess()) {
                                showPostingProgress(false);
                                Toast.makeText(requireContext(), R.string.success_listing_created, Toast.LENGTH_SHORT).show();
                                resetForm();
                                isSubmitting = false;

                                if (requireActivity() instanceof MainActivity) {
                                    ((MainActivity) requireActivity()).selectMarketTab();
                                }
                            } else if (createResource.isError()) {
                                isSubmitting = false;
                                showPostingProgress(false);
                                Snackbar.make(binding.getRoot(), createResource.message != null ? createResource.message : "Failed to post listing", Snackbar.LENGTH_LONG).show();
                            }
                        });

                    } else if (uploadResource.isError()) {
                        isSubmitting = false;
                        showPostingProgress(false);
                        Snackbar.make(binding.getRoot(), uploadResource.message != null ? uploadResource.message : "Image upload failed", Snackbar.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    private void resetForm() {
        selectedImageUri = null;
        binding.ivSelectedPhoto.setImageDrawable(null);
        binding.ivSelectedPhoto.setVisibility(View.GONE);
        binding.tvChangePhoto.setVisibility(View.GONE);
        binding.layoutPhotoPlaceholder.setVisibility(View.VISIBLE);

        binding.etItemTitle.setText("");
        binding.etSellingPrice.setText("");
        binding.etOriginalPrice.setText("");
        binding.etDescription.setText("");

        String[] categoryOptions = getResources().getStringArray(R.array.item_categories);
        if (categoryOptions.length > 0) {
            binding.actvCategory.setText(categoryOptions[0], false);
        }
        binding.tilCategory.setError(null);

        String[] conditionOptions = getResources().getStringArray(R.array.item_conditions);
        if (conditionOptions.length > 0) {
            binding.actvCondition.setText(conditionOptions[0], false);
        }
        binding.tilCondition.setError(null);

        UserModel cachedUser = prefManager.getCachedUser();
        if (cachedUser != null) {
            populateUserFields(cachedUser);
        }
    }

    private void showPostingProgress(boolean isPosting) {
        binding.layoutPostingLoading.setVisibility(isPosting ? View.VISIBLE : View.GONE);
        binding.btnPostListing.setEnabled(!isPosting);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
