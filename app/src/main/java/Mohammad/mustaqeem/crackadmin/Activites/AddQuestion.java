package Mohammad.mustaqeem.crackadmin.Activites;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Map;

import Mohammad.mustaqeem.crackadmin.Adapters.EditQuestionAdapter;
import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.Model.Subject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityAddQuestionBinding;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityAddQuestionPaperBinding;

public class AddQuestion extends AppCompatActivity {

    ActivityAddQuestionBinding binding;
    String[] categoryArray;
    String[] studyArray;
    String[] subCategoryArray;
    String[] qpArray;
    Dialog cropDialogue;
    String[] typeArray;
    FirebaseFirestore database;
    private FirebaseStorage storage;
    int UCROP_REQUEST_CODE = 35;
    int UCROP_SOLUTION_REQUEST_CODE = 45;

    String catId,subId,qpId;
    HashMap<String, String> questionImageMap = new HashMap<>();
    HashMap<String, String> solutionImageMap = new HashMap<>();

    String downloadUrlLink,solutiondownloadUrlLink;

    String categoryName, subCategoryName, studyCategoryName, qpName, qId;

    ArrayList<AddCategoryModel> categoriesList;
    ArrayList<AddSubCategoryModel> subCategoriesList;

    ArrayList<AddQuestionPaperModel> qplist;

    int SELECT_IMAGE_REQUEST_CODE = 25;
    int SELECT_QUESTION_IMAGE_FOLDER = 50;
    int SELECT_SOLTUTION_IMAGE_FOLDER = 55;
    int SELECT_SOLUTION_IMAGE_REQUEST_CODE = 30;
    private Uri imageUri,solutionImageUri;

    private ProgressDialog dialog;

    String checkAnswer = null;
    int index;

    String type = "MCQ";

