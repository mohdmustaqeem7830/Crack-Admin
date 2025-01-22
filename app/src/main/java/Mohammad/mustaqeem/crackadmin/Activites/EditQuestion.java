package Mohammad.mustaqeem.crackadmin.Activites;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import Mohammad.mustaqeem.crackadmin.Adapters.EditQuestionAdapter;
import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityEditQuestionBinding;

public class EditQuestion extends AppCompatActivity {
    String[] categoryArray;
    String[] studyArray;
    String[] subCategoryArray;
    FirebaseFirestore database;
    String[] qpArray;

    String[] typeArray;

    String catId;

    String categoryName,subCategoryName,studyCategoryName,subjectName,qpname,qtype;

    ArrayList<AddCategoryModel> categoriesList;
    ArrayList<AddSubCategoryModel> subCategoriesList;
    ArrayList<AddQuestionPaperModel> qplist;
    ArrayList<Question> qlist;
    ActivityEditQuestionBinding binding;
    ProgressDialog dialog;

    EditQuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityEditQuestionBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = FirebaseFirestore.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading Question Paper");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        categoriesList = new ArrayList<>();
        subCategoriesList = new ArrayList<>();
        qplist = new ArrayList<>();
        qlist =  new ArrayList<>();
        getCategoryList();
        getStudyCategory();
        getTypeList();

