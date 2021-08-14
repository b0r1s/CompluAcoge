package gal.boris.compluacoge.ui.adapters;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import gal.boris.compluacoge.databinding.FullImageItemBinding;

public class FullImagesAdapter extends RecyclerView.Adapter<FullImagesAdapter.ViewHolder> {

    private final Uri[] uriList;
    private final Fragment fragment;

    public FullImagesAdapter(Uri[] uriList, Fragment fragment) {
        this.uriList = uriList;
        this.fragment = fragment;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FullImageItemBinding binding;

        public ViewHolder(@NonNull FullImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FullImageItemBinding binding = FullImageItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //RECUERDA, las tarjetas se reciclan para otros
        Uri uri = uriList[position];
        Glide.with(fragment.requireContext()).load(uri).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.binding.fiProgressBar.setVisibility(View.GONE);
                holder.binding.fiErrorText.setVisibility(View.VISIBLE);
                return false;
            }
            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.binding.fiProgressBar.setVisibility(View.GONE);
                return false;
            }
        }).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.fiImage);
    }

    @Override
    public int getItemCount() {
        return uriList.length;
    }
}