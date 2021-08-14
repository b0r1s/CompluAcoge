package gal.boris.compluacoge.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.databinding.FragmentAlertBinding;

public class InfoDialogFragment extends DialogFragment {

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        FragmentAlertBinding binding = FragmentAlertBinding.inflate(LayoutInflater.from(requireContext()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setView(binding.getRoot())
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        String title = InfoDialogFragmentArgs.fromBundle(requireArguments()).getTitle();
        String message = InfoDialogFragmentArgs.fromBundle(requireArguments()).getMessage();

        binding.alertTitle.setText(title);
        binding.alertText.setText(message);
        binding.alertOkay.setVisibility(View.VISIBLE);
        binding.alertOkay.setText(getString(R.string.okay));
        binding.alertOkay.setOnClickListener(v -> {
            dismiss();
        });
        binding.alertCancel.setVisibility(View.GONE);

        return dialog;
    }
}
