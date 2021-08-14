package gal.boris.compluacoge.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.objects.PrivateInstitution;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.data.objects.PublicInstitution;
import gal.boris.compluacoge.databinding.FragmentSelectVersionBinding;
import gal.boris.compluacoge.logic.MyViewModel;
import gal.boris.compluacoge.ui.adapters.VersionsAdapter;

import static gal.boris.compluacoge.extras.GridSpaceCalculator.dpToPx;

public class SelectVersionDialogFragment extends DialogFragment {

    public static String ID_PROC = "idProcedure";
    public static String VERSION_PROC = "versionProcedure";
    public static String CLOSED_PROC = "closedProcedure";

    private FragmentSelectVersionBinding binding;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        binding = FragmentSelectVersionBinding.inflate(LayoutInflater.from(requireContext()));

        MyViewModel viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setView(binding.getRoot())
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        String idProcedure = SelectVersionDialogFragmentArgs.fromBundle(requireArguments()).getIdProcedure();
        int idStartDestination = SelectVersionDialogFragmentArgs.fromBundle(requireArguments()).getIdStartDestination();
        boolean showEmpty = SelectVersionDialogFragmentArgs.fromBundle(requireArguments()).getShowEmpty();

        Pair<PrivateInstitution, PublicInstitution> pair = viewModel.getRepository().getCloudDB().getDataInstitution().getValue();
        PublicInstitution publicInstitution = pair.second;
        List<Procedure> versionProcs = publicInstitution.getCopyVersionsProcVisible(idProcedure);

        binding.svTitle.setText(getString(showEmpty ? R.string.create_new_version : R.string.versions));
        binding.svText.setText(getString(showEmpty ? R.string.based_on : R.string.choose_which_view));

        VersionsAdapter adapterProcs = new VersionsAdapter(versionProcs,NavHostFragment.findNavController(this),getResources(),idStartDestination,showEmpty);
        improveGridColumns(adapterProcs);

        return dialog;
    }

    private void improveGridColumns(VersionsAdapter adapterProcs) {
        binding.svList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            private int width = 0;
            private GridLayoutManager gridLayoutManager = null;

            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                int widthAux = binding.svList.getWidth();
                if(widthAux>0 && width!=widthAux) {
                    width = widthAux;
                    float pxMinWidth = dpToPx(requireContext(),90);
                    int numColumns = (int) (width/pxMinWidth);

                    if(gridLayoutManager==null) {
                        gridLayoutManager = new GridLayoutManager(requireContext(),numColumns,GridLayoutManager.VERTICAL,false);
                        binding.svList.setLayoutManager(gridLayoutManager);
                        binding.svList.setAdapter(adapterProcs);
                    } else {
                        gridLayoutManager.setSpanCount(numColumns);
                    }
                }
            }
        });
    }
}
