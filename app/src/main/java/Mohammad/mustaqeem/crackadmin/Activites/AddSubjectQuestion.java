package Mohammad.mustaqeem.crackadmin.Activites;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityAddQuestionBinding;

public class AddSubjectQuestion extends AppCompatActivity {
    ActivityAddQuestionBinding binding;
    String[] categoryArray;
    String[] studyArray;
    String[] subCategoryArray;

    String[] qpArray;

    String[] typeArray;

    int totalQuestions ;
    ProgressDialog progressDialog;
    String questionName;
    int  UCROP_REQUEST_CODE = 40;
    int UCROP_SOLUTION_REQUEST_CODE = 37;
    FirebaseFirestore database;
    private FirebaseStorage storage;

    String catId;
    LinkedHashMap<String, String> questionImageMap = new LinkedHashMap<>();
    LinkedHashMap<String, String> solutionImageMap = new LinkedHashMap<>();
    Iterator<Map.Entry<String, String>> questionIterator;

    String downloadUrlLink,solutiondownloadUrlLink;

    String categoryName, subCategoryName, studyCategoryName, qpName, qId,subjectName;

    ArrayList<AddCategoryModel> categoriesList;
    ArrayList<AddSubCategoryModel> subCategoriesList;

    ArrayList<AddQuestionPaperModel> qplist;
    int currentIndex ;
    String multiple ;
    int SELECT_QUESTION_IMAGE_FOLDER = 50;
    int SELECT_SOLTUTION_IMAGE_FOLDER = 55;

    int SELECT_IMAGE_REQUEST_CODE = 25;
    int SELECT_SOLUTION_IMAGE_REQUEST_CODE = 30;
    private Uri imageUri,solutionImageUri;

    private ProgressDialog dialog;

    String checkAnswer = null;
    int index;

    String type = "MCQ";

    String question="", option1="1", option2="2", option3="3", option4="4", answer="",solution="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAddQuestionBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        storage = FirebaseStorage.getInstance();
        database = FirebaseFirestore.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading Question Paper");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        categoriesList = new ArrayList<>();
        subCategoriesList = new ArrayList<>();
        qplist = new ArrayList<>();


        categoryName = getIntent().getStringExtra("categoryName");
        subCategoryName = getIntent().getStringExtra("subCategoryName");
        studyCategoryName = getIntent().getStringExtra("studyCategoryName");
        qpName = getIntent().getStringExtra("qpName");
        subjectName = getIntent().getStringExtra("subject");
        qId = getIntent().getStringExtra("qId");
        String qpId = getIntent().getStringExtra("qpId");

        if (categoryName != null) {
            binding.addBtn.setVisibility(View.GONE);
            binding.updateBtn.setVisibility(View.VISIBLE);
            binding.imageDeleteBtn.setVisibility(View.VISIBLE);
            binding.deleteSolutionImageBtn.setVisibility(View.VISIBLE);
            fetchAndUpdateUI();
            dialog.setTitle("Updating Question");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
        }

        getCategoryList();
        getStudyCategory();

        getTypeList();


        binding.check1.setOnCheckedChangeListener(onCheckedChangeListener);
        binding.check2.setOnCheckedChangeListener(onCheckedChangeListener);
        binding.check3.setOnCheckedChangeListener(onCheckedChangeListener);
        binding.check4.setOnCheckedChangeListener(onCheckedChangeListener);

