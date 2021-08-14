package gal.boris.compluacoge.ui;


import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.databinding.FragmentDeleteAccountBinding;
import gal.boris.compluacoge.extras.MyTextWatcherAdapter;
import gal.boris.compluacoge.logic.MyViewModel;

public class DeleteAccountDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        MyViewModel viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        FragmentDeleteAccountBinding binding = FragmentDeleteAccountBinding.inflate(LayoutInflater.from(requireContext()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setView(binding.getRoot())
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        binding.alertPasswdEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                binding.alertPasswd.setError(null);
            }
        });
        MyTextWatcherAdapter myTextWatcherAdapter = new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                binding.alertPasswd.setError(null);
            }
        };
        binding.alertPasswdEdit.addTextChangedListener(myTextWatcherAdapter);

        binding.alertOkay.setOnClickListener(v -> {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(email, binding.alertPasswdEdit.getText().toString());
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    user.delete().addOnCompleteListener(task2 -> {
                        if(task2.isSuccessful()) {
                            viewModel.getAccountManager().signOut();
                            NavHostFragment.findNavController(this).navigate(R.id.action_deleteAccountDialogFragment_to_loadingFragment);
                        } else {
                            Toast.makeText(requireContext(),getString(R.string.error_deleting_account),Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    binding.alertPasswd.setError(getString(R.string.incorrect_passwd));
                }
            });
        });


        return dialog;
    }
}
