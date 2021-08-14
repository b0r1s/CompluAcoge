package gal.boris.compluacoge.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.UploadPictures;
import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.PrivateUser;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.data.objects.PublicUser;
import gal.boris.compluacoge.data.pojos.DataPrivateUser;
import gal.boris.compluacoge.data.pojos.DataPublicInstitution;
import gal.boris.compluacoge.data.pojos.DataPublicUser;
import gal.boris.compluacoge.databinding.FragmentEditProfileBinding;
import gal.boris.compluacoge.extras.Box;
import gal.boris.compluacoge.extras.GetPicture;
import gal.boris.compluacoge.extras.MyTextWatcherAdapter;
import gal.boris.compluacoge.logic.CloudDB;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.logic.TypeUser;

import static gal.boris.compluacoge.ui.CreateProfileFragment.checkName;

public class EditProfileFragment extends Fragment {

    public EditProfileFragment() {}

    private MyViewModel viewModel;
    private FragmentEditProfileBinding binding;
    private TypeUser typeUser;

    private final GetPicture profile = new GetPicture(this);
    private final GetPicture background = new GetPicture(this);

    private String nameOriginal;
    private String numberSWOriginal;
    private String keywordsOriginal;
    private String descriptionOriginal;
    private Box<Uri> profileOriginal;
    private Box<Uri> backgroundOriginal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overrideBackPressed();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater,container,false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profile.init(R.drawable.icon_person,binding.registerImageTitle, binding.registerImage, binding.registerImageButtonEdit, binding.registerImageButtonDelete);
        background.init(null,binding.registerBackImageTitle, binding.registerBackImage, binding.registerBackImageButtonEdit, binding.registerBackImageButtonDelete);

        typeUser = viewModel.getAccountManager().getInfoLogin().getTypeUser();

        List<TextInputEditText> allEditTexts = new ArrayList<>(Arrays.asList(binding.registerNameEdit, binding.registerDescriptionEdit,
                binding.registerKeywordsEdit,binding.registerNumberSwEdit));
        List<TextInputLayout> allTexts = new ArrayList<>(Arrays.asList(binding.registerName, binding.registerDescription,
                binding.registerKeywords,binding.registerNumberSw));

        for(int i=0; i<allTexts.size(); i++) {
            int finalI = i;
            allEditTexts.get(i).setOnFocusChangeListener((v, hasFocus) -> {
                if(hasFocus) {
                    allTexts.get(finalI).setError(null);
                }
            });
            MyTextWatcherAdapter myTextWatcherAdapter = new MyTextWatcherAdapter() {
                @Override
                public void afterTextChanged(@NonNull Editable s) {
                    allTexts.get(finalI).setError(null);
                }
            };
            allEditTexts.get(i).addTextChangedListener(myTextWatcherAdapter);
        }

        binding.registerKeywords.setVisibility(View.GONE);
        binding.registerTextKeywords.setVisibility(View.GONE);
        binding.registerNumberSw.setVisibility(View.GONE);
        binding.registerTextSw.setVisibility(View.GONE);
        background.hide();

        if(typeUser.isAKindOfUser()) {
            if(typeUser == TypeUser.SOCIAL_WORKER) {
                binding.registerNumberSw.setVisibility(View.VISIBLE);
                binding.registerTextSw.setVisibility(View.VISIBLE);
            }
            binding.registerKeywords.setVisibility(View.VISIBLE);
            binding.registerTextKeywords.setVisibility(View.VISIBLE);
        } else {
            background.show();
        }
        generateWatcherSignUp();

        if(typeUser.isAKindOfUser()) {
            Pair<PrivateUser, PublicUser> user = viewModel.getRepository().getCloudDB().getDataUser().getValue();
            if(typeUser == TypeUser.SOCIAL_WORKER) {
                numberSWOriginal = user.first.getNumberSocialWorker();
                binding.registerNumberSwEdit.setText(numberSWOriginal);
            }
            keywordsOriginal = user.first.getPrivateKeywords().stream().reduce("", (a,b) -> a+" "+b);
            keywordsOriginal = !"".equals(keywordsOriginal) ? keywordsOriginal.substring(1) : "";
            binding.registerKeywordsEdit.setText(keywordsOriginal);

            nameOriginal = user.second.getName();
            binding.registerNameEdit.setText(nameOriginal);
            descriptionOriginal = user.second.getPublicDescription();
            binding.registerDescriptionEdit.setText(descriptionOriginal);
        } else {
            Pair<PrivateInstitution, PublicInstitution> inst = viewModel.getRepository().getCloudDB().getDataInstitution().getValue();
            nameOriginal = inst.second.getName();
            binding.registerNameEdit.setText(nameOriginal);
            descriptionOriginal = inst.second.getDescription();
            binding.registerDescriptionEdit.setText(descriptionOriginal);
        }

