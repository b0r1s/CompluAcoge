package gal.boris.compluacoge.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import gal.boris.compluacoge.databinding.SearchItemBinding;
import gal.boris.compluacoge.logic.CloudDB;
import gal.boris.compluacoge.logic.search.ProcSummary;
import gal.boris.compluacoge.ui.SearchFragmentDirections;

public class SearchListAdapter extends ListAdapter<ProcSummary, SearchListAdapter.ViewHolder> {

    private final Fragment fragment;

    public SearchListAdapter(Fragment fragment) {
        super(DIFF_CALLBACK);
        this.fragment = fragment;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final SearchItemBinding binding;

        public ViewHolder(@NonNull SearchItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindTo(ProcSummary proc) {
            binding.searchTitle.setText(proc.getTitle());
            binding.searchDesc.setText(proc.getNameInst());
            StorageReference profileRef = FirebaseStorage.getInstance().getReference().child("images/"+proc.getUidInst()+"/"+ CloudDB.PROFILE_FILE_NAME);
            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(fragment.requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.searchImage);
            });
            binding.searchView.setOnClickListener(v -> {
                SearchFragmentDirections.ActionSearchFragmentToInitProcDialogFragment action =
                        SearchFragmentDirections.actionSearchFragmentToInitProcDialogFragment(proc.getProcId(),proc.getProcVersion(),proc.getUidInst());
                Navigation.findNavController(v).navigate(action);
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SearchItemBinding binding = SearchItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

    private static final DiffUtil.ItemCallback<ProcSummary> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ProcSummary>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull ProcSummary oldUser, @NonNull ProcSummary newUser) {
                    return oldUser.getIdentifier().equals(newUser.getIdentifier());
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull ProcSummary oldUser, @NonNull ProcSummary newUser) {
                    return oldUser.equals(newUser);
                }
            };
}