        binding.categoryName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categoryName = binding.categoryName.getText().toString();
                getSubCategory(categoryName);
            }
        });

        binding.subcategoryName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subCategoryName = binding.subcategoryName.getText().toString();
            }
        });

        binding.studyCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                if (studyCategoryName.equals("Mock Subjectwise")){
                       binding.selecter4.setVisibility(View.VISIBLE);
                       getSubjectList();
                }else{
                    getQuestionPaperList(categoryName, subCategoryName, studyCategoryName);
                }
            }
        });

        binding.subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subjectName = binding.subject.getText().toString();
                getSubjectQuestionPaperList();
            }
        });
        binding.qpName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                qpname = binding.qpName.getText().toString();
            }
        });



        binding.getQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                qpname = binding.qpName.getText().toString();
                qtype = binding.qtype.getText().toString();
                qpname = binding.qpName.getText().toString();
                subjectName = binding.subject.getText().toString();





                if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qtype.isEmpty() ||qpname.isEmpty()){
                    Toast.makeText(EditQuestion.this, "Please Fill all the field", Toast.LENGTH_SHORT).show();
                }
                else{
                    if (!subjectName.isEmpty()){
                        binding.categoryName.setVisibility(View.GONE);
                        binding.subcategoryName.setVisibility(View.GONE);
                        binding.studyCategory.setVisibility(View.GONE);
                        binding.questionType.setVisibility(View.GONE);
                        binding.qpName.setVisibility(View.GONE);
                        binding.subject.setVisibility(View.GONE);
                        dialog.show();
                        getSubjectQuestionList();
                    }else{
                        getQuestionList();
                        binding.categoryName.setVisibility(View.GONE);
                        binding.subcategoryName.setVisibility(View.GONE);
                        binding.studyCategory.setVisibility(View.GONE);
                        binding.questionType.setVisibility(View.GONE);
                        binding.qpName.setVisibility(View.GONE);
//                        binding.getQuestion.setVisibility(View.GONE);
                        dialog.show();
                    }

                }
            }
        });


    }



    public void getCategoryList() {
        database.collection("categories")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        categoriesList.clear();
                        List<String> cat = new ArrayList<>();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            AddCategoryModel model = snapshot.toObject(AddCategoryModel.class);
                            model.setCatId(snapshot.getId());
                            categoriesList.add(model);
                            cat.add(model.getcategoryName());
                        }
                        categoryArray = cat.toArray(new String[0]);
                        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, categoryArray);
                        binding.categoryName.setAdapter(classAdapter);

                    }
                });
    }
    public void getSubjectQuestionPaperList() {
        // Get the category ID

        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(EditQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Get the sub-category ID
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(EditQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String subcatId = queryDocumentSnapshots1.getDocuments().get(0).getId();

                                // Get the question papers
                                database.collection("categories").document(catId).collection("subCategories").document(subcatId)
                                        .collection(studyCategoryName).whereEqualTo("subjectName",subjectName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                String subjectId =  queryDocumentSnapshots.getDocuments().get(0).getId();
                                                database.collection("categories").document(catId)
                                                        .collection("subCategories").document(subcatId)
                                                        .collection(studyCategoryName).document(subjectId)
                                                        .collection("subject_question_paper").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                qplist.clear();
                                                                List<String> list = new ArrayList<>();
                                                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                                    AddQuestionPaperModel model = snapshot.toObject(AddQuestionPaperModel.class);
                                                                    if (model != null) {
                                                                        model.setQpId(snapshot.getId());
                                                                        qplist.add(model);
                                                                        list.add(model.getQpName());
                                                                    }
                                                                }
                                                                qpArray = list.toArray(new String[0]);
                                                                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, qpArray);
                                                                binding.qpName.setAdapter(categoryAdapter);

                                                            }
                                                        });
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(EditQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(EditQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }
    private void getSubCategory(String categoryName) {
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String catId = snapshot.getId();

                            database.collection("categories").document(catId).collection("subCategories").get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            List<String> subList = new ArrayList<>();
                                            subCategoriesList.clear();

                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                for (DocumentSnapshot snapshot1 : queryDocumentSnapshots.getDocuments()) {
                                                    AddSubCategoryModel submodel = snapshot1.toObject(AddSubCategoryModel.class);
                                                    if (submodel != null) {
                                                        submodel.setSubCategoryId(snapshot1.getId());
                                                        subCategoriesList.add(submodel);
                                                        subList.add(submodel.getSubCategoryName());
                                                    }
                                                }

                                                subCategoryArray = subList.toArray(new String[0]);
                                                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, subCategoryArray);
                                                binding.subcategoryName.setAdapter(classAdapter);
                                            } else {
                                                Toast.makeText(EditQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getStudyCategory() {
        List<String> cat = new ArrayList<>();
        cat.add("Check Paper");
        cat.add("Mock PYQ");
        cat.add("Mock Topicwise");
        cat.add("Mock Subjectwise");
        cat.add("Mock Full length");

        studyArray = cat.toArray(new String[0]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, studyArray);
        binding.studyCategory.setAdapter(classAdapter);
    }

    public void getQuestionPaperList(String categoryName, String subCategoryName, String studyCategoryName) {
        // Get the category ID

        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(EditQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Get the sub-category ID
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(EditQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String subcatId = queryDocumentSnapshots1.getDocuments().get(0).getId();

                                // Get the question papers
                                database.collection("categories").document(catId).collection("subCategories").document(subcatId)
                                        .collection(studyCategoryName).get()
                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                            if (!queryDocumentSnapshots2.isEmpty()) {
                                                qplist.clear();
                                                List<String> list = new ArrayList<>();
                                                for (DocumentSnapshot snapshot : queryDocumentSnapshots2.getDocuments()) {
                                                    AddQuestionPaperModel model = snapshot.toObject(AddQuestionPaperModel.class);
                                                    if (model != null) {
                                                        model.setQpId(snapshot.getId());
                                                        qplist.add(model);
                                                        list.add(model.getQpName());
                                                    }
                                                }
                                                qpArray = list.toArray(new String[0]);
                                                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, qpArray);
                                                binding.qpName.setAdapter(categoryAdapter);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(EditQuestion.this, "Failed to fetch question papers", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(EditQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(EditQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void getTypeList() {
        typeArray = new String[3];
        List<String> type = new ArrayList<>();
        type.add("MCQ");
        type.add("MSQ");
        type.add("OCQ");
        typeArray = type.toArray(new String[0]);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, typeArray);
        binding.qtype.setAdapter(categoryAdapter);

    }
    public void getQuestionList() {


            CollectionReference categoriesRef = database.collection("categories");

            // Fetch category document
            categoriesRef.whereEqualTo("categoryName", categoryName).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            showToastAndDismissDialog("No categories found");
                            return;
                        }

                        String catId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        CollectionReference subCategoriesRef = categoriesRef.document(catId).collection("subCategories");

                        // Fetch sub-category document
                        subCategoriesRef.whereEqualTo("subCategoryName", subCategoryName).get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (queryDocumentSnapshots1.isEmpty()) {
                                        showToastAndDismissDialog("No sub-categories found");
                                        return;
                                    }

                                    String subcatId = queryDocumentSnapshots1.getDocuments().get(0).getId();
                                    CollectionReference questionsRef = subCategoriesRef.document(subcatId)
                                            .collection(studyCategoryName);

                                    // Fetch question papers
                                    questionsRef.whereEqualTo("qpName", qpname).get()
                                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                if (queryDocumentSnapshots2.isEmpty()) {
                                                    showToastAndDismissDialog("No question papers found");
                                                    return;
                                                }

                                                String qpId = queryDocumentSnapshots2.getDocuments().get(0).getId();
                                                CollectionReference questionDocsRef = questionsRef.document(qpId).collection("questions");

                                                // Fetch questions
                                                questionDocsRef.get().addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                    if (queryDocumentSnapshots3.isEmpty()) {
                                                        showToastAndDismissDialog("No matching questions found.");
                                                        return;
                                                    }

                                                    // Populate the questions list
                                                    qlist.clear();
                                                    for (DocumentSnapshot questionDoc : queryDocumentSnapshots3.getDocuments()) {
                                                        Question question = questionDoc.toObject(Question.class);
                                                        if (question != null) {
                                                            question.setqId(questionDoc.getId());
                                                            qlist.add(question);
                                                        }
                                                    }

                                                    // Update the adapter
                                                    adapter = new EditQuestionAdapter(EditQuestion.this, qlist, categoryName, subCategoryName,studyCategoryName,null,qpname);
                                                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(EditQuestion.this));
                                                    binding.recyclerView.setAdapter(adapter);
                                                    adapter.notifyDataSetChanged();

                                                    dialog.dismiss();
                                                    // Handle the populated questionArrayList here
                                                }).addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch questions"));

                                            }).addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch question papers"));

                                }).addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch sub-categories"));

                    }).addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch categories"));
        }