        String uid = FirebaseAuth.getInstance().getUid();
        profileOriginal = new Box<>();
        StorageReference profileRef1 = FirebaseStorage.getInstance().getReference().child("images/"+uid+"/"+ CloudDB.PROFILE_FILE_NAME);
        profileRef1.getDownloadUrl().addOnSuccessListener(uri -> {
            profile.connect(uri,true);
            profileOriginal.set(uri);
        });
        if(typeUser.equals(TypeUser.INSTITUTION)) {
            backgroundOriginal = new Box<>();
            StorageReference backgroundRef = FirebaseStorage.getInstance().getReference().child("images/"+uid+"/"+ CloudDB.BACKGROUND_FILE_NAME);
            backgroundRef.getDownloadUrl().addOnSuccessListener(uri -> {
                background.connect(uri,true);
                backgroundOriginal.set(uri);
            });
        }

        binding.registerDeleteAccount.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_editProfileFragment_to_deleteAccountDialogFragment);
        });

        binding.registerTick.setOnClickListener(v -> {
            boolean everythingCorrect = false;
            String name = binding.registerNameEdit.getText().toString();
            String keywords = binding.registerKeywordsEdit.getText().toString();
            String numberSW = binding.registerNumberSwEdit.getText().toString();
            String description = binding.registerDescriptionEdit.getText().toString();
            if(!checkName(name)) {
                binding.registerName.setError(getString(R.string.invalid_name));
            } else if(typeUser.isAKindOfUser() && keywords.length()==0) {
                binding.registerKeywords.setError(getString(R.string.must_write_keywords));
            } else if(typeUser==TypeUser.SOCIAL_WORKER && numberSW.length()==0) {
                binding.registerNumberSw.setError(getString(R.string.must_write_sw));
            } else {
                everythingCorrect = true;
                Map<String,Object> dataPublic = new HashMap<>();
                Map<String,Object> dataPrivate = new HashMap<>();
                if(typeUser.isAKindOfUser()) {
                    if(!name.equals(nameOriginal)) {
                        dataPublic.put(DataPublicUser.KEY_NAME, name);
                    }
                    if(!description.equals(descriptionOriginal)) {
                        dataPublic.put(DataPublicUser.KEY_PUBLIC_DESCRIP, description);
                    }
                    if(!keywords.equals(keywordsOriginal)) {
                        List<String> keywordsList = new ArrayList<>(Arrays.asList(keywords.split(" ")));
                        dataPrivate.put(DataPrivateUser.KEY_PRIVATE_KEYWORDS, keywordsList);
                    }
                    if(typeUser == TypeUser.SOCIAL_WORKER && !numberSW.equals(numberSWOriginal)) {
                        dataPrivate.put(DataPrivateUser.KEY_NUMBER_SOCIAL_WORKER, numberSW);
                        Pair<PrivateUser, PublicUser> user = viewModel.getRepository().getCloudDB().getDataUser().getValue();
                        dataPrivate.put(DataPrivateUser.KEY_EMAIL, user.first.getEmail());
                    }
                }
                if(typeUser == TypeUser.INSTITUTION) {
                    if(!name.equals(nameOriginal)) {
                        dataPublic.put(DataPublicInstitution.KEY_NAME, name);
                    }
                    if(!description.equals(descriptionOriginal)) {
                        dataPublic.put(DataPublicInstitution.KEY_PUBLIC_DESCRIP, description);
                    }
                    if(background.getUri()==null ? backgroundOriginal.get()!=null : !background.getUri().equals(backgroundOriginal.get())) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference backgroundRef = storage.getReference().child("images/"+uid+"/"+ CloudDB.BACKGROUND_FILE_NAME);
                        if(background.getUri()==null) {
                            Task<Void> deleteTask = backgroundRef.delete();
                            deleteTask.addOnFailureListener(e -> { //todo
                                Toast.makeText(requireContext(), getString(R.string.deleting_failed), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            UploadPictures.uploadImage(requireContext(),getResources(),background.getUri(),backgroundRef);
                        }
                    }
                }
                if(profile.getUri()==null ? profileOriginal.get()!=null : !profile.getUri().equals(profileOriginal.get())) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference profileRef = storage.getReference().child("images/"+uid+"/"+ CloudDB.PROFILE_FILE_NAME);
                    if(profile.getUri()==null) {
                        Task<Void> deleteTask = profileRef.delete();
                        deleteTask.addOnFailureListener(e -> { //todo
                            Toast.makeText(requireContext(), getString(R.string.deleting_failed), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        UploadPictures.uploadImage(requireContext(),getResources(),profile.getUri(),profileRef);
                    }
                }

                if(!dataPrivate.isEmpty() || !dataPublic.isEmpty()) {
                    viewModel.getRepository().getCloudDB().writeExistingUser(uid,dataPrivate,dataPublic);
                }
                NavHostFragment.findNavController(this).popBackStack();
            }
            if(!everythingCorrect) {
                Toast.makeText(requireContext(),getString(R.string.wrong_field),Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean hasChanged() {
        String name = binding.registerNameEdit.getText().toString();
        String description = binding.registerDescriptionEdit.getText().toString();
        String keywords = binding.registerKeywordsEdit.getText().toString();
        String numberSW = binding.registerNumberSwEdit.getText().toString();
        return !name.equals(nameOriginal) ||
                !description.equals(descriptionOriginal) ||
                !equals(profile.getUri(),profileOriginal.get()) ||
                (typeUser.isAKindOfUser() && !keywords.equals(keywordsOriginal)) ||
                (typeUser.equals(TypeUser.SOCIAL_WORKER) && !numberSW.equals(numberSWOriginal)) ||
                (typeUser.equals(TypeUser.INSTITUTION) && !equals(background.getUri(),backgroundOriginal.get()));
    }

    private static boolean equals(Uri uri1, Uri uri2) {
        if(uri1==null) {
            return uri2==null;
        }
        return uri1.equals(uri2);
    }

    private void overrideBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(!hasChanged()) {
                    NavHostFragment.findNavController(EditProfileFragment.this).popBackStack();
                    return;
                }

                String title = getString(R.string.exit);
                String message = getString(R.string.discard_changes);
                String yes = getString(R.string.discard);
                String no = getString(R.string.cancel);
                EditProfileFragmentDirections.ActionEditProfileFragmentToAlertDialogFragment action =
                        EditProfileFragmentDirections.actionEditProfileFragmentToAlertDialogFragment(
                                title, message,yes,no,R.id.editProfileFragment);
                NavHostFragment.findNavController(EditProfileFragment.this).navigate(action);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this,callback);
    }
    
    private void generateWatcherSignUp() {
        binding.registerTick.setEnabled(false);
        List<TextInputEditText> allEditTexts = getAllEditTexts(typeUser);
        MyTextWatcherAdapter signUpChecker = new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                boolean ready = true;
                for(TextInputEditText edit : allEditTexts) {
                    ready = ready && edit.getText().toString().length()>0;
                }
                binding.registerTick.setEnabled(ready);
            }
        };
        for(TextInputEditText edit : allEditTexts) {
            edit.addTextChangedListener(signUpChecker);
        }
        signUpChecker.afterTextChanged(null);
    }

    private List<TextInputEditText> getAllEditTexts(TypeUser typeUser) {
        List<TextInputEditText> allEditTexts = new ArrayList<>(Arrays.asList(binding.registerNameEdit, binding.registerDescriptionEdit));
        switch (typeUser) {
            case SOCIAL_WORKER:
                allEditTexts.add(binding.registerNumberSwEdit);
            case USER:
                allEditTexts.add(binding.registerKeywordsEdit);
                break;
            case INSTITUTION:
                break;
        }
        return allEditTexts;
    }

}