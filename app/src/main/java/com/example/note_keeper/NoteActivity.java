package com.example.note_keeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

    public static final String NOTE_POSITION = "com.example.note_keeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // loads up layout resource
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // used for spinner
        mSpinnerCourses = findViewById(R.id.spinner_courses);

        // Load in data to spinner - thats in file
        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        // can be used with arrays or list
        // below format the selected item in our spinner
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        // dropdown list of courses
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Associates adapter with Spinner
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();

        // get title and text
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        // pass in local variables

        // display note if created, if not it is new - do not display
        if(!mIsNewNote)
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

    }

    // when you leave the note
    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling) {
            // remove from storing if new note
            if(mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            }
        } else {
            saveNote();
        }
    }

    // allows saving of note
    private void saveNote() {
        // currently selected note
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }


    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        // get list of courses from data manager
        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        int courseIndex = courses.indexOf(mNote.getCourse());

        spinnerCourses.setSelection(courseIndex);

        // sets the title
        textNoteTitle.setText(mNote.getTitle());

        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        // member fields to be preceded by m
        // if not found, we pass in POSITION_NOT_SET (-1)
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        // check if note is null
        mIsNewNote = position == POSITION_NOT_SET;
        // makes new note
        // if not new, we get that note at that position
        if(mIsNewNote){
            createNewNote();
        } else {
            mNote = DataManager.getInstance().getNotes().get(position);
        }

    }

    private void createNewNote() {
        // references data manager
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        // get note at that position
        mNote = dm.getNotes().get(mNotePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            // canceling note changes
            mIsCancelling = true;
            // ends activity
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // implicit intent to send email with not e
    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        // gives email a body
        String text = "Check out what I learned in this course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        // tells it its an email
        intent.setType("message/rfc2822");

        // subject
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        // body
        intent.putExtra(Intent.EXTRA_SUBJECT, text);
        // intent makes difference
        startActivity(intent);
    }
}