package Mohammad.mustaqeem.crackadmin.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import Mohammad.mustaqeem.crackadmin.Activites.Banner;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.BannerItemBinding;

public class BannerAdapter  extends RecyclerView.Adapter<BannerAdapter.bannerViewHolder> {

    List<String> bannerList;
    String catId,subId;
    Context context;

    FirebaseFirestore database;

    ProgressDialog dialog;

    public BannerAdapter(Context context, List<String> bannerList, String catId, String subId){
        this.catId = catId;
        this.subId = subId;
        this.bannerList = bannerList;
        this.context = context;
        this.database = FirebaseFirestore.getInstance();
        this.dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait");
        dialog.setTitle("Deleting Banner");
        dialog.setCancelable(false);
    }
    @NonNull
    @Override
    public bannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner_item,parent,false);
        return  new BannerAdapter.bannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull bannerViewHolder holder, int position) {
        Glide.with(context).load(bannerList.get(position)).into(holder.binding.image);

        holder.binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                deleteBannerImage(bannerList.get(position),position);
            }
        });
    }

    private void deleteBannerImage(String url,int position) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);

        // Delete the file
        storageReference.delete().addOnSuccessListener(aVoid -> {
            deleteDataFromFirestore(url,position);
        }).addOnFailureListener(e -> {
            // Failed to delete
            Toast.makeText(context, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteDataFromFirestore(String url,int position) {
        database.collection("categories").document(catId).collection("subCategories").document(subId).collection("Banner")
                .whereEqualTo("bannerImageUrl",url).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            String objectId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            database.collection("categories").document(catId).collection("subCategories").document(subId).collection("Banner")
                                    .document(objectId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialog.dismiss();
                                            bannerList.remove(position);
                                            notifyDataSetChanged();
                                            Toast.makeText(context, "Delete Banner Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public class bannerViewHolder extends RecyclerView.ViewHolder {

        BannerItemBinding binding ;
        public bannerViewHolder(@NonNull View itemView) {

            super(itemView);
            binding = BannerItemBinding.bind(itemView);

        }
    }



}
