package ie.dit.myswing;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class Login extends AppCompatActivity {

    private AppCompatButton login, reg;
    private TextView title;
    private EditText mEmail, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    private String firstName, lastName, dob, teeBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);

        title = (TextView) findViewById(R.id.title);
        Typeface customFont = Typeface.createFromAsset(getAssets(),  "fonts/FontsFree-Net-Proxima-Nova-Bold.otf");
        title.setTypeface(customFont);

        login = (AppCompatButton)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmail.getText().toString().contentEquals("")
                        || mPassword.getText().toString().contentEquals("")
                        || !mEmail.getText().toString().contains("@")) {
                    Toast.makeText(Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
                else {
                    firebaseSignIn(false);
                }
            }
        });

        reg = (AppCompatButton)findViewById(R.id.register);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(Login.this, Register.class);
                startActivity(regIntent);
            }
        });
    }

    public void firebaseSignIn(boolean alreadySignedIn) {
        if (!alreadySignedIn) {
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                openApp();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else {
            openApp();
        }
    }

    public void openApp() {
        usersRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                firstName = dataSnapshot.child("first name").getValue().toString();
                lastName = dataSnapshot.child("last name").getValue().toString();
                dob = dataSnapshot.child("dob").getValue().toString();
                teeBox = dataSnapshot.child("tee box").getValue().toString();
                Intent homeIntent = new Intent(Login.this, Home.class);
                homeIntent.putExtra("UID", mAuth.getCurrentUser().getUid());
                homeIntent.putExtra("userFirstName", firstName);
                homeIntent.putExtra("userLastName", lastName);
                homeIntent.putExtra("DOB", dob);
                homeIntent.putExtra("teeBox", teeBox);
                startActivity(homeIntent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
           firebaseSignIn(true);
        }
    }
}
