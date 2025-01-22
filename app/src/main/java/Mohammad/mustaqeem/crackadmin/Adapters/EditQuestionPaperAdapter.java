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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import Mohammad.mustaqeem.crackadmin.Activites.AddQuestion;
import Mohammad.mustaqeem.crackadmin.Activites.AddSubjectQuestion;
import Mohammad.mustaqeem.crackadmin.Activites.addQuestionPaper;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.databinding.QuestionListItemsBinding;

public class EditQuestionPaperAdapter extends RecyclerView.Adapter<EditQuestionPaperAdapter.QuestionPaperViewHolder> {

    Context context;

    ArrayList<AddQuestionPaperModel> questionpaperArrayList;
    String categoryName,subCategoryName,studyCategoryName,subject,qpname;

    FirebaseFirestore database;

    ProgressDialog dialog;

    public EditQuestionPaperAdapter(Context context, ArrayList<AddQuestionPaperModel> questionPaperArrayList, String categoryName, String subCategoryName, String studyCategoryName, String subject){
        this.context = context;
        this.questionpaperArrayList = questionPaperArrayList;
        this.qpname= qpname;
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
        this.studyCategoryName = studyCategoryName;
        this.database = FirebaseFirestore.getInstance();

        this.dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait");
        dialog.setTitle("Deleting Question");
        dialog.setCancelable(false);
        this.subject = subject;



    }

