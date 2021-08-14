package gal.boris.compluacoge.ui.adapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.objects.Procedure;
import gal.boris.compluacoge.databinding.ProcItemBinding;
import gal.boris.compluacoge.ui.InstFragmentDirections;

import static gal.boris.compluacoge.extras.AdapterUtils.fuseSorted;

public class InstProcsAdapter extends RecyclerView.Adapter<InstProcsAdapter.ViewHolder> {

    private List<Procedure> list;
    private final Resources resources;
    private final NavController navController;

    public InstProcsAdapter(List<Procedure> list, Resources resources, NavController navController) {
        this.list = new ArrayList<>(list);
        this.resources = resources;
        this.navController = navController;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ProcItemBinding binding;

        public ViewHolder(@NonNull ProcItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void changeList(Collection<Procedure> list) {
        List<Procedure> oldList = this.list;
        this.list = new ArrayList<>(list);
        Collections.sort(this.list, (left,right) -> Long.compare(left.getLastModified(),
                right.getLastModified()));
        fuseSorted(this,oldList,this.list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProcItemBinding binding = ProcItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //RECUERDA, las tarjetas se reciclan para otros
        Procedure proc = list.get(position);
        holder.binding.procTitle.setText(proc.getName());
        holder.binding.procInst.setText(proc.getShortDescription());
        long versionProcedure = Long.parseLong(proc.getVersionProcedure());
        boolean multiple = versionProcedure>1;
        if(proc.getClosed()) {
            holder.binding.procState.setText(resources.getString(R.string.closed_lock));
            holder.binding.procState.setVisibility(View.VISIBLE);
        } else {
            //holder.binding.procState.setText(String.format(resources.getString(R.string.version_num), versionProcedure));
            holder.binding.procState.setVisibility(View.GONE);
        }
        holder.binding.procCard.setOnClickListener(v -> {
            if(multiple) {
                InstFragmentDirections.ActionInstFragmentToSelectVersionDialogFragment action =
                        InstFragmentDirections.actionInstFragmentToSelectVersionDialogFragment(proc.getIdProcedure(),R.id.instFragment);
                navController.navigate(action);
            } else {
                InstFragmentDirections.ActionInstFragmentToInitProcDialogFragment action =
                        InstFragmentDirections.actionInstFragmentToInitProcDialogFragment(proc.getIdProcedure(),proc.getVersionProcedure(),proc.getPublicIdInstitution());
                navController.navigate(action);
            }
        });
        holder.binding.procShadow.setVisibility(multiple ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