    String question, option1, option2, option3, option4, answer,solution;


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
                Toast.makeText(AddQuestion.this, "Question "+questionImageMap.size(), Toast.LENGTH_SHORT).show();
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
                    binding.check1.setVisibility(View.GONE);
                    binding.check2.setVisibility(View.GONE);
                    binding.check3.setVisibility(View.GONE);
                    binding.check4.setVisibility(View.GONE);
                    binding.question.setVisibility(View.VISIBLE);
                    binding.answerInput.setVisibility(View.VISIBLE);
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
                    binding.solution.setVisibility(View.VISIBLE);
                    binding.solutionImage.setVisibility(View.VISIBLE);
                    binding.answer.setVisibility(View.GONE);

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
        binding.selectSolutionImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_SOLUTION_IMAGE_REQUEST_CODE);
            }
        });


        binding.categoryName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categoryName = binding.categoryName.getText().toString();
                getSubCategory(categoryName);
            }
        });

        binding.qpName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                qpName = binding.qpName.getText().toString();
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
                getQuestionPaperList(categoryName, subCategoryName, studyCategoryName);

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


        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question = binding.question.getText().toString();
                solution = binding.solution.getText().toString();
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                qpName = binding.qpName.getText().toString();
                dialog.show();

                if (type.equals("MCQ") || type.equals("MSQ")) {

                    option1 = binding.option1.getText().toString();
                    option2 = binding.option2.getText().toString();
                    option3 = binding.option3.getText().toString();
                    option4 = binding.option4.getText().toString();
                    answer = checkAnswer;
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() || answer.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            if (solutionImageUri!=null){
                                uploadSolutionImage();
                            }else{
                                uploadImage(imageUri, categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer);
                            }
                        } else {
                            if (solutionImageUri!=null){
                                uploadSolutionImage();
                            }else{
                                uploadQuestion(categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer, null);
                            }
                        }
                    }
                } else if (type.equals("OCQ")) {
                    answer = binding.answer.getText().toString();
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() || answer.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            uploadImage(imageUri, categoryName, subCategoryName, studyCategoryName, qpName, question, null, null, null, null, answer);
                        } else {
                            uploadQuestion(categoryName, subCategoryName, studyCategoryName, qpName, question, null, null, null, null, answer, null);
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
                solution = binding.solution.getText().toString();
                categoryName = binding.categoryName.getText().toString();
                subCategoryName = binding.subcategoryName.getText().toString();
                studyCategoryName = binding.studyCategory.getText().toString();
                qpName = binding.qpName.getText().toString();
                dialog.show();
                if (type.equals("MCQ") || type.equals("MSQ")) {
                    option1 = binding.option1.getText().toString();
                    option2 = binding.option2.getText().toString();
                    option3 = binding.option3.getText().toString();
                    option4 = binding.option4.getText().toString();
                    answer = checkAnswer;
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty()|| option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() || answer.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {

                            if (solutionImageUri!=null){
                                updateSolutionImage();
                            }else{
                                updateImage(imageUri, categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer);
                            }

                        } else {
                            if (solutionImageUri!=null){
                                updateSolutionImage();
                            }else{
                                updateQuestion(categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer, downloadUrlLink);

                            }
                        }
                    }
                }  else if (type.equals("OCQ")) {

                    answer = binding.answer.getText().toString();
                    if (categoryName.isEmpty() || subCategoryName.isEmpty() || studyCategoryName.isEmpty() || qpName.isEmpty() || answer.isEmpty()) {
                        dialog.dismiss();
                        Toast.makeText(AddQuestion.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            updateImage(imageUri, categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer);
                        } else {
                            updateQuestion(categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer, downloadUrlLink);
                        }
                    }
                }
            }
        });

        binding.abcCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.option1.setText("a");
                binding.option2.setText("b");
                binding.option3.setText("c");
                binding.option4.setText("d");
            }
        });

        binding.smallRoman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.option1.setText("(i)");
                binding.option2.setText("(ii)");
                binding.option3.setText("(iii)");
                binding.option4.setText("(iv)");
            }
        });

        binding.roman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.option1.setText("(I)");
                binding.option2.setText("(II)");
                binding.option3.setText("(III)");
                binding.option4.setText("(IV)");
            }
        });

        binding.clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.option1.setText("");
                binding.option2.setText("");
                binding.option3.setText("");
                binding.option4.setText("");
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





    private void startCrop(@NonNull Uri uri, int code) {
        // Make the destination file name unique
        String destinationFileName = (code == UCROP_REQUEST_CODE)
                ? "CroppedQuestionImage.png"
                : "CroppedSolutionImage.png";

        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(100);
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(true);
        UCrop.of(uri, destinationUri)
                .withAspectRatio(0.5f, 0.5f)
                .withMaxResultSize(800, 800)
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
                                                Toast.makeText(AddQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(AddQuestion.this, "No categories found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddQuestion.this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddQuestion.this, "No sub-categories found", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(AddQuestion.this, "Failed to fetch question papers", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(AddQuestion.this, "Failed to fetch sub-categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearInputFields(String type) {
        binding.question.setText("");
        binding.image.setImageResource(R.drawable.baseline_image_24);
        binding.solutionImage.setImageResource(R.drawable.baseline_image_24);
        solutionImageUri=null;
        imageUri = null;
        if (type.equals("MCQ") || type.equals("MSQ")) {
            binding.option1.setText("1");
            binding.option2.setText("2");
            binding.option3.setText("3");
            binding.option4.setText("4");
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
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (type.equals("MSQ")) {
                // For "MSQ", allow multiple selections and concatenate text
                StringBuilder selectedAnswers = new StringBuilder();
                if (binding.check1.isChecked()) {
                    selectedAnswers.append(binding.option1.getText().toString()).append(" ");
                }
                if (binding.check2.isChecked()) {
                    selectedAnswers.append(binding.option2.getText().toString()).append(" ");
                }
                if (binding.check3.isChecked()) {
                    selectedAnswers.append(binding.option3.getText().toString()).append(" ");
                }
                if (binding.check4.isChecked()) {
                    selectedAnswers.append(binding.option4.getText().toString()).append(" ");
                }
                // Remove the trailing space, if any
                checkAnswer = selectedAnswers.toString().trim();
            } else {
                // For single selection, uncheck other checkboxes
                if (isChecked) {
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

    private void uploadImage(Uri imageUri, String categoryName, String subCategoryName, String studyCategoryName, String questionPaper, String question, String option1, String option2, String option3, String option4, String answer) {

        StorageReference storageRef = storage.getReference().child(categoryName+"/"+subCategoryName+"/"+studyCategoryName+"/"+qpName+"/"+"question_images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {

                                downloadUrlLink = downloadUri.toString();

                                uploadQuestion(categoryName, subCategoryName, studyCategoryName, questionPaper, question, option1, option2, option3, option4, answer, downloadUrlLink);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void uploadSolutionImage() {

        StorageReference storageRef = storage.getReference().child(categoryName+"/"+subCategoryName+"/"+studyCategoryName+"/"+qpName+"/"+"solutionImage/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(solutionImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {

                                solutiondownloadUrlLink = downloadUri.toString();
                                if(imageUri!=null){
                                    uploadImage(imageUri, categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer);

                                }else{
                                    uploadQuestion(categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer, null);
                                }

                                Toast.makeText(AddQuestion.this, "uploaded", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void uploadQuestion(String categoryName, String subCategoryName, String studyCategoryName,
                               String questionPaper, String question, String option1, String option2,
                               String option3, String option4, String answer, String qImage) {


        // Fetch the study category
        database.collection("categories").document(catId).collection("subCategories").document(subId)
                .collection(studyCategoryName).whereEqualTo("qpName", questionPaper).get()
                .addOnSuccessListener(qpTask -> {
                    if (qpTask.isEmpty()) {
                        handleError("Question Paper not found");
                        return;
                    }
                    DocumentReference qpDocRef = qpTask.getDocuments().get(0).getReference();

                    // Fetch existing questions and add new one
                    qpDocRef.collection("questions").get()
                            .addOnSuccessListener(questionsTask -> {
                                int newIndex = questionsTask.size() + 1;
                                Question questionModel;

                                switch (type) {
                                    case "MCQ":
                                        questionModel = new Question(question, option1, option2, option3, option4, answer, newIndex, qImage, type, solution, solutiondownloadUrlLink);
                                        break;
                                    case "MSQ":
                                        questionModel = new Question(question, option1, option2, option3, option4, answer, newIndex, qImage, type, solution, solutiondownloadUrlLink);
                                        break;
                                    case "OCQ":
                                        questionModel = new Question(question, answer, newIndex, qImage, type, solution, solutiondownloadUrlLink);
                                        break;
                                    default:
                                        handleError("Invalid question type");
                                        return;
                                }

                                qpDocRef.collection("questions").add(questionModel)
                                        .addOnCompleteListener(task -> {
                                            dialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Toast.makeText(AddQuestion.this, "Question Added Successfully", Toast.LENGTH_SHORT).show();
                                                clearInputFields(type);
                                            } else {
                                                Toast.makeText(AddQuestion.this, "Failed to add question", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> handleError("Error adding question"));
                            })
                            .addOnFailureListener(e -> handleError("Error fetching questions"));
                })
                .addOnFailureListener(e -> handleError("Error fetching Question Paper"));
    }

    // Helper method to handle errors
    private void handleError(String message) {
        dialog.dismiss();
        Toast.makeText(AddQuestion.this, message, Toast.LENGTH_SHORT).show();
    }

    private void fetchAndUpdateUI() {
        CollectionReference categoriesRef = database.collection("categories");

        // Fetch category document
        categoriesRef.whereEqualTo("categoryName", categoryName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showToastAndDismissDialog("No categories found");
                        return;
                    }

                    catId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    CollectionReference subCategoriesRef = categoriesRef.document(catId).collection("subCategories");

                    // Fetch sub-category document
                    subCategoriesRef.whereEqualTo("subCategoryName", subCategoryName).get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (queryDocumentSnapshots1.isEmpty()) {
                                    showToastAndDismissDialog("No sub-categories found");
                                    return;
                                }

                                subId = queryDocumentSnapshots1.getDocuments().get(0).getId();
                                CollectionReference questionsRef = subCategoriesRef.document(subId)
                                        .collection(studyCategoryName);

                                // Fetch question papers
                                questionsRef.whereEqualTo("qpName", qpName).get()
                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                            if (queryDocumentSnapshots2.isEmpty()) {
                                                showToastAndDismissDialog("No question papers found");
                                                return;
                                            }

                                            qpId = queryDocumentSnapshots2.getDocuments().get(0).getId();
                                            questionsRef.document(qpId).collection("questions").document(qId).get()
                                                    .addOnSuccessListener(documentSnapshot -> {
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
                                                            binding.qpName.setText(qpName);
                                                            binding.qtype.setText(question.getQtype());
                                                            binding.qtype.setText(question.getQtype());
                                                            index = question.getIndex();
                                                            if (question.getqImage() != null) {
                                                                Glide.with(AddQuestion.this)
                                                                        .load(question.getqImage())
                                                                        .into(binding.image);

                                                                downloadUrlLink = question.getqImage();
                                                            }
                                                            if (question.getSolutionImage()!=null){
                                                                Glide.with(AddQuestion.this)
                                                                        .load(question.getSolutionImage())
                                                                        .into(binding.solutionImage);
                                                                solutiondownloadUrlLink = question.getSolutionImage();
                                                            }
                                                        } else {
                                                            Toast.makeText(AddQuestion.this, "No such question found.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch question details"));

                                        })
                                        .addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch question papers"));

                            })
                            .addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch sub-categories"));

                })
                .addOnFailureListener(e -> showToastAndDismissDialog("Failed to fetch categories"));
    }

    private void showToastAndDismissDialog(String message) {

        Toast.makeText(AddQuestion.this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();


    }




    private void deleteImage() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrlLink);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AddQuestion.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                binding.image.setImageResource(R.drawable.baseline_image_24);
                imageUri = null;
                downloadUrlLink = null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddQuestion.this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.image.setImageResource(R.drawable.baseline_image_24);
                imageUri = null;
                downloadUrlLink = null;
            }
        });
    }
    private void deleteSolutionImage() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(solutiondownloadUrlLink);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AddQuestion.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                binding.solutionImage.setImageResource(R.drawable.baseline_image_24);
                solutionImageUri = null;
                solutiondownloadUrlLink = null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddQuestion.this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.solutionImage.setImageResource(R.drawable.baseline_image_24);
                solutionImageUri = null;
                solutiondownloadUrlLink = null;
            }
        });
    }

    private void updateImage(Uri imageUri, String categoryName, String subCategoryName, String studyCategoryName, String qpName, String question, String option1, String option2, String option3, String option4, String answer) {

        StorageReference storageRef = storage.getReference().child(categoryName+"/"+subCategoryName+"/"+studyCategoryName+"/"+qpName+"/"+"question_images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                updateQuestion(categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer, downloadUri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void updateSolutionImage() {
        StorageReference storageRef = storage.getReference().child(categoryName+"/"+subCategoryName+"/"+studyCategoryName+"/"+qpName+"/"+"solutionImage/" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(solutionImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                solutiondownloadUrlLink = downloadUri.toString();
                                if (imageUri!=null){
                                    updateImage(imageUri, categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer);
                                }else{
                                    updateQuestion(categoryName, subCategoryName, studyCategoryName, qpName, question, option1, option2, option3, option4, answer, null);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddQuestion.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateQuestion(String categoryName, String subCategoryName, String studyCategoryName, String qpName,
                                String question, String option1, String option2, String option3, String option4,
                                String answer, String qImage) {

        database.collection("categories").document(catId).collection("subCategories").document(subId)
                .collection(studyCategoryName).document(qpId).collection("questions")
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
                            Toast.makeText(AddQuestion.this, "Invalid question type", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Perform the update operation
                        questionDocRef.update(updatedFields)
                                .addOnCompleteListener(task -> {
                                    dialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AddQuestion.this, "Question Updated Successfully", Toast.LENGTH_SHORT).show();
                                        clearInputFields(type);
                                    } else {
                                        Toast.makeText(AddQuestion.this, "Failed to update question", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        dialog.dismiss();
                        Toast.makeText(AddQuestion.this, "Question not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    handleError("Error fetching questions");
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
                        questionImageMap.put(imageName, imageUri.toString());
                    } else if (requestCode == SELECT_SOLTUTION_IMAGE_FOLDER) {
                        solutionImageMap.put(imageName, imageUri.toString());
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
   




}
