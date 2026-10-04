package io.mastercoding.vssmart.view.detail;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.databinding.ActivityListingDetailBinding;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.WhatsAppHelper;

/**
 * Listing Detail Activity displaying comprehensive item specifications, campus location,
 * and direct WhatsApp / Call connection actions with real-time status synchronization.
 */
public class ListingDetailActivity extends AppCompatActivity {

    private ActivityListingDetailBinding binding;
    private ListingModel listing;
    private ListenerRegistration listingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityListingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractListingData();
        setupToolbar();
        populateListingDetails();
        setupActionButtons();
        attachRealtimeListingListener();
    }

    private void attachRealtimeListingListener() {
        if (listing == null || listing.getId() == null || listing.getId().isEmpty()) return;

        listingListener = FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_LISTINGS)
                .document(listing.getId())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    ListingModel liveListing = snapshot.toObject(ListingModel.class);
                    if (liveListing != null) {
                        liveListing.setId(snapshot.getId());
                        this.listing = liveListing;
                        populateListingDetails();
                    }
                });
    }

    private void extractListingData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.EXTRA_LISTING)) {
            listing = (ListingModel) getIntent().getSerializableExtra(Constants.EXTRA_LISTING);
        }
        if (listing == null) {
            finish();
        }
    }

    private void setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());
    }

    private void populateListingDetails() {
        if (listing == null) return;

        binding.tvDetailTitle.setText(listing.getTitle());
        binding.tvDetailPrice.setText(listing.getFormattedPrice());
        binding.tvDetailCategory.setText(listing.getCategory());
        binding.tvDetailCondition.setText("Condition: " + listing.getCondition());
        binding.tvDetailLocation.setText(listing.getCampusLocationString());
        binding.tvDetailSellerName.setText(listing.getSellerName());

        // Description
        if (!listing.getDescription().isEmpty()) {
            binding.tvDetailDescription.setText(listing.getDescription());
        } else {
            binding.tvDetailDescription.setText("No additional description provided by the seller.");
        }

        // Original Price & Discount
        if (listing.getOriginalPrice() > listing.getPrice() && listing.getOriginalPrice() > 0) {
            binding.tvDetailOriginalPrice.setVisibility(View.VISIBLE);
            binding.tvDetailOriginalPrice.setText(listing.getFormattedOriginalPrice());
            binding.tvDetailOriginalPrice.setPaintFlags(binding.tvDetailOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            int discount = listing.getDiscountPercent();
            if (discount > 0) {
                binding.tvDetailDiscount.setVisibility(View.VISIBLE);
                binding.tvDetailDiscount.setText(discount + "% OFF");
            }
        }

        // Sold Overlay
        if (listing.isSold()) {
            binding.layoutDetailSoldOverlay.setVisibility(View.VISIBLE);
            binding.btnChatWhatsApp.setEnabled(false);
            binding.btnChatWhatsApp.setText(R.string.sold_tag);
            binding.btnChatWhatsApp.setBackgroundColor(getColor(R.color.sold_red));
        }

        // Hero Image
        String imageUrl = listing.resolveImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            com.bumptech.glide.request.RequestOptions heroOptions = new com.bumptech.glide.request.RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                    .downsample(com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.CENTER_INSIDE)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder);

            Glide.with(this)
                    .load(imageUrl)
                    .apply(heroOptions)
                    .thumbnail(
                            Glide.with(this)
                                    .load(imageUrl)
                                    .override(50, 50)
                                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(150))
                    )
                    .centerCrop()
                    .into(binding.ivDetailPhoto);
        } else {
            binding.ivDetailPhoto.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Admin Moderation Card Visibility
        if (io.mastercoding.vssmart.utils.SharedPrefManager.getInstance(this).isAdmin()) {
            binding.cardDetailAdminActions.setVisibility(View.VISIBLE);
        } else {
            binding.cardDetailAdminActions.setVisibility(View.GONE);
        }
    }

    private void setupActionButtons() {
        binding.btnChatWhatsApp.setOnClickListener(v -> {
            WhatsAppHelper.openWhatsAppChat(this, listing);
        });

        binding.btnCallSeller.setOnClickListener(v -> {
            WhatsAppHelper.openPhoneDialer(this, listing.getWhatsappPhone());
        });

        binding.btnDetailAdminRemove.setOnClickListener(v -> showAdminRemoveConfirmation());
    }

    private void showAdminRemoveConfirmation() {
        if (listing == null || listing.getId() == null) return;

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Admin Moderation")
                .setMessage("Are you sure you want to permanently remove this listing? It will be completely purged from the public marketplace and all user listings.")
                .setPositiveButton("Remove Listing", (dialog, which) -> {
                    String imageUrl = listing.resolveImageUrl();

                    // Step A: Delete Image from Firebase Storage if URL exists
                    if (imageUrl != null && imageUrl.startsWith("https://firebasestorage.googleapis.com")) {
                        try {
                            com.google.firebase.storage.StorageReference photoRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            photoRef.delete()
                                    .addOnSuccessListener(aVoid -> android.util.Log.d("StorageDelete", "Listing image deleted from Storage successfully"))
                                    .addOnFailureListener(e -> android.util.Log.e("StorageDelete", "Failed to delete image from Storage: " + e.getMessage()));
                        } catch (Exception e) {
                            android.util.Log.e("StorageDelete", "Invalid storage reference URL", e);
                        }
                    }

                    // Step B: Delete Document from Firestore
                    FirebaseFirestore.getInstance().collection(Constants.COLLECTION_LISTINGS)
                            .document(listing.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                android.widget.Toast.makeText(this, "Listing and associated photo completely removed", android.widget.Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                android.widget.Toast.makeText(this, "Failed to delete listing: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listingListener != null) {
            listingListener.remove();
            listingListener = null;
        }
    }
}
