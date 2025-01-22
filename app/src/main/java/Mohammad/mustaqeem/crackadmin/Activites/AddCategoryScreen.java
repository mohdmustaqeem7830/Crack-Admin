package Mohammad.mustaqeem.crackadmin.Activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Mohammad.mustaqeem.crackadmin.Model.AddCategoryModel;
import Mohammad.mustaqeem.crackadmin.Model.AddSubCategoryModel;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityAddCategoryScreenBinding;

public class AddCategoryScreen extends AppCompatActivity {
    ActivityAddCategoryScreenBinding binding;
    int SELECT_IMAGE_REQUEST_CODE = 25;
    private Uri imageUri;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    private ProgressDialog dialog;
    ArrayList<AddCategoryModel> categories;
    String[] classArray;

    String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAddCategoryScreenBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading Category");
        dialog.setCancelable(false);

        categories = new ArrayList<>();
         String layout = getIntent().getStringExtra("layout");
         if (layout.equals("category")){
             binding.catlayout.setVisibility(View.VISIBLE);
             binding.subcatlayout.setVisibility(View.GONE);
         }else {
             binding.catlayout.setVisibility(View.GONE);
             binding.subcatlayout.setVisibility(View.VISIBLE);
             getCategoryList();
         }



        binding.selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
            }
        });


        binding.createCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                String categoryName = binding.name.getText().toString();
                String categorySubCategory = binding.categorySubCategory.getText().toString();
                if(categoryName.isEmpty() || categorySubCategory.isEmpty()){
                    Toast.makeText(AddCategoryScreen.this, "Please fill all field", Toast.LENGTH_SHORT).show();
                }else{
                    uploadData(imageUri, categoryName,categorySubCategory);
                }
            }
        });

       binding.subcreateCatBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String catName = binding.classInput.getText().toString();
               String subCatName = binding.subname.getText().toString();
               if (catName.isEmpty()||subCatName.isEmpty()){
                   Toast.makeText(AddCategoryScreen.this, "Please fill all the field", Toast.LENGTH_SHORT).show();
               }
               else{
                   uploadSubCategory(catName,subCatName);
               }

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

    private void uploadData(Uri imageUri, String name,String categorySubCategory) {
        if (imageUri != null && name != null) {
            StorageReference storageRef = storage.getReference().child("Category_images/" + System.currentTimeMillis() + ".jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    upload(downloadUri, name,categorySubCategory);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddCategoryScreen.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void upload(Uri downloadUri, String name,String categorySubCategory) {
        AddCategoryModel model = new AddCategoryModel("",name, downloadUri.toString(),categorySubCategory);
        Toast.makeText(this, downloadUri.toString(), Toast.LENGTH_SHORT).show();
        firestore.collection("categories").document().set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                dialog.dismiss();
                Toast.makeText(AddCategoryScreen.this, "Category Added", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void getCategoryList(){
        firestore.collection("categories")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        categories.clear();
                        List<String> categoriesList = new ArrayList<>();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            AddCategoryModel model = snapshot.toObject(AddCategoryModel.class);
                            model.setCatId(snapshot.getId());
                            categories.add(model);
                            categoriesList.add(model.getcategoryName());
                        }
                        classArray = categoriesList.toArray(new String[0]);
                        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, classArray);
                        binding.classInput.setAdapter(classAdapter);

                    }
                });
    }
    public void uploadSubCategory(String catName, String subCatName) {
        AddSubCategoryModel model = new AddSubCategoryModel(subCatName,"");
        firestore.collection("categories").whereEqualTo("categoryName",catName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
               if(!queryDocumentSnapshots.isEmpty()){
                   
                   DocumentSnapshot snapshot  = queryDocumentSnapshots.getDocuments().get(0);
                   String documentId =snapshot.getId();


                   firestore.collection("categories").document(documentId).collection("subCategories").document().set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void unused) {
                           Toast.makeText(AddCategoryScreen.this, "Added Sub Category", Toast.LENGTH_SHORT).show();
                       }
                   });

               }
               
            }
        });
    }
}