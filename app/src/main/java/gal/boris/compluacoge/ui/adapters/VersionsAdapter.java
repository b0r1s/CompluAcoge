package gal.boris.compluacoge.ui.adapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.databinding.VersionItemBinding;

import static gal.boris.compluacoge.ui.SelectVersionDialogFragment.ID_PROC;
import static gal.boris.compluacoge.ui.SelectVersionDialogFragment.VERSION_PROC;

public class VersionsAdapter extends RecyclerView.Adapter<VersionsAdapter.ViewHolder> {

    private final List<Procedure> cpyList;
    private final NavController navController;
    private final Resources resources;
    private final int idStartDestination;
    private final boolean showEmpty;

    public VersionsAdapter(List<Procedure> list, NavController navController, Resources resources,
                           int idStartDestination, boolean showEmpty) {
        this.cpyList = new ArrayList<>(list);
        this.navController = navController;
        this.resources = resources;
        this.idStartDestination = idStartDestination;
        this.showEmpty = showEmpty;

        Collections.sort(cpyList, (a,b) -> (-1)*Long.compare(
                Long.parseLong(a.getVersionProcedure()),Long.parseLong(b.getVersionProcedure())));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final VersionItemBinding binding;

        public ViewHolder(@NonNull VersionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VersionItemBinding binding = VersionItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //RECUERDA, las tarjetas se reciclan para otros todo quitar
        if(showEmpty && position==0) {
            holder.binding.versionPadlock.setVisibility(View.GONE);
            holder.binding.versionDesc.setText(resources.getString(R.string.empty));
            holder.binding.versionCard.setOnClickListener(v -> {
                navController.getBackStackEntry(idStartDestination).getSavedStateHandle().set(ID_PROC,cpyList.get(0).getIdProcedure());
                navController.getBackStackEntry(idStartDestination).getSavedStateHandle().set(VERSION_PROC,"");
                navController.popBackStack();
            });
        } else {
            Procedure proc = cpyList.get(showEmpty ? position-1 : position);
            holder.binding.versionPadlock.setVisibility(showEmpty ? View.GONE : (proc.getClosed() ? View.VISIBLE : View.GONE));
            holder.binding.versionDesc.setText(proc.getVersionProcedure());
            holder.binding.versionCard.setOnClickListener(v -> {
                navController.getBackStackEntry(idStartDestination).getSavedStateHandle().set(ID_PROC,proc.getIdProcedure());
                navController.getBackStackEntry(idStartDestination).getSavedStateHandle().set(VERSION_PROC,proc.getVersionProcedure());
                navController.popBackStack();
            });
        }
    }

    @Override
    public int getItemCount() {
        return cpyList.size() + (showEmpty ? 1 : 0);
    }
}