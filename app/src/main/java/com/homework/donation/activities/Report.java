package com.homework.donation.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.homework.donation.api.DonationApi;
import com.homework.donation.models.Donation;
import com.homework.donation.R;
import java.util.List;

public class Report extends Base {
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DonationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        listView = findViewById(R.id.report_list);
        mSwipeRefreshLayout = findViewById(R.id.report_swipe_refresh_layout);
        new GetAllTask(this).execute("/donations");
        mSwipeRefreshLayout.setOnRefreshListener(
                () -> new GetAllTask(Report.this).execute("/donations")
        );

    }


    //    DONATION ADAPTER
    class DonationAdapter extends ArrayAdapter<Donation> {
        private final Context context;

        public List<Donation> donations;

        public DonationAdapter(Context context, List<Donation> donations) {
            super(context, R.layout.row_donate, donations);
            this.context = context;
            this.donations = donations;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("ViewHolder")
            View view = inflater.inflate(R.layout.row_donate, parent, false);
            Donation donation = donations.get(position);

            TextView amountView = view.findViewById(R.id.row_amount);
            TextView methodView = view.findViewById(R.id.row_method);
            TextView upvotesView = view.findViewById(R.id.row_upvotes);

            amountView.setText(String.format("$%d", donation.amount));
            methodView.setText(donation.paymentType);
            upvotesView.setText(String.valueOf(donation.upvotes));
            view.setTag(donation.id);

            ImageView imgDelete = view.findViewById(R.id.imgDelete);
            imgDelete.setTag(donation);
            imgDelete.setOnClickListener(view1 -> {
                Log.v("tag", String.valueOf(view1.getTag()));
                if (view1.getTag() instanceof Donation) {
                    onDonationDelete((Donation) view1.getTag());
                }
            });

            return view;
        }

        @Override
        public int getCount() {
            return donations.size();
        }
    }

    public void onDonationDelete(final Donation donation) {
        String stringId = donation.id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Donation?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete the 'Donation with ID' \n [ "
                + stringId + " ] ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, id) -> new DeleteTask(Report.this).execute("/donations", donation.id)).setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }


    //    GET ALL TASK
    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {
        protected ProgressDialog dialog;
        protected Context context;

        public GetAllTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }

        @Override
        protected List<Donation> doInBackground(String... params) {
            try {
                return DonationApi.getAll(params[0]);
            } catch (Exception e) {
                Log.v("ASYNC", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);
            app.donations = result;
            adapter = new DonationAdapter(context, app.donations);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((adapterView, donate, i, l) -> {
                String _id = donate.getTag().toString();
                new GetTask(Report.this).execute("/donations", _id);
            });
            mSwipeRefreshLayout.setRefreshing(false);

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    //    GET TASK
    private class GetTask extends AsyncTask<String, Void, Donation> {
        protected ProgressDialog dialog;
        protected Context context;

        public GetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donation Details");
            this.dialog.show();
        }

        @Override
        protected Donation doInBackground(String... params) {
            try {
                return DonationApi.get(params[0], params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Donation result) {
            super.onPostExecute(result);
            Toast.makeText(Report.this, "Donation Data [ " + result.upvotes +
                    "]\n " +
                    "With ID of [" + result.id + "]", Toast.LENGTH_LONG).show();
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    //  DELETE TASK
    private class DeleteTask extends AsyncTask<String, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;

        public DeleteTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donation");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return DonationApi.delete(params[0], params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.v("donate", "DELETE REQUEST : " + result);
            new GetAllTask(Report.this).execute("/donations");
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }


}
