package gal.boris.compluacoge.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.databinding.FragmentInitialBinding;
import gal.boris.compluacoge.extras.MyTextWatcherAdapter;
import gal.boris.compluacoge.logic.MyViewModel;

import static gal.boris.compluacoge.ui.CreateProfileFragment.checkEmail;
import static gal.boris.compluacoge.ui.CreateProfileFragment.checkPasswd;

public class LoginFragment extends Fragment {

    public LoginFragment() {}

    private MyViewModel viewModel;
    private FragmentInitialBinding binding;

    public static String LOGIN_SUCCESSFUL = "LOGIN_SUCCESSFUL";
    private SavedStateHandle savedStateHandle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        binding = FragmentInitialBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        savedStateHandle = Navigation.findNavController(view)
                .getPreviousBackStackEntry()
                .getSavedStateHandle();
        savedStateHandle.set(LOGIN_SUCCESSFUL, false); //Proteccion por si da hacia atras => a LoadingFragment

        viewModel.isSignedIn().observe(getViewLifecycleOwner(), b -> {
            if(b && isHere()) {
                savedStateHandle.set(LOGIN_SUCCESSFUL, true);
                NavHostFragment.findNavController(this).popBackStack();
            }
        });

        viewModel.getFlags().getDarkModeOn().observe(getViewLifecycleOwner(), dark -> {
            binding.initialImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), dark ? R.mipmap.dark_ic_launcher : R.mipmap.ic_launcher, null));
        });

        binding.initialWithoutAccount.setOnClickListener(v -> {
            if(isHere()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment);
            }
        });

        binding.initialForgot.setOnClickListener(v -> {
            if (isHere()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_forgotFragment);
            }
        });

        binding.initialLogIn.setOnClickListener(v -> {
            String email = binding.initialEmailEdit.getText().toString();
            String passwd = binding.initialPasswdEdit.getText().toString();
            if(!checkEmail(email)) {
                binding.initialEmail.setError(getString(R.string.invalid_format));
            } else if(!checkPasswd(passwd)) {
                binding.initialPasswd.setError(getString(R.string.invalid_passwd));
            }
            else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, passwd)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                viewModel.getAccountManager().signIn(user);
                            } else {
                                Toast.makeText(requireContext(),getString(R.string.incorrect_passwd_account), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        binding.initialLogIn.setEnabled(false);
        MyTextWatcherAdapter logInChecker = new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                String email = binding.initialEmailEdit.getText().toString();
                String passwd = binding.initialPasswdEdit.getText().toString();
                binding.initialLogIn.setEnabled(email.length()>0 && passwd.length()>0);
            }
        };
        binding.initialEmailEdit.addTextChangedListener(logInChecker);
        binding.initialPasswdEdit.addTextChangedListener(logInChecker);

        binding.initialEmailEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                binding.initialEmail.setError(null);
            }
        });

        binding.initialPasswdEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                binding.initialPasswd.setError(null);
            }
        });
    }

    public boolean isHere() {
        NavDestination destNow = NavHostFragment.findNavController(this).getCurrentDestination();
        return destNow!=null && destNow.getId()==R.id.loginFragment;
    }

}