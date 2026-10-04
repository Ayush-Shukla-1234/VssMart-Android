package io.mastercoding.vssmart.data.repository;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository managing Firestore CRUD operations for Product Listings and Firebase Storage uploads.
 */
public class ListingRepository {

    private static final String TAG = "ListingRepository";

    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private ListenerRegistration activeListingsListener;
    private ListenerRegistration myListingsListener;

    public ListingRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Listens to active product listings in real-time, sorting by timestamp and filtering in-memory
     * to completely prevent Firestore composite index requirements.
     */
    public LiveData<Resource<List<ListingModel>>> getActiveListings(String category) {
        MutableLiveData<Resource<List<ListingModel>>> listingsLiveData = new MutableLiveData<>();
        listingsLiveData.setValue(Resource.loading());

        if (activeListingsListener != null) {
            activeListingsListener.remove();
        }

        // Single-field order query to avoid composite index demands
        Query query = firestore.collection(Constants.COLLECTION_LISTINGS)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        activeListingsListener = query.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Failed to load listings snapshot: " + error.getMessage(), error);
                listingsLiveData.setValue(Resource.error("Failed to load listings: " + error.getMessage()));
                return;
            }

            List<ListingModel> listings = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    ListingModel listing = doc.toObject(ListingModel.class);
                    if (listing != null) {
                        listing.setId(doc.getId());

                        // In-memory filter: exclude sold, removed, banned, or unavailable items
                        if (!listing.isAvailable() || listing.isSold()
                                || ListingModel.STATUS_SOLD.equalsIgnoreCase(listing.getStatus())
                                || Constants.STATUS_REMOVED_BY_ADMIN.equalsIgnoreCase(listing.getStatus())
                                || "BANNED".equalsIgnoreCase(listing.getStatus())
                                || listing.isBanned()) {
                            continue;
                        }

                        // In-memory filter: category matching
                        if (category != null && !category.equalsIgnoreCase(Constants.CATEGORY_ALL) && !category.isEmpty()) {
                            if (!category.equalsIgnoreCase(listing.getCategory())) {
                                continue;
                            }
                        }

                        listings.add(listing);
                    }
                }
            }
            listingsLiveData.setValue(Resource.success(listings));
        });

        return listingsLiveData;
    }

    /**
     * Uploads compressed image bytes to Firebase Storage and returns its public download URL.
     */
    public LiveData<Resource<String>> uploadImage(byte[] imageBytes) {
        MutableLiveData<Resource<String>> uploadLiveData = new MutableLiveData<>();
        uploadLiveData.setValue(Resource.loading());

        if (imageBytes == null || imageBytes.length == 0) {
            Log.e(TAG, "Image upload failed: imageBytes is null or empty");
            uploadLiveData.setValue(Resource.error("Invalid image data"));
            return uploadLiveData;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Image upload failed: User is not authenticated in Firebase Auth");
            uploadLiveData.setValue(Resource.error("User not authenticated. Please log in again."));
            return uploadLiveData;
        }

        String imageName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference()
                .child(Constants.STORAGE_LISTINGS)
                .child(imageName);

        com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        Log.d(TAG, "Starting Storage upload for compressed bytes (" + (imageBytes.length / 1024) + " KB) to " + imageRef.getPath());

        imageRef.putBytes(imageBytes, metadata)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Storage upload succeeded. Fetching download URL for " + imageName);
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                Log.d(TAG, "Download URL retrieved: " + downloadUri.toString());
                                uploadLiveData.setValue(Resource.success(downloadUri.toString()));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to retrieve download URL for " + imageName, e);
                                uploadLiveData.setValue(Resource.error("Failed to get image download URL: " + e.getMessage()));
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase Storage upload failed for " + imageName + ": " + e.getMessage(), e);
                    uploadLiveData.setValue(Resource.error("Image upload failed: " + e.getMessage()));
                });

        return uploadLiveData;
    }

    /**
     * Uploads the selected image URI to Firebase Storage and returns its public download URL.
     */
    public LiveData<Resource<String>> uploadImage(Uri imageUri) {
        MutableLiveData<Resource<String>> uploadLiveData = new MutableLiveData<>();
        uploadLiveData.setValue(Resource.loading());

        if (imageUri == null) {
            Log.e(TAG, "Image upload failed: imageUri is null");
            uploadLiveData.setValue(Resource.error("Invalid image path"));
            return uploadLiveData;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Image upload failed: User is not authenticated in Firebase Auth");
            uploadLiveData.setValue(Resource.error("User not authenticated. Please log in again."));
            return uploadLiveData;
        }

        String imageName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference()
                .child(Constants.STORAGE_LISTINGS)
                .child(imageName);

        Log.d(TAG, "Starting Storage upload for: " + imageUri + " to " + imageRef.getPath());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Storage upload succeeded. Fetching download URL for " + imageName);
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                Log.d(TAG, "Download URL retrieved: " + downloadUri.toString());
                                uploadLiveData.setValue(Resource.success(downloadUri.toString()));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to retrieve download URL for " + imageName, e);
                                uploadLiveData.setValue(Resource.error("Failed to get image download URL: " + e.getMessage()));
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase Storage upload failed for " + imageName + ": " + e.getMessage(), e);
                    uploadLiveData.setValue(Resource.error("Image upload failed: " + e.getMessage()));
                });

        return uploadLiveData;
    }

    /**
     * Creates a new listing document in Firestore.
     */
    public LiveData<Resource<String>> createListing(ListingModel listing) {
        MutableLiveData<Resource<String>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Create listing failed: User is not authenticated");
            resultLiveData.setValue(Resource.error("User not authenticated. Please log in again."));
            return resultLiveData;
        }

        String authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (listing.getSellerId() == null || listing.getSellerId().isEmpty()) {
            listing.setSellerId(authUid);
        }

        if (listing.getStatus() == null || listing.getStatus().isEmpty()) {
            listing.setStatus(ListingModel.STATUS_AVAILABLE);
        }
        listing.setAvailable(true);
        listing.setSold(false);

        Date now = new Date();
        if (listing.getTimestamp() == null) {
            listing.setTimestamp(now);
        }
        if (listing.getCreatedAt() == null) {
            listing.setCreatedAt(now);
        }

        String docId = firestore.collection(Constants.COLLECTION_LISTINGS).document().getId();
        listing.setId(docId);

        Log.d(TAG, "Creating Firestore listing: docId=" + docId + ", sellerId=" + listing.getSellerId()
                + ", title=" + listing.getTitle() + ", price=" + listing.getPrice() + ", imageUrl=" + listing.getImageUrl());

        firestore.collection(Constants.COLLECTION_LISTINGS).document(docId)
                .set(listing)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore listing created successfully with docId: " + docId);
                    resultLiveData.setValue(Resource.success(docId));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore createListing failed for docId=" + docId + ", sellerId=" + listing.getSellerId() + ": " + e.getMessage(), e);
                    // Clean up orphaned storage image to avoid abandoned files
                    if (listing.getImageUrl() != null && !listing.getImageUrl().isEmpty()) {
                        deleteImageByUrl(listing.getImageUrl());
                    }
                    resultLiveData.setValue(Resource.error("Failed to post listing: " + e.getMessage()));
                });

        return resultLiveData;
    }

    /**
     * Deletes an uploaded image from Firebase Storage if document creation fails or upon listing removal.
     */
    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        try {
            StorageReference photoRef = storage.getReferenceFromUrl(imageUrl);
            photoRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully cleaned up orphaned storage image: " + imageUrl))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to clean up orphaned storage image: " + e.getMessage()));
        } catch (Exception e) {
            Log.w(TAG, "Could not resolve storage reference from URL: " + imageUrl, e);
        }
    }

    /**
     * Fetches all listings posted by a specific user (both active and sold) in real-time.
     */
    public LiveData<Resource<List<ListingModel>>> getUserListings(String sellerId) {
        MutableLiveData<Resource<List<ListingModel>>> myListingsLiveData = new MutableLiveData<>();
        myListingsLiveData.setValue(Resource.loading());

        if (myListingsListener != null) {
            myListingsListener.remove();
        }

        if (sellerId == null || sellerId.isEmpty()) {
            myListingsLiveData.setValue(Resource.error("Invalid seller ID"));
            return myListingsLiveData;
        }

        // Single field equality filter to prevent composite index errors
        Query query = firestore.collection(Constants.COLLECTION_LISTINGS)
                .whereEqualTo("sellerId", sellerId);

        myListingsListener = query.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Failed to fetch user listings snapshot: " + error.getMessage(), error);
                myListingsLiveData.setValue(Resource.error("Failed to fetch your listings: " + error.getMessage()));
                return;
            }

            List<ListingModel> list = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    ListingModel listing = doc.toObject(ListingModel.class);
                    if (listing != null) {
                        listing.setId(doc.getId());

                        // Exclude banned or admin-removed items from "My Listings"
                        if (Constants.STATUS_REMOVED_BY_ADMIN.equalsIgnoreCase(listing.getStatus())
                                || "BANNED".equalsIgnoreCase(listing.getStatus())
                                || listing.isBanned()) {
                            continue;
                        }

                        list.add(listing);
                    }
                }
                // Sort in-memory: 1. AVAILABLE items first, 2. Newest timestamp/createdAt first
                list.sort((item1, item2) -> {
                    boolean isSold1 = "SOLD".equalsIgnoreCase(item1.getStatus()) || !item1.isAvailable() || item1.isSold();
                    boolean isSold2 = "SOLD".equalsIgnoreCase(item2.getStatus()) || !item2.isAvailable() || item2.isSold();

                    // 1. Prioritize AVAILABLE over SOLD
                    if (isSold1 != isSold2) {
                        return isSold1 ? 1 : -1; // AVAILABLE items come first
                    }

                    // 2. Secondary sort: Newest first based on timestamp/createdAt
                    Date date1 = item1.getCreatedAt() != null ? item1.getCreatedAt() : item1.getTimestamp();
                    Date date2 = item2.getCreatedAt() != null ? item2.getCreatedAt() : item2.getTimestamp();
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1);
                    } else if (date1 != null) {
                        return -1;
                    } else if (date2 != null) {
                        return 1;
                    }
                    return 0;
                });
            }
            Log.d(TAG, "Real-time user listings update received: " + list.size() + " items");
            myListingsLiveData.setValue(Resource.success(list));
        });

        return myListingsLiveData;
    }

    /**
     * Toggles the sold status of a listing in real-time without deleting the document.
     */
    public LiveData<Resource<Boolean>> updateListingSoldStatus(String listingId, boolean isSold) {
        MutableLiveData<Resource<Boolean>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading());

        Map<String, Object> updates = new HashMap<>();
        if (isSold) {
            updates.put("status", ListingModel.STATUS_SOLD);
            updates.put("isAvailable", false);
            updates.put("sold", true);
            updates.put("isSold", true);
            updates.put("soldAt", FieldValue.serverTimestamp());
        } else {
            updates.put("status", ListingModel.STATUS_AVAILABLE);
            updates.put("isAvailable", true);
            updates.put("sold", false);
            updates.put("isSold", false);
            updates.put("soldAt", null);
        }

        firestore.collection(Constants.COLLECTION_LISTINGS).document(listingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Listing " + listingId + " status updated to " + (isSold ? "SOLD" : "AVAILABLE"));
                    resultLiveData.setValue(Resource.success(isSold));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update status for listing " + listingId + ": " + e.getMessage(), e);
                    resultLiveData.setValue(Resource.error("Failed to update status: " + e.getMessage()));
                });

        return resultLiveData;
    }

    /**
     * Deletes an image file from Firebase Storage if it exists and points to Firebase Storage.
     */
    public void deleteStorageImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("https://firebasestorage.googleapis.com")) {
            try {
                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                photoRef.delete()
                        .addOnSuccessListener(aVoid -> Log.d("StorageDelete", "Listing image deleted from Storage successfully: " + imageUrl))
                        .addOnFailureListener(e -> Log.e("StorageDelete", "Failed to delete image from Storage: " + e.getMessage()));
            } catch (Exception e) {
                Log.e("StorageDelete", "Invalid storage reference URL: " + imageUrl, e);
            }
        }
    }

    /**
     * Deletes a listing from Firestore and purges its image from Firebase Storage.
     */
    public LiveData<Resource<Boolean>> deleteListing(ListingModel listing) {
        if (listing == null) {
            MutableLiveData<Resource<Boolean>> error = new MutableLiveData<>();
            error.setValue(Resource.error("Invalid listing"));
            return error;
        }
        return deleteListing(listing.getId(), listing.resolveImageUrl());
    }

    public LiveData<Resource<Boolean>> deleteListing(String listingId) {
        return deleteListing(listingId, null);
    }

    public LiveData<Resource<Boolean>> deleteListing(String listingId, String imageUrl) {
        MutableLiveData<Resource<Boolean>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading());

        if (listingId == null || listingId.isEmpty()) {
            resultLiveData.setValue(Resource.error("Invalid listing ID"));
            return resultLiveData;
        }

        // Step A: Delete Image from Firebase Storage if URL exists
        deleteStorageImage(imageUrl);

        // Step B: Delete Document from Firestore
        firestore.collection(Constants.COLLECTION_LISTINGS).document(listingId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Listing " + listingId + " and associated photo completely removed from Firestore & Storage");
                    resultLiveData.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete listing " + listingId + ": " + e.getMessage(), e);
                    resultLiveData.setValue(Resource.error("Failed to delete listing: " + e.getMessage()));
                });

        return resultLiveData;
    }

    /**
     * Admin action to permanently ban/remove a listing from the marketplace and purge its photo.
     */
    public LiveData<Resource<Boolean>> adminRemoveListing(ListingModel listing) {
        if (listing == null) {
            MutableLiveData<Resource<Boolean>> error = new MutableLiveData<>();
            error.setValue(Resource.error("Invalid listing"));
            return error;
        }
        return adminRemoveListing(listing.getId(), listing.resolveImageUrl());
    }

    public LiveData<Resource<Boolean>> adminRemoveListing(String listingId) {
        return adminRemoveListing(listingId, null);
    }

    public LiveData<Resource<Boolean>> adminRemoveListing(String listingId, String imageUrl) {
        return deleteListing(listingId, imageUrl);
    }

    public void cleanup() {
        if (activeListingsListener != null) {
            activeListingsListener.remove();
            activeListingsListener = null;
        }
        if (myListingsListener != null) {
            myListingsListener.remove();
            myListingsListener = null;
        }
    }
}
