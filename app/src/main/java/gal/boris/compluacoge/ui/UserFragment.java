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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.objects.AProcedure;
import gal.boris.compluacoge.data.objects.PrivateUser;
import gal.boris.compluacoge.data.objects.PublicUser;
import gal.boris.compluacoge.databinding.FragmentUserBinding;
import gal.boris.compluacoge.extras.GridSpaceCalculator;
import gal.boris.compluacoge.logic.CloudDB;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.ui.adapters.UserProcsAdapter;

import static gal.boris.compluacoge.extras.GridSpaceCalculator.dpToPx;

public class UserFragment extends Fragment {

    public UserFragment() {}

    private MyViewModel viewModel;
    private FragmentUserBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        binding = FragmentUserBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.userMotion.setMinimumHeight((int)dpToPx(requireContext(),56) + windowInsets.top);

            for(int id : binding.userMotion.getConstraintSetIds()) {
                ConstraintSet constraintSet = binding.userMotion.getConstraintSet(id);
                ConstraintSet.Layout layout = constraintSet.getConstraint(binding.userSettings.getId()).layout;
                int newMargin = (int)dpToPx(requireContext(),0) + windowInsets.top;
                if(layout.topMargin != newMargin) {
                    constraintSet.setMargin(binding.userSettings.getId(),ConstraintSet.TOP,newMargin);
                    binding.userMotion.updateState(id,constraintSet);
                }
            }

            return insets;
        });

        int numColumns = GridSpaceCalculator.getNumColumns(requireActivity(),125);

        binding.userFab.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_userFragment_to_searchFragment);
        });

        binding.userOpenList.setLayoutManager(new GridLayoutManager(requireContext(),numColumns,GridLayoutManager.VERTICAL,false));
        UserProcsAdapter adapterOpen = new UserProcsAdapter(new ArrayList<>(),getResources());
        binding.userOpenList.setAdapter(adapterOpen);

        binding.userClosedList.setLayoutManager(new GridLayoutManager(requireContext(),numColumns,GridLayoutManager.VERTICAL,false));
        UserProcsAdapter adapterClosed = new UserProcsAdapter(new ArrayList<>(),getResources());
        binding.userClosedList.setAdapter(adapterClosed);

        viewModel.getRepository().getCloudDB().getDataUser().observe(getViewLifecycleOwner(), data -> {
            PrivateUser privateUser = data.first;
            PublicUser publicUser = data.second;

            StorageReference profileRef = FirebaseStorage.getInstance().getReference("images/"+privateUser.getID()+"/"+ CloudDB.PROFILE_FILE_NAME);
            connectProfile(profileRef);
            List<UploadTask> uploadingProfile = profileRef.getActiveUploadTasks();
            if(uploadingProfile.size() > 0) {
                uploadingProfile.get(0).addOnSuccessListener(task -> connectProfile(profileRef));
            }

            binding.userTitle.setText(publicUser.getName());
            binding.userSw.setVisibility(publicUser.isSocialWorker() ? View.VISIBLE : View.GONE);
            binding.userLatTitle.setText(publicUser.getName());
            binding.userEmail.setText(privateUser.getEmail());
            binding.userLatEmail.setText(privateUser.getEmail());
            binding.userDescription.setText(publicUser.getPublicDescription());
            String keywords = privateUser.getPrivateKeywords().stream().reduce("", (a,b) -> a+" • "+b);
            keywords = !"".equals(keywords) ? keywords.substring(3) : "";
            binding.userKeywords.setText(keywords);

            List<AProcedure> openAProcs = privateUser.getAProceduresList().stream().filter(p -> p.getStatus().isOpen()).collect(Collectors.toList());
            List<AProcedure> closedAProcs = privateUser.getAProceduresList().stream().filter(p -> !p.getStatus().isOpen()).collect(Collectors.toList());
            binding.userOpenProcs.setText(String.format(getString(R.string.open_procs_num),openAProcs.size()));
            binding.userProcsOpenSubtitle.setText(String.format(getString(R.string.open_num),openAProcs.size()));
            binding.userClosedProcs.setText(String.format(getString(R.string.closed_procs_num),closedAProcs.size()));
            binding.userProcsClosedSubtitle.setText(String.format(getString(R.string.closed_num),closedAProcs.size()));

            binding.userEmptyOpen.setVisibility(openAProcs.isEmpty() ? View.VISIBLE : View.GONE);
            binding.userOpenList.setVisibility(openAProcs.isEmpty() ? View.INVISIBLE : View.VISIBLE);
            binding.userEmptyClosed.setVisibility(closedAProcs.isEmpty() ? View.VISIBLE : View.GONE);
            binding.userClosedList.setVisibility(closedAProcs.isEmpty() ? View.INVISIBLE : View.VISIBLE);

            adapterOpen.changeList(openAProcs);
            adapterClosed.changeList(closedAProcs);
        });

        binding.userSettings.setOnClickListener(v -> binding.userDrawerLayout.openDrawer(GravityCompat.END));

        binding.userLatEdit.setOnClickListener(v -> {
            binding.userDrawerLayout.closeDrawer(GravityCompat.END);
            NavHostFragment.findNavController(this).navigate(R.id.action_userFragment_to_editProfileFragment);
        });

        binding.userLatAbout.setOnClickListener(v -> {
            if(isHere()) {
                binding.userDrawerLayout.closeDrawer(GravityCompat.END);
                UserFragmentDirections.ActionUserFragmentToInfoDialogFragment action =
                        UserFragmentDirections.actionUserFragmentToInfoDialogFragment(
                                getString(R.string.about),getString(R.string.about_desc));
                NavHostFragment.findNavController(this).navigate(action);
            }
        });

        binding.userLatSignOut.setOnClickListener(v -> {
            viewModel.getRepository().getCloudDB().getDataUser().removeObservers(getViewLifecycleOwner());
            viewModel.getAccountManager().signOut();
        });

        viewModel.getRepository().getCloudDB().getConnectedUserDB().observe(getViewLifecycleOwner(), b -> {
            if(!b && isHere()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_userFragment_to_loadingFragment);
            }
        });
    }

    private void connectProfile(StorageReference profileRef) {
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> { //todo revisar context != null
            Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.userImage);
            Glide.with(requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.userLatImage);
            binding.userImage.setOnClickListener(v -> {
                UserFragmentDirections.ActionUserFragmentToFullImageFragment action =
                        UserFragmentDirections.actionUserFragmentToFullImageFragment(new Uri[]{uri},0);
                NavHostFragment.findNavController(this).navigate(action);
            });
        }).addOnFailureListener(e -> {
            binding.userImage.setImageResource(R.drawable.icon_person);
            binding.userLatImage.setImageResource(R.drawable.icon_person);
            binding.userImage.setOnClickListener(null);
        });
    }

    public boolean isHere() {
        NavDestination destNow = NavHostFragment.findNavController(this).getCurrentDestination();
        return destNow!=null && destNow.getId()==R.id.userFragment;
    }
}