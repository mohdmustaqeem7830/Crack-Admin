package Mohammad.mustaqeem.crackadmin.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import Mohammad.mustaqeem.crackadmin.Activites.AddPDF;
import Mohammad.mustaqeem.crackadmin.Activites.addQuestionPaper;
import Mohammad.mustaqeem.crackadmin.Model.AddPDFModel;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.PdflistItemBinding;



public class EditPDFAdapter extends RecyclerView.Adapter<EditPDFAdapter.EditPDFViewHolder> {

    Context context;

    ArrayList<AddPDFModel> pdfModelsArrayList;
    String catId,subId,subject,studyCategoryName,categoryName,subCategoryName,subjectId;

    FirebaseFirestore database;

    ProgressDialog dialog;

    public EditPDFAdapter(Context context, ArrayList<AddPDFModel> pdfModelsArrayList,String catId,String subId, String categoryName, String subCategoryName, String studyCategoryName,String subjectId,String subject){
        this.context = context;
        this.pdfModelsArrayList = pdfModelsArrayList;
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
        this.studyCategoryName = studyCategoryName;
        this.database = FirebaseFirestore.getInstance();
        this.catId = catId;
        this.subId = subId;
        this.dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait");
        dialog.setTitle("Deleting Question");
        dialog.setCancelable(false);
        this.subjectId = subjectId;
        this.subject = subject;
    }



    @NonNull
    @Override
    public EditPDFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdflist_item,parent,false);
        return  new EditPDFAdapter.EditPDFViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditPDFViewHolder holder, int position) {
        AddPDFModel pdf = pdfModelsArrayList.get(position);
        holder.binding.pdfName.setText(pdf.getPdfName());
        holder.binding.pdfSubtitle.setText(pdf.getPdfSubName());


        holder.binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subjectId!=null){
                    deleteSubjectPDF(pdf.getPdfUrl(),position);
                }else{
                    deletePDF(pdf.getPdfUrl(),position);
                }
            }
        });

        holder.binding.editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddPDF.class);
                intent.putExtra("catId",catId);
                intent.putExtra("categoryName",categoryName);
                intent.putExtra("subCategoryName",subCategoryName);
                intent.putExtra("subId",subId);
                intent.putExtra("studyCategoryName",studyCategoryName);
                intent.putExtra("subject",subject);
                intent.putExtra("subjectId",subjectId);
                intent.putExtra("pdfUrl",pdf.getPdfUrl());
                intent.putExtra("edit","edit");
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return pdfModelsArrayList.size();
    }

    public class EditPDFViewHolder extends RecyclerView.ViewHolder {

        PdflistItemBinding binding;
        public EditPDFViewHolder(@NonNull View itemView) {
            super(itemView);

            binding= PdflistItemBinding.bind(itemView);


        }
    }


    private void deleteSubjectPDF(String pdfUrl,int position) {
        dialog.show();

        // Create a Storage reference from the URL
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference pdfRef = storage.getReferenceFromUrl(pdfUrl);

        // Delete the file from Storage
        pdfRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // After deleting from Storage, delete from Firestore
                database.collection("categories").document(catId)
                        .collection("subCategories").document(subId)
                        .collection(studyCategoryName).document(subjectId)
                        .collection(subject).whereEqualTo("pdfUrl", pdfUrl)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        document.getReference().delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        dialog.dismiss();
                                                        Toast.makeText(context, "PDF deleted successfully", Toast.LENGTH_SHORT).show();
                                                        pdfModelsArrayList.remove(position); // Update list
                                                        notifyDataSetChanged();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(context, "Failed to delete PDF from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(context, "PDF document not found in Firestore", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(context, "Failed to query Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(context, "Failed to delete file from Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePDF(String pdfUrl,int position) {
        dialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference pdfRef = storage.getReferenceFromUrl(pdfUrl);
        pdfRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // After deleting from Storage, delete from Firestore
                database.collection("categories").document(catId)
                        .collection("subCategories").document(subId)
                        .collection(studyCategoryName).whereEqualTo("pdfUrl", pdfUrl)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        document.getReference().delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        dialog.dismiss();
                                                        Toast.makeText(context, "PDF deleted successfully", Toast.LENGTH_SHORT).show();
                                                        pdfModelsArrayList.remove(position); // Update list
                                                        notifyDataSetChanged();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(context, "Failed to delete PDF from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(context, "PDF document not found in Firestore", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(context, "Failed to query Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(context, "Failed to delete file from Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
