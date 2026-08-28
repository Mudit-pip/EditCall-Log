package com.smartcalllogmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.role.RoleManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CallLog;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Tab Views
    private View layoutHistory;
    private View layoutAddEntry;
    private View layoutStats;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;

    // History Tab Components
    private RecyclerView historyRecyclerView;
    private TextView tvEmptyHistory;
    private CallLogAdapter callLogAdapter;
    private final List<CallLogItem> callLogItems = new ArrayList<>();

    // Stats Tab Components
    private TextView tvStatTotal, tvStatIncoming, tvStatOutgoing, tvStatMissed, tvStatDuration;
    private TextView tvStatAvgDuration, tvStatMostContacted, tvStatIncomingDuration, tvStatOutgoingDuration, tvStatMissedRate, tvStatPeakHour;

    // Add Entry Tab Premium Components
    private com.google.android.material.textfield.TextInputEditText etNumber;
    private com.google.android.material.textfield.TextInputEditText etDate;
    private com.google.android.material.textfield.TextInputEditText etTime;
    private com.google.android.material.textfield.TextInputEditText etDuration;
    private com.google.android.material.button.MaterialButton btnAddCallAction;

    // Time/Date picker state variables
    DatePickerDialog datePickerDialog;
    int hour, minute;
    String strmonth;
    int arYear, arMonth, arDay;
    int type = 1; // Default to 1 (Missed Card)
    int duration_min = 0;
    int duration_sec = 0;
    String mobilenumber;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize state variables to current time
        Calendar cal = Calendar.getInstance();
        arYear = cal.get(Calendar.YEAR);
        arMonth = cal.get(Calendar.MONTH) + 1;
        arDay = cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);

        // Bind Tab Views
        layoutHistory = findViewById(R.id.layout_history);
        layoutAddEntry = findViewById(R.id.layout_add_entry);
        layoutStats = findViewById(R.id.layout_stats);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF")));

        // Bind History Components
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Bind Stats Components
        tvStatTotal = findViewById(R.id.tv_stat_total);
        tvStatIncoming = findViewById(R.id.tv_stat_incoming);
        tvStatOutgoing = findViewById(R.id.tv_stat_outgoing);
        tvStatMissed = findViewById(R.id.tv_stat_missed);
        tvStatDuration = findViewById(R.id.tv_stat_duration);
        tvStatAvgDuration = findViewById(R.id.tv_stat_avg_duration);
        tvStatMostContacted = findViewById(R.id.tv_stat_most_contacted);
        tvStatIncomingDuration = findViewById(R.id.tv_stat_incoming_duration);
        tvStatOutgoingDuration = findViewById(R.id.tv_stat_outgoing_duration);
        tvStatMissedRate = findViewById(R.id.tv_stat_missed_rate);
        tvStatPeakHour = findViewById(R.id.tv_stat_peak_hour);

        // Bind Add Entry Components
        etNumber = findViewById(R.id.et_number);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etDuration = findViewById(R.id.et_duration);
        btnAddCallAction = findViewById(R.id.btn_add_call_action);

        // Setup Card Selectors listeners
        findViewById(R.id.card_select_missed).setOnClickListener(v -> updateTypeSelection(1));
        findViewById(R.id.card_select_outgoing).setOnClickListener(v -> updateTypeSelection(2));
        findViewById(R.id.card_select_incoming).setOnClickListener(v -> updateTypeSelection(3));

        // Set Default selection
        updateTypeSelection(1);

        // Setup Adapter
        callLogAdapter = new CallLogAdapter(callLogItems, new CallLogAdapter.OnCallLogClickListener() {
            @Override
            public void onItemClick(CallLogItem item) {
                // Populate the Form
                mobilenumber = item.getNumber();
                etNumber.setText(mobilenumber);

                // Set Type
                int itemType = item.getType();
                if (itemType == CallLog.Calls.MISSED_TYPE) {
                    updateTypeSelection(1);
                } else if (itemType == CallLog.Calls.OUTGOING_TYPE) {
                    updateTypeSelection(2);
                } else if (itemType == CallLog.Calls.INCOMING_TYPE) {
                    updateTypeSelection(3);
                }

                // Set Duration
                duration_min = item.getDuration() / 60;
                duration_sec = item.getDuration() % 60;
                if (duration_sec > 0) {
                    etDuration.setText(duration_min + " min " + duration_sec + " sec");
                } else {
                    etDuration.setText(duration_min + " minutes");
                }

                // Set Date & Time
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(item.getDate());
                arYear = cal.get(Calendar.YEAR);
                arMonth = cal.get(Calendar.MONTH) + 1;
                arDay = cal.get(Calendar.DAY_OF_MONTH);
                hour = cal.get(Calendar.HOUR_OF_DAY);
                minute = cal.get(Calendar.MINUTE);

                String strhour = hour < 10 ? "0" + hour : "" + hour;
                String strmin = minute < 10 ? "0" + minute : "" + minute;
                etTime.setText(strhour + " : " + strmin);

                updateMonthString(arMonth);
                etDate.setText(arDay + " " + strmonth + " " + arYear);

                // Switch to Add Tab
                bottomNavigation.setSelectedItemId(R.id.navigation_add);
                Toast.makeText(MainActivity.this, "Call details copied to form", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(CallLogItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Call Log")
                        .setMessage("Are you sure you want to delete this entry from the system call logs?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver().delete(
                                        CallLog.Calls.CONTENT_URI,
                                        CallLog.Calls._ID + "=?",
                                        new String[]{String.valueOf(item.getId())}
                                );
                                Toast.makeText(MainActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                                loadCallLogs();
                                calculateStatistics();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onCallClick(CallLogItem item) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(android.net.Uri.parse("tel:" + item.getNumber()));
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 2);
                }
            }
        });
        historyRecyclerView.setAdapter(callLogAdapter);

        // Bottom Navigation Controller
        bottomNavigation.setOnNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.navigation_history) {
                layoutHistory.setVisibility(View.VISIBLE);
                layoutAddEntry.setVisibility(View.GONE);
                layoutStats.setVisibility(View.GONE);
                loadCallLogs();
                return true;
            } else if (itemId == R.id.navigation_add) {
                layoutHistory.setVisibility(View.GONE);
                layoutAddEntry.setVisibility(View.VISIBLE);
                layoutStats.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.navigation_stats) {
                layoutHistory.setVisibility(View.GONE);
                layoutAddEntry.setVisibility(View.GONE);
                layoutStats.setVisibility(View.VISIBLE);
                loadCallLogs();
                calculateStatistics();
                return true;
            }
            return false;
        });

        // Initialize permissions and dialer
        checkAndRequestPermissions();
        if (!isDefaultDialer()) {
            requestDefaultDialer();
        }

        // Load Initial Logs and set default tab to Add Call
        loadCallLogs();
        bottomNavigation.setSelectedItemId(R.id.navigation_add);

        // Form Callbacks
        etDuration.setOnClickListener(v -> showDurationPicker());
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> popTimePicker());

        btnAddCallAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobilenumber = etNumber.getText().toString().trim();
                String dateVal = etDate.getText().toString().trim();
                String timeVal = etTime.getText().toString().trim();

                if (dateVal.isEmpty() || timeVal.isEmpty() || mobilenumber.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all details", Toast.LENGTH_SHORT).show();
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, arYear);
                    cal.set(Calendar.MONTH, arMonth - 1);
                    cal.set(Calendar.DAY_OF_MONTH, arDay);
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                    cal.set(Calendar.SECOND, 0);

                    ContentValues values = new ContentValues();
                    values.put(CallLog.Calls.NUMBER, mobilenumber);
                    values.put(CallLog.Calls.DATE, cal.getTimeInMillis());
                    values.put(CallLog.Calls.DURATION, duration_min * 60 + duration_sec);

                    int callType;
                    String toastMsg;

                    if (type == 1) {
                        callType = CallLog.Calls.MISSED_TYPE;
                        toastMsg = "Successfully Added Missed-Call";
                    } else if (type == 2) {
                        callType = CallLog.Calls.OUTGOING_TYPE;
                        toastMsg = "Successfully Added Outgoing-Call";
                    } else {
                        callType = CallLog.Calls.INCOMING_TYPE;
                        toastMsg = "Successfully Added Incoming-Call";
                    }

                    values.put(CallLog.Calls.TYPE, callType);

                    getApplicationContext().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
                    Toast.makeText(getApplicationContext(), toastMsg + " to Call Logs", Toast.LENGTH_LONG).show();

                    // Refresh list and stats
                    loadCallLogs();
                    calculateStatistics();

                    // Clear fields
                    etNumber.setText("");
                    etDate.setText("");
                    etTime.setText("");
                    etDuration.setText("");
                    duration_min = 0;
                    duration_sec = 0;

                    // Switch to History Tab
                    bottomNavigation.setSelectedItemId(R.id.navigation_history);
                }
            }
        });
    }

    private void updateTypeSelection(int selectedType) {
        type = selectedType;

        com.google.android.material.card.MaterialCardView cardMissed = findViewById(R.id.card_select_missed);
        com.google.android.material.card.MaterialCardView cardOutgoing = findViewById(R.id.card_select_outgoing);
        com.google.android.material.card.MaterialCardView cardIncoming = findViewById(R.id.card_select_incoming);

        android.widget.ImageView imgMissed = findViewById(R.id.img_select_missed);
        android.widget.ImageView imgOutgoing = findViewById(R.id.img_select_outgoing);
        android.widget.ImageView imgIncoming = findViewById(R.id.img_select_incoming);

        TextView tvMissed = findViewById(R.id.tv_select_missed);
        TextView tvOutgoing = findViewById(R.id.tv_select_outgoing);
        TextView tvIncoming = findViewById(R.id.tv_select_incoming);

        // Reset state (thin light border stroke)
        int defaultStrokeColor = Color.parseColor("#ECEFF1");
        int defaultStrokeWidthPx = (int) (1.5 * getResources().getDisplayMetrics().density);

        cardMissed.setCardBackgroundColor(Color.WHITE);
        cardMissed.setStrokeColor(defaultStrokeColor);
        cardMissed.setStrokeWidth(defaultStrokeWidthPx);

        cardOutgoing.setCardBackgroundColor(Color.WHITE);
        cardOutgoing.setStrokeColor(defaultStrokeColor);
        cardOutgoing.setStrokeWidth(defaultStrokeWidthPx);

        cardIncoming.setCardBackgroundColor(Color.WHITE);
        cardIncoming.setStrokeColor(defaultStrokeColor);
        cardIncoming.setStrokeWidth(defaultStrokeWidthPx);

        imgMissed.setColorFilter(Color.GRAY);
        imgOutgoing.setColorFilter(Color.GRAY);
        imgIncoming.setColorFilter(Color.GRAY);

        tvMissed.setTextColor(Color.DKGRAY);
        tvOutgoing.setTextColor(Color.DKGRAY);
        tvIncoming.setTextColor(Color.DKGRAY);

        int activeStrokeWidthPx = (int) (2.5 * getResources().getDisplayMetrics().density);

        // Highlight selection (background tint AND custom color border stroke)
        if (selectedType == 1) {
            cardMissed.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            cardMissed.setStrokeColor(Color.parseColor("#D32F2F"));
            cardMissed.setStrokeWidth(activeStrokeWidthPx);
            imgMissed.setColorFilter(Color.parseColor("#D32F2F"));
            tvMissed.setTextColor(Color.parseColor("#D32F2F"));
        } else if (selectedType == 2) {
            cardOutgoing.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
            cardOutgoing.setStrokeColor(Color.parseColor("#1976D2"));
            cardOutgoing.setStrokeWidth(activeStrokeWidthPx);
            imgOutgoing.setColorFilter(Color.parseColor("#1976D2"));
            tvOutgoing.setTextColor(Color.parseColor("#1976D2"));
        } else if (selectedType == 3) {
            cardIncoming.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            cardIncoming.setStrokeColor(Color.parseColor("#388E3C"));
            cardIncoming.setStrokeWidth(activeStrokeWidthPx);
            imgIncoming.setColorFilter(Color.parseColor("#388E3C"));
            tvIncoming.setTextColor(Color.parseColor("#388E3C"));
        }
    }

    private void showDurationPicker() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_duration_picker, null);

        com.google.android.material.textfield.TextInputEditText etMin = dialogView.findViewById(R.id.et_dialog_min);
        com.google.android.material.textfield.TextInputEditText etSec = dialogView.findViewById(R.id.et_dialog_sec);

        etMin.setText(String.valueOf(duration_min));
        etSec.setText(String.valueOf(duration_sec));

        // Presets setup
        dialogView.findViewById(R.id.btn_preset_30s).setOnClickListener(v -> { etMin.setText("0"); etSec.setText("30"); });
        dialogView.findViewById(R.id.btn_preset_1m).setOnClickListener(v -> { etMin.setText("1"); etSec.setText("0"); });
        dialogView.findViewById(R.id.btn_preset_2m).setOnClickListener(v -> { etMin.setText("2"); etSec.setText("0"); });
        dialogView.findViewById(R.id.btn_preset_5m).setOnClickListener(v -> { etMin.setText("5"); etSec.setText("0"); });
        dialogView.findViewById(R.id.btn_preset_10m).setOnClickListener(v -> { etMin.setText("10"); etSec.setText("0"); });
        dialogView.findViewById(R.id.btn_preset_15m).setOnClickListener(v -> { etMin.setText("15"); etSec.setText("0"); });
        dialogView.findViewById(R.id.btn_preset_30m).setOnClickListener(v -> { etMin.setText("30"); etSec.setText("0"); });
        dialogView.findViewById(R.id.btn_preset_1h).setOnClickListener(v -> { etMin.setText("60"); etSec.setText("0"); });

        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        ab.setTitle("Set Call Duration");
        ab.setView(dialogView);
        ab.setNegativeButton("Cancel", null);
        ab.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mStr = etMin.getText().toString().trim();
                String sStr = etSec.getText().toString().trim();

                duration_min = mStr.isEmpty() ? 0 : Integer.parseInt(mStr);
                duration_sec = sStr.isEmpty() ? 0 : Integer.parseInt(sStr);

                if (duration_min > 0 || duration_sec > 0) {
                    StringBuilder sb = new StringBuilder();
                    if (duration_min >= 60) {
                        int h = duration_min / 60;
                        int m = duration_min % 60;
                        sb.append(h).append(" hr ");
                        if (m > 0) sb.append(m).append(" min ");
                    } else if (duration_min > 0) {
                        sb.append(duration_min).append(" min ");
                    }
                    if (duration_sec > 0) {
                        sb.append(duration_sec).append(" sec");
                    }
                    etDuration.setText(sb.toString().trim());
                } else {
                    etDuration.setText("0 sec");
                }
            }
        });
        ab.create().show();
    }

    public void popTimePicker() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;

                String strhour = hour < 10 ? "0" + hour : "" + hour;
                String strmin = minute < 10 ? "0" + minute : "" + minute;

                etTime.setText(strhour + " : " + strmin);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                arYear = year;
                arMonth = month;
                arDay = day;

                updateMonthString(month);
                etDate.setText(day + " " + strmonth + " " + year);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        datePickerDialog.setTitle("Select Date");
        datePickerDialog.show();
    }

    private void updateMonthString(int month) {
        if (month == 1) strmonth = "JAN";
        else if (month == 2) strmonth = "FEB";
        else if (month == 3) strmonth = "MAR";
        else if (month == 4) strmonth = "APR";
        else if (month == 5) strmonth = "MAY";
        else if (month == 6) strmonth = "JUN";
        else if (month == 7) strmonth = "JUL";
        else if (month == 8) strmonth = "AUG";
        else if (month == 9) strmonth = "SEP";
        else if (month == 10) strmonth = "OCT";
        else if (month == 11) strmonth = "NOV";
        else if (month == 12) strmonth = "DEC";
    }

    private boolean isDefaultDialer() {
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        return telecomManager != null && getPackageName().equals(telecomManager.getDefaultDialerPackage());
    }

    private void requestDefaultDialer() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    startActivityForResult(intent, 101);
                }
            }
        } else {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, 101);
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE
        };

        java.util.ArrayList<String> listPermissionsNeeded = new java.util.ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 1);
        }
    }

    private void loadCallLogs() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        callLogItems.clear();

        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls._ID,
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.CACHED_NAME,
                        CallLog.Calls.TYPE,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION
                },
                null,
                null,
                CallLog.Calls.DATE + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls._ID));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                int typeVal = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                long dateVal = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                int durationVal = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));

                callLogItems.add(new CallLogItem(id, number, name, typeVal, dateVal, durationVal));
            }
            cursor.close();
        }

        if (callLogItems.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
        }

        callLogAdapter.notifyDataSetChanged();
    }

    private String formatDuration(long totalSecs) {
        long hrs = totalSecs / 3600;
        long mins = (totalSecs % 3600) / 60;
        long secs = totalSecs % 60;

        StringBuilder sb = new StringBuilder();
        if (hrs > 0) {
            sb.append(hrs).append(" hr ");
        }
        if (mins > 0 || hrs > 0) {
            sb.append(mins).append(" min ");
        }
        sb.append(secs).append(" sec");
        return sb.toString().trim();
    }

    private void calculateStatistics() {
        int total = 0;
        int incoming = 0;
        int outgoing = 0;
        int missed = 0;
        long totalDurationSec = 0;
        long incomingDurationSec = 0;
        long outgoingDurationSec = 0;

        // Statistics filtered for the past 30 days
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        Map<String, Integer> contactCounts = new HashMap<>();
        int[] hourCounts = new int[24];

        for (CallLogItem item : callLogItems) {
            if (item.getDate() < thirtyDaysAgo) {
                continue; // Skip logs older than 30 days
            }

            total++;

            String identifier = (item.getName() != null && !item.getName().isEmpty()) ? item.getName() : item.getNumber();
            if (identifier != null && !identifier.isEmpty()) {
                contactCounts.put(identifier, contactCounts.getOrDefault(identifier, 0) + 1);
            }

            // Extract call hour for peak time analysis
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(item.getDate());
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            hourCounts[hourOfDay]++;

            switch (item.getType()) {
                case CallLog.Calls.INCOMING_TYPE:
                    incoming++;
                    incomingDurationSec += item.getDuration();
                    totalDurationSec += item.getDuration();
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    outgoing++;
                    outgoingDurationSec += item.getDuration();
                    totalDurationSec += item.getDuration();
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    missed++;
                    break;
            }
        }

        // Most Contacted Calculation
        String mostContacted = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : contactCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostContacted = entry.getKey() + " (" + entry.getValue() + ")";
            }
        }

        // Peak Call Hour Calculation
        int peakHour = -1;
        int maxHourCount = 0;
        for (int i = 0; i < 24; i++) {
            if (hourCounts[i] > maxHourCount) {
                maxHourCount = hourCounts[i];
                peakHour = i;
            }
        }
        
        String peakHourStr = "N/A";
        if (peakHour != -1) {
            int startHour = peakHour;
            int endHour = (peakHour + 1) % 24;
            String startAmPm = (startHour >= 12) ? "PM" : "AM";
            String endAmPm = (endHour >= 12) ? "PM" : "AM";

            int start12 = startHour % 12;
            if (start12 == 0) start12 = 12;
            int end12 = endHour % 12;
            if (end12 == 0) end12 = 12;

            peakHourStr = String.format(java.util.Locale.US, "%02d %s - %02d %s", start12, startAmPm, end12, endAmPm);
        }

        // Missed Call Rate Calculation
        double missedRate = (total > 0) ? ((double) missed / total * 100) : 0.0;
        String missedRateStr = String.format(java.util.Locale.US, "%.1f%%", missedRate);

        // Average Call Duration Calculation (Excluding Missed Calls)
        int countWithDuration = incoming + outgoing;
        long avgSec = (countWithDuration > 0) ? (totalDurationSec / countWithDuration) : 0;

        // Bind text fields
        tvStatTotal.setText(String.valueOf(total));
        tvStatIncoming.setText(String.valueOf(incoming));
        tvStatOutgoing.setText(String.valueOf(outgoing));
        tvStatMissed.setText(String.valueOf(missed));
        
        tvStatMostContacted.setText(mostContacted);
        tvStatAvgDuration.setText(formatDuration(avgSec));
        tvStatIncomingDuration.setText(formatDuration(incomingDurationSec));
        tvStatOutgoingDuration.setText(formatDuration(outgoingDurationSec));
        tvStatDuration.setText(formatDuration(totalDurationSec));
        tvStatMissedRate.setText(missedRateStr);
        tvStatPeakHour.setText(peakHourStr);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                loadCallLogs();
                calculateStatistics();
            } else {
                Toast.makeText(this, "Permissions required to view and write call logs", Toast.LENGTH_SHORT).show();
            }
        }
    }
}