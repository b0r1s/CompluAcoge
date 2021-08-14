package gal.boris.compluacoge.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.databinding.FragmentLoadingBinding;
import gal.boris.compluacoge.logic.MyViewModel;

public class LoadingFragment extends Fragment {

    public LoadingFragment() {}

    private MyViewModel viewModel;
    private FragmentLoadingBinding binding;

    private CountDownTimer timerSignOut;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Si le da a atras, esto lo devuelve a LoginFragment
        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
        savedStateHandle.getLiveData(LoginFragment.LOGIN_SUCCESSFUL)
                .observe(navBackStackEntry, success -> {
                    if(!(boolean)success) {
                        requireActivity().finish(); //hemos vuelto sin iniciar sesion
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timerSignOut!=null) {
            timerSignOut.cancel();
            timerSignOut = null;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        binding = FragmentLoadingBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(timerSignOut != null) {
            timerSignOut.cancel();
        }
        timerSignOut = new CountDownTimer(5*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                if(isHere() && binding!=null) {
                    binding.instLatSignOut.setVisibility(View.VISIBLE);
                }
            }
        };
        timerSignOut.start();

        viewModel.isSignedIn().observe(getViewLifecycleOwner(), b -> {
            if(!b && isHere()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_loadingFragment_to_loginFragment);
            }
        });

        viewModel.getRepository().getCloudDB().getConnectedUserDB().observe(getViewLifecycleOwner(), b -> {
            if(b && isHere()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_loadingFragment_to_userFragment);
            }
        });

        viewModel.getRepository().getCloudDB().getConnectedInstitutionDB().observe(getViewLifecycleOwner(), b -> {
            if(b && isHere()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_loadingFragment_to_instFragment);
            }
        });

        viewModel.getRepository().getCloudDB().getPendingDB().observe(getViewLifecycleOwner(), b -> {
            binding.progressBar.setVisibility(b ? View.GONE : View.VISIBLE);
            binding.textPending.setVisibility(b ? View.VISIBLE : View.GONE);
            binding.pendingIcon.setVisibility(b ? View.VISIBLE : View.GONE);
            binding.instLatSignOut.setVisibility(View.VISIBLE);
        });

        binding.instLatSignOut.setOnClickListener(v -> {
            viewModel.getRepository().getCloudDB().getDataUser().removeObservers(getViewLifecycleOwner());
            viewModel.getAccountManager().signOut();
        });
        binding.instLatSignOut.setVisibility(View.GONE);

    }

    public boolean isHere() {
        NavDestination destNow = NavHostFragment.findNavController(this).getCurrentDestination();
        return destNow!=null && destNow.getId()==R.id.loadingFragment;
    }
}