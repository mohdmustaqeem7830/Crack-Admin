package Mohammad.mustaqeem.crackadmin.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import Mohammad.mustaqeem.crackadmin.EditingTools.EditPdf;
import Mohammad.mustaqeem.crackadmin.EditingTools.EditQuestion;
import Mohammad.mustaqeem.crackadmin.EditingTools.EditQuestionPaper;
import Mohammad.mustaqeem.crackadmin.EditingTools.EditSubject;
import Mohammad.mustaqeem.crackadmin.R;
import Mohammad.mustaqeem.crackadmin.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddCategoryScreen.class);
                intent.putExtra("layout","category");
                startActivity(intent);
            }
        });

        binding.addSubCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddCategoryScreen.class);
                intent.putExtra("layout","subCategory");
                startActivity(intent);
            }
        });

        binding.quePaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, addQuestionPaper.class));

            }
        });

        binding.editQuePaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditQuestionPaper.class));
            }
        });



        binding.question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddQuestion.class));
            }
        });

        binding.subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, addQuestionPaper.class);
                intent.putExtra("subject","subject");
                startActivity(intent);
            }
        });
        
        binding.editSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditSubject.class);
                intent.putExtra("subject","subject");
                startActivity(intent);
            }
        });

        binding.addSubjectQuetion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddSubjectQuestion.class));
            }
        });


        binding.editSubjectQuetion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditQuestion.class));
            }
        });


        binding.addPyqPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPDF.class);
                intent.putExtra("studyCategoryName","PYQ PDF");
                startActivity(intent);
            }
        });

        binding.editPyqPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditPdf.class);
                intent.putExtra("studyCategoryName","PYQ PDF");
                startActivity(intent);
            }
        });


        binding.addAnswerKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPDF.class);
                intent.putExtra("studyCategoryName","Exam Answerkey");
                startActivity(intent);
            }
        });

        binding.editAnswerKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditPdf.class);
                intent.putExtra("studyCategoryName","Exam Answerkey");
                startActivity(intent);
            }
        });

        binding.addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPDF.class);
                intent.putExtra("studyCategoryName","Course Books");
                startActivity(intent);
            }
        });

        binding.editBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditPdf.class);
                intent.putExtra("studyCategoryName","Course Books");
                startActivity(intent);
            }
        });

        binding.addNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPDF.class);
                intent.putExtra("studyCategoryName","Subject Notes");
                startActivity(intent);
            }
        });

        binding.editSubjectNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditPdf.class);
                intent.putExtra("studyCategoryName","Subject Notes");
                startActivity(intent);
            }
        });

        binding.addBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Banner.class);
                startActivity(intent);
            }
        });



        binding.question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddQuestion.class));
            }
        });




//        binding.deleteCatBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, DeleteCat.class));
//
//            }
//        });
//
//        binding.DeleteQuePaperBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, DeleteQuestionPaper.class));
//            }
//        });


        binding.EditQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditQuestion.class));

            }
        });


    }
}