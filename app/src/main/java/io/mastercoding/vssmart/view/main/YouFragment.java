package io.mastercoding.vssmart.view.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.data.model.UserModel;
import io.mastercoding.vssmart.databinding.DialogConfirmDeleteBinding;
import io.mastercoding.vssmart.databinding.DialogEditProfileBinding;
import io.mastercoding.vssmart.databinding.FragmentYouBinding;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.SharedPrefManager;
import io.mastercoding.vssmart.utils.WhatsAppHelper;
import io.mastercoding.vssmart.view.adapter.MyListingsAdapter;
import io.mastercoding.vssmart.view.auth.AuthActivity;
import io.mastercoding.vssmart.view.detail.ListingDetailActivity;
import io.mastercoding.vssmart.view.support.AboutActivity;
import io.mastercoding.vssmart.view.support.HelpFeedbackActivity;
import io.mastercoding.vssmart.viewmodel.AuthViewModel;
import io.mastercoding.vssmart.viewmodel.UserViewModel;
import java.util.List;

/**
 * You / Profile Fragment managing user info, campus location, posted items, and settings.
 */
public class YouFragment extends Fragment implements MyListingsAdapter.OnMyListingActionListener {

    private FragmentYouBinding binding;
    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;
    private MyListingsAdapter myListingsAdapter;
    private SharedPrefManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentYouBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        prefManager = SharedPrefManager.getInstance(requireContext());

        setupProfileView();
        setupMyListingsRecyclerView();
        setupSupportAndAboutCards();
        setupLogoutButton();
        observeUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupProfileView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setupProfileView();
        }
    }

    private void setupProfileView() {
        UserModel user = prefManager.getCachedUser();
        if (user != null) {
            binding.tvUserName.setText(user.getName() + (user.isAdmin() ? " (Admin)" : ""));
            binding.tvUserContact.setText(!user.getPhone().isEmpty() ? user.getPhone() : user.getEmail());
            binding.tvUserCampusDetails.setText(user.getFormattedCampusLocation());
        }

        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void setupMyListingsRecyclerView() {
        myListingsAdapter = new MyListingsAdapter(this);
        binding.rvMyListings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyListings.setAdapter(myListingsAdapter);
        binding.rvMyListings.setHasFixedSize(true);
        binding.rvMyListings.setItemViewCacheSize(20);
    }

    private void setupSupportAndAboutCards() {
        binding.cardHelpFeedback.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), HelpFeedbackActivity.class));
        });

        binding.cardAbout.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AboutActivity.class));
        });
    }

    private void setupLogoutButton() {
        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_logout_title)
                    .setMessage(R.string.confirm_logout_message)
                    .setPositiveButton(R.string.btn_logout, (dialog, which) -> {
                        authViewModel.signOut();
                        Intent intent = new Intent(requireContext(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        });
    }

    private void observeUserData() {
        String uid = prefManager.getUserId();
        if (uid.isEmpty()) return;

        // Observe profile from Firestore
        userViewModel.getUserProfile(uid).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.isSuccess() && resource.data != null) {
                UserModel user = resource.data;
                prefManager.saveUser(user);
                binding.tvUserName.setText(user.getName() + (user.isAdmin() ? " (Admin)" : ""));
                binding.tvUserContact.setText(!user.getPhone().isEmpty() ? user.getPhone() : user.getEmail());
                binding.tvUserCampusDetails.setText(user.getFormattedCampusLocation());
            }
        });

        // Observe user's listings
        userViewModel.getMyListings(uid).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.isSuccess()) {
                List<ListingModel> items = resource.data;
                myListingsAdapter.setItems(items);

                if (items == null || items.isEmpty()) {
                    binding.tvNoMyListings.setVisibility(View.VISIBLE);
                    binding.rvMyListings.setVisibility(View.GONE);
                    binding.tvMyListingsCount.setText("0 Items");
                } else {
                    binding.tvNoMyListings.setVisibility(View.GONE);
                    binding.rvMyListings.setVisibility(View.VISIBLE);
                    binding.tvMyListingsCount.setText(items.size() + " Items");
                }
            }
        });
    }

    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        UserModel user = prefManager.getCachedUser();
        if (user != null) {
            dialogBinding.etEditName.setText(user.getName());
            dialogBinding.etEditPhone.setText(user.getPhone().replaceAll("[^0-9]", ""));
            dialogBinding.etEditHostel.setText(user.getHostel());
            dialogBinding.etEditRoom.setText(user.getRoom());
        }

        dialogBinding.btnCancelEditProfile.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSaveProfile.setOnClickListener(v -> {
            String name = dialogBinding.etEditName.getText() != null ? dialogBinding.etEditName.getText().toString().trim() : "";
            String phone = dialogBinding.etEditPhone.getText() != null ? dialogBinding.etEditPhone.getText().toString().trim() : "";
            String hostel = dialogBinding.etEditHostel.getText() != null ? dialogBinding.etEditHostel.getText().toString().trim() : "";
            String room = dialogBinding.etEditRoom.getText() != null ? dialogBinding.etEditRoom.getText().toString().trim() : "";

            if (!phone.isEmpty() && !WhatsAppHelper.isValidPhoneNumber(phone)) {
                dialogBinding.tilEditPhone.setError(getString(R.string.invalid_phone_error));
                return;
            }

            String uid = prefManager.getUserId();
            userViewModel.updateCampusProfile(uid, name, phone, hostel, room).observe(getViewLifecycleOwner(), resource -> {
                if (resource != null && resource.isSuccess()) {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), R.string.success_profile_updated, Toast.LENGTH_SHORT).show();
                    setupProfileView();
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onListingClick(ListingModel listing, int position) {
        Intent intent = new Intent(requireContext(), ListingDetailActivity.class);
        intent.putExtra(Constants.EXTRA_LISTING, listing);
        startActivity(intent);
    }

    @Override
    public void onToggleSold(ListingModel listing, int position) {
        if (listing.isSold()) {
            return;
        }

        // 1. Optimistic UI update immediately without waiting for network
        myListingsAdapter.updateItemSoldStatus(position, true);

        // 2. Persist update in Firestore (status: "SOLD", isAvailable: false, soldAt: serverTimestamp)
        userViewModel.toggleSoldStatus(listing.getId(), true).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), getString(R.string.success_item_sold), Toast.LENGTH_SHORT).show();
            } else if (resource.isError()) {
                // Revert optimistic update on failure
                myListingsAdapter.updateItemSoldStatus(position, false);
                Snackbar.make(binding.getRoot(), "Failed to mark as sold: " + resource.message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDeleteListing(ListingModel listing, int position) {
        DialogConfirmDeleteBinding deleteBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(deleteBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        deleteBinding.btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        deleteBinding.btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();

            // 1. Optimistically remove item from UI immediately
            myListingsAdapter.removeItem(position);
            updateListingCount();

            // 2. Persist deletion in Firestore and Firebase Storage
            userViewModel.deleteListing(listing).observe(getViewLifecycleOwner(), resource -> {
                if (resource == null) return;

                if (resource.isSuccess()) {
                    Toast.makeText(requireContext(), "Listing and associated photo completely removed", Toast.LENGTH_SHORT).show();
                } else if (resource.isError()) {
                    // Revert optimistic deletion on failure
                    myListingsAdapter.insertItem(position, listing);
                    updateListingCount();
                    Snackbar.make(binding.getRoot(), "Failed to delete listing: " + resource.message, Snackbar.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void updateListingCount() {
        if (binding == null) return;
        int count = myListingsAdapter.getItemCount();
        if (count == 0) {
            binding.tvNoMyListings.setVisibility(View.VISIBLE);
            binding.rvMyListings.setVisibility(View.GONE);
            binding.tvMyListingsCount.setText("0 Items");
        } else {
            binding.tvNoMyListings.setVisibility(View.GONE);
            binding.rvMyListings.setVisibility(View.VISIBLE);
            binding.tvMyListingsCount.setText(count + " Items");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
