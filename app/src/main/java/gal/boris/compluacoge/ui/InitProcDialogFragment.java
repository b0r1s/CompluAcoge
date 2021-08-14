package gal.boris.compluacoge.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.objects.AProcedure;
import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.PrivateUser;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.data.objects.PublicUser;
import gal.boris.compluacoge.databinding.FragmentInitProcBinding;
import gal.boris.compluacoge.logic.MyViewModel;

public class InitProcDialogFragment extends DialogFragment {

    private FragmentInitProcBinding binding;
    private MyViewModel viewModel;

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        binding = FragmentInitProcBinding.inflate(LayoutInflater.from(requireContext()));
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setView(binding.getRoot())
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String idInst = InitProcDialogFragmentArgs.fromBundle(requireArguments()).getIdInst();
        String idProc = InitProcDialogFragmentArgs.fromBundle(requireArguments()).getIdProcedure();
        String versionProc = InitProcDialogFragmentArgs.fromBundle(requireArguments()).getVersionProcedure();
        if("".equals(idInst) || "".equals(idProc) || "".equals(versionProc)) {
            NavHostFragment.findNavController(this).popBackStack();
            return null;
        }

        boolean isKindOfUser = viewModel.getAccountManager().getInfoLogin().getTypeUser().isAKindOfUser();
        boolean instOwner = idInst.equals(FirebaseAuth.getInstance().getUid());

        if(isKindOfUser) {
            Pair<PrivateUser, PublicUser> pair = viewModel.getRepository().getCloudDB().getDataUser().getValue();
            AProcedure aproc = pair.first.getAProcedure(idInst, idProc, versionProc);
            if(aproc != null ) {
                Procedure proc = aproc.getProcedure();
                String name = aproc.getPublicInstitution().getName();
                paintProc(proc,name,getString(R.string.continue_));
            } else {
                download(idInst,idProc,versionProc);
            }
        } else {
            if(instOwner) {
                Pair<PrivateInstitution, PublicInstitution> pair = viewModel.getRepository().getCloudDB().getDataInstitution().getValue();
                Procedure proc = pair.second.getProcVisible(idProc,versionProc);
                String name = pair.second.getName();
                paintProc(proc,name,getString(R.string.see));
            } else {
                download(idInst,idProc,versionProc);
            }
        }

        return binding.getRoot();
    }

    private void download(String idInst, String idProc, String versionProc) {
        LiveData<PublicInstitution> result = viewModel.getRepository().getCloudDB().getSearchInstCache().getInstitution(idInst);
        result.observe(getViewLifecycleOwner(), publicInst -> {
            setIsLoadedVisibility(publicInst != null);
            if(publicInst != null) {
                paintProc(publicInst.getProcVisible(idProc,versionProc),publicInst.getName(),getString(R.string.start));
            }
        });
    }

    private void paintProc(Procedure proc, String instName, String buttonText) {
        if(proc == null) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        String title = proc.getName() + (proc.getClosed() ? " " + getString(R.string.closed_lock) : "");
        binding.initTitle.setText(title);
        binding.initSubtitle.setText(instName);
        binding.initText.setText(proc.getLongDescription());
        binding.initStart.setText(buttonText);
        binding.initStart.setOnClickListener(v -> {
            InitProcDialogFragmentDirections.ActionInitProcDialogFragmentToViewProcFragment action =
                    InitProcDialogFragmentDirections.actionInitProcDialogFragmentToViewProcFragment(proc.getIdProcedure(),proc.getVersionProcedure(),proc.getPublicIdInstitution());
            NavHostFragment.findNavController(this).navigate(action);
        });
    }

    private void setIsLoadedVisibility(boolean loaded) {
        List<View> loadedViews = Arrays.asList(binding.initTitle,binding.initSubtitle,binding.initText,binding.initStart);
        for(View view : loadedViews) {
            view.setVisibility(loaded ? View.VISIBLE : View.INVISIBLE);
        }
        binding.initLoading.setVisibility(loaded ? View.GONE : View.VISIBLE);
    }

}
