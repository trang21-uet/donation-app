package com.homework.donation.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.homework.donation.R;
import com.homework.donation.api.DonationApi;
import com.homework.donation.models.Donation;

import java.util.List;


public class Donate extends Base {

//    private AppBarConfiguration appBarConfiguration;

    private Button donateButton;
    private NumberPicker amountPicker;
    private RadioGroup paymentMethod;
    private ProgressBar progressBar;
    private EditText amountText;
    private TextView amountTotal;

    private int amountOfDonating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        donateButton = findViewById(R.id.donation_button);

        paymentMethod = findViewById(R.id.payment_methods);
        progressBar = findViewById(R.id.progress_donation);
        amountPicker = findViewById(R.id.donation_picker);
        amountText = findViewById(R.id.amount_donating);
        amountTotal = findViewById(R.id.total_so_far_value);

//        progressbar
        progressBar.setMax(10000);
        progressBar.setProgress(app.totalDonated);

//        donation_picker
        amountPicker.setMinValue(0);
        amountPicker.setMaxValue(999);
        amountTotal.setText(String.format("%d$", app.totalDonated));

        amountText.setInputType(InputType.TYPE_CLASS_NUMBER);


        donateButton.setOnClickListener(view -> {
            amountOfDonating = amountPicker.getValue();

            String message;
            if (amountOfDonating == 0 && app.totalDonated < app.target) {
                message = "Please enter the valid amount you want to donate!";
            } else if (app.totalDonated == app.target) {
                message = "Sorry! You have donated too much! Thank a lot!";
            } else if (app.totalDonated < app.target && app.totalDonated + amountOfDonating <= app.target) {
//                    Phương thức donate
                int paymentId = paymentMethod.getCheckedRadioButtonId();
                String method = paymentId == R.id.paypal_method ? "Paypal" : "Direct";
                Donation _newDonation = new Donation(amountOfDonating, method, 0);
                app.newDonation(_newDonation);
                new InsertTask(Donate.this).execute("/donations", _newDonation);
                message = String.format("You have donated %d$. Thank you so much!", amountOfDonating);
            } else {
                message = String.format("Sorry! The total amount you donate cannot exceed 10000$. You can only donate another %d$", app.target - app.totalDonated);
            }

            progressBar.setProgress(app.totalDonated);
            amountTotal.setText(String.format("%d$", app.totalDonated));

            Toast myToast = Toast.makeText(Donate.this, message, Toast.LENGTH_SHORT);
            myToast.show();
        });

        amountPicker.setOnValueChangedListener((donationPicker, oldValue, newValue) -> {
            amountOfDonating = newValue;
            String strAmountOfDonating = String.valueOf(amountOfDonating);
            amountText.setText(strAmountOfDonating);
            amountText.setSelection(strAmountOfDonating.length());
        });

        amountText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                String strText = s.toString();
                if (strText.length() == 0) {
                    amountOfDonating = 0;
                } else if (strText.length() > 3) {
                    amountOfDonating = 999;
                    amountText.setText("999");
                    amountText.setSelection(amountText.getText().length());
                } else {
                    amountOfDonating = Integer.parseInt(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                amountPicker.setValue(amountOfDonating);
            }
        });
    }

    public void onDonationDeleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete ALL Donations?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete ALL the Donations");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, id) -> {
            new ResetTask(Donate.this).execute("/donations");
            app.donations.clear();
        }).setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void reset(MenuItem item) {
        super.reset(item);
        onDonationDeleteAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetAllTask(this).execute("/donations");
    }


    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {
        protected ProgressDialog dialog;
        protected Context context;

        public GetAllTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 0);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }

        @Override
        protected List<Donation> doInBackground(String... params) {
            try {
                Log.v("donate", "Donation App Getting All Donations" + DonationApi.getAll(params[0]));

                return DonationApi.getAll(params[0]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);
            //use result to calculate the totalDonated amount here
            app.totalDonated = 0;
            Log.v("result", String.valueOf(result));
            app.donations = result;

            for (int i = 0; i < result.size(); i++) {
                app.totalDonated += result.get(i).amount;
                app.donations.get(i).id = result.get(i).id;
            }

            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    //    INSERT DONATE
    private class InsertTask extends AsyncTask<Object, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;

        public InsertTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Saving Donation....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            String res = null;
            try {
                Log.v("donate", "Donation App Inserting");
                res = DonationApi.insert((String) params[0], (Donation) params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            this.dialog.setMessage("Success....");
            this.dialog.show();
            if (this.dialog.isShowing())
                this.dialog.dismiss();
        }
    }

    //  RESET DONATION
    private class ResetTask extends AsyncTask<Object, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;

        public ResetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donations....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            String res = null;
            try {
                Log.v("ids", String.valueOf(params[1]));
                res = DonationApi.deleteAll((String) params[0]);
            } catch (Exception e) {
                Log.v("donate", " RESET ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            app.totalDonated = 0;
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }


}

