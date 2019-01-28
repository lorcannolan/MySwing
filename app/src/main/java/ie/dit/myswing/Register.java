package ie.dit.myswing;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.InputType;
import android.view.View;
import android.webkit.WebStorage;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.Calendar;

public class Register extends AppCompatActivity {

    private EditText fname, sname, email, pwd, dob, gender;
    private AppCompatButton register;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        fname = (EditText)findViewById(R.id.fname);
        sname = (EditText)findViewById(R.id.sname);
        email = (EditText)findViewById(R.id.email);
        pwd = (EditText)findViewById(R.id.password);

        // Capture Date of Birth text field
        dob = (EditText)findViewById(R.id.dob);
        dob.setInputType(InputType.TYPE_NULL);
        // Upon clicking text field
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                /*
                Date Picker Dialog Constructor Parameters:
                    - context
                    - OnDateSet listener
                    - year, month and day default values obtained above
                */
                DatePickerDialog datePickerDialog = new DatePickerDialog(Register.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // When OK is selected and date is set, EditText field is populated with the selected values
                                dob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        gender = (EditText)findViewById(R.id.gender);
        gender.setInputType(InputType.TYPE_NULL);
        gender.setOnClickListener(new View.OnClickListener() {
            /*
            Source code on Dialogs found at:
                - https://developer.android.com/guide/topics/ui/dialogs
                - https://developer.android.com/reference/android/app/AlertDialog.Builder
            */
            @Override
            public void onClick(View v) {
                // Custom built dialog pop-up
                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                builder.setTitle("Which Tee Box Do You Play From?");
                builder.setItems(R.array.teeBoxes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Resources res = getResources();
                        String[] teeBoxes = res.getStringArray(R.array.teeBoxes);
                        gender.setText(teeBoxes[which]);
                    }
                });
                builder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                AlertDialog chooseTeeBoxes = builder.create();
                chooseTeeBoxes.show();
            }
        });

        register = (AppCompatButton)findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fname.getText().toString().contentEquals("")
                        || sname.getText().toString().contentEquals("")
                        || email.getText().toString().contentEquals("")
                        || pwd.getText().toString().contentEquals("")
                        || dob.getText().toString().contentEquals("")
                        || gender.getText().toString().contentEquals("")) {
                    Toast.makeText(Register.this, "All fields must be filled", Toast.LENGTH_LONG).show();
                }
                else if (pwd.getText().toString().length() < 6) {
                    pwd.setText("");
                    pwd.setHint("Must be a minimum 6 characters long");
                    pwd.setHintTextColor(Color.RED);
                    Toast.makeText(Register.this, "Password must be 6 or more characters in length", Toast.LENGTH_LONG).show();
                }
                else if (!email.getText().toString().contains("@") || !email.getText().toString().contains(".")) {
                    Toast.makeText(Register.this, "Invalid email address", Toast.LENGTH_LONG).show();
                }
                else {
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), pwd.getText().toString())
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Intent homeIntent = new Intent(Register.this, Home.class);
                                        startActivity(homeIntent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
