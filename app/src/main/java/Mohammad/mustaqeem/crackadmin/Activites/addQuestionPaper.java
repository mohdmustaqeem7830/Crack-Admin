package Mohammad.mustaqeem.crackadmin.Activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityAddQuestionPaperBinding;

public class addQuestionPaper extends AppCompatActivity {

    ActivityAddQuestionPaperBinding binding;
    String[] categoryArray;
    String[] studyArray;
    String[] subCategoryArray;
    FirebaseFirestore database;

    String downloadUrlLink;

    String plan;
    
    String categoryName,subCategoryName,studyCategoryName,qpname,qpsubTitle,subjectName,totalQuestion,status;

    String catId,subId,subjectId,qpId;

    ArrayList<AddCategoryModel> categoriesList;
    ArrayList<AddSubCategoryModel> subCategoriesList;

    int SELECT_IMAGE_REQUEST_CODE = 25;
    private Uri imageUri;

    private FirebaseStorage storage;


    private ProgressDialog dialog;

    String time,correctMarks,negativeMarks;

    String subject,subjectEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAddQuestionPaperBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dialog = new ProgressDialog(this);

        storage = FirebaseStorage.getInstance();
        database = FirebaseFirestore.getInstance();
        categoriesList = new ArrayList<>();
        subCategoriesList = new ArrayList<>();



        subject = getIntent().getStringExtra("subject");
        if (subject!=null){

            //logic code for subject addition
            binding.questionSubTitile.setVisibility(View.GONE);
            binding.addQPBtn.setVisibility(View.GONE);
            binding.addSubject.setVisibility(View.VISIBLE);
            binding.totalQuestion.setVisibility(View.GONE);
            binding.questionName.setHint("Subject Name");
            binding.time.setVisibility(View.GONE);
            binding.correctInput.setVisibility(View.GONE);
            binding.negativeInput.setVisibility(View.GONE);
            dialog.setTitle("Adding Subject");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            getStudyCategoryforsubject();
        }else{

            // logic code for addition Question Paper
            dialog.setTitle("Uploading Question Paper");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            getStudyCategory();
        }



        //logic code for editing the question paper
        qpId = getIntent().getStringExtra("qpId");
        categoryName = getIntent().getStringExtra("categoryName");
        subCategoryName = getIntent().getStringExtra("subCategoryName");
        studyCategoryName = getIntent().getStringExtra("studyCategoryName");
        subjectEdit = getIntent().getStringExtra("subjectEdit");
        if (qpId!=null){
            binding.addQPBtn.setVisibility(View.GONE);
            binding.updateQP.setVisibility(View.VISIBLE);

            if (subjectEdit!=null){
                EditSubjectQuestionPaper();
            }else{
                EditQuestionPaperFilling();
            }

        }else{
            getStatusList();
            getPlanList();
            getCategoryList();
        }


        binding.selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
            }
        });

        binding.status.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                status = binding.status.getText().toString();
            }
        });

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
                studyCategoryName = binding.studyCategory.getText().toString();
                if (studyCategoryName.equals("Mock Subjectwise")){
                    binding.selecter4.setVisibility(View.VISIBLE);
                    getSubjectList();
                }
            }
        });

        binding.subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subjectName = binding.subject.getText().toString();
            }
        });
        
        binding.addQPBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                qpname =binding.qpname.getText().toString();
                qpsubTitle = binding.qpsubTitle.getText().toString();
                time = binding.time.getText().toString();
                correctMarks = binding.correct.getText().toString();
                negativeMarks = binding.negative.getText().toString();
                status = binding.status.getText().toString();
                totalQuestion = binding.totalQuestion.getText().toString();
                plan = binding.plan.getText().toString();
                if (imageUri==null || categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || time.isEmpty() || correctMarks.isEmpty() || negativeMarks.isEmpty() || status.isEmpty() || totalQuestion.isEmpty()){
                    Toast.makeText(addQuestionPaper.this, "Please fill all the field", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.show();
                    uploadImage(imageUri);
                }
            }
        });

        binding.updateQP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageUri!=null){

                }
//                if (subjectEdit!=null){
//                    UpdateSubjectQP();
//                }else{
//                    UpdateQp();
//                }
            }
        });

        binding.addSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                status = binding.status.getText().toString();
                qpname =binding.qpname.getText().toString();
                if (imageUri==null || categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty()  || status.isEmpty()){
                    Toast.makeText(addQuestionPaper.this, "Please fill all the field", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.show();
                    uploadImage(imageUri);
                }
            }
        });




    }




    private void getStatusList() {
        String [] statusArray = new String[2];
        List<String> type = new ArrayList<>();
        type.add("Live Now");
        type.add("Not Live");
        statusArray = type.toArray(new String[0]);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, statusArray);
        binding.status.setAdapter(categoryAdapter);

    }    private void getPlanList() {
        String [] statusArray = new String[2];
        List<String> type = new ArrayList<>();
        type.add("Free");
        type.add("Premium");
        type.add("Premium Pro");
        statusArray = type.toArray(new String[0]);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, statusArray);
        binding.plan.setAdapter(categoryAdapter);

    }

    private void getSubCategory(String cat) {
        database.collection("categories").whereEqualTo("categoryName", cat).get()
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
                                                Toast.makeText(addQuestionPaper.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(addQuestionPaper.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(addQuestionPaper.this, "No categories found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(addQuestionPaper.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                binding.image.setImageBitmap(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage(Uri imageUri ) {
        StorageReference storageRef;

        if (subject!=null){
             storageRef = storage.getReference().child("Subject_images/" + System.currentTimeMillis() + ".jpg");

        }else{
            if (subjectName!=null){
                storageRef = storage.getReference().child("Subject_Question_Paper/" + System.currentTimeMillis() + ".jpg");

            }else{
                storageRef = storage.getReference().child("Question_Paper_images/" + System.currentTimeMillis() + ".jpg");
            }
        }



        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                if (subject!=null){
                                    uploadSubject(downloadUri);
                                }else{
                                    if (subjectName!=null){
                                        uploadSubjectQuestionPaper(downloadUri);
                                    }else{
                                        upload(downloadUri);

                                    }
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(addQuestionPaper.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void uploadSubject(Uri downloadUri) {
        Subject subjectModel = new Subject(binding.qpname.getText().toString(),downloadUri.toString(),"");
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(addQuestionPaper.this, "No categories found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentSnapshot categorySnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String catId = categorySnapshot.getId();

                        // Then, get the sub-category ID
                        database.collection("categories").document(catId).collection("subCategories")
                                .whereEqualTo("subCategoryName", subCategoryName).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            dialog.dismiss();
                                            Toast.makeText(addQuestionPaper.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        DocumentSnapshot subCategorySnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String subcatId = subCategorySnapshot.getId();

                                        // Finally, upload the data
                                        database.collection("categories").document(catId).collection("subCategories").document(subcatId)
                                                .collection(studyCategoryName).document()
                                                .set(subjectModel)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            dialog.dismiss();
                                                            Toast.makeText(addQuestionPaper.this, "Subject Added", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            dialog.dismiss();
                                                            Toast.makeText(addQuestionPaper.this, "Failed to add Subject", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(addQuestionPaper.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(addQuestionPaper.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void upload(Uri downloadUri) {
        AddQuestionPaperModel model = new AddQuestionPaperModel(downloadUri.toString(), qpname, qpsubTitle, categoryName, null, subCategoryName, studyCategoryName,time,correctMarks,negativeMarks,totalQuestion,status,plan);

        // First, get the category ID
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(addQuestionPaper.this, "No categories found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentSnapshot categorySnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String catId = categorySnapshot.getId();

                        // Then, get the sub-category ID
                        database.collection("categories").document(catId).collection("subCategories")
                                .whereEqualTo("subCategoryName", subCategoryName).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            dialog.dismiss();
                                            Toast.makeText(addQuestionPaper.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        DocumentSnapshot subCategorySnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String subcatId = subCategorySnapshot.getId();

                                        // Finally, upload the data
                                        database.collection("categories").document(catId).collection("subCategories").document(subcatId)
                                                .collection(studyCategoryName).document()
                                                .set(model)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            dialog.dismiss();
                                                            Toast.makeText(addQuestionPaper.this, "Question Paper Added", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            dialog.dismiss();
                                                            Toast.makeText(addQuestionPaper.this, "Failed to add Question Paper", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(addQuestionPaper.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(addQuestionPaper.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    public void getCategoryList(){
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

    public void getStudyCategory(){
                        List<String> cat = new ArrayList<>();
                        cat.add("Check Paper");
                        cat.add("Mock PYQ");
                        cat.add("Mock Topicwise");
                        cat.add("Mock Full length");
                        cat.add("Mock Subjectwise");

                        studyArray = cat.toArray(new String[0]);
                        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, studyArray);
                        binding.studyCategory.setAdapter(classAdapter);
    }

    public void getStudyCategoryforsubject(){
                        List<String> cat = new ArrayList<>();
                        cat.add("Mock Subjectwise");
                        cat.add("Course Books");
                        cat.add("Subject Notes");
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
        Toast.makeText(addQuestionPaper.this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateSubjectSpinner(List<String> subjects) {
        String[] subjectArray = subjects.toArray(new String[0]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, subjectArray);
        binding.subject.setAdapter(classAdapter);
    }
    private void uploadSubjectQuestionPaper(Uri downloadUri) {
        AddQuestionPaperModel model = new AddQuestionPaperModel(downloadUri.toString(), qpname, qpsubTitle, categoryName, null, subCategoryName, studyCategoryName,time,correctMarks,negativeMarks,totalQuestion,status,plan);

        // Get the category ID
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showToastAndDismissDialog("No categories found");
                        return;
                    }

                    DocumentSnapshot categorySnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    String catId = categorySnapshot.getId();

                    // Get the sub-category ID
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    showToastAndDismissDialog("No sub-categories found");
                                    return;
                                }

                                DocumentSnapshot subCategorySnapshot = queryDocumentSnapshots1.getDocuments().get(0);
                                String subcatId = subCategorySnapshot.getId();

                                // Upload the data
                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subcatId)
                                        .collection(studyCategoryName).whereEqualTo("subjectName", subjectName).get()
                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                            if (queryDocumentSnapshots2.isEmpty()) {
                                                showToastAndDismissDialog("No subjects found");
                                                return;
                                            }

                                            DocumentSnapshot subjectSnapshot = queryDocumentSnapshots2.getDocuments().get(0);
                                            String subjectId = subjectSnapshot.getId();

                                            database.collection("categories").document(catId)
                                                    .collection("subCategories").document(subcatId)
                                                    .collection(studyCategoryName).document(subjectId)
                                                    .collection("subject_question_paper").document()
                                                    .set(model)
                                                    .addOnSuccessListener(unused -> {
                                                        showToastAndDismissDialog("Question Paper Added");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        showToastAndDismissDialog("Failed to add question paper");
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            showToastAndDismissDialog("Failed to fetch subjects");
                                        });
                            })
                            .addOnFailureListener(e -> {
                                showToastAndDismissDialog("Failed to fetch sub-categories");
                            });
                })
                .addOnFailureListener(e -> {
                    showToastAndDismissDialog("Failed to fetch categories");
                });
    }


    private void showToastAndDismissDialog(String message) {
        showToast(message);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void EditQuestionPaperFilling() {
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        handleError("Category not found");
                        return;
                    }
                    catId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    handleError("Sub-category not found");
                                    return;
                                }
                                subId = queryDocumentSnapshots1.getDocuments().get(0).getId();
                                database.collection("categories").document(catId).collection("subCategories").document(subId)
                                        .collection(studyCategoryName).document(qpId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                AddQuestionPaperModel addQuestionPaperModel = documentSnapshot.toObject(AddQuestionPaperModel.class);
                                                if (addQuestionPaperModel.getQpImage()!=null){
                                                    Glide.with(addQuestionPaper.this)
                                                            .load(addQuestionPaperModel.getQpImage())
                                                            .into(binding.image);

                                                    downloadUrlLink = addQuestionPaperModel.getQpImage();
                                                }

                                               setQuestionInfo(addQuestionPaperModel);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                handleError("Error fetching sub-category");
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    handleError("Error fetching category");
                });
    }

    private void EditSubjectQuestionPaper() {
        // Fetch the category by name
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(addQuestionPaper.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Fetch the sub-category by name
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(addQuestionPaper.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                subId = queryDocumentSnapshots1.getDocuments().get(0).getId();

                                // Fetch the subject by name
                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subId)
                                        .collection(studyCategoryName).whereEqualTo("subjectName", subjectEdit).get()
                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                            if (queryDocumentSnapshots2.isEmpty()) {
                                                dialog.dismiss();
                                                Toast.makeText(addQuestionPaper.this, "No subjects found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            subjectId = queryDocumentSnapshots2.getDocuments().get(0).getId();

                                            // Fetch the specific question paper
                                            database.collection("categories").document(catId)
                                                    .collection("subCategories").document(subId)
                                                    .collection(studyCategoryName).document(subjectId)
                                                    .collection("subject_question_paper").document(qpId).get()
                                                    .addOnSuccessListener(documentSnapshot -> {
                                                        if (documentSnapshot.exists()) {
                                                            AddQuestionPaperModel addQuestionPaperModel = documentSnapshot.toObject(AddQuestionPaperModel.class);
                                                            if (addQuestionPaperModel != null && addQuestionPaperModel.getQpImage() != null) {
                                                                Glide.with(addQuestionPaper.this)
                                                                        .load(addQuestionPaperModel.getQpImage())
                                                                        .into(binding.image);
                                                                downloadUrlLink = addQuestionPaperModel.getQpImage();
                                                            }
                                                            setQuestionInfo(addQuestionPaperModel);
                                                        } else {
                                                            dialog.dismiss();
                                                            Toast.makeText(addQuestionPaper.this, "Question not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(addQuestionPaper.this, "Failed to fetch question details", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(addQuestionPaper.this, "Failed to fetch study categories", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(addQuestionPaper.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(addQuestionPaper.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void setQuestionInfo(AddQuestionPaperModel addQuestionPaperModel) {
        binding.status.setText(addQuestionPaperModel.getStatus());
        binding.plan.setText(addQuestionPaperModel.getPlan());
        binding.categoryName.setText(categoryName);
        binding.subcategoryName.setText(subCategoryName);
        binding.studyCategory.setText(studyCategoryName);
        binding.qpname.setText(addQuestionPaperModel.getQpName());
        binding.qpsubTitle.setText(addQuestionPaperModel.getQpSubTitle());
        binding.time.setText(addQuestionPaperModel.getTime());
        binding.totalQuestion.setText(addQuestionPaperModel.getTotalQuestion());
        binding.correct.setText(addQuestionPaperModel.getCorrectMarks());
        binding.negative.setText(addQuestionPaperModel.getNegativeMarks());
    }

    private void handleError(String message) {
        dialog.dismiss();
        Toast.makeText(addQuestionPaper.this, message, Toast.LENGTH_SHORT).show();
    }

    private void UpdateQp() {

    }
}