package gal.boris.compluacoge.ui;


import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.databinding.FragmentAlertBinding;

public class AlertDialogFragment extends DialogFragment {

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        FragmentAlertBinding binding = FragmentAlertBinding.inflate(LayoutInflater.from(requireContext()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setView(binding.getRoot())
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        String title = AlertDialogFragmentArgs.fromBundle(requireArguments()).getTitle();
        String message = AlertDialogFragmentArgs.fromBundle(requireArguments()).getMessage();
        String yes = AlertDialogFragmentArgs.fromBundle(requireArguments()).getYes();
        String no = AlertDialogFragmentArgs.fromBundle(requireArguments()).getNo();

        binding.alertTitle.setText(title);
        binding.alertText.setText(message);
        binding.alertOkay.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
            NavHostFragment.findNavController(this).popBackStack();
        });
        binding.alertOkay.setText(yes);
        binding.alertCancel.setOnClickListener(v -> {
            dismiss();
        });
        binding.alertCancel.setText(no);


        return dialog;
    }
}
