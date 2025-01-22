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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import Mohammad.mustaqeem.crackadmin.Activites.AddQuestion;
import Mohammad.mustaqeem.crackadmin.Activites.AddSubjectQuestion;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.QuestionListItemsBinding;
public class EditQuestionAdapter extends RecyclerView.Adapter<EditQuestionAdapter.EditViewHolder> {

    Context context;

    ArrayList<Question> questionArrayList;
    String categoryName,subCategoryName,studyCategoryName,subject,qpname;

    FirebaseFirestore database;

    ProgressDialog dialog;

     public EditQuestionAdapter(Context context, ArrayList<Question> questionArrayList, String categoryName,String subCategoryName,String studyCategoryName, String subject,String qpname){
         this.context = context;
         this.questionArrayList = questionArrayList;
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
    public EditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(Mohammad.mustaqeem.crackadmin.R.layout.question_list_items,parent,false);
        return  new EditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditViewHolder holder, int position) {

         Question question = questionArrayList.get(position);
                  holder.binding.qindex.setText(String.valueOf(question.getIndex()));
                  holder.binding.qname.setText(question.getQuestion());

                  holder.binding.qDeleteBtn.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          dialog.show();
                          if (subject!=null){
                              DeleteSubjectQuestion(question.getqId(),position);
                          }else{
                              Deletquestion(question.getqId(),position);

                          }
                      }
                  });

                  holder.binding.editBtn.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          if(subject!=null){
                              Intent intent = new Intent(context, AddSubjectQuestion.class);
                              intent.putExtra("categoryName",categoryName);
                              intent.putExtra("subCategoryName",subCategoryName);
                              intent.putExtra("studyCategoryName",studyCategoryName);
                              intent.putExtra("qpName",qpname);
                              intent.putExtra("subject",subject);
                              intent.putExtra("qId",question.getqId());
                              context.startActivity(intent);
                          }else{
                              Intent intent = new Intent(context, AddQuestion.class);
                              intent.putExtra("categoryName",categoryName);
                              intent.putExtra("subCategoryName",subCategoryName);
                              intent.putExtra("studyCategoryName",studyCategoryName);
                              intent.putExtra("qpName",qpname);
                              intent.putExtra("qId",question.getqId());
                              context.startActivity(intent);
                          }

                      }
                  });
    }



    @Override
    public int getItemCount() {
        return questionArrayList.size();
    }

    public class EditViewHolder extends RecyclerView.ViewHolder {
        QuestionListItemsBinding binding;
        public EditViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = QuestionListItemsBinding.bind(itemView);
        }
    }




    private void Deletquestion(String qID, int position) {
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
                                                .collection(studyCategoryName).whereEqualTo("qpName", qpname).get()
                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                    if (!queryDocumentSnapshots2.isEmpty()) {
                                                        String subjectId = queryDocumentSnapshots2.getDocuments().get(0).getId();

                                                        // Reference to the question document
                                                        database.collection("categories").document(catId)
                                                                .collection("subCategories").document(subCatId)
                                                                .collection(studyCategoryName).document(subjectId)
                                                                .collection("questions").document(qID).get()
                                                                .addOnSuccessListener(documentSnapshot -> {
                                                                    if (documentSnapshot.exists()) {
                                                                        Question question = documentSnapshot.toObject(Question.class);
                                                                        String imageUrl = question.getqImage();

                                                                        if (imageUrl != null) {
                                                                            // Delete image from Firebase Storage
                                                                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                                                            imageRef.delete().addOnSuccessListener(aVoid -> {
                                                                                // After image deletion, delete the question document
                                                                                deleteQuestionDocument(catId, subCatId, studyCategoryName, subjectId, qID, position);
                                                                            }).addOnFailureListener(e -> {
                                                                                dialog.dismiss();
                                                                                Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                                                                deleteQuestionDocument(catId, subCatId, studyCategoryName, subjectId, qID, position);
                                                                            });
                                                                        } else {
                                                                            // Directly delete the question document if no image URL
                                                                            deleteQuestionDocument(catId, subCatId, studyCategoryName, subjectId, qID, position);
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
                                                        Toast.makeText(context, "Subject not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(e -> {
                                                    dialog.dismiss();
                                                    Toast.makeText(context, "Failed to retrieve subject", Toast.LENGTH_SHORT).show();
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
    private void deleteQuestionDocument(String catId, String subCatId, String studyCategoryName, String subjectId, String qID, int position) {
        database.collection("categories").document(catId)
                .collection("subCategories").document(subCatId)
                .collection(studyCategoryName).document(subjectId)
                .collection("questions").document(qID).delete()
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    questionArrayList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Question deleted successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, update UI or notify user
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Failed to delete question", Toast.LENGTH_SHORT).show();
                });
    }

    private void DeleteSubjectQuestion(String qID, int position) {

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
                                                    .collection("subject_question_paper").whereEqualTo("qpName", qpname).get()
                                                    .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                        if (queryDocumentSnapshots3.isEmpty()) {
                                                            dialog.dismiss();
                                                            Toast.makeText(context, "No question papers found", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }
                                                        String qpID = queryDocumentSnapshots3.getDocuments().get(0).getId();
                                                        DocumentReference qpDocRef = database.collection("categories").document(catId)
                                                                .collection("subCategories").document(subcatId)
                                                                .collection(studyCategoryName).document(subjectId)
                                                                .collection("subject_question_paper").document(qpID);

                                                        // Fetch the question document
                                                        qpDocRef.collection("questions").document(qID).get().addOnSuccessListener(documentSnapshot -> {
                                                            if (documentSnapshot.exists()) {
                                                                Question question = documentSnapshot.toObject(Question.class);
                                                                String imageUrl =  question.getqImage();

                                                                if (imageUrl != null) {
                                                                    // Delete image from Firebase Storage
                                                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                                                    imageRef.delete().addOnSuccessListener(aVoid -> {
                                                                        // After image deletion, delete the question document
                                                                        deleteQuestionDocument(qpDocRef, qID, position);
                                                                    }).addOnFailureListener(e -> {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                                                        deleteQuestionDocument(qpDocRef, qID, position);
                                                                    });
                                                                } else {
                                                                    deleteQuestionDocument(qpDocRef, qID, position);
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
                                                        Toast.makeText(context, "Failed to fetch question papers", Toast.LENGTH_SHORT).show();
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

    // Method to delete the question document and update the RecyclerView
    private void deleteQuestionDocument(DocumentReference qpDocRef, String qID, int position) {
        qpDocRef.collection("questions").document(qID).delete().addOnSuccessListener(unused -> {
            dialog.dismiss();
            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();

            questionArrayList.remove(position);
            notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(context, "Failed to delete question", Toast.LENGTH_SHORT).show();
        });
    }



}
