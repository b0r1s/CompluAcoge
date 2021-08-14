package gal.boris.compluacoge.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.Node;
import gal.boris.compluacoge.data.RepaintFragment;
import gal.boris.compluacoge.data.objects.AProcedure;
import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.PrivateUser;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.data.objects.PublicUser;
import gal.boris.compluacoge.data.objects.Step;
import gal.boris.compluacoge.databinding.FragmentViewProcBinding;
import gal.boris.compluacoge.extras.GetListPictures;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.ui.adapters.StepsAdapter;

public class ViewProcFragment extends Fragment implements RepaintFragment {

    public ViewProcFragment() {}

    private MyViewModel viewModel;
    private FragmentViewProcBinding binding;

    //3 cases: user (create or edit aproc), inst owner, inst visitor
    private boolean isKindOfUser;
    private boolean userFirstTime;
    private boolean instOwner;
    private AProcedure aproc;
    private List<Node<Step>> stepsDoneOriginal;
    private List<Node<Step>> stepsDoneNow;
    private int numRepaints;

    private final GetListPictures getListPictures = new GetListPictures(this,false);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        String idInst = ViewProcFragmentArgs.fromBundle(requireArguments()).getIdInst();
        String idProc = ViewProcFragmentArgs.fromBundle(requireArguments()).getIdProcedure();
        String versionProc = ViewProcFragmentArgs.fromBundle(requireArguments()).getVersionProcedure();
        if("".equals(idInst) || "".equals(idProc) || "".equals(versionProc)) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        isKindOfUser = viewModel.getAccountManager().getInfoLogin().getTypeUser().isAKindOfUser();
        instOwner = idInst.equals(FirebaseAuth.getInstance().getUid());
        userFirstTime = false;

        if(isKindOfUser) {
            Pair<PrivateUser, PublicUser> pair = viewModel.getRepository().getCloudDB().getDataUser().getValue();
            aproc = pair.first.getAProcedure(idInst, idProc, versionProc); //take started aproc
            if(aproc == null) {
                PublicInstitution publicInst = viewModel.getRepository().getCloudDB().getSearchInstCache().getInstitution(idInst).getValue();
                aproc = AProcedure.createProcBased(publicInst,idProc,versionProc); //new aproc
                userFirstTime = true;
            }
        } else {
            if(instOwner) {
                Pair<PrivateInstitution, PublicInstitution> pair = viewModel.getRepository().getCloudDB().getDataInstitution().getValue();
                aproc = AProcedure.createProcBased(pair.second,idProc,versionProc);
            } else {
                PublicInstitution publicInst = viewModel.getRepository().getCloudDB().getSearchInstCache().getInstitution(idInst).getValue();
                aproc = AProcedure.createProcBased(publicInst,idProc,versionProc);
            }
        }

        stepsDoneOriginal = aproc.getStepsDone();
        if(stepsDoneOriginal.isEmpty()) {
            stepsDoneOriginal.add(aproc.getProcedure().getRootStep());
        }
        stepsDoneNow = new ArrayList<>(stepsDoneOriginal);
        numRepaints = 0;
        overrideBackPressed();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewProcBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListPictures.init(binding.vpCardImages,null);

        binding.vpPreviousList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        StepsAdapter adapterPrevious = new StepsAdapter(aproc.getPreviousStepsDone(),this,true);
        binding.vpPreviousList.setAdapter(adapterPrevious);

        binding.vpNextList.setHasFixedSize(true);
        binding.vpNextList.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        binding.vpNextList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        StepsAdapter adapterNext = new StepsAdapter(aproc.getStepNow().getChildren(),this,false);
        binding.vpNextList.setAdapter(adapterNext);

        binding.vpTitle.setText(aproc.getProcedure().getName());
        binding.vpClosedCard.setVisibility(aproc.getProcedure().getClosed() ? View.VISIBLE : View.GONE);

        setVariableButtons();

