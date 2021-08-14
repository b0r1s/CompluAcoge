package gal.boris.compluacoge.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.databinding.FragmentInstBinding;
import gal.boris.compluacoge.extras.GridSpaceCalculator;
import gal.boris.compluacoge.logic.CloudDB;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.ui.adapters.InstDraftsAdapter;
import gal.boris.compluacoge.ui.adapters.InstProcsAdapter;

import static gal.boris.compluacoge.extras.GridSpaceCalculator.dpToPx;

public class InstFragment extends Fragment {

    public InstFragment() {}

    private MyViewModel viewModel;
    private FragmentInstBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        binding = FragmentInstBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.instMotion.setMinimumHeight((int)dpToPx(requireContext(),56) + windowInsets.top);

            for(int id : binding.instMotion.getConstraintSetIds()) {
                ConstraintSet constraintSet = binding.instMotion.getConstraintSet(id);
                ConstraintSet.Layout layout = constraintSet.getConstraint(binding.instSettings.getId()).layout;
                int newMargin = (int)dpToPx(requireContext(),0) + windowInsets.top;
                if(layout.topMargin != newMargin) {
                    constraintSet.setMargin(binding.instSettings.getId(),ConstraintSet.TOP,newMargin);
                    constraintSet.setMargin(binding.instImage.getId(),ConstraintSet.TOP,newMargin);
                    binding.instMotion.updateState(id,constraintSet);
                }
            }

