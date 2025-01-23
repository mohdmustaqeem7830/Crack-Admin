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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import Mohammad.mustaqeem.crackadmin.Activites.addQuestionPaper;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.databinding.QuestionListItemsBinding;

public class EditSubjectAdapter  extends  RecyclerView.Adapter<EditSubjectAdapter.SubjectViewHolder>{


    Context context;

    ArrayList<Subject> subjectArrayList;
    String catId,subId,studyCategoryName,categoryName,subCategoryName;

    FirebaseFirestore database;

    ProgressDialog dialog;

    public EditSubjectAdapter(Context context, ArrayList<Subject> subjectArrayList, String catId, String subId, String studyCategoryName,String categoryName,String subCategoryName){
        this.context = context;
        this.subjectArrayList = subjectArrayList;
        this.catId = catId;
        this.subId = subId;
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
        this.studyCategoryName = studyCategoryName;
        this.database = FirebaseFirestore.getInstance();
        this.dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait");
        dialog.setTitle("Deleting Question");
        dialog.setCancelable(false);
    }

    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(Mohammad.mustaqeem.crackadmin.R.layout.question_list_items,parent,false);
        return  new EditSubjectAdapter.SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectArrayList.get(position);
        holder.binding.qindex.setText(String.valueOf(position+1));
        holder.binding.qname.setText(subject.getSubjectName());

        holder.binding.qDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                // Delete the image from Firebase Storage if the URL is not empty
                if (subject.getSubjectImage() != null && !subject.getSubjectImage().isEmpty()) {
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(subject.getSubjectImage());
                    imageRef.delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(context, "Image deleted successfully!", Toast.LENGTH_SHORT).show();
                                DeleteSubject(subject.getSubjectName());
                            });
                }else{
                    DeleteSubject(subject.getSubjectName());
                }

            }
        });

        holder.binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, addQuestionPaper.class);
                intent.putExtra("catId",catId);
                intent.putExtra("categoryName",categoryName);
                intent.putExtra("subCategoryName",subCategoryName);
                intent.putExtra("subId",subId);
                intent.putExtra("studyCategoryName",studyCategoryName);
                intent.putExtra("subject",subject.getSubjectName());
                intent.putExtra("edit","edit");
                context.startActivity(intent);
            }
        });
    }

    private void DeleteSubject(String subjectName) {


        // Get the Firestore reference to the document
        database.collection("categories").document(catId).collection("subCategories").document(subId)
                .collection(studyCategoryName).whereEqualTo("subjectName", subjectName).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Check if the document exists
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Loop through the documents (in case there are multiple matches)
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                // Get the document ID
                                String docId = document.getId();

                                // Delete the document from Firestore
                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subId)
                                        .collection(studyCategoryName).document(docId).delete()
                                        .addOnSuccessListener(unused -> {
                                            dialog.dismiss();
                                            Toast.makeText(context, "Subject deleted successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(context, "Failed to delete subject: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            dialog.dismiss();
                            Toast.makeText(context, "No document found with the given subject name.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Failed to fetch document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public int getItemCount() {
        return subjectArrayList.size();
    }

    public class SubjectViewHolder extends RecyclerView.ViewHolder {

        QuestionListItemsBinding binding;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = QuestionListItemsBinding.bind(itemView);
        }
    }
}