        //repaint(aproc.getProcedure().getRootStep());
        repaint(stepsDoneNow.get(stepsDoneNow.size()-1));
    }

    private void setVariableButtons() {
        if(isKindOfUser) {
            binding.vpCloseProc.setVisibility(View.GONE);
            binding.vpCreateVersion.setVisibility(View.GONE);
            binding.vpSeparator.setVisibility(View.GONE);
        } else {
            binding.vpCloseProc.setVisibility(View.VISIBLE);
            binding.vpCreateVersion.setVisibility(View.VISIBLE);
            binding.vpSeparator.setVisibility(View.VISIBLE);

            //Close procedure
            binding.vpCloseProc.setText(getString(R.string.take_it_off));
            binding.vpCloseProc.setEnabled(!aproc.getProcedure().getClosed());
            binding.vpCloseProc.setOnClickListener(v -> {
                if(!isHere()) {
                    return;
                }
                if(viewModel.closeAllVisibleVersions(aproc.getProcedure())) {
                    binding.vpCloseProc.setEnabled(false);
                    //binding.vpClosedCard.setVisibility(View.VISIBLE);
                    NavHostFragment.findNavController(this).popBackStack();
                } else {
                    Toast.makeText(requireContext(),getString(R.string.error_closing),Toast.LENGTH_LONG).show();
                }
            });

            //Create new version
            createObserverToGetFromDialog();
            binding.vpCreateVersion.setText(getString(R.string.create_new_version));
            binding.vpCreateVersion.setOnClickListener(v -> {
                if(!isHere()) {
                    return;
                }
                ViewProcFragmentDirections.ActionViewProcFragmentToSelectVersionDialogFragment action =
                        ViewProcFragmentDirections.actionViewProcFragmentToSelectVersionDialogFragment(aproc.getProcedure().getIdProcedure(),R.id.viewProcFragment);
                action.setShowEmpty(true);
                NavHostFragment.findNavController(this).navigate(action);
            });
        }

        //These appear and disappear dinamically
        binding.vpDeleteProc.setOnClickListener(v -> {
            if(!isHere()) {
                return;
            }
            if(isKindOfUser) {
                if(viewModel.deleteAProc(aproc)) {
                    goBackPreviousToSearch();
                } else {
                    Toast.makeText(requireContext(),getString(R.string.error_deleting_proc),Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.vpTick.setOnClickListener(v -> {
            if(!isHere()) {
                return;
            }
            aproc.setStepsDone(stepsDoneNow);
            boolean uploaded = userFirstTime ? viewModel.addAProc(aproc) : viewModel.updateAProc(aproc,aproc);
            if(uploaded) {
                goBackPreviousToSearch();
            } else {
                showToast(getString(R.string.error_updating_proc));
            }
        });
        binding.vpEndButton.setOnClickListener(v -> {
            if(!isHere()) {
                return;
            }
            if(isKindOfUser) {
                aproc.setStepsDone(stepsDoneNow);
                boolean uploaded = userFirstTime ? viewModel.addAProc(aproc) : viewModel.updateAProc(aproc,aproc);
                if(uploaded) {
                    goBackPreviousToSearch();
                } else {
                    showToast(getString(R.string.error_updating_proc));
                }
            } else {
                goBackPreviousToSearch();
            }
        });
    }

    private void goBackPreviousToSearch() {
        if(viewModel.getAccountManager().getInfoLogin().getTypeUser().isAKindOfUser()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_viewProcFragment_to_userFragment);
        } else {
            NavHostFragment.findNavController(this).navigate(R.id.action_viewProcFragment_to_instFragment);
        }
    }

    private void changeTo(Node<Step> newNow) {
        Node<Step> oldNow = stepsDoneNow.get(stepsDoneNow.size()-1);
        if(oldNow.getChildren().contains(newNow)) {
            stepsDoneNow.add(newNow);
        } else if(stepsDoneNow.contains(newNow)) {
            int index = stepsDoneNow.indexOf(newNow);
            stepsDoneNow.subList(index+1,stepsDoneNow.size()).clear();
        } else {
            throw new IllegalArgumentException("No se encontró este nodo");
        }
    }

    private void createObserverToGetFromDialog() {
        final NavBackStackEntry navBackStackEntry = NavHostFragment.findNavController(this).getBackStackEntry(R.id.viewProcFragment);
        final LifecycleEventObserver observer = (source, event) -> {
            if(event.equals(Lifecycle.Event.ON_RESUME) &&
                    navBackStackEntry.getSavedStateHandle().contains(SelectVersionDialogFragment.ID_PROC) &&
                    navBackStackEntry.getSavedStateHandle().contains(SelectVersionDialogFragment.VERSION_PROC)) {
                String idProc = navBackStackEntry.getSavedStateHandle().get(SelectVersionDialogFragment.ID_PROC);
                navBackStackEntry.getSavedStateHandle().remove(SelectVersionDialogFragment.ID_PROC);
                String versionProc = navBackStackEntry.getSavedStateHandle().get(SelectVersionDialogFragment.VERSION_PROC);
                navBackStackEntry.getSavedStateHandle().remove(SelectVersionDialogFragment.VERSION_PROC);
                if(idProc==null || versionProc==null || "".equals(idProc)) {
                    FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException());
                    return;
                }
                ViewProcFragmentDirections.ActionViewProcFragmentToCreateProcFragment action =
                        ViewProcFragmentDirections.actionViewProcFragmentToCreateProcFragment(idProc,"",versionProc);
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

    private void overrideBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(stepsDoneOriginal.equals(stepsDoneNow) || !isKindOfUser) {
                    NavHostFragment.findNavController(ViewProcFragment.this).popBackStack();
                    return;
                }

                String title = getString(R.string.exit);
                String message = getString(R.string.discard_changes);
                String yes = getString(R.string.discard);
                String no = getString(R.string.cancel);
                ViewProcFragmentDirections.ActionViewProcFragmentToAlertDialogFragment action =
                        ViewProcFragmentDirections.actionViewProcFragmentToAlertDialogFragment(
                                title, message,yes,no,R.id.viewProcFragment);
                NavHostFragment.findNavController(ViewProcFragment.this).navigate(action);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this,callback);
    }

    private void showToast(String error) {
        Context context = getContext();
        if(context!=null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isHere() {
        NavDestination destNow = NavHostFragment.findNavController(this).getCurrentDestination();
        return destNow!=null && destNow.getId()==R.id.viewProcFragment;
    }

    @Override
    public void repaint(Node<Step> stepNow) {
        numRepaints++;
        changeTo(stepNow);

        StepsAdapter previousAdapter = (StepsAdapter) binding.vpPreviousList.getAdapter();
        List<Node<Step>> previousSteps = new ArrayList<>(stepsDoneNow.subList(0,stepsDoneNow.size()-1));;//aproc.getPreviousStepsDone();
        previousAdapter.changeList(previousSteps);
        checkVisibilityPrevious(previousSteps.size());

        binding.vpCardStepName.setText(stepNow.getNode().getName());
        binding.vpCardStepDesc.setText(stepNow.getNode().getDescription());
        List<Uri> uriList = stepNow.getNode().getImagesURL().stream()
                .map(Uri::parse).collect(Collectors.toList());
        getListPictures.changeList(uriList);

        binding.vpCardFlag.setVisibility(stepNow.isEnd() ? View.VISIBLE : View.GONE);

        StepsAdapter nextAdapter = (StepsAdapter) binding.vpNextList.getAdapter();
        nextAdapter.changeList(stepNow.getChildren());
        checkVisibilityNext(stepNow.getChildren().size());

        checkVisibilityEnd(stepNow.isEnd());
    }

    private void checkVisibilityPrevious(int size) {
        List<View> list = Arrays.asList(binding.vpPreviousStepTitle,binding.vpPreviousList);
        for(View view : list) {
            view.setVisibility(size>0 ? View.VISIBLE : View.GONE);
        }
    }

    private void checkVisibilityNext(int size) {
        List<View> list = Arrays.asList(binding.vpNextTitle,binding.vpNextList,binding.vpIcon3);
        for(View view : list) {
            view.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
        }
        binding.vpIcon4.setVisibility(size>1 ? View.VISIBLE : View.GONE);
    }

    private void checkVisibilityEnd(boolean end) {
        List<View> listFinish = Arrays.asList(binding.vpEnd,binding.vpEndBalloon1,binding.vpEndBalloon2,
                binding.vpEndButton);
        for(View view : listFinish) {
            view.setVisibility(end ? View.VISIBLE : View.GONE);
        }
        binding.vpTick.setVisibility(!end && isKindOfUser ? View.VISIBLE : View.GONE);
        binding.vpDeleteProc.setVisibility(!end && isKindOfUser ? View.VISIBLE : View.GONE);
    }
}