package gal.boris.compluacoge.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.Node;
import gal.boris.compluacoge.data.RepaintFragment;
import gal.boris.compluacoge.data.objects.Step;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.ViewHolder> {

    private List<Node<Step>> cpyList;
    private final RepaintFragment fragment;
    private final boolean longItem;

    public StepsAdapter(List<Node<Step>> list, RepaintFragment fragment, boolean longItem) {
        this.cpyList = new ArrayList<>(list);
        this.fragment = fragment;
        this.longItem = longItem;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView title;
        private final MaterialTextView desc;
        private final MaterialCardView card;
        private final MaterialButton flag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.step_title);
            desc = itemView.findViewById(R.id.step_desc);
            card = itemView.findViewById(R.id.step_card);
            flag = itemView.findViewById(R.id.step_flag);
        }
    }

    public void addStep(Node<Step> newStep) {
        cpyList.add(newStep);
        notifyDataSetChanged();
    }

    public void changeList(List<Node<Step>> list) {
        this.cpyList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void notifyStep(Node<Step> step) { //todo
        int index = cpyList.indexOf(step);
        if(index != -1) {
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(longItem ? R.layout.step_long_item : R.layout.step_short_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //RECUERDA, las tarjetas se reciclan para otros
        Node<Step> step = cpyList.get(position);
        holder.title.setText(step.getNode().getName());
        holder.desc.setText(step.getNode().getDescription());
        holder.card.setOnClickListener(v -> {
            fragment.repaint(step);
        });
        holder.flag.setVisibility(step.isEnd() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return cpyList.size();
    }
}