    @NonNull
    @Override
    public QuestionPaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(Mohammad.mustaqeem.crackadmin.R.layout.question_list_items,parent,false);
        return  new EditQuestionPaperAdapter.QuestionPaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionPaperViewHolder holder, int position) {
        AddQuestionPaperModel questionPaperModel = questionpaperArrayList.get(position);
        holder.binding.qindex.setText(String.valueOf(position+1));
        holder.binding.qname.setText(questionPaperModel.getQpName());

        holder.binding.qDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                if (subject!=null){
                    DeleteSubjectQuestionPaper(questionPaperModel.getQpId(),position);
                }else{
                    DeletquestionPaper(questionPaperModel.getQpId(),position);

                }
            }
        });

        holder.binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subject!=null){
                    Intent intent = new Intent(context, addQuestionPaper.class);
                    intent.putExtra("categoryName",categoryName);
                    intent.putExtra("subCategoryName",subCategoryName);
                    intent.putExtra("studyCategoryName",studyCategoryName);
                    intent.putExtra("subject",subject);
                    intent.putExtra("qpId", questionPaperModel.getQpId());
                    context.startActivity(intent);
                }else{
                    Intent intent = new Intent(context, addQuestionPaper.class);
                    intent.putExtra("categoryName",categoryName);
                    intent.putExtra("subCategoryName",subCategoryName);
                    intent.putExtra("studyCategoryName",studyCategoryName);
                    intent.putExtra("qId",questionPaperModel.getQpId());
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return questionpaperArrayList.size();
    }


    public class QuestionPaperViewHolder extends RecyclerView.ViewHolder {

        QuestionListItemsBinding binding;
        public QuestionPaperViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = QuestionListItemsBinding.bind(itemView);
        }
    }


    private void DeletquestionPaper(String qID, int position) {
        // Retrieve the category ID
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Retrieve the subcategory ID
                        database.collection("categories").document(catId)
                                .collection("subCategories").whereEqualTo("subCategoryName", subCategoryName).get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.isEmpty()) {
                                        String subCatId = queryDocumentSnapshots1.getDocuments().get(0).getId();

                                        // Retrieve the subject ID
                                        database.collection("categories").document(catId)
                                                .collection("subCategories").document(subCatId)
                                                .collection(studyCategoryName).document(qID).get()
                                                                .addOnSuccessListener(documentSnapshot -> {
                                                                    if (documentSnapshot.exists()) {
                                                                        Question question = documentSnapshot.toObject(Question.class);
                                                                        String imageUrl = question.getqImage();

                                                                        if (imageUrl != null) {
                                                                            // Delete image from Firebase Storage
                                                                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                                                            imageRef.delete().addOnSuccessListener(aVoid -> {
                                                                                // After image deletion, delete the question document
                                                                                deleteQuestionDocument(catId, subCatId, studyCategoryName, qID, position);
                                                                            }).addOnFailureListener(e -> {
                                                                                dialog.dismiss();
                                                                                Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                                                                deleteQuestionDocument(catId, subCatId, studyCategoryName, qID, position);
                                                                            });
                                                                        } else {
                                                                            // Directly delete the question document if no image URL
                                                                            deleteQuestionDocument(catId, subCatId, studyCategoryName, qID, position);
                                                                        }
                                                                    } else {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(context, "Question not found", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(e -> {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(context, "Failed to retrieve question", Toast.LENGTH_SHORT).show();
                                                                });
                                                    } else {
                                        dialog.dismiss();
                                        Toast.makeText(context, "Subcategory not found", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(e -> {
                                    dialog.dismiss();
                                    Toast.makeText(context, "Failed to retrieve subcategory", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, "Category not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Failed to retrieve category", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper function to delete the question document
    private void deleteQuestionDocument(String catId, String subCatId, String studyCategoryName, String qID, int position) {
        database.collection("categories").document(catId)
                .collection("subCategories").document(subCatId)
                .collection(studyCategoryName).document(qID).delete()
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    questionpaperArrayList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Question Paper deleted successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, update UI or notify user
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Failed to delete question", Toast.LENGTH_SHORT).show();
                });
    }

    private void DeleteSubjectQuestionPaper(String qID, int position) {

        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(context, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Fetch sub-categories
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(context, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String subcatId = queryDocumentSnapshots1.getDocuments().get(0).getId();

                                // Fetch question papers
                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subcatId)
                                        .collection(studyCategoryName).whereEqualTo("subjectName", subject).get()
                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                            if (queryDocumentSnapshots2.isEmpty()) {
                                                dialog.dismiss();
                                                Toast.makeText(context, "No question papers found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            String subjectId = queryDocumentSnapshots2.getDocuments().get(0).getId();

                                            // Fetch specific question paper
                                            database.collection("categories").document(catId)
                                                    .collection("subCategories").document(subcatId)
                                                    .collection(studyCategoryName).document(subjectId)
                                                    .collection("subject_question_paper").document(qID).get().addOnSuccessListener(documentSnapshot -> {
                                                            if (documentSnapshot.exists()) {
                                                                Question question = documentSnapshot.toObject(Question.class);
                                                                String imageUrl =  question.getqImage();

                                                                if (imageUrl != null) {
                                                                    // Delete image from Firebase Storage
                                                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                                                    imageRef.delete().addOnSuccessListener(aVoid -> {
                                                                        // After image deletion, delete the question document
                                                                        deleteQuestionDocument(catId,subcatId,studyCategoryName,subjectId, qID, position);
                                                                    }).addOnFailureListener(e -> {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                                                        deleteQuestionDocument(catId,subcatId,studyCategoryName,subjectId, qID, position);
                                                                    });
                                                                } else {
                                                                    deleteQuestionDocument(catId,subcatId,studyCategoryName,subjectId, qID, position);
                                                                }
                                                            } else {
                                                                dialog.dismiss();
                                                                Toast.makeText(context, "Question not found", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }).addOnFailureListener(e -> {
                                                            dialog.dismiss();
                                                            Toast.makeText(context, "Failed to fetch question details", Toast.LENGTH_SHORT).show();
                                                        });

                                                    })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(context, "Failed to fetch study categories", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(context, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper function to delete the question document
    private void deleteQuestionDocument(String catId, String subCatId, String studyCategoryName, String subjectId,String qID, int position) {
        database.collection("categories").document(catId)
                .collection("subCategories").document(subCatId)
                .collection(studyCategoryName).document(subjectId)
                .collection("subject_question_paper").
                document(qID).delete()
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    questionpaperArrayList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Question Paper deleted successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, update UI or notify user
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Failed to delete question", Toast.LENGTH_SHORT).show();
                });
    }

}
