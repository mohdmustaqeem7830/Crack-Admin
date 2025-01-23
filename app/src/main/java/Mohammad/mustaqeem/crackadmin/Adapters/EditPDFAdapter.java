package Mohammad.mustaqeem.crackadmin.Adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import Mohammad.mustaqeem.crackadmin.databinding.PdflistItemBinding;

public class EditPDFAdapter extends RecyclerView.Adapter<EditPDFAdapter.EditPDFViewHolder> {


    @NonNull
    @Override
    public EditPDFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull EditPDFViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class EditPDFViewHolder extends RecyclerView.ViewHolder {

        PdflistItemBinding binding;
        public EditPDFViewHolder(@NonNull View itemView) {
            super(itemView);

            binding= PdflistItemBinding.bind(itemView);


        }
    }
}
