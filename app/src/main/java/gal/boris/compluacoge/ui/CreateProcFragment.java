package gal.boris.compluacoge.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.Node;
import gal.boris.compluacoge.data.RepaintFragment;
import gal.boris.compluacoge.data.UploadPictures;
import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.data.objects.Step;
import gal.boris.compluacoge.databinding.FragmentCreateProcBinding;
import gal.boris.compluacoge.extras.Box;
import gal.boris.compluacoge.extras.GetListPictures;
import gal.boris.compluacoge.extras.MyTextWatcherAdapter;
import gal.boris.compluacoge.extras.OnCompleteMultipleListener;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.ui.adapters.StepsAdapter;

public class CreateProcFragment extends Fragment implements RepaintFragment {

    public CreateProcFragment() {}

    private MyViewModel viewModel;
    private FragmentCreateProcBinding binding;

    private Procedure tempProcedure;
    private Node<Step> stepNow;

    private Procedure originalProcedure;

    private final GetListPictures getListPictures = new GetListPictures(this,true);

    private Map<String, List<Uri>> tempPhotos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        String idProc = CreateProcFragmentArgs.fromBundle(requireArguments()).getIdProcedure();
        String versionProc = CreateProcFragmentArgs.fromBundle(requireArguments()).getVersionProc(); //version to edit, if "" then create
        String basedOnVersion = CreateProcFragmentArgs.fromBundle(requireArguments()).getBasedOnVersion();
        Pair<PrivateInstitution, PublicInstitution> pair = viewModel.getRepository().getCloudDB().getDataInstitution().getValue();
        if("".equals(idProc)) {
            String publicID = pair.first.getID();
            originalProcedure = null;
            tempProcedure = Procedure.createEmpty(publicID, getString(R.string.initial_step),getString(R.string.description));
        } else {
            if(!"".equals(versionProc)) {
                originalProcedure = pair.first.getProcHidden(idProc,versionProc);
                tempProcedure = new Procedure(originalProcedure);
            } else {
                versionProc = ""+(-1+pair.first.getCopyVersionsProcHidden(idProc).stream() //coherent negative version until being published
                        .map(Procedure::getVersionProcedure)
                        .mapToLong(Long::parseLong)
                        .min().orElse(0));
                originalProcedure = null;
                if("".equals(basedOnVersion)) {
                    String publicID = pair.first.getID();
                    tempProcedure = Procedure.createEmpty(publicID, getString(R.string.initial_step),getString(R.string.description));
                } else {
                    Procedure based = pair.second.getProcVisible(idProc,basedOnVersion);
                    if(based==null) {
                        FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException());
                        NavHostFragment.findNavController(this).popBackStack();
                        return;
                    }
                    tempProcedure = Procedure.createBased(based);
                }
                tempProcedure.setIdProcedure(idProc);
                tempProcedure.setVersionProcedure(versionProc);
                tempProcedure.setCreated(System.currentTimeMillis());
            }
        }
        stepNow = tempProcedure.getRootStep();
        overrideBackPressed();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateProcBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tempPhotos = new HashMap<>();
        getListPictures.init(binding.cpCardImagesList,binding.cpCardImagesButton);

        binding.cpPreviousList.setHasFixedSize(true);
        binding.cpPreviousList.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        binding.cpPreviousList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        StepsAdapter adapterPrevious = new StepsAdapter(new ArrayList<>(),this,false);
        binding.cpPreviousList.setAdapter(adapterPrevious);

        binding.cpNextList.setHasFixedSize(true);
        binding.cpNextList.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        binding.cpNextList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        StepsAdapter adapterNext = new StepsAdapter(new ArrayList<>(),this,false);
        binding.cpNextList.setAdapter(adapterNext);

        binding.cpCreate.setOnClickListener(v -> {
            Node<Step> newStep = new Node<>(Step.createEmpty(getString(R.string.step)+" "+(tempProcedure.getAllSteps().size()+1),getString(R.string.description)));
            tempProcedure.addStep(stepNow,newStep);
            checkVisibilityNext(stepNow.getChildren().size());
            adapterNext.addStep(newStep);
        });

        binding.cpLink.setOnClickListener(v -> {
            showPopupExisting();
        });

        binding.cpCardDelete.setOnClickListener(v -> {
            if(tempProcedure.removeStep(stepNow)) {
                tempPhotos.get(stepNow.getNode().getIdStep()).clear();
                repaint(stepNow.getParents().get(0));
            } else {
                Toast.makeText(requireContext(),getString(R.string.step_delete_error),Toast.LENGTH_LONG).show();
            }
        });

        binding.cpCardStepNameEdit.addTextChangedListener(new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                stepNow.getNode().setName(s.toString());
            }
        });
        binding.cpCardStepDescEdit.addTextChangedListener(new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                stepNow.getNode().setDescription(s.toString());
            }
        });

        binding.cpNameEdit.setText(tempProcedure.getName());
        binding.cpNameEdit.addTextChangedListener(new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                tempProcedure.setName(s.toString());
            }
        });
        binding.cpShortDescEdit.setText(tempProcedure.getShortDescription());
        binding.cpShortDescEdit.addTextChangedListener(new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                tempProcedure.setShortDescription(s.toString());
            }
        });
        binding.cpLongDescEdit.setText(tempProcedure.getLongDescription());
        binding.cpLongDescEdit.addTextChangedListener(new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                tempProcedure.setLongDescription(s.toString());
            }
        });
        String tags = tempProcedure.getTags().stream().reduce("", (a,b) -> a+" "+b);
        tags = !"".equals(tags) ? tags.substring(1) : "";
        binding.cpTagsEdit.setText(tags);
        binding.cpTagsEdit.addTextChangedListener(new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                tempProcedure.setTags(new ArrayList<>(Arrays.asList(s.toString().split(" "))));
            }
        });

        /*
        binding.cpCardMarkEnd.setOnClickListener(v -> {
            Node<Step> step = tempProcedure.getEndStep();
            if(step != stepNow) {
                tempProcedure.setEndTree(null);
                if(step != null) {
                    adapterPrevious.notifyStep(step);
                    adapterNext.notifyStep(step);
                }
                tempProcedure.setEndTree(stepNow);
                binding.cpCardFlag.setVisibility(View.VISIBLE);
                binding.cpCardMarkEnd.setText(getString(R.string.unmark_as_end));
            } else {
                tempProcedure.setEndTree(null);
                binding.cpCardFlag.setVisibility(View.GONE);
                binding.cpCardMarkEnd.setText(getString(R.string.mark_as_end));
            }
        });
         */

        binding.cpTitle.setText(getString(originalProcedure!=null ? R.string.edit_procedure : R.string.create_procedure));

        binding.cpDraft.setOnClickListener(v -> {
            if(!isHere()) {
                return;
            }
            updateCloudPhotos(()->{
                if(originalProcedure!=null) {
                    if(viewModel.updateNewProcDraft(tempProcedure,originalProcedure)) {
                        NavHostFragment.findNavController(this).popBackStack();
                    } else {
                        showToast(getString(R.string.error_updating_draft));
                    }
                } else {
                    if(viewModel.addNewProcDraft(tempProcedure)) {
                        NavHostFragment.findNavController(this).popBackStack();
                    } else {
                        showToast(getString(R.string.error_creating_draft));
                    }
                }
            });
        });

        binding.cpDeleteProc.setOnClickListener(v -> {
            if(originalProcedure!=null && isHere()) {
                removeAllPhotosProc(()->{
                    if(viewModel.deleteDraft(originalProcedure)) {
                        NavHostFragment.findNavController(CreateProcFragment.this).popBackStack();
                    } else {
                        showToast(getString(R.string.error_deleting_draft));
                    }
                });
            }
        });
        binding.cpDeleteProc.setVisibility(originalProcedure!=null ? View.VISIBLE : View.GONE);

        binding.cpPublish.setOnClickListener(v -> {
            if(!isHere()) {
                return;
            }
            Box<String> error = new Box<>();
            if(!draftIsValid(error)) {
                showToast(error.get());
            } else {
                updateCloudPhotos(() -> {
                    if (!viewModel.publishDraft(tempProcedure,originalProcedure)) {
                        showToast(getString(R.string.error_publishing_draft));
                    } else {
                        NavHostFragment.findNavController(this).popBackStack();
                    }
                });
            }
        });

        repaint(stepNow);
    }

    private void updateCloudPhotos(Runnable afterComplete) {
        Set<Uri> allAdded = new HashSet<>();
        Set<Uri> allMaintained = new HashSet<>();
        Set<Uri> allRemoved = new HashSet<>();
        for(Map.Entry<String,List<Uri>> entry : tempPhotos.entrySet()) {
            Node<Step> node = tempProcedure.getNode(entry.getKey());
            node = node!=null ? node : originalProcedure.getNode(entry.getKey());
            Set<Uri> oldImagesUris = node.getNode().getImagesURL().stream().map(Uri::parse).collect(Collectors.toSet());
            Set<Uri> newImagesUris = new HashSet<>(entry.getValue());
            Set<Uri> added = new HashSet<>(newImagesUris);
            added.removeAll(oldImagesUris);
            Set<Uri> removed = new HashSet<>(oldImagesUris);
            removed.removeAll(newImagesUris);
            Set<Uri> maintained = new HashSet<>(oldImagesUris);
            maintained.removeAll(removed);
            allAdded.addAll(added);
            allRemoved.addAll(removed);
            allMaintained.addAll(maintained);
        }
        allRemoved.removeAll(allMaintained);
        for(Uri uri : allRemoved) {
            List<String> paths = uri.getPathSegments();
            String nameInDB = paths.get(paths.size()-1);
            String[] splitted = nameInDB.split("/");
            removePhoto(splitted[splitted.length-1]);
        }

        UploadPictures.OnCompleteListener onUploaded = mapResult -> {
            if (mapResult.size() != allAdded.size()) {
                showToast(getString(R.string.error_uploading_photos));
            }
            for (Map.Entry<String, List<Uri>> entry : tempPhotos.entrySet()) {
                Node<Step> node = tempProcedure.getNode(entry.getKey());
                if(node == null) {
                    if(!entry.getValue().isEmpty()) {
                        FirebaseCrashlytics.getInstance().recordException(new IllegalStateException());
                    } else {
                        continue;
                    }
                }
                Map<Uri, String> allUrisMap = node.getNode().getImagesURL().stream().map(Uri::parse)
                        .collect(Collectors.toMap(Function.identity(), Uri::toString));
                allUrisMap.putAll(mapResult);
                List<String> newImagesList = entry.getValue().stream().filter(allUrisMap::containsKey)
                        .map(allUrisMap::get).collect(Collectors.toList());
                node.getNode().setImageList(newImagesList);
            }

            afterComplete.run();
        };
        UploadPictures.uploadList(tempProcedure.getIdProcedure(), new ArrayList<>(allAdded), onUploaded,
                getContext(), getResources());
    }

    private void removeAllPhotosProc(Runnable success) {
        FirebaseStorage.getInstance().getReference()
                .child("images/"+ FirebaseAuth.getInstance().getUid()+"/"+tempProcedure.getIdProcedure()).listAll()
                .addOnFailureListener(e -> showToast(getString(R.string.error_deleting_photos)))
                .addOnSuccessListener(listResult -> {
                    OnCompleteListener<Void> onCompleteListener = new OnCompleteMultipleListener(listResult.getItems().size(), success,
                            () -> showToast(getString(R.string.error_deleting_photos)));
                    for(StorageReference pref : listResult.getItems()) {
                        pref.delete().addOnCompleteListener(onCompleteListener);
                    }
                });
    }

    private void removePhoto(String name) {
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("images/"+ FirebaseAuth.getInstance().getUid()+"/"+tempProcedure.getIdProcedure()+"/"+name);
        Task<Void> deleteTask = ref.delete();
        deleteTask.addOnFailureListener(e -> { //todo
            showToast(getString(R.string.error_deleting_photos));
        });
    }

    private void showToast(String error) {
        Context context = getContext();
        if(context!=null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }
    }

    private void overrideBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String title = getString(R.string.exit);
                String message = getString(R.string.discard_changes);
                String yes = getString(R.string.discard);
                String no = getString(R.string.cancel);
                CreateProcFragmentDirections.ActionCreateProcFragmentToAlertDialogFragment action =
                        CreateProcFragmentDirections.actionCreateProcFragmentToAlertDialogFragment(
                                title, message,yes,no,R.id.createProcFragment);
                NavHostFragment.findNavController(CreateProcFragment.this).navigate(action);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this,callback);
    }

    public boolean isHere() {
        NavDestination destNow = NavHostFragment.findNavController(this).getCurrentDestination();
        return destNow!=null && destNow.getId()==R.id.createProcFragment;
    }

    public boolean draftIsValid(Box<String> error) {
        error.set(null);
        if(tempProcedure==null) {
            error.set(getString(R.string.error_publishing_not_found));
        } else if(tempProcedure.getName().isEmpty()) {
            error.set(getString(R.string.error_publishing_name));
        } else if(tempProcedure.getShortDescription().isEmpty()) {
            error.set(getString(R.string.error_publishing_short_desc));
        } else if(tempProcedure.getLongDescription().isEmpty()) {
            error.set(getString(R.string.error_publishing_long_desc));
        } else if(tempProcedure.getTags().isEmpty()) {
            error.set(getString(R.string.error_publishing_tags));
        } else {
            Pair<Boolean,Integer> resultCheck = tempProcedure.checkGraphCorrectness();
            if(!resultCheck.first) {
                error.set(getString(resultCheck.second));
            }
        }
        return error.get()==null;
    }

    private void showPopupExisting() {
        List<Node<Step>> notAncestors = new ArrayList<>(tempProcedure.getAllSteps());
        notAncestors.removeAll(stepNow.getAncestors());
        PopupMenu popupMenu = new PopupMenu(requireContext(), binding.cpLink);
        Map<MenuItem, Node<Step>> map = new HashMap<>();
        for(Node<Step> node : notAncestors) {
            map.put(popupMenu.getMenu().add(node.getNode().getName()),node);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            Node<Step> nextStep = map.get(item);
            tempProcedure.linkStep(stepNow,nextStep);
            checkVisibilityNext(stepNow.getChildren().size());
            StepsAdapter nextAdapter = (StepsAdapter) binding.cpNextList.getAdapter();
            nextAdapter.addStep(nextStep);
            return true;
        });
        popupMenu.show();
    }

    @Override
    public void repaint(Node<Step> current) {
        stepNow = current;

        if(!tempPhotos.containsKey(stepNow.getNode().getIdStep())) {
            List<Uri> uriList = stepNow.getNode().getImagesURL().stream()
                    .map(Uri::parse).collect(Collectors.toList());
            tempPhotos.put(stepNow.getNode().getIdStep(),uriList);
        }
        getListPictures.changeList(tempPhotos.get(stepNow.getNode().getIdStep()));

        StepsAdapter previousAdapter = (StepsAdapter) binding.cpPreviousList.getAdapter();
        previousAdapter.changeList(stepNow.getParents());
        checkVisibilityPrevious(stepNow.getParents().size());

        binding.cpCardStepNameEdit.setText(stepNow.getNode().getName());
        binding.cpCardStepDescEdit.setText(stepNow.getNode().getDescription());

        //boolean isTheLastOne = tempProcedure.getEndStep()!=null &&
        //        stepNow.getNode().getIdStep().equals(tempProcedure.getEndStep().getNode().getIdStep());
        //binding.cpCardFlag.setVisibility(isTheLastOne ? View.VISIBLE : View.GONE);
        //binding.cpCardMarkEnd.setText(getString(isTheLastOne ? R.string.unmark_as_end : R.string.mark_as_end));
        binding.cpCardDelete.setVisibility(stepNow.isBeginning() ? View.GONE : View.VISIBLE);

        StepsAdapter nextAdapter = (StepsAdapter) binding.cpNextList.getAdapter();
        nextAdapter.changeList(stepNow.getChildren());
        checkVisibilityNext(stepNow.getChildren().size());
    }

    private void checkVisibilityPrevious(int size) {
        List<View> list = Arrays.asList(binding.cpPreviousStepTitle,binding.cpPreviousList,binding.cpIcon1);
        for(View view : list) {
            view.setVisibility(size>0 ? View.VISIBLE : View.GONE);
        }
        binding.cpIcon2.setVisibility(size>1 ? View.VISIBLE : View.GONE);
    }

    private void checkVisibilityNext(int size) {
        List<View> list = Arrays.asList(binding.cpNextTitle,binding.cpNextList,binding.cpIcon3);
        for(View view : list) {
            view.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
        }
        binding.cpIcon4.setVisibility(size>1 ? View.VISIBLE : View.GONE);
    }

}