package Mohammad.mustaqeem.crackadmin.EditingTools;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Mohammad.mustaqeem.crackadmin.Adapters.EditQuestionAdapter;
import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityEditQuestionBinding;
import Mohammad.mustaqeem.crackadmin.databinding.SetAnswerLayoutBinding;

public class EditQuestion extends AppCompatActivity {
    String[] categoryArray;
    String[] studyArray;
    String[] subCategoryArray;
    FirebaseFirestore database;
    String[] qpArray;

    String[] typeArray;

    String catId, subId, qpId, subjectId;

    String categoryName, subCategoryName, studyCategoryName, subjectName, qpname, qtype;

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
        qlist = new ArrayList<>();
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
                if (studyCategoryName.equals("Mock Subjectwise")) {
                    binding.selecter4.setVisibility(View.VISIBLE);
                    getSubjectList();
                } else {
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


                if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qtype.isEmpty() || qpname.isEmpty()) {
                    Toast.makeText(EditQuestion.this, "Please Fill all the field", Toast.LENGTH_SHORT).show();
                } else {
                    if (!subjectName.isEmpty()) {
                        binding.categoryName.setVisibility(View.GONE);
                        binding.subcategoryName.setVisibility(View.GONE);
                        binding.studyCategory.setVisibility(View.GONE);
                        binding.questionType.setVisibility(View.GONE);
                        binding.qpName.setVisibility(View.GONE);
                        binding.subject.setVisibility(View.GONE);
                        dialog.show();
                        getSubjectQuestionList();
                    } else {
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

        binding.setAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuizDialog(qlist.size());
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


        // Get the question papers
        database.collection("categories").document(catId).collection("subCategories").document(subId)
                .collection(studyCategoryName).whereEqualTo("subjectName", subjectName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        subjectId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        database.collection("categories").document(catId)
                                .collection("subCategories").document(subId)
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

    }

    private void getSubCategory(String categoryName) {
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                            catId = snapshot.getId();

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

        database.collection("categories").document(catId).collection("subCategories")
                .whereEqualTo("subCategoryName", subCategoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                    if (queryDocumentSnapshots1.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(EditQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    subId = queryDocumentSnapshots1.getDocuments().get(0).getId();

                    // Get the question papers
                    database.collection("categories").document(catId).collection("subCategories").document(subId)
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
        // Fetch question papers
        database.collection("categories").document(catId)
                .collection("subCategories").document(subId).collection(studyCategoryName).whereEqualTo("qpName", qpname).get()
                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                    if (queryDocumentSnapshots2.isEmpty()) {
                        showToastAndDismissDialog("No question papers found");
                        return;
                    }

                    qpId = queryDocumentSnapshots2.getDocuments().get(0).getId();
                    database.collection("categories").document(catId)
                            .collection("subCategories").document(subId).collection(studyCategoryName).document(qpId).collection("questions").
                            get().addOnSuccessListener(queryDocumentSnapshots3 -> {
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
                                adapter = new EditQuestionAdapter(EditQuestion.this, qlist, categoryName, subCategoryName, studyCategoryName, null, qpname);
                                binding.recyclerView.setLayoutManager(new LinearLayoutManager(EditQuestion.this));
                                binding.recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                visibilityFunction();

                                dialog.dismiss();
                                // Handle the populated questionArrayList here
                            }).addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch questions"));

                }).addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch question papers"));

    }


    // Utility method to show a toast message and dismiss the dialog
    private void showToastAndDismissDialog(String message) {
        Toast.makeText(EditQuestion.this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    public void getSubjectList() {

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
                    subId = subCategorySnapshots.getDocuments().get(0).getId();

                    // Fetch subjects
                    database.collection("categories").document(catId)
                            .collection("subCategories").document(subId)
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

        database.collection("categories").document(catId)
                .collection("subCategories").document(subId)
                .collection(studyCategoryName).document(subjectId)
                .collection("subject_question_paper").whereEqualTo("qpName", qpname).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        qpId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        database.collection("categories").document(catId)
                                .collection("subCategories").document(subId)
                                .collection(studyCategoryName).document(subjectId)
                                .collection("subject_question_paper").document(qpId)
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
                                        adapter = new EditQuestionAdapter(EditQuestion.this, qlist, categoryName, subCategoryName, studyCategoryName, subjectName, qpname);
                                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(EditQuestion.this));
                                        binding.recyclerView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                        visibilityFunction();
                                        dialog.dismiss();

                                    }
                                });
                    }
                });

    }

    private void visibilityFunction() {
        binding.selecter1.setVisibility(View.GONE);
        binding.selecter2.setVisibility(View.GONE);
        binding.selecter3.setVisibility(View.GONE);
        binding.selecter4.setVisibility(View.GONE);
        binding.selecter5.setVisibility(View.GONE);
        binding.getQuestion.setVisibility(View.GONE);
        binding.setAnswer.setVisibility(View.VISIBLE);
    }


    //SetAnswerCode

    private void showQuizDialog(int size) {
        SetAnswerLayoutBinding bindingSet = SetAnswerLayoutBinding.inflate(getLayoutInflater());
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(bindingSet.getRoot());
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        // ✅ Current Question Index
        final int[] currentQuestion = {0};

        // ✅ Store Selected Options for Each Question
        List<List<String>> selectedOptions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            selectedOptions.add(new ArrayList<>()); // Empty list for each question
        }

        // ✅ Initialize with First Question
        updateQuestionNumber(bindingSet, currentQuestion[0]);
        updateCheckboxSelection(bindingSet, selectedOptions, currentQuestion[0]);

        // ✅ Next Button Click (Navigate Forward)
        bindingSet.btnNext.setOnClickListener(v -> {
            saveSelection(bindingSet, selectedOptions, currentQuestion[0]);
            if (currentQuestion[0] < size - 1) {
                currentQuestion[0]++;
                updateQuestionNumber(bindingSet, currentQuestion[0]);
                updateCheckboxSelection(bindingSet, selectedOptions, currentQuestion[0]); // Update checkbox selection for next question
            } else {
                bindingSet.saveAnswer.setVisibility(View.VISIBLE);
                Toast.makeText(dialog.getContext(), "Last Question Reached", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Prev Button Click (Navigate Backward)
        bindingSet.btnPrev.setOnClickListener(v -> {
            saveSelection(bindingSet, selectedOptions, currentQuestion[0]);
            if (currentQuestion[0] > 0) {
                currentQuestion[0]--;
                updateQuestionNumber(bindingSet, currentQuestion[0]);
                updateCheckboxSelection(bindingSet, selectedOptions, currentQuestion[0]); // Update checkbox selection for previous question
            } else {
                Toast.makeText(dialog.getContext(), "First Question Reached", Toast.LENGTH_SHORT).show();
            }
        });

        bindingSet.saveAnswer.setOnClickListener(v -> {
            if (studyCategoryName.equals("Mock Subjectwise")) {
                setAnswerSubject(selectedOptions);
            } else {
                setAnswer(selectedOptions);
            }

        });

        // ✅ Show Dialog
        dialog.show();
    }

    // ✅ Update Question Number
    private void updateQuestionNumber(SetAnswerLayoutBinding bindingSet, int index) {
        bindingSet.questionnumber.setText("Question " + (index + 1));
    }

    // ✅ Save Selected Options for Current Question
    private void saveSelection(SetAnswerLayoutBinding bindingSet, List<List<String>> selectedOptions, int index) {
        List<String> selected = new ArrayList<>();
        if (bindingSet.checkBox1.isChecked()) selected.add("1");
        if (bindingSet.checkBox2.isChecked()) selected.add("2");
        if (bindingSet.checkBox3.isChecked()) selected.add("3");
        if (bindingSet.checkBox4.isChecked()) selected.add("4");
        selectedOptions.set(index, selected);
    }

    // ✅ Update Checkboxes Based on Previous Selection
    private void updateCheckboxSelection(SetAnswerLayoutBinding bindingSet, List<List<String>> selectedOptions, int index) {
        List<String> selected = selectedOptions.get(index);

        // Uncheck all checkboxes first
        bindingSet.checkBox1.setChecked(false);
        bindingSet.checkBox2.setChecked(false);
        bindingSet.checkBox3.setChecked(false);
        bindingSet.checkBox4.setChecked(false);

        // Check checkboxes based on previously selected options
        if (selected.contains(1)) bindingSet.checkBox1.setChecked(true);
        if (selected.contains(2)) bindingSet.checkBox2.setChecked(true);
        if (selected.contains(3)) bindingSet.checkBox3.setChecked(true);
        if (selected.contains(4)) bindingSet.checkBox4.setChecked(true);
    }

    public void setAnswer(List<List<String>> selectedOptions) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Initialize ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating answers...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(selectedOptions.size());  // Set the max value to total number of answers to update
        progressDialog.setCancelable(false);  // Prevent user from canceling the dialog
        progressDialog.show();  // Show the ProgressDialog

        // Loop through the selected answers and update them one by one
        for (int i = 0; i < selectedOptions.size(); i++) {
            final int index = i;  // Create a final variable to pass into the lambda
            List<String> selected = selectedOptions.get(i); // Get the selected options for this question

            // Join the selected options into a single string (comma-separated)
            String answerString = TextUtils.join(", ", selected); // Join options as a single string

            Query query = database.collection("categories")
                    .document(catId)
                    .collection("subCategories")
                    .document(subId)
                    .collection(studyCategoryName)
                    .document(qpId)
                    .collection("questions")
                    .whereEqualTo("index", index + 1);  // Use 'index' here, which is effectively final

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DocumentReference questionRef = document.getReference();
                        Map<String, Object> updatedAnswer = new HashMap<>();
                        updatedAnswer.put("answer", answerString);  // Set the 'answer' field as a single string

                        // Update the document with the selected options
                        questionRef.update(updatedAnswer)
                                .addOnSuccessListener(aVoid -> {
                                    // Update progress
                                    progressDialog.setProgress(index + 1);  // Update progress using 'index'

                                    // If all answers are updated, dismiss the progress dialog
                                    if (index + 1 == selectedOptions.size()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "All answers updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Toast.makeText(getApplicationContext(), "Error updating answer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error fetching documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    public void setAnswerSubject(List<List<String>> selectedOptions){
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Initialize ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating answers...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(selectedOptions.size());  // Set the max value to total number of answers to update
        progressDialog.setCancelable(false);  // Prevent user from canceling the dialog
        progressDialog.show();  // Show the ProgressDialog

        // Loop through the selected answers and update them one by one
        for (int i = 0; i < selectedOptions.size(); i++) {
            final int index = i;  // Create a final variable to pass into the lambda
            List<String> selected = selectedOptions.get(i); // Get the selected options for this question

            // Join the selected options into a single string (comma-separated)
            String answerString = TextUtils.join(", ", selected); // Join options as a single string

            Query query = database.collection("categories")
                    .document(catId)
                    .collection("subCategories")
                    .document(subId)
                    .collection(studyCategoryName)
                    .document(subjectId).collection("subject_question_paper")stat
                    .document(qpId)
                    .collection("questions")
                    .whereEqualTo("index", index + 1);  // Use 'index' here, which is effectively final

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DocumentReference questionRef = document.getReference();
                        Map<String, Object> updatedAnswer = new HashMap<>();
                        updatedAnswer.put("answer", answerString);  // Set the 'answer' field as a single string

                        // Update the document with the selected options
                        questionRef.update(updatedAnswer)
                                .addOnSuccessListener(aVoid -> {
                                    // Update progress
                                    progressDialog.setProgress(index + 1);  // Update progress using 'index'

                                    // If all answers are updated, dismiss the progress dialog
                                    if (index + 1 == selectedOptions.size()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "All answers updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Toast.makeText(getApplicationContext(), "Error updating answer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error fetching documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}

