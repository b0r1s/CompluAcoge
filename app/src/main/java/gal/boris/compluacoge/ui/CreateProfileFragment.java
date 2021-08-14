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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.pojos.DataPrivateInstitution;
import gal.boris.compluacoge.data.pojos.DataPrivateUser;
import gal.boris.compluacoge.data.pojos.DataPublicInstitution;
import gal.boris.compluacoge.data.pojos.DataPublicUser;
import gal.boris.compluacoge.databinding.FragmentCreateProfileBinding;
import gal.boris.compluacoge.extras.GetPicture;
import gal.boris.compluacoge.extras.MyTextWatcherAdapter;
import gal.boris.compluacoge.logic.CloudDB;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.logic.TypeUser;

import static gal.boris.compluacoge.data.UploadPictures.uploadImage;

public class CreateProfileFragment extends Fragment {

    public CreateProfileFragment() {}

    private MyViewModel viewModel;
    private FragmentCreateProfileBinding binding;
    private TypeUser typeUser;

    private final GetPicture profile = new GetPicture(this);
    private final GetPicture background = new GetPicture(this);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateProfileBinding.inflate(inflater,container,false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profile.init(R.drawable.icon_person,binding.registerImageTitle, binding.registerImage, binding.registerImageButtonEdit, binding.registerImageButtonDelete);
        background.init(null,binding.registerBackImageTitle, binding.registerBackImage, binding.registerBackImageButtonEdit, binding.registerBackImageButtonDelete);

        List<TextInputEditText> allEditTexts = new ArrayList<>(Arrays.asList(binding.registerNameEdit, binding.registerEmailEdit,
                binding.registerPasswdEdit, binding.registerPasswdConfirmEdit, binding.registerDescriptionEdit,
                binding.registerKeywordsEdit,binding.registerNumberSwEdit));
        List<TextInputLayout> allTexts = new ArrayList<>(Arrays.asList(binding.registerName, binding.registerEmail,
                binding.registerPasswd, binding.registerPasswdConfirm, binding.registerDescription,
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

        binding.registerRadio.setOnCheckedChangeListener((group, checkedId) -> {
            binding.registerKeywords.setVisibility(View.GONE);
            binding.registerTextKeywords.setVisibility(View.GONE);
            binding.registerNumberSw.setVisibility(View.GONE);
            binding.registerTextSw.setVisibility(View.GONE);
            background.hide();
            switch (checkedId) {
                case R.id.register_radio_sw:
                    typeUser = TypeUser.SOCIAL_WORKER;
                    break;
                case R.id.register_radio_user:
                    typeUser = TypeUser.USER;
                    break;
                case R.id.register_radio_inst:
                    typeUser = TypeUser.INSTITUTION;
                    break;
            }
            if(typeUser == TypeUser.SOCIAL_WORKER) {
                binding.registerNumberSw.setVisibility(View.VISIBLE);
                binding.registerTextSw.setVisibility(View.VISIBLE);
            }
            if(typeUser.isAKindOfUser()) {
                binding.registerKeywords.setVisibility(View.VISIBLE);
                binding.registerTextKeywords.setVisibility(View.VISIBLE);
            } else {
                background.show();
            }
            generateWatcherSignUp();
        });
        binding.registerRadio.check(R.id.register_radio_user);
        typeUser = TypeUser.USER;
        generateWatcherSignUp();

        binding.registerSignUp.setOnClickListener(v -> {
            boolean everythingCorrect = false;
            String name = binding.registerNameEdit.getText().toString();
            String email = binding.registerEmailEdit.getText().toString();
            String passwd = binding.registerPasswdEdit.getText().toString();
            String passwdConfirm = binding.registerPasswdConfirmEdit.getText().toString();
            String keywords = binding.registerKeywordsEdit.getText().toString();
            String numberSW = binding.registerNumberSwEdit.getText().toString();
            String description = binding.registerDescriptionEdit.getText().toString();
            if(!checkName(name)) {
                binding.registerName.setError(getString(R.string.invalid_name));
            } else if (!checkEmail(email)) {
                binding.registerEmail.setError(getString(R.string.invalid_format));
            } else if(!checkPasswd(passwd)) {
                binding.registerPasswd.setError(getString(R.string.invalid_passwd));
            } else if(!passwd.equals(passwdConfirm)) {
                binding.registerPasswdConfirm.setError(getString(R.string.not_same_passwd));
            } else if(typeUser.isAKindOfUser() && keywords.isEmpty()) {
                binding.registerKeywords.setError(getString(R.string.must_write_keywords));
            } else if(typeUser==TypeUser.SOCIAL_WORKER && numberSW.isEmpty()) {
                binding.registerNumberSw.setError(getString(R.string.must_write_sw));
            }
            else {
                everythingCorrect = true;
                final TypeUser createdUser = typeUser;
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,passwd)
                        .addOnCompleteListener(task -> {
                            FirebaseUser user = null;
                            if(task.isSuccessful()) {
                                user = FirebaseAuth.getInstance().getCurrentUser();
                            }
                            if(user != null) {
                                Map<String,Object> dataPublic = new HashMap<>();
                                Map<String,Object> dataPrivate = new HashMap<>();
                                String uid = user.getUid();
                                if(createdUser.isAKindOfUser()) {
                                    dataPublic.put(DataPublicUser.KEY_ID, uid);
                                    dataPrivate.put(DataPrivateUser.KEY_ID, uid);
                                    dataPublic.put(CloudDB.KEY_TYPE_USER, createdUser.toString());
                                    dataPrivate.put(CloudDB.KEY_TYPE_USER, createdUser.toString());
                                    dataPublic.put(DataPublicUser.KEY_CREATED, System.currentTimeMillis());
                                    dataPublic.put(DataPublicUser.KEY_LAST_MODIFIED, System.currentTimeMillis());

                                    dataPublic.put(DataPublicUser.KEY_NAME, name);
                                    dataPrivate.put(DataPrivateUser.KEY_EMAIL, email);
                                    dataPublic.put(DataPublicUser.KEY_PUBLIC_DESCRIP, description);
                                    List<String> keywordsList = Arrays.asList(keywords.split(" "));
                                    dataPrivate.put(DataPrivateUser.KEY_PRIVATE_KEYWORDS, keywordsList);
                                    if(createdUser == TypeUser.SOCIAL_WORKER) {
                                        dataPrivate.put(DataPrivateUser.KEY_NUMBER_SOCIAL_WORKER, numberSW);
                                    }
                                }
                                if(createdUser == TypeUser.INSTITUTION) {
                                    dataPublic.put(DataPublicInstitution.KEY_ID, uid);
                                    dataPrivate.put(DataPrivateInstitution.KEY_ID, uid);
                                    dataPublic.put(CloudDB.KEY_TYPE_USER, createdUser.toString());
                                    dataPrivate.put(CloudDB.KEY_TYPE_USER, createdUser.toString());
                                    dataPublic.put(DataPublicInstitution.KEY_CREATED, System.currentTimeMillis());
                                    dataPublic.put(DataPublicInstitution.KEY_LAST_MODIFIED, System.currentTimeMillis());

                                    dataPublic.put(DataPublicInstitution.KEY_NAME, name);
                                    dataPrivate.put(DataPrivateInstitution.KEY_EMAIL, email);
                                    dataPublic.put(DataPublicInstitution.KEY_PUBLIC_DESCRIP, description);

                                    if(background.getUri()!=null) {
                                        StorageReference backgroundRef = FirebaseStorage.getInstance().getReference().child("images/"+uid+"/"+ CloudDB.BACKGROUND_FILE_NAME);
                                        uploadImage(requireContext(), getResources(), background.getUri(), backgroundRef);
                                    }
                                }

                                if(profile.getUri()!=null) {
                                    StorageReference profileRef = FirebaseStorage.getInstance().getReference().child("images/"+uid+"/"+ CloudDB.PROFILE_FILE_NAME);
                                    uploadImage(requireContext(), getResources(), profile.getUri(), profileRef);
                                }
                                viewModel.getRepository().getCloudDB().writeNewUser(uid,dataPrivate,dataPublic);
                                viewModel.getAccountManager().signIn(user);
                                NavHostFragment.findNavController(this).popBackStack();
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            if(!everythingCorrect) {
                Toast.makeText(requireContext(),getString(R.string.wrong_field),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void generateWatcherSignUp() {
        binding.registerSignUp.setEnabled(false);
        List<TextInputEditText> allEditTexts = getAllEditTexts(typeUser);
        MyTextWatcherAdapter signUpChecker = new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                boolean ready = true;
                for(TextInputEditText edit : allEditTexts) {
                    ready = ready && edit.getText().toString().length()>0;
                }
                binding.registerSignUp.setEnabled(ready);
            }
        };
        for(TextInputEditText edit : allEditTexts) {
            edit.addTextChangedListener(signUpChecker);
        }
        signUpChecker.afterTextChanged(null); //todo !
    }

    private List<TextInputEditText> getAllEditTexts(TypeUser typeUser) {
        List<TextInputEditText> allEditTexts = new ArrayList<>(Arrays.asList(binding.registerNameEdit, binding.registerEmailEdit,
                binding.registerPasswdEdit, binding.registerPasswdConfirmEdit, binding.registerDescriptionEdit));
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

    public static boolean checkName(String name) {
        return name.matches("^[a-zA-Z _-]{3,20}$");
    }

    public static boolean checkEmail(String email) {
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public static boolean checkPasswd(String passwd) {
        return passwd.matches("^[\\w-\\.]{8,15}$");
    }
}