// Utility method to show a toast message and dismiss the dialog
private void showToastAndDismissDialog(String message) {
    Toast.makeText(EditQuestion.this, message, Toast.LENGTH_SHORT).show();
    dialog.dismiss();
}

    public void getSubjectList() {
        // Fetch categories
        database.collection("categories")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showToast("No categories found");
                        return;
                    }

                    // Get category ID
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Fetch sub-categories
                    database.collection("categories").document(catId)
                            .collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName)
                            .get()
                            .addOnSuccessListener(subCategorySnapshots -> {
                                if (subCategorySnapshots.isEmpty()) {
                                    showToast("No sub-categories found");
                                    dialog.dismiss();
                                    return;
                                }

                                // Get sub-category ID
                                String subcatId = subCategorySnapshots.getDocuments().get(0).getId();

                                // Fetch subjects
                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subcatId)
                                        .collection(studyCategoryName).get()
                                        .addOnSuccessListener(subjectSnapshots -> {
                                            List<String> subjectList = new ArrayList<>();
                                            for (DocumentSnapshot snapshot : subjectSnapshots.getDocuments()) {
                                                Subject subject = snapshot.toObject(Subject.class);
                                                if (subject != null) {
                                                    subjectList.add(subject.getSubjectName());
                                                }
                                            }

                                            if (subjectList.isEmpty()) {
                                                showToast("No subjects found");
                                            } else {
                                                updateSubjectSpinner(subjectList);
                                            }

                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            showToast("Failed to fetch subjects");
                                            dialog.dismiss();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                showToast("Failed to fetch sub-categories");
                                dialog.dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to fetch categories");
                    dialog.dismiss();
                });
    }

    private void showToast(String message) {
        Toast.makeText(EditQuestion.this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateSubjectSpinner(List<String> subjects) {
        String[] subjectArray = subjects.toArray(new String[0]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, subjectArray);
        binding.subject.setAdapter(classAdapter);
    }


    private void getSubjectQuestionList() {
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(EditQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Fetch sub-categories
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(EditQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String subcatId = queryDocumentSnapshots1.getDocuments().get(0).getId();

                                // Fetch question papers
                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subcatId)
                                        .collection(studyCategoryName).whereEqualTo("subjectName", subjectName).get()
                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                            if (queryDocumentSnapshots2.isEmpty()) {
                                                dialog.dismiss();
                                                Toast.makeText(EditQuestion.this, "No question papers found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            String subjectId = queryDocumentSnapshots2.getDocuments().get(0).getId();

                                            // Fetch specific question paper
                                            database.collection("categories").document(catId)
                                                    .collection("subCategories").document(subcatId)
                                                    .collection(studyCategoryName).document(subjectId)
                                                    .collection("subject_question_paper").whereEqualTo("qpName", qpname).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            String qpID = queryDocumentSnapshots.getDocuments().get(0).getId();
                                                            database.collection("categories").document(catId)
                                                                    .collection("subCategories").document(subcatId)
                                                                    .collection(studyCategoryName).document(subjectId)
                                                                    .collection("subject_question_paper").document(qpID)
                                                                    .collection("questions").orderBy("index").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots3) {
                                                                            qlist.clear();
                                                                            for (DocumentSnapshot questionDoc : queryDocumentSnapshots3.getDocuments()) {
                                                                                Question question = questionDoc.toObject(Question.class);
                                                                                if (question != null) {
                                                                                    question.setqId(questionDoc.getId());
                                                                                    qlist.add(question);
                                                                                }
                                                                            }

                                                                            // Update the adapter
                                                                            adapter = new EditQuestionAdapter(EditQuestion.this, qlist, categoryName, subCategoryName,studyCategoryName,subjectName,qpname);
                                                                            binding.recyclerView.setLayoutManager(new LinearLayoutManager(EditQuestion.this));
                                                                            binding.recyclerView.setAdapter(adapter);
                                                                            adapter.notifyDataSetChanged();

                                                                            dialog.dismiss();

                                                                        }
                                                                    });
                                                        }
                                                    });

                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(EditQuestion.this, "Failed to fetch study categories", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(EditQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(EditQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }

}

