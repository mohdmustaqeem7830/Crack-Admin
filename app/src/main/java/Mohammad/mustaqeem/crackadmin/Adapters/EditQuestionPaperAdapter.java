package Mohammad.mustaqeem.crackadmin.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import Mohammad.mustaqeem.crackadmin.Activites.AddQuestion;
import Mohammad.mustaqeem.crackadmin.Activites.AddSubjectQuestion;
import Mohammad.mustaqeem.crackadmin.Model.AddQuestionPaperModel;
import Mohammad.mustaqeem.crackadmin.Model.Question;
import Mohammad.mustaqeem.crackadmin.databinding.QuestionListItemsBinding;

public class EditQuestionPaperAdapter extends RecyclerView.Adapter<EditQuestionPaperAdapter.QuestionPaperViewHolder> {

    Context context;

    ArrayList<AddQuestionPaperModel> questionpaperArrayList;
    String categoryName,subCategoryName,studyCategoryName,subject,qpname;

    FirebaseFirestore database;

    ProgressDialog dialog;

    public EditQuestionPaperAdapter(Context context, ArrayList<AddQuestionPaperModel> questionPaperArrayList, String categoryName, String subCategoryName, String studyCategoryName, String subject){
        this.context = context;
        this.questionpaperArrayList = questionPaperArrayList;
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
    public QuestionPaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(Mohammad.mustaqeem.crackadmin.R.layout.question_list_items,parent,false);
        return  new EditQuestionPaperAdapter.QuestionPaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionPaperViewHolder holder, int position) {
        AddQuestionPaperModel questionPaperModel = questionpaperArrayList.get(position);
        holder.binding.qindex.setText(String.valueOf(position+1));
        holder.binding.qname.setText(questionPaperModel.getQpName());

        holder.binding.qDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                if (subject!=null){
//                    DeleteSubjectQuestion(question.getqId(),position);
                }else{
//                    Deletquestion(question.getqId(),position);

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
                    intent.putExtra("subject",subject);
                    intent.putExtra("qpId", questionPaperModel.getQpId());
                    context.startActivity(intent);
                }else{
                    Intent intent = new Intent(context, AddQuestion.class);
                    intent.putExtra("categoryName",categoryName);
                    intent.putExtra("subCategoryName",subCategoryName);
                    intent.putExtra("studyCategoryName",studyCategoryName);
                    intent.putExtra("qId",questionPaperModel.getQpId());
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return questionpaperArrayList.size();
    }


    public class QuestionPaperViewHolder extends RecyclerView.ViewHolder {

        QuestionListItemsBinding binding;
        public QuestionPaperViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = QuestionListItemsBinding.bind(itemView);
        }
    }
}
