package com.example.reminderdemoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class AllReminderActivity extends AppCompatActivity {

    private RecyclerView mList;
    private ReminderAdapter mAdapter;

    private TextView mNoReminderView;
    private FloatingActionButton mAddReminderButton;
    private int mTempPost;
    private LinkedHashMap<Integer, Integer> IDmap = new LinkedHashMap<>();
    private ReminderDatabase rmdDb;
    private MultiSelector mMultiSelector = new MultiSelector();
    private ReminderReceiver mReminderReceiver;
    List<ReminderModel> mTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reminder);
        rmdDb=new ReminderDatabase(getApplicationContext());
        mReminderReceiver=new ReminderReceiver();

        mAddReminderButton = (FloatingActionButton) findViewById(R.id.add_reminder);
        mList = (RecyclerView) findViewById(R.id.reminder_list);
        mNoReminderView = (TextView) findViewById(R.id.no_reminder_text);

        mTest = rmdDb.getAllReminders();

        if (mTest.isEmpty()) {
            mNoReminderView.setVisibility(View.VISIBLE);
        }

        mList.setLayoutManager(getLayoutManager());
        registerForContextMenu(mList);
        mAdapter = new ReminderAdapter();
        mAdapter.setItemCount(getDefaultItemCount());
        mList.setAdapter(mAdapter);


        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddNewReminder.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
    }


    @SuppressLint("WrongConstant")
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }


    protected int getDefaultItemCount() {
        return 100;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAdapter.setItemCount(getDefaultItemCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<ReminderModel> mTest = rmdDb.getAllReminders();

        if (mTest.isEmpty()) {
            mNoReminderView.setVisibility(View.VISIBLE);
        } else {
            mNoReminderView.setVisibility(View.GONE);
        }

        mAdapter.setItemCount(getDefaultItemCount());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.actionSearch);

        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //mAdapter.notifyDataSetChanged();
               // mAdapter.setItemCount(getDefaultItemCount());
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                mAdapter.filter(newText);

                return false;
            }
        });
        return true;
    }

    private void selectReminder(int mClickID) {
        String mStringClickID =Integer.toString(mClickID);

        // Create intent to edit the reminder
        // Put reminder id as extra
      Intent i = new Intent(this, ReminderEditActivity.class);
      i.putExtra(ReminderEditActivity.EXTRA_REMINDER_ID, mStringClickID);
      startActivityForResult(i, 1);
    }
    /*private void selectReminder(int mClickID) {
        String mStringClickID = Integer.toString(mClickID);

        // Create intent to edit the reminder
        // Put reminder id as extra
      Intent i = new Intent(this, ReminderEditActivity.class);
      i.putExtra(ReminderEditActivity.EXTRA_REMINDER_ID, mStringClickID);
      startActivityForResult(i, 1);
    }*/

    private void selectReminderTime(String mClickID) {
        String mStringClickID = mClickID;

        // Create intent to edit the reminder
        // Put reminder id as extra
        Intent i = new Intent(this, ReminderEditActivity.class);
        i.putExtra(ReminderEditActivity.EXTRA_REMINDER_ID_TIME, mStringClickID);
        startActivityForResult(i, 1);
    }


    private androidx.appcompat.view.ActionMode.Callback mDeleteMode = new ModalMultiSelectorCallback(mMultiSelector) {

        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode actionMode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                // On clicking discard reminders
                case R.id.discard_reminder:
                    // Close the context menu
                    actionMode.finish();

                    // Get the reminder id associated with the recycler view item
                    for (int i = IDmap.size(); i >= 0; i--) {
                        if (mMultiSelector.isSelected(i, 0)) {
                            int id = IDmap.get(i);

                            // Get reminder from reminder database using id
                            ReminderModel temp = rmdDb.getReminder(id);
                            // Delete reminder
                            rmdDb.deleteReminder(temp);
                            // Remove reminder from recycler view
                            mAdapter.removeItemSelected(i);
                            // Delete reminder alarm
                            //mReminderReceiver.cancelAlarm(getApplicationContext(), id);
                        }
                    }

                    // Clear selected items in recycler view
                    mMultiSelector.clearSelections();
                    // Recreate the recycler items
                    // This is done to remap the item and reminder ids
                    mAdapter.onDeleteItem(getDefaultItemCount());

                    // Display toast to confirm delete
                    Toast.makeText(getApplicationContext(),
                            "Deleted",
                            Toast.LENGTH_SHORT).show();

                    // To check is there are saved reminders
                    // If there are no reminders display a message asking the user to create reminders
                    List<ReminderModel> mTest = rmdDb.getAllReminders();

                    if (mTest.isEmpty()) {
                        mNoReminderView.setVisibility(View.VISIBLE);
                    } else {
                        mNoReminderView.setVisibility(View.GONE);
                    }

                    return true;

                // On clicking save reminders
                case R.id.save_reminder:
                    // Close the context menu
                    actionMode.finish();
                    // Clear selected items in recycler view
                    mMultiSelector.clearSelections();
                    return true;

                default:
                    break;
            }
            return false;
        }
    };

    public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.VerticalItemHolder>{

        private ArrayList<ReminderItem> mItems;
        ArrayList<ReminderModel> filterlistOne;

    public ReminderAdapter() {
            mItems = new ArrayList<>();
        }

        public void setItemCount(int count) {
            mItems.clear();
            mItems.addAll(generateData(count));
            notifyDataSetChanged();
        }

        public void onDeleteItem(int count) {
            mItems.clear();
            mItems.addAll(generateData(count));
        }

        public void removeItemSelected(int selected) {
            if (mItems.isEmpty()) return;
            mItems.remove(selected);
            notifyItemRemoved(selected);
        }

        // View holder for recycler view items
        @Override
        public VerticalItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            View root = inflater.inflate(R.layout.recycle_items, container, false);

            return new VerticalItemHolder(root, this);
        }



        @Override
        public void onBindViewHolder(VerticalItemHolder itemHolder, int position) {
            ReminderItem item = mItems.get(position);
            itemHolder.setReminderTitle(item.mTitle);
            itemHolder.setReminderDateTime(item.mDateTime);
            itemHolder.setReminderRepeatInfo(item.mRepeat, item.mRepeatNo, item.mRepeatType);
            itemHolder.setActiveImage(item.mActive);
            itemHolder.setReminderID(item.mID);


        }

        private void filter(String text) {

            ArrayList<ReminderItem> filteredlist = new  ArrayList<ReminderItem>();

           for (int i =0; i<mItems.size();i++) {
                ReminderItem item = mItems.get(i);

                if (item.mTitle.toLowerCase().contains(text.toLowerCase())) {
                    filteredlist.add(new ReminderAdapter.ReminderItem(item.mID,item.mTitle,item.mDateTime,item.mRepeat,item.mRepeatNo,item.mRepeatType,item.mActive));
                }
            }
           /* for (ReminderItem item:mItems) {
                if (item.mTitle.toLowerCase().contains(text.toLowerCase())) {
                    filteredlist.add(new ReminderAdapter.ReminderItem(item.mTitle,item.mDateTime,item.mRepeat,item.mRepeatNo,item.mRepeatType,item.mActive));
                }
            }*/

            if (filteredlist.isEmpty()) {
                Toast.makeText(getApplicationContext(), "No Data Found..", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            } else {
                mItems.clear();
                mItems = filteredlist;

                notifyDataSetChanged();
            }

        }


        public void filterList(ArrayList<ReminderItem> filterlist) {
            // below line is to add our filtered
            // list in our course array list.
            mItems = filterlist;
            // below line is to notify our adapter
            // as change in recycler view data.
           notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        // Class for recycler view items
        public  class ReminderItem {
            private int mID;
            public String mTitle;
            public String mDateTime;
            public String mRepeat;
            public String mRepeatNo;
            public String mRepeatType;
            public String mActive;
            //public int mID =0;

            public ReminderItem(int mID,String Title, String DateTime, String Repeat, String RepeatNo, String RepeatType, String Active) {
                this.mID = mID;
                this.mTitle = Title;
                this.mDateTime = DateTime;
                this.mRepeat = Repeat;
                this.mRepeatNo = RepeatNo;
                this.mRepeatType = RepeatType;
                this.mActive = Active;
            }

        }

        // Class to compare date and time so that items are sorted in ascending order
        public class DateTimeComparator implements Comparator {
            DateFormat f = new SimpleDateFormat("dd/mm/yyyy hh:mm");

            public int compare(Object a, Object b) {
                String o1 = ((DateTimeSorter)a).getDateTime();
                String o2 = ((DateTimeSorter)b).getDateTime();

                try {
                    return f.parse(o1).compareTo(f.parse(o2));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        // UI and data class for recycler view items
        public  class VerticalItemHolder extends SwappingHolder
                implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
            private TextView mTitleText, mDateAndTimeText, mRepeatInfoText;
            private ImageView mActiveImage , mThumbnailImage;
            private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;
            private TextDrawable mDrawableBuilder;
            private ReminderAdapter mAdapter;
            private TextView prmTxt;

            private ImageButton popupButton;

            public VerticalItemHolder(View itemView, ReminderAdapter adapter) {
                super(itemView, mMultiSelector);
                //itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                itemView.setLongClickable(true);

                // Initialize adapter for the items
                mAdapter = adapter;

                // Initialize views
                mTitleText = (TextView) itemView.findViewById(R.id.recycle_title);
                mDateAndTimeText = (TextView) itemView.findViewById(R.id.recycle_date_time);
                mRepeatInfoText = (TextView) itemView.findViewById(R.id.recycle_repeat_info);
                mActiveImage = (ImageView) itemView.findViewById(R.id.active_image);
                mThumbnailImage = (ImageView) itemView.findViewById(R.id.thumbnail_image);
                popupButton = (ImageButton) itemView.findViewById(R.id.action_popupmenus);
                prmTxt = (TextView) itemView.findViewById(R.id.prikeytxt);

                popupButton.setOnClickListener(this);
            }

            // On clicking a reminder item
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        private void showPopupMenu(View view)
        {
            PopupMenu popupMenu = new PopupMenu(view.getContext(),view);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true);
            }
            popupMenu.show();
        }
            // On long press enter action mode with context menu
            @Override
            public boolean onLongClick(View v) {
                AppCompatActivity activity = AllReminderActivity.this;
                activity.startSupportActionMode(mDeleteMode);
                mMultiSelector.setSelected(this, true);
                return true;
            }

            // Set reminder title view
            public void setReminderTitle(String title) {
                mTitleText.setText(title);
                String letter = "A";

                if(title != null && !title.isEmpty()) {
                    letter = title.substring(0, 1);
                }

                int color = mColorGenerator.getRandomColor();

                // Create a circular icon consisting of  a random background colour and first letter of title
                mDrawableBuilder = TextDrawable.builder()
                        .buildRound(letter, color);
                mThumbnailImage.setImageDrawable(mDrawableBuilder);
            }

            // Set date and time views
            public void setReminderDateTime(String datetime) {
                mDateAndTimeText.setText(datetime);
            }
            public void setReminderID(int datetime) {
                prmTxt.setText(""+datetime);
            }
            // Set repeat views
            public void setReminderRepeatInfo(String repeat, String repeatNo, String repeatType) {
                if(repeat.equals("true")){
                    mRepeatInfoText.setText("Every " + repeatNo + " " + repeatType + "(s)");
                }else if (repeat.equals("false")) {
                    mRepeatInfoText.setText("Repeat Off");
                }
            }

            // Set active image as on or off
            public void setActiveImage(String active){
                if(active.equals("true")){
                    mActiveImage.setImageResource(R.drawable.ic_baseline_notifications_active);
                }else if (active.equals("false")) {
                    mActiveImage.setImageResource(R.drawable.ic_baseline_notifications_off);
                }
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.action_popup_edit:
                        if (!mMultiSelector.tapSelection(this)) {
                           // mTempPost = mList.getChildAdapterPosition(v);
                           // mTempPost = getAdapterPosition();
                           mTempPost = Integer.parseInt(prmTxt.getText().toString());
                            //int mReminderClickID = IDmap.get(mTempPost);
                            selectReminder(mTempPost);

                        } else if(mMultiSelector.getSelectedPositions().isEmpty()){
                            mAdapter.setItemCount(getDefaultItemCount());
                        }
                        return true;
                    case R.id.action_popup_delete:
                        mTempPost = getAdapterPosition();
                        int id = IDmap.get(mTempPost);

                        // Get reminder from reminder database using id
                        ReminderModel temp = rmdDb.getReminder(id);
                        // Delete reminder
                        rmdDb.deleteReminder(temp);
                        // Remove reminder from recycler view
                        mAdapter.removeItemSelected(mTempPost);
                        return true;

                    default:
                        return false;
                }
            }
        }

        // Generate random test data
      /*  public  ReminderItem generateDummyData() {
            return new ReminderItem("1", "2", "3", "4", "5", "6");
        }*/

        // Generate real data for each item
        public List<ReminderItem> generateData(int count) {
            ArrayList<ReminderAdapter.ReminderItem> items = new ArrayList<>();

            // Get all reminders from the database
            List<ReminderModel> reminders = rmdDb.getAllReminders();

            // Initialize lists
            List<String> Titles = new ArrayList<>();
            List<String> Repeats = new ArrayList<>();
            List<String> RepeatNos = new ArrayList<>();
            List<String> RepeatTypes = new ArrayList<>();
            List<String> Actives = new ArrayList<>();
            List<String> DateAndTime = new ArrayList<>();
            List<Integer> IDList= new ArrayList<>();
            List<DateTimeSorter> DateTimeSortList = new ArrayList<>();

            // Add details of all reminders in their respective lists
            for (ReminderModel r : reminders) {
                Titles.add(r.getTitle());
                DateAndTime.add(r.getDate() + " " + r.getTime());
                Repeats.add(r.getRepeat());
                RepeatNos.add(r.getRepeatNo());
                RepeatTypes.add(r.getRepeatType());
                Actives.add(r.getActive());
                IDList.add(r.getID());
            }

            int key = 0;

            // Add date and time as DateTimeSorter objects
            for(int k = 0; k<Titles.size(); k++){
                DateTimeSortList.add(new DateTimeSorter(key, DateAndTime.get(k)));
                key++;
            }

            // Sort items according to date and time in ascending order
            Collections.sort(DateTimeSortList, new DateTimeComparator());

            int k = 0;

            // Add data to each recycler view item
            for (DateTimeSorter item:DateTimeSortList) {
                int i = item.getIndex();

                items.add(new ReminderAdapter.ReminderItem(IDList.get(i),Titles.get(i), DateAndTime.get(i), Repeats.get(i),
                        RepeatNos.get(i), RepeatTypes.get(i), Actives.get(i)));
                IDmap.put(k, IDList.get(i));
                k++;
            }
            return items;
        }
    }


}