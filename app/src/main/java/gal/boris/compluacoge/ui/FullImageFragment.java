package gal.boris.compluacoge.ui;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.databinding.FragmentFullImageBinding;
import gal.boris.compluacoge.ui.adapters.FullImagesAdapter;

public class FullImageFragment extends DialogFragment {

    public FullImageFragment() {}

    private FragmentFullImageBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentFullImageBinding.inflate(LayoutInflater.from(requireContext()));
        Uri[] uris = FullImageFragmentArgs.fromBundle(requireArguments()).getUris();
        if(uris.length==0) {
            NavHostFragment.findNavController(this).popBackStack();
        }
        int pos = FullImageFragmentArgs.fromBundle(requireArguments()).getPos();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL,false);
        binding.fiRecyclerView.setLayoutManager(layoutManager);
        FullImagesAdapter adapter = new FullImagesAdapter(uris,this);
        binding.fiRecyclerView.setAdapter(adapter);
        layoutManager.scrollToPositionWithOffset(pos,0);

        binding.fiClose.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        binding.fiRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean toRight = true;
            boolean animating = false;
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                toRight = dx > 0;
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(!animating) {
                        animating = true;
                        int futurePos = layoutManager.findFirstVisibleItemPosition() + (toRight ? 1 : 0);
                        recyclerView.smoothScrollToPosition(futurePos);
                    } else {
                        animating = false;
                    }
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.getRoot().setPaddingRelative(0, windowInsets.top, 0, 0);
            return insets;
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setView(binding.getRoot())
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        return dialog;
    }

}