            return insets;
        });

        binding.instSettings.setOnClickListener(v -> {
            binding.instDrawerLayout.openDrawer(GravityCompat.END);
        });

        binding.instLatEdit.setOnClickListener(v -> {
            binding.instDrawerLayout.closeDrawer(GravityCompat.END);
            NavHostFragment.findNavController(this).navigate(R.id.action_instFragment_to_editProfileFragment);
        });

        binding.instLatAbout.setOnClickListener(v -> {
            if(isHere()) {
                binding.instDrawerLayout.closeDrawer(GravityCompat.END);
                InstFragmentDirections.ActionInstFragmentToInfoDialogFragment action =
                        InstFragmentDirections.actionInstFragmentToInfoDialogFragment(
                                getString(R.string.about),getString(R.string.about_desc));
                NavHostFragment.findNavController(this).navigate(action);
            }
        });

        binding.instLatSignOut.setOnClickListener(v -> {
            viewModel.getRepository().getCloudDB().getDataUser().removeObservers(getViewLifecycleOwner());
            viewModel.getAccountManager().signOut();
        });

        viewModel.getRepository().getCloudDB().getConnectedInstitutionDB().observe(getViewLifecycleOwner(), b -> {
            if(!b && isHere()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_instFragment_to_loadingFragment);
            }
        });

        binding.instFab.setOnClickListener(v -> {
            InstFragmentDirections.ActionInstFragmentToCreateProcFragment action =
                    InstFragmentDirections.actionInstFragmentToCreateProcFragment("","","");
            NavHostFragment.findNavController(this).navigate(action);
        });

        int numColumns = GridSpaceCalculator.getNumColumns(requireActivity(),125);

        binding.instProcOpenList.setLayoutManager(new GridLayoutManager(requireContext(),numColumns,GridLayoutManager.VERTICAL,false));
        InstProcsAdapter adapterProcsOpen = new InstProcsAdapter(new ArrayList<>(),getResources(),NavHostFragment.findNavController(this));
        binding.instProcOpenList.setAdapter(adapterProcsOpen);

        binding.instProcClosedList.setLayoutManager(new GridLayoutManager(requireContext(),numColumns,GridLayoutManager.VERTICAL,false));
        InstProcsAdapter adapterProcsClosed = new InstProcsAdapter(new ArrayList<>(),getResources(),NavHostFragment.findNavController(this));
        binding.instProcClosedList.setAdapter(adapterProcsClosed);

        binding.instDraftList.setLayoutManager(new GridLayoutManager(requireContext(),numColumns,GridLayoutManager.VERTICAL,false));
        InstDraftsAdapter adapterDrafts = new InstDraftsAdapter(new ArrayList<>(),getResources(),NavHostFragment.findNavController(this));
        binding.instDraftList.setAdapter(adapterDrafts);

        viewModel.getRepository().getCloudDB().getDataInstitution().observe(getViewLifecycleOwner(), data -> {
            PrivateInstitution privateInstitution = data.first;
            PublicInstitution publicInstitution = data.second;

            StorageReference profileRef = FirebaseStorage.getInstance().getReference().child("images/"+privateInstitution.getID()+"/"+ CloudDB.PROFILE_FILE_NAME);
            connectProfile(profileRef);
            List<UploadTask> uploadingProfile = profileRef.getActiveUploadTasks();
            if(uploadingProfile.size() > 0) {
                uploadingProfile.get(0).addOnSuccessListener(task -> connectProfile(profileRef));
            }

            StorageReference backgroundRef = FirebaseStorage.getInstance().getReference().child("images/"+privateInstitution.getID()+"/"+ CloudDB.BACKGROUND_FILE_NAME);
            connectBackground(backgroundRef);
            List<UploadTask> uploadingBackground = backgroundRef.getActiveUploadTasks();
            if(uploadingBackground.size() > 0) {
                uploadingBackground.get(0).addOnSuccessListener(task -> connectBackground(backgroundRef));
            }

            binding.instTitle.setText(publicInstitution.getName());
            binding.instLatTitle.setText(publicInstitution.getName());
            binding.instEmail.setText(privateInstitution.getEmail());
            binding.instLatEmail.setText(privateInstitution.getEmail());
            binding.instDescription.setText(publicInstitution.getDescription());

            Set<Procedure> procsList = publicInstitution.getCopyMainProcVisibles();
            Set<Procedure> openList = procsList.stream().filter(p -> !p.getClosed()).collect(Collectors.toSet());
            Set<Procedure> closedList = procsList.stream().filter(Procedure::getClosed).collect(Collectors.toSet());
            Set<Procedure> draftsList = privateInstitution.getCopyAllProcHidden();
            binding.instDraftTitle.setText(String.format(getString(R.string.drafts_num),draftsList.size()));
            binding.instProcsOpenSubtitle.setText(String.format(getString(R.string.open_num),openList.size()));
            binding.instProcsClosedSubtitle.setText(String.format(getString(R.string.closed_num),closedList.size()));

            binding.instEmptyOpenProcs.setVisibility(openList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.instProcOpenList.setVisibility(openList.isEmpty() ? View.INVISIBLE : View.VISIBLE);
            binding.instEmptyClosedProcs.setVisibility(closedList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.instProcClosedList.setVisibility(closedList.isEmpty() ? View.INVISIBLE : View.VISIBLE);
            binding.instEmptyDrafts.setVisibility(draftsList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.instDraftList.setVisibility(draftsList.isEmpty() ? View.INVISIBLE : View.VISIBLE);

            adapterProcsOpen.changeList(openList);
            adapterProcsClosed.changeList(closedList);
            adapterDrafts.changeList(draftsList);
        });

        createObserverToGetSelectionFromDialog();
    }

    private void connectProfile(StorageReference profileRef) {
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.instImage);
            Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.instLatImage);
            binding.instImage.setOnClickListener(v -> {
                InstFragmentDirections.ActionInstFragmentToFullImageFragment action =
                        InstFragmentDirections.actionInstFragmentToFullImageFragment(new Uri[]{uri},0);
                NavHostFragment.findNavController(this).navigate(action);
            });
        }).addOnFailureListener(e -> {
            binding.instImage.setImageResource(R.drawable.icon_person);
            binding.instLatImage.setImageResource(R.drawable.icon_person);
            binding.instImage.setOnClickListener(null);
        });
    }

    private void connectBackground(StorageReference backgroundRef) {
        backgroundRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.instBackground);
        }).addOnFailureListener(e -> {
            binding.instBackground.setImageDrawable(null);
        });
    }

    private void createObserverToGetSelectionFromDialog() {
        final NavBackStackEntry navBackStackEntry = NavHostFragment.findNavController(this).getBackStackEntry(R.id.instFragment);
        final LifecycleEventObserver observer = (source, event) -> {
            if(event.equals(Lifecycle.Event.ON_RESUME) &&
                    navBackStackEntry.getSavedStateHandle().contains(SelectVersionDialogFragment.ID_PROC) &&
                    navBackStackEntry.getSavedStateHandle().contains(SelectVersionDialogFragment.VERSION_PROC)) {
                String idProc = navBackStackEntry.getSavedStateHandle().get(SelectVersionDialogFragment.ID_PROC);
                navBackStackEntry.getSavedStateHandle().remove(SelectVersionDialogFragment.ID_PROC);
                String versionProc = navBackStackEntry.getSavedStateHandle().get(SelectVersionDialogFragment.VERSION_PROC);
                navBackStackEntry.getSavedStateHandle().remove(SelectVersionDialogFragment.VERSION_PROC);
                if(idProc==null || versionProc==null || "".equals(idProc) || "".equals(versionProc)) {
                    FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException());
                    return;
                }
                InstFragmentDirections.ActionInstFragmentToInitProcDialogFragment action = InstFragmentDirections.actionInstFragmentToInitProcDialogFragment(idProc,versionProc,"");
                NavHostFragment.findNavController(this).navigate(action);
            }
        };
        navBackStackEntry.getLifecycle().addObserver(observer);
        getViewLifecycleOwner().getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                navBackStackEntry.getLifecycle().removeObserver(observer);
            }
        });
    }

    public boolean isHere() {
        NavDestination destNow = NavHostFragment.findNavController(this).getCurrentDestination();
        return destNow!=null && destNow.getId()==R.id.instFragment;
    }
}