        binding.multipleQuestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                binding.questionlayer.setVisibility(View.GONE);
                binding.quefolder.setVisibility(View.VISIBLE);
                binding.ansfolder.setVisibility(View.VISIBLE);
                binding.addMultipleQuestion.setVisibility(View.VISIBLE);

            }
        });

        binding.singleQuestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                binding.questionlayer.setVisibility(View.VISIBLE);
                binding.quefolder.setVisibility(View.GONE);
                binding.ansfolder.setVisibility(View.GONE);
                binding.addMultipleQuestion.setVisibility(View.GONE);
            }
        });

        binding.addMultipleQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalQuestions = questionImageMap.size();
                if (totalQuestions == 0) {
                    Toast.makeText(v.getContext(), "No Question to upload!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog = new ProgressDialog(v.getContext());
                progressDialog.setTitle("Uploading Questions");
                progressDialog.setCancelable(false);
                progressDialog.show();

                questionIterator = questionImageMap.entrySet().iterator();
                currentIndex = 1;
                uploadNextImage(questionIterator, progressDialog, currentIndex, totalQuestions);
            }
        });

        binding.qtype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                type = binding.qtype.getText().toString();
                if (type.equals("OCQ")) {

                    binding.option1.setVisibility(View.GONE);
                    binding.option2.setVisibility(View.GONE);
                    binding.option3.setVisibility(View.GONE);
                    binding.option4.setVisibility(View.GONE);
                    binding.question.setVisibility(View.VISIBLE);
                    binding.answer.setVisibility(View.GONE);
                    binding.check1.setVisibility(View.GONE);
                    binding.check2.setVisibility(View.GONE);
                    binding.check3.setVisibility(View.GONE);
                    binding.check4.setVisibility(View.GONE);
                    binding.solution.setVisibility(View.VISIBLE);
                    binding.solutionImage.setVisibility(View.VISIBLE);
                } else if (type.equals("MSQ")) {

                    binding.question.setVisibility(View.VISIBLE);
                    binding.option1.setVisibility(View.VISIBLE);
                    binding.option2.setVisibility(View.VISIBLE);
                    binding.option3.setVisibility(View.VISIBLE);
                    binding.option4.setVisibility(View.VISIBLE);
                    binding.answer.setVisibility(View.VISIBLE);
                    binding.solution.setVisibility(View.VISIBLE);
                    binding.solutionImage.setVisibility(View.VISIBLE);

                } else {
                    binding.question.setVisibility(View.VISIBLE);
                    binding.option1.setVisibility(View.VISIBLE);
                    binding.option2.setVisibility(View.VISIBLE);
                    binding.option3.setVisibility(View.VISIBLE);
                    binding.option4.setVisibility(View.VISIBLE);
                    binding.answer.setVisibility(View.GONE);
                    binding.solution.setVisibility(View.VISIBLE);
                    binding.solutionImage.setVisibility(View.VISIBLE);

                }
            }
        });

        binding.selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
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
                getSubjectList();
            }
        });

        binding.quefolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, SELECT_QUESTION_IMAGE_FOLDER);
            }
        });

        binding.ansfolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, SELECT_SOLTUTION_IMAGE_FOLDER);
            }
        });

        binding.subject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subjectName = binding.subject.getText().toString();
                getQuestionPaperList();
            }
        });

        binding.qpName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                qpName = binding.qpName.getText().toString();
            }
        });


        binding.selectSolutionImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_SOLUTION_IMAGE_REQUEST_CODE);
            }
        });

        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                question = binding.question.getText().toString();
                option1 = binding.option1.getText().toString();
                option2 = binding.option2.getText().toString();
                option3 = binding.option3.getText().toString();
                option4 = binding.option4.getText().toString();
                answer = checkAnswer;
                solution = binding.solution.getText().toString();
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                subjectName = binding.subject.getText().toString();
                qpName = binding.qpName.getText().toString();


                dialog.show();

                if (type.equals("MCQ")  || type.equals("MSQ")) {
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() ||  option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            if (solutionImageUri!=null){
                                uploadSolutionImage();
                            }else{
                                uploadImage(imageUri);
                            }
                        } else {
                            if (solutionImageUri!=null){
                                uploadSolutionImage();
                            }else{
                                uploadQuestion( null);
                            }
                        }
                    }
                }  else if (type.equals("OCQ")) {
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() || question.isEmpty() || answer.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            uploadImage(imageUri);
                        } else {
                            uploadQuestion( null);
                        }
                    }
                }
            }
        });

        binding.imageDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadUrlLink != null) {
                    deleteImage();
                    Toast.makeText(AddSubjectQuestion.this, downloadUrlLink, Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.deleteSolutionImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSolutionImage();
            }
        });


        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question = binding.question.getText().toString();
                option1 = binding.option1.getText().toString();
                option2 = binding.option2.getText().toString();
                option3 = binding.option3.getText().toString();
                option4 = binding.option4.getText().toString();
                answer = checkAnswer;
                solution = binding.solution.getText().toString();
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                subjectName = binding.subject.getText().toString();
                qpName = binding.qpName.getText().toString();

                dialog.show();

                if (type.equals("MCQ")) {
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() ||  option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() ) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            if (solutionImageUri!=null){
                                updateSolutionImage();
                            }else{
                                updateImage(imageUri);
                            }
                        } else {
                            if (solutionImageUri!=null){
                                updateSolutionImage();
                            }else{
                                updateQuestion( null);
                            }
                        }
                    }
                } else if (type.equals("MSQ")) {
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() || question.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() || answer.isEmpty() ) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            updateImage(imageUri);
                        } else {
                            updateQuestion( null);
                        }
                    }
                } else if (type.equals("OCQ")) {
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() || question.isEmpty() || answer.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            updateImage(imageUri);
                        } else {
                            updateQuestion( null);
                        }
                    }
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
                // For question image selection
                imageUri = data.getData();
                if (imageUri != null) {
                    startCrop(imageUri, UCROP_REQUEST_CODE);
                }
            } else if (requestCode == UCROP_REQUEST_CODE) {
                // For question image cropping
                final Uri resultUriQuestion = UCrop.getOutput(data);
                if (resultUriQuestion != null) {
                    imageUri = resultUriQuestion;
                    try {
                        binding.question.setText(imageUri.toString());
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        binding.image.setImageBitmap(selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == SELECT_SOLUTION_IMAGE_REQUEST_CODE) {
                // For solution image selection
                solutionImageUri = data.getData();
                if (solutionImageUri != null) {
                    startCrop(solutionImageUri, UCROP_SOLUTION_REQUEST_CODE);
                }
            }else if (requestCode==SELECT_QUESTION_IMAGE_FOLDER || requestCode == SELECT_SOLTUTION_IMAGE_FOLDER){
                if (requestCode==SELECT_QUESTION_IMAGE_FOLDER){
                    questionImageMap.clear();
                    handleImageResult(requestCode, data);
                }else{
                    solutionImageMap.clear();
                    handleImageResult(requestCode, data);
                }


            } else if (requestCode == UCROP_SOLUTION_REQUEST_CODE) {
                // For solution image cropping
                final Uri resultUriSolution = UCrop.getOutput(data);
                if (resultUriSolution != null) {
                    solutionImageUri = resultUriSolution;
                    try {
                        binding.solution.setText(solutionImageUri.toString());
                        InputStream imageStream = getContentResolver().openInputStream(solutionImageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        binding.solutionImage.setImageBitmap(selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Cropping failed: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void startCrop(@NonNull Uri uri,int code) {
        String destinationFileName = "CroppedImage";
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(100);
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(true);

        UCrop.of(uri, destinationUri)
                .withAspectRatio(16, 9) // Adjust aspect ratio as needed
                .withMaxResultSize(1080, 720) // Adjust size as needed
                .withOptions(options)
                .start(this, code);
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
                                                Toast.makeText(AddSubjectQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddSubjectQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(AddSubjectQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddSubjectQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getStudyCategory() {
        List<String> cat = new ArrayList<>();
        cat.add("Mock Subjectwise");
        studyArray = cat.toArray(new String[0]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, studyArray);
        binding.studyCategory.setAdapter(classAdapter);
    }

    private void clearInputFields(String type) {
        binding.question.setText("");
        binding.image.setImageResource(R.drawable.baseline_image_24);
        binding.solutionImage.setImageResource(R.drawable.baseline_image_24);
        solutionImageUri=null;
        imageUri = null;
        if (type.equals("MCQ") || type.equals("MSQ")) {
            binding.option1.setText("");
            binding.option2.setText("");
            binding.option3.setText("");
            binding.option4.setText("");
        }
        binding.answer.setText("");
        binding.solution.setText("");
        if (type.equals("MSQ")) {
        }
    }

    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish(); // Finish the activity if the user clicks "Yes"
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // Dismiss the dialog if the user clicks "No"
                    }
                })
                .show();
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // Uncheck other CheckBoxes
                if (buttonView != binding.check1) binding.check1.setChecked(false);
                if (buttonView != binding.check2) binding.check2.setChecked(false);
                if (buttonView != binding.check3) binding.check3.setChecked(false);
                if (buttonView != binding.check4) binding.check4.setChecked(false);

                // Get text of the checked option
                if (buttonView == binding.check1) {
                    checkAnswer = binding.option1.getText().toString();
                } else if (buttonView == binding.check2) {
                    checkAnswer = binding.option2.getText().toString();
                } else if (buttonView == binding.check3) {
                    checkAnswer = binding.option3.getText().toString();
                } else if (buttonView == binding.check4) {
                    checkAnswer = binding.option4.getText().toString();
                }
            }
        }
    };

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
        Toast.makeText(AddSubjectQuestion.this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateSubjectSpinner(List<String> subjects) {
        String[] subjectArray = subjects.toArray(new String[0]);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, subjectArray);
        binding.subject.setAdapter(classAdapter);
    }


    public void getQuestionPaperList() {
        // Get the category ID

        Toast.makeText(this, categoryName + " " + subCategoryName + " " + studyCategoryName, Toast.LENGTH_SHORT).show();
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(AddSubjectQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Get the sub-category ID
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(AddSubjectQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(AddSubjectQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(AddSubjectQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }
    private void uploadImage(Uri imageUri) {

        StorageReference storageRef = storage.getReference().child(categoryName+"/"+subCategoryName+"/"+studyCategoryName+"/"+subjectName+"/"+qpName+"/"+"question_images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {

                                downloadUrlLink = downloadUri.toString();

                                uploadQuestion(downloadUrlLink);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddSubjectQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadSolutionImage() {

        StorageReference storageRef = storage.getReference().child(categoryName+"/"+subCategoryName+"/"+studyCategoryName+"/"+subjectName+"/"+qpName+"/"+"solutionImage/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(solutionImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {

                                solutiondownloadUrlLink = downloadUri.toString();
                                if(imageUri!=null){
                                    uploadImage(imageUri);

                                }else{
                                   uploadQuestion(null);
                                }

                                Toast.makeText(AddSubjectQuestion.this, "uploaded", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddSubjectQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteSolutionImage() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(solutiondownloadUrlLink);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AddSubjectQuestion.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                binding.solutionImage.setImageResource(R.drawable.baseline_image_24);
                solutionImageUri = null;
                solutiondownloadUrlLink = null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddSubjectQuestion.this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.solutionImage.setImageResource(R.drawable.baseline_image_24);
                solutionImageUri = null;
                solutiondownloadUrlLink = null;
            }
        });
    }

    public void uploadQuestion(String qImage) {
        // Fetch categories
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Fetch sub-categories
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(AddSubjectQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddSubjectQuestion.this, "No question papers found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            String subjectId = queryDocumentSnapshots2.getDocuments().get(0).getId();

                                            // Fetch specific question paper
                                            database.collection("categories").document(catId)
                                                    .collection("subCategories").document(subcatId)
                                                    .collection(studyCategoryName).document(subjectId)
                                                    .collection("subject_question_paper").whereEqualTo("qpName", qpName).get()
                                                    .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                        if (queryDocumentSnapshots3.isEmpty()) {
                                                            dialog.dismiss();
                                                            Toast.makeText(AddSubjectQuestion.this, "No question papers found", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }
                                                        String qpID = queryDocumentSnapshots3.getDocuments().get(0).getId();
                                                        DocumentReference qpDocRef = database.collection("categories").document(catId)
                                                                .collection("subCategories").document(subcatId)
                                                                .collection(studyCategoryName).document(subjectId)
                                                                .collection("subject_question_paper").document(qpID);

                                                        // Fetch existing questions
                                                        qpDocRef.collection("questions").get()
                                                                .addOnSuccessListener(questionsTask -> {
                                                                    int newIndex = questionsTask.size() + 1;
                                                                    Question questionModel;

                                                                    switch (type) {
                                                                        case "MCQ":
                                                                            questionModel = new Question(question, option1, option2, option3, option4, answer, newIndex, qImage, type,solution,solutiondownloadUrlLink);
                                                                            break;
                                                                        case "MSQ":
                                                                            questionModel = new Question(question, option1, option2, option3, option4, answer, newIndex, qImage, type,solution,solutiondownloadUrlLink);
                                                                            break;
                                                                        case "OCQ":
                                                                            questionModel = new Question(question, answer, newIndex, qImage, type,solution,solutiondownloadUrlLink);
                                                                            break;
                                                                        default:
                                                                            handleError("Invalid question type");
                                                                            return;
                                                                    }

                                                                    qpDocRef.collection("questions").add(questionModel)
                                                                            .addOnCompleteListener(task -> {
                                                                                dialog.dismiss();
                                                                                if (task.isSuccessful()) {
                                                                                    clearInputFields(type);
                                                                                    Toast.makeText(AddSubjectQuestion.this, "Question Added Successfully", Toast.LENGTH_SHORT).show();
                                                                                    if (multiple.equals("multiple")){
                                                                                        questionIterator.remove();
                                                                                        solutionImageMap.remove(questionName);
                                                                                        uploadNextImage(questionIterator, progressDialog, currentIndex + 1, totalQuestions);
                                                                                    }
                                                                                } else {
                                                                                    Toast.makeText(AddSubjectQuestion.this, "Failed to add question", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(e -> handleError("Error adding question"));
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(AddSubjectQuestion.this, "Failed to fetch questions", Toast.LENGTH_SHORT).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(AddSubjectQuestion.this, "Failed to fetch question papers", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(AddSubjectQuestion.this, "Failed to fetch study categories", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(AddSubjectQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(AddSubjectQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAndUpdateUI() {
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Fetch sub-categories
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(AddSubjectQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddSubjectQuestion.this, "No question papers found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            String subjectId = queryDocumentSnapshots2.getDocuments().get(0).getId();

                                            // Fetch specific question paper
                                            database.collection("categories").document(catId)
                                                    .collection("subCategories").document(subcatId)
                                                    .collection(studyCategoryName).document(subjectId)
                                                    .collection("subject_question_paper").whereEqualTo("qpName", qpName).get()
                                                    .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                        if (queryDocumentSnapshots3.isEmpty()) {
                                                            dialog.dismiss();
                                                            Toast.makeText(AddSubjectQuestion.this, "No question papers found", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }
                                                        String qpID = queryDocumentSnapshots3.getDocuments().get(0).getId();
                                                        DocumentReference qpDocRef = database.collection("categories").document(catId)
                                                                .collection("subCategories").document(subcatId)
                                                                .collection(studyCategoryName).document(subjectId)
                                                                .collection("subject_question_paper").document(qpID);

                                                        // Fetch existing questions
                                                        qpDocRef.collection("questions").document(qId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                Question question = documentSnapshot.toObject(Question.class);
                                                                if (question != null) {
                                                                    // Update the UI elements with the fetched data
                                                                    binding.question.setText(question.getQuestion());
                                                                    binding.option1.setText(question.getOption1());
                                                                    binding.option2.setText(question.getOption2());
                                                                    binding.option3.setText(question.getOption3());
                                                                    binding.option4.setText(question.getOption4());
                                                                    binding.answer.setText(question.getAnswer());
                                                                    binding.solution.setText(question.getSolution());
                                                                    binding.categoryName.setText(categoryName);
                                                                    binding.subcategoryName.setText(subCategoryName);
                                                                    binding.studyCategory.setText(studyCategoryName);
                                                                    binding.subject.setText(subjectName);
                                                                    binding.qpName.setText(qpName);
                                                                    binding.qtype.setText(question.getQtype());
                                                                    binding.qtype.setText(question.getQtype());
                                                                    index = question.getIndex();
                                                                    if (question.getqImage() != null) {
                                                                        Glide.with(AddSubjectQuestion.this)
                                                                                .load(question.getqImage())
                                                                                .into(binding.image);

                                                                        downloadUrlLink = question.getqImage();
                                                                    }
                                                                    if (question.getSolutionImage()!=null){
                                                                        Glide.with(AddSubjectQuestion.this)
                                                                                .load(question.getSolutionImage())
                                                                                .into(binding.solutionImage);
                                                                        solutiondownloadUrlLink = question.getSolutionImage();
                                                                    }
                                                                } else {
                                                                    Toast.makeText(AddSubjectQuestion.this, "No such question found.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                    })
                                                    .addOnFailureListener(e -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(AddSubjectQuestion.this, "Failed to fetch question papers", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(AddSubjectQuestion.this, "Failed to fetch study categories", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(AddSubjectQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(AddSubjectQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to handle errors
    private void handleError(String message) {
        dialog.dismiss();
        Toast.makeText(AddSubjectQuestion.this, message, Toast.LENGTH_SHORT).show();
    }
    private void deleteImage() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrlLink);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AddSubjectQuestion.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                binding.image.setImageResource(R.drawable.baseline_image_24);
                imageUri = null;
                downloadUrlLink = null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddSubjectQuestion.this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImage(Uri imageUri) {

        StorageReference storageRef = storage.getReference().child("question_images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                updateQuestion( downloadUri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddSubjectQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void updateQuestion( String qImage) {

        Toast.makeText(this, String.valueOf(index), Toast.LENGTH_SHORT).show();
        database.collection("categories").whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddSubjectQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String catId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    // Fetch sub-categories
                    database.collection("categories").document(catId).collection("subCategories")
                            .whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    dialog.dismiss();
                                    Toast.makeText(AddSubjectQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddSubjectQuestion.this, "No Subject found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            String subjectId = queryDocumentSnapshots2.getDocuments().get(0).getId();

                                            // Fetch specific question paper
                                            database.collection("categories").document(catId)
                                                    .collection("subCategories").document(subcatId)
                                                    .collection(studyCategoryName).document(subjectId)
                                                    .collection("subject_question_paper").whereEqualTo("qpName", qpName).get()
                                                    .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                        if (queryDocumentSnapshots3.isEmpty()) {
                                                            dialog.dismiss();
                                                            Toast.makeText(AddSubjectQuestion.this, "No question papers found", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }
                                                        String qpID = queryDocumentSnapshots3.getDocuments().get(0).getId();
                                                        DocumentReference qpDocRef = database.collection("categories").document(catId)
                                                                .collection("subCategories").document(subcatId)
                                                                .collection(studyCategoryName).document(subjectId)
                                                                .collection("subject_question_paper").document(qpID);

                                                        // Fetch existing questions
                                                        qpDocRef.collection("questions")
                                                                .whereEqualTo("index", index)
                                                                .get()
                                                                .addOnSuccessListener(questionsTask -> {
                                                                    if (!questionsTask.isEmpty()) {
                                                                        // Question exists, update it
                                                                        DocumentReference questionDocRef = questionsTask.getDocuments().get(0).getReference();
                                                                        Map<String, Object> updatedFields = new HashMap<>();

                                                                        // Always update the question text
                                                                        updatedFields.put("question", question);
                                                                        updatedFields.put("qImage", (qImage == null || qImage.isEmpty()) ? null : qImage);
                                                                        updatedFields.put("solutionImage", (solutiondownloadUrlLink == null || solutiondownloadUrlLink.isEmpty()) ? null : solutiondownloadUrlLink);

                                                                        // Update the fields based on question type
                                                                        if (type.equals("MCQ")) {
                                                                            updatedFields.put("option1", option1);
                                                                            updatedFields.put("option2", option2);
                                                                            updatedFields.put("option3", option3);
                                                                            updatedFields.put("option4", option4);
                                                                            updatedFields.put("answer", answer);
                                                                            updatedFields.put("solution", solution);

                                                                        } else if (type.equals("MSQ")) {
                                                                            updatedFields.put("option1", option1);
                                                                            updatedFields.put("option2", option2);
                                                                            updatedFields.put("option3", option3);
                                                                            updatedFields.put("option4", option4);
                                                                            updatedFields.put("answer", answer);
                                                                            updatedFields.put("solution", solution);

                                                                        } else if (type.equals("OCQ")) {
                                                                            updatedFields.put("answer", answer);
                                                                            updatedFields.put("solution", solution);

                                                                        } else {
                                                                            dialog.dismiss();
                                                                            Toast.makeText(AddSubjectQuestion.this, "Invalid question type", Toast.LENGTH_SHORT).show();
                                                                            return;
                                                                        }

                                                                        questionDocRef.update(updatedFields)
                                                                                .addOnCompleteListener(task -> {
                                                                                    dialog.dismiss();
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(AddSubjectQuestion.this, "Question Updated Successfully", Toast.LENGTH_SHORT).show();
                                                                                        clearInputFields(type);
                                                                                    } else {
                                                                                        Toast.makeText(AddSubjectQuestion.this, "Failed to update question", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(AddSubjectQuestion.this, "Question not found", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    dialog.dismiss();
                                                                    handleError("Error fetching questions");
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(AddSubjectQuestion.this, "Failed to fetch question papers", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(AddSubjectQuestion.this, "Failed to fetch study categories", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(AddSubjectQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(AddSubjectQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                });
    }
    private void updateSolutionImage() {
        StorageReference storageRef = storage.getReference().child("solutionImage/" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(solutionImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                solutiondownloadUrlLink = downloadUri.toString();
                                if (imageUri!=null){
                                    updateImage(imageUri);
                                }else{
                                    updateQuestion( null);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddSubjectQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void handleImageResult(int requestCode, Intent data) {
        if (data != null) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    String imageName = getFileName(imageUri);

                    if (requestCode == SELECT_QUESTION_IMAGE_FOLDER) {
                        questionImageMap.put(imageName, imageUri.toString()); // Order maintain hoga
                    } else if (requestCode == SELECT_SOLTUTION_IMAGE_FOLDER) {
                        solutionImageMap.put(imageName, imageUri.toString()); // Order maintain hoga
                    }
                }
            } else {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    String imageName = getFileName(imageUri);

                    if (requestCode == SELECT_QUESTION_IMAGE_FOLDER) {
                        questionImageMap.put(imageName, imageUri.toString());
                    } else if (requestCode == SELECT_SOLTUTION_IMAGE_FOLDER) {
                        solutionImageMap.put(imageName, imageUri.toString());
                    }
                }
            }
        }
    }


    // Function to extract file name from URI
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadNextImage( Iterator<Map.Entry<String, String>> questionIterator,
                                  ProgressDialog progressDialog, int currentIndex, int totalQuestions) {
        if (!questionIterator.hasNext()) {
            progressDialog.dismiss();
            Toast.makeText(progressDialog.getContext(), "All images uploaded successfully!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map.Entry<String, String> questionEntry = questionIterator.next();
        questionName = questionEntry.getKey();
        String questionUrl = questionEntry.getValue();
        if (solutionImageMap.containsKey(questionName)) {
            String solutionUrl = solutionImageMap.get(questionName);
            solutionImageUri = Uri.parse(solutionUrl);
            imageUri =Uri.parse(questionUrl);
            multiple = "multiple";
            progressDialog.setMessage("Uploading " + currentIndex + "/" + totalQuestions + ": " + questionName);
            uploadSolutionImage();
        } else {
            uploadNextImage(questionIterator, progressDialog, currentIndex + 1, totalQuestions);
        }
    }
}
