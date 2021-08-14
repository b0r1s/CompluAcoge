package gal.boris.compluacoge.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.databinding.FragmentForgotBinding;
import gal.boris.compluacoge.extras.MyTextWatcherAdapter;
import gal.boris.compluacoge.logic.MyViewModel;

import static gal.boris.compluacoge.ui.CreateProfileFragment.checkEmail;

public class ForgotFragment extends Fragment {

    public ForgotFragment() {}

    private MyViewModel viewModel;
    private FragmentForgotBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        binding = FragmentForgotBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.forgotEmailEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                binding.forgotEmail.setError(null);
            }
        });
        MyTextWatcherAdapter myTextWatcherAdapter = new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                binding.forgotEmail.setError(null);
            }
        };
        binding.forgotEmailEdit.addTextChangedListener(myTextWatcherAdapter);

        binding.forgotRecover.setOnClickListener(v -> {
            String email = binding.forgotEmailEdit.getText().toString();
            if (!checkEmail(email)) {
                binding.forgotEmail.setError(getString(R.string.invalid_format));
            } else {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(),getString(R.string.email_sent),Toast.LENGTH_LONG).show();
                                NavHostFragment.findNavController(this).popBackStack();
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.error_sending_email), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        binding.forgotRecover.setEnabled(false);
        MyTextWatcherAdapter signUpChecker = new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                binding.forgotRecover.setEnabled(checkEmail(binding.forgotEmailEdit.getText().toString()));
            }
        };
        binding.forgotEmailEdit.addTextChangedListener(signUpChecker);

    }

}