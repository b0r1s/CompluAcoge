package gal.boris.compluacoge.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Arrays;
import java.util.List;

import gal.boris.compluacoge.databinding.FragmentSearchBinding;
import gal.boris.compluacoge.extras.MyTextWatcherAdapter;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.logic.search.SearchIndex;
import gal.boris.compluacoge.ui.adapters.SearchListAdapter;

import static gal.boris.compluacoge.extras.GridSpaceCalculator.dpToPx;

public class SearchFragment extends Fragment {

    public SearchFragment() {}

    private MyViewModel viewModel;
    private FragmentSearchBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        binding = FragmentSearchBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.searchBar.getLayoutParams();
            int dp = (int) dpToPx(requireContext(),16);
            params.setMargins(dp,dp+windowInsets.top,dp,0);
            binding.searchBar.setLayoutParams(params);
            return insets;
        });

        binding.searchList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false));
        SearchListAdapter adapter = new SearchListAdapter(this);
        binding.searchList.setAdapter(adapter);

        SearchIndex searchIndex = viewModel.getRepository().getCloudDB().getSearchIndex();
        searchIndex.getResult().observe(getViewLifecycleOwner(), pair -> {
            showHideView(Arrays.asList(binding.searchLoadingImage,binding.searchLoadingText),!pair.first, 1000);
            adapter.submitList(pair.second);
        });

        binding.searchBarEdit.addTextChangedListener(new MyTextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                searchIndex.searchSentence(s.toString());
            }
        });
        searchIndex.searchSentence("");
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.searchBarEdit.clearFocus();
    }

    //Se pisan los inicios y por eso se cancela y se pone a 1.
    //El final tan pronto se activa para la anim del inicio, espera el delay y arranca
    //Es decir, no hace lo que parece que hace, pero va bien todo
    private static void showHideView(List<View> views, boolean visible, int startDelay) {
        if(visible) {
            for(View view : views) {
                final ViewPropertyAnimator animation = view.animate();
                animation.setDuration(120).setStartDelay(startDelay).alpha(1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        animation.setListener(null);
                        view.setAlpha(1);
                    }
                }).start();
            }
        } else {
            for(View view : views) {
                final ViewPropertyAnimator animation = view.animate();
                animation.setDuration(120).setStartDelay(startDelay).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        animation.setListener(null);
                        view.setAlpha(0);
                        view.setVisibility(View.GONE);
                    }
                }).start();
            }
        }
    }
}