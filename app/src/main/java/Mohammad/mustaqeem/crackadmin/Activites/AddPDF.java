package Mohammad.mustaqeem.crackadmin.Activites;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddPDFModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityAddPdfBinding;

import Mohammad.mustaqeem.crackadmin.R;

public class AddPDF extends AppCompatActivity {

    ActivityAddPdfBinding binding;

    String[] categoryArray;

    int UCROP_REQUEST_CODE = 26;
    String[] subCategoryArray;

    Uri pdfUri;

    String pdfUrl,pdfId;
    ArrayList<AddCategoryModel> categoriesList;
    ArrayList<AddSubCategoryModel> subCategoriesList;

    private FirebaseStorage storage;
    private FirebaseFirestore database;

    String categoryName, subCategoryName, studyCategoryName, name, subTitle,subjectName;
    String catId,subId,subjectId,edit;


    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAddPdfBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        studyCategoryName = getIntent().getStringExtra("studyCategoryName");

        if (studyCategoryName.equals("Subject Notes")|| studyCategoryName.equals("Course Books")){
            binding.addQPBtn.setVisibility(View.GONE);
            binding.add.setVisibility(View.VISIBLE);
            binding.selecter3.setVisibility(View.VISIBLE);
        }
        
        storage = FirebaseStorage.getInstance();
        database = FirebaseFirestore.getInstance();
        categoriesList = new ArrayList<>();
        subCategoriesList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");


        categoryName = getIntent().getStringExtra("categoryName");
        subCategoryName = getIntent().getStringExtra("subCategoryName");
        studyCategoryName = getIntent().getStringExtra("studyCategoryName");
        catId = getIntent().getStringExtra("catId");
        subId = getIntent().getStringExtra("subId");
        subjectId = getIntent().getStringExtra("subjectId");
        edit = getIntent().getStringExtra("edit");
        pdfUrl = getIntent().getStringExtra("pdfUrl");
        subjectName = getIntent().getStringExtra("subject");

        if (edit!=null){
            binding.selecter1.setVisibility(View.GONE);
            binding.selecter2.setVisibility(View.GONE);
            binding.selecter3.setVisibility(View.GONE);
            binding.add.setVisibility(View.GONE);
            binding.addQPBtn.setVisibility(View.GONE);
            binding.updatePDF.setVisibility(View.VISIBLE);
            if (studyCategoryName.equals("Subject Notes")|| studyCategoryName.equals("Course Books")){
                setDetailSubjectPdf();
            }else{
                setDetailPdf();
            }
        }



        getCategoryList();
        binding.selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                name = binding.qpname.getText().toString();
                subTitle = binding.qpsubTitle.getText().toString();
                if (name.isEmpty() || subTitle.isEmpty()) {
                    Toast.makeText(AddPDF.this, "Please fill all the above field", Toast.LENGTH_SHORT).show();
                } else {
                    openPDFGallery();
                }


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
                if (studyCategoryName.equals("Subject Notes")|| studyCategoryName.equals("Course Books")){
                      getSubjectList();
                      progressDialog.setMessage("please wait");
                      progressDialog.setCancelable(false);
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
                uploadPDFToStorage();
            }
        });


        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPDFToStorage();
            }
        });

        binding.updatePDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studyCategoryName.equals("Subject Notes")|| studyCategoryName.equals("Course Books")){
                    if (pdfUri!=null){
                        pdfsubjectDeleteAndUpdatePdf(pdfUrl);
                    }else{
                        updateSubjectPDF(pdfUrl);
                    }
                }else{
                    if (pdfUri!=null){
                        pdfsubjectDeleteAndUpdatePdf(pdfUrl);
                    }
                    else{
                       updatePDF(pdfUrl);
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
                                                Toast.makeText(AddPDF.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddPDF.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(AddPDF.this, "No categories found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPDF.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openPDFGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(AddPDF.this, "Please install a PDF reader", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();
        }
    }


    public void uploadData(String pdfUrl) {
        AddPDFModel model = new AddPDFModel(name, "", subTitle, pdfUrl);
        database.collection("categories").whereEqualTo("categoryName", categoryName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String catId = queryDocumentSnapshots.getDocuments().get(0).getId();
                database.collection("categories").document(catId).collection("subCategories").whereEqualTo("subCategoryName", subCategoryName)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                String subId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subId)
                                        .collection(studyCategoryName).document().set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(AddPDF.this, "Pdf Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });
    }

    private void uploadPDFToStorage() {
        if (pdfUri != null) {

            progressDialog.show();

            String fileName = binding.qpname.getText().toString() + ".pdf"; // Create a unique file name

            String path;

            if (studyCategoryName.equals("Subject Notes")|| studyCategoryName.equals("Course Books")){
                path = "pdfs/"+studyCategoryName+"/"+binding.subject.getText().toString()+"/"+fileName;
            }else{
                path = "pdfs/" + studyCategoryName + "/" + fileName;
            }


            storage.getReference().child(path).putFile(pdfUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    pdfUrl = uri.toString();
                                    progressDialog.dismiss();

                                    if (studyCategoryName.equals("Subject Notes")|| studyCategoryName.equals("Course Books")){
                                        uploadDataSubject(pdfUrl);
                                    }else{
                                        uploadData(pdfUrl);
                                    }


                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddPDF.this, "Failed to upload PDF", Toast.LENGTH_SHORT).show();
                    });
        }
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
                                    progressDialog.dismiss();
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

                                            progressDialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            showToast("Failed to fetch subjects");
                                            progressDialog.dismiss();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                showToast("Failed to fetch sub-categories");
                                progressDialog.dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to fetch categories");
                    progressDialog.dismiss();
                });
    }

    private void showToast(String message) {
        Toast.makeText(AddPDF.this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateSubjectSpinner(List<String> subjects) {
        String[] subjectArray = subjects.toArray(new String[0]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, subjectArray);
        binding.subject.setAdapter(classAdapter);
    }

    private void uploadDataSubject(String pdfUrl) {

        AddPDFModel model = new AddPDFModel(name, "", subTitle, pdfUrl);
        database.collection("categories").whereEqualTo("categoryName", categoryName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String catId = queryDocumentSnapshots.getDocuments().get(0).getId();
                database.collection("categories").document(catId).collection("subCategories").whereEqualTo("subCategoryName", subCategoryName)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                String subId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                database.collection("categories").document(catId)
                                        .collection("subCategories").document(subId)
                                        .collection(studyCategoryName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                String studId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                                database.collection("categories").document(catId)
                                                        .collection("subCategories").document(subId)
                                                        .collection(studyCategoryName).document(studId).collection(subjectName).document().set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(AddPDF.this, "Pdf added successfully", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                
                                            }
                                        });
                            }
                        });
            }
        });
    }


    //Editing pdf
    private void setDetailPdf() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Fetching details");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait ....");
        progressDialog.show();
        // After deleting from Storage, delete from Firestore
        database.collection("categories").document(catId)
                .collection("subCategories").document(subId)
                .collection(studyCategoryName).whereEqualTo("pdfUrl", pdfUrl).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            pdfId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            AddPDFModel pdfModel = queryDocumentSnapshots.getDocuments().get(0).toObject(AddPDFModel.class);
                            binding.updatePDF.setVisibility(View.VISIBLE);
                            binding.qpname.setText(pdfModel.getPdfName());
                            binding.qpsubTitle.setText(pdfModel.getPdfSubName());
                            progressDialog.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPDF.this, "PDF Not Found", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void setDetailSubjectPdf() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Fetching details");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait ....");
        progressDialog.show();
        // After deleting from Storage, delete from Firestore
        database.collection("categories").document(catId)
                .collection("subCategories").document(subId)
                .collection(studyCategoryName).document(subjectId).collection(subjectName).whereEqualTo("pdfUrl", pdfUrl).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            pdfId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            AddPDFModel pdfModel = queryDocumentSnapshots.getDocuments().get(0).toObject(AddPDFModel.class);

                            binding.qpname.setText(pdfModel.getPdfName());
                            binding.qpsubTitle.setText(pdfModel.getPdfSubName());
                            progressDialog.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPDF.this, "PDF Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pdfsubjectDeleteAndUpdatePdf(String pdfUrl) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Updating PDF");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            FirebaseStorage storage = FirebaseStorage.getInstance();

            try {
                StorageReference oldPdfRef = storage.getReferenceFromUrl(pdfUrl);
                oldPdfRef.delete().addOnSuccessListener(unused -> {
                    StorageReference newPdfRef = oldPdfRef;
                    newPdfRef.putFile(pdfUri).addOnSuccessListener(taskSnapshot -> {
                        newPdfRef.getDownloadUrl().addOnSuccessListener(newUrl -> {
                            progressDialog.dismiss();
                            if (studyCategoryName.equals("Subject Notes")|| studyCategoryName.equals("Course Books")){
                               updateSubjectPDF(newUrl.toString());
                            }else{
                               updatePDF(newUrl.toString());
                            }

                        });
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to upload new PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to delete old PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                // Handle invalid URL errors
                progressDialog.dismiss();
                Toast.makeText(this, "Invalid URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
    }
    private void updatePDF(String pdfUrl) {
        Map<String, Object> model = new HashMap<>();
        model.put("pdfName",binding.qpname.getText().toString().trim());
        model.put("pdfSubName",binding.qpsubTitle.getText().toString().trim());
        model.put("pdfUrl",pdfUrl);
        
        database.collection("categories").document(catId).collection("subCategories").document(subId)
                .collection(studyCategoryName).document(pdfId).update(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPDF.this, "Updated PDF Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPDF.this, "Failed to Update PDF", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSubjectPDF(String pdfUrl) {

        Map<String, Object> model = new HashMap<>();
        model.put("pdfName",binding.qpname.getText().toString().trim());
        model.put("pdfSubName",binding.qpsubTitle.getText().toString().trim());
        model.put("pdfUrl",pdfUrl);

        database.collection("categories").document(catId).collection("subCategories").document(subId)
                .collection(studyCategoryName).document(subjectId).collection(subjectName).document(pdfId).update(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPDF.this, "Updated PDF Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPDF.this, "Failed to Update PDF", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
