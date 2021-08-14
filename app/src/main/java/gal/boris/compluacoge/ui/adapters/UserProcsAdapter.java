package gal.boris.compluacoge.ui.adapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.objects.AProcedure;
import gal.boris.compluacoge.databinding.ProcItemBinding;
import gal.boris.compluacoge.ui.UserFragmentDirections;

import static gal.boris.compluacoge.extras.AdapterUtils.fuseSorted;

public class UserProcsAdapter extends RecyclerView.Adapter<UserProcsAdapter.ViewHolder> {

    private List<AProcedure> list;
    private final Resources resources;

    public UserProcsAdapter(List<AProcedure> list, Resources resources) {
        this.list = new ArrayList<>(list);
        this.resources = resources;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ProcItemBinding binding;

        public ViewHolder(@NonNull ProcItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void changeList(List<AProcedure> list) {
        List<AProcedure> oldList = this.list;
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
        AProcedure aproc = list.get(position);
        holder.binding.procTitle.setText(aproc.getProcedure().getName());
        holder.binding.procInst.setText(aproc.getPublicInstitution().getName());
        String status = "";
        switch (aproc.getStatus()){
            case OPEN:
                status = (int) (aproc.getProgress() * 100) +" %";//String.format(resources.getString(R.string.percent),aproc.getProgress()*100);
                break;
            case FINISHED:
                status = resources.getString(R.string.finished);
                break;
            case OLD_WITHOUT_FINISH:
                status = resources.getString(R.string.expired);
                break;
        }
        holder.binding.procState.setText(status);
        holder.binding.procCard.setOnClickListener(v -> {
            UserFragmentDirections.ActionUserFragmentToInitProcDialogFragment action =
                    UserFragmentDirections.actionUserFragmentToInitProcDialogFragment(aproc.getIdProcedure(),aproc.getVersionProcedure(),aproc.getPublicInstitution().getID());
            Navigation.findNavController(v).navigate(action);
        });
        holder.binding.procShadow.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
