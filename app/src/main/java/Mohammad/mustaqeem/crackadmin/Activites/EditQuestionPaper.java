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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import Mohammad.mustaqeem.crackadmin.Adapters.EditQuestionAdapter;
import Mohammad.mustaqeem.crackadmin.Adapters.EditQuestionPaperAdapter;
import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityEditQuestionBinding;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityEditQuestionPaperBinding;

public class EditQuestionPaper extends AppCompatActivity {
    String[] categoryArray;
    String[] studyArray;
    String[] subCategoryArray;
    FirebaseFirestore database;
    String[] qpArray;

    EditQuestionPaperAdapter adapter;

    String[] typeArray;

    String catId;

    String categoryName,subCategoryName,studyCategoryName,subjectName,qpname,qtype;

    ArrayList<AddCategoryModel> categoriesList;
    ArrayList<AddSubCategoryModel> subCategoriesList;
    ArrayList<AddQuestionPaperModel> qplist;
    ArrayList<Question> qlist;
    ActivityEditQuestionPaperBinding binding;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityEditQuestionPaperBinding.inflate(getLayoutInflater());
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
//                    getQuestionPaperList(categoryName, subCategoryName, studyCategoryName);
                }
            }
        });

        binding.subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subjectName = binding.subject.getText().toString();
//                getSubjectQuestionPaperList();
            }
        });


        binding.getQuestionPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                String category = binding.categoryName.getText().toString();
                String subCategory =binding.subcategoryName.getText().toString();
                String studyCategory = binding.studyCategory.getText().toString();
                getQuestionPaperList(category,subCategory,studyCategory);
                if (binding.subject.getText().toString()==null){
                    getQuestionPaperList(category,subCategory,studyCategory);
                }else{
                    getSubjectQuestionPaperList();
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
                                                Toast.makeText(EditQuestionPaper.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditQuestionPaper.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditQuestionPaper.this, "No categories found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditQuestionPaper.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(EditQuestionPaper.this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateSubjectSpinner(List<String> subjects) {
        String[] subjectArray = subjects.toArray(new String[0]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, subjectArray);
        binding.subject.setAdapter(classAdapter);
    }



    public void getSubjectQuestionPaperList() {
        // Get the category ID

        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(EditQuestionPaper.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Get the sub-category ID
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(EditQuestionPaper.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
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
                                                                    }
                                                                }

                                                                Toast.makeText(EditQuestionPaper.this, String.valueOf(qplist.size()), Toast.LENGTH_SHORT).show();
//                                                                adapter = new EditQuestionPaperAdapter(EditQuestionPaper.this, qplist, categoryName, subCategoryName,studyCategoryName,subjectName);
//                                                                binding.recyclerView.setLayoutManager(new LinearLayoutManager(EditQuestionPaper.this));
//                                                                binding.recyclerView.setAdapter(adapter);
//                                                                adapter.notifyDataSetChanged();
                                                                dialog.dismiss();
                                                            }
                                                        });
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(EditQuestionPaper.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(EditQuestionPaper.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }


    public void getQuestionPaperList(String categoryName, String subCategoryName, String studyCategoryName) {
        // Get the category ID

        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(EditQuestionPaper.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Get the sub-category ID
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(EditQuestionPaper.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
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
                                                    }
                                                }

                                                Toast.makeText(this, String.valueOf(qplist.size()), Toast.LENGTH_SHORT).show();
//                                                adapter = new EditQuestionPaperAdapter(EditQuestionPaper.this, qplist, categoryName, subCategoryName,studyCategoryName,"");
//                                                binding.recyclerView.setLayoutManager(new LinearLayoutManager(EditQuestionPaper.this));
//                                                binding.recyclerView.setAdapter(adapter);
//                                                adapter.notifyDataSetChanged();
                                                dialog.dismiss();

                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(EditQuestionPaper.this, "Failed to fetch question papers", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(EditQuestionPaper.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(EditQuestionPaper.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }




}