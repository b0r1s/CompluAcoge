package gal.boris.compluacoge.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import gal.boris.compluacoge.databinding.ImageItemBinding;
import gal.boris.compluacoge.ui.CreateProcFragment;
import gal.boris.compluacoge.ui.CreateProcFragmentDirections;
import gal.boris.compluacoge.ui.ViewProcFragment;
import gal.boris.compluacoge.ui.ViewProcFragmentDirections;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private List<Uri> uriList;
    private final Fragment fragment;
    private final RecyclerView recyclerView;
    private final boolean editable;

    public ImagesAdapter(List<Uri> uriList, Fragment fragment, RecyclerView recyclerView, boolean editable) {
        this.uriList = uriList;
        this.fragment = fragment;
        this.recyclerView = recyclerView;
        this.editable = editable;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageItemBinding binding;

        public ViewHolder(@NonNull ImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void addUri(Uri uri) {
        uriList.add(uri);
        notifyItemInserted(uriList.size());
    }

    private void removeUri(int position) {
        uriList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,uriList.size()-position);
        recyclerView.setVisibility(uriList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void changeList(List<Uri> list) {
        this.uriList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageItemBinding binding = ImageItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //RECUERDA, las tarjetas se reciclan para otros
        Uri uri = uriList.get(position);
        if(editable) {
            holder.binding.imageBadge.setOnClickListener(v -> {
                removeUri(position);
            });
            holder.binding.imageBadge.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imageBadge.setOnClickListener(null);
            holder.binding.imageBadge.setVisibility(View.GONE);
        }
        Glide.with(fragment.requireContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.imageImage);
        holder.binding.imageImage.setOnClickListener(v -> {
            if(fragment instanceof CreateProcFragment) {
                CreateProcFragmentDirections.ActionCreateProcFragmentToFullImageFragment action =
                        CreateProcFragmentDirections.actionCreateProcFragmentToFullImageFragment(uriList.toArray(new Uri[0]),position);
                NavHostFragment.findNavController(fragment).navigate(action);
            } else if(fragment instanceof ViewProcFragment){
                ViewProcFragmentDirections.ActionViewProcFragmentToFullImageFragment action =
                        ViewProcFragmentDirections.actionViewProcFragmentToFullImageFragment(uriList.toArray(new Uri[0]),position);
                NavHostFragment.findNavController(fragment).navigate(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }
}