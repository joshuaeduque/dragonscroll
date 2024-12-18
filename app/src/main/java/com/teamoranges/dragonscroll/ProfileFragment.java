package com.teamoranges.dragonscroll;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

/**
 * ProfileFragment is a java class that represents the view the user sees when they click the profile item in the
 * bottom navigation bar. It displays an editable profile picture, name, and the number of books
 * a user has read.
 * @author Joshua Duque
 * @author Mateo Garcia
 * @author Emiliano Garza
 * @author Samatha Poole
 * @author Alaine Liserio
 * UTSA CS 3443 - Team Oranges Project
 * Fall 2024
 */
public class ProfileFragment extends Fragment {

    private int booksRead;
    private String profileName;
    private String profileImageUri;
    private String favoriteBook;

    private ImageView profileImageView;

    private SharedPreferences sharedPrefs;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    /**
     * Constructor for the ProfileFragment
     */
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Method that starts a new instance of the ProfileFragment
     * @return ProfileFragment with populated data
     */
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Method that runs when the Profile Fragment is first created.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get book DAO
        BookDao bookDao = ((MainActivity) requireActivity()).getBookDao();

        // Get books read
        booksRead = bookDao.getCount();

        // Get SharedPreferences
        sharedPrefs = requireContext().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );

        // Get profile name
        profileName = sharedPrefs.getString(
                getString(R.string.profile_name_key),
                getString(R.string.profile_name_default)
        );

        // Get profile image
        profileImageUri = sharedPrefs.getString(
                getString(R.string.profile_uri_key),
                null);

        // Get favorite book
        favoriteBook = sharedPrefs.getString(
                getString(R.string.favorite_book_key),
                null);

        // Registers a photo picker activity launcher in single-select mode.
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);

                        try {
                            // Do some nonsense with permissions
                            Context context = requireContext();
                            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception exception) {
                            Log.d("PhotoPicker", "Permissions failed ig");
                            return;
                        }

                        // Update book cover
                        // updateBookCover(coverImageView, uri);
                        updateProfilePicture(uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
    }

    /**
     * Method that runs when a new view is created.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return View that is created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get profile name TextView
        TextView profileNameTextView = view.findViewById(R.id.nameTextView);

        // Setup profile name
        profileNameTextView.setText(this.profileName);
        profileNameTextView.setOnClickListener(this::onNameTextViewClicked);

        // Set books read TextView
        TextView booksReadTextView = view.findViewById(R.id.booksReadTextView);
        booksReadTextView.setText(String.format(Locale.getDefault(), "Books Read: %d", booksRead));

        // Setup profile picture
        profileImageView = view.findViewById(R.id.profileImageView);
        if (profileImageUri != null) {
            Uri uri = Uri.parse(profileImageUri);
            profileImageView.setImageURI(uri);
        }
        profileImageView.setOnClickListener(this::onProfileImageViewClicked);

        // Setup favorite book
        TextView favoriteBookTextView = view.findViewById(R.id.favoriteBookTextView);
        if (favoriteBook != null) {
            String favoriteBookText = String.format(Locale.getDefault(), "Favorite Book: %s", favoriteBook);
            favoriteBookTextView.setText(favoriteBookText);
        }

        return view;
    }

    /**
     * Method that runs when the Profile ImageView is clicked.
     * @param view Current view (view)
     */
    private void onProfileImageViewClicked(View view) {
        // Launch the photo picker and let the user choose only images.
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    /**
     * Method that runs the the Name TextView is clicked.
     * @param view Current view (View)
     */
    private void onNameTextViewClicked(View view) {
        Context context = requireContext();

        // Create EditText for user input
        EditText editText = new EditText(context);
        editText.setHint("Profile name");

        // Create AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setMessage("Enter a new profile name")
                .setView(editText);

        // Set AlertDialog positive button on click listener
        alert.setPositiveButton("Save", (dialog, button) -> {
            // Get EditText text
            String editTextValue = String.valueOf(editText.getText());

            // Validate EditText text.
            // If it's empty, just return without doing anything.
            if (editTextValue.trim().isEmpty())
                return;

            // Set text of TextView with user profile name
            ((TextView) view).setText(editTextValue);

            // Save new profile name to SharedPreferences
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(
                    getString(R.string.profile_name_key),
                    editTextValue);
            editor.apply();
        });

        // Give AlertDialog negative button and empty on click listener
        alert.setNegativeButton("Cancel", (dialog, button) -> {
            // The negative button needs an empty on click listener
            // otherwise it won't show at all.
            // There's probably a better way to do this lmao.
        });

        // Show the AlertDialog
        alert.show();
    }

    /**
     * Method that updates the user's profile picture URI
     * @param uri profile picture URI (Uri)
     */
    private void updateProfilePicture(Uri uri) {
        // Get uri string
        String uriString = uri.toString();

        // Set profile image uri
        profileImageView.setImageURI(uri);

        // Update uri in SharedPreferences
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(
                getString(R.string.profile_uri_key),
                uriString);
        editor.apply();
    }
}