package com.vijay.jsonwizard.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.comparisons.Comparison;
import com.vijay.jsonwizard.comparisons.EqualToComparison;
import com.vijay.jsonwizard.comparisons.GreaterThanComparison;
import com.vijay.jsonwizard.comparisons.GreaterThanEqualToComparison;
import com.vijay.jsonwizard.comparisons.LessThanComparison;
import com.vijay.jsonwizard.comparisons.LessThanEqualToComparison;
import com.vijay.jsonwizard.comparisons.NotEqualToComparison;
import com.vijay.jsonwizard.comparisons.RegexComparison;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonFormActivity extends AppCompatActivity implements JsonApi {

    private static final String TAG = "JsonFormActivity";

    private Toolbar             mToolbar;

    private JSONObject          mJSONObject;
    private PropertyManager propertyManager;
    private ArrayList<View> skipLogicViews;
    private ArrayList<View> constrainedViews;
    private String functionRegex;
    private HashMap<String, Comparison> comparisons;

    public void init(String json) {
        try {
            mJSONObject = new JSONObject(json);
            if(!mJSONObject.has("encounter_type")) {
                mJSONObject = new JSONObject();
                throw new JSONException("Form encounter_type not set");
            }
        } catch (JSONException e) {
            Log.d(TAG, "Initialization error. Json passed is invalid : " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_form);
        mToolbar = (Toolbar) findViewById(R.id.tb_top);
        setSupportActionBar(mToolbar);
        skipLogicViews = new ArrayList<>();
        if (savedInstanceState == null) {
            init(getIntent().getStringExtra("json"));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, JsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME)).commit();
            onFormStart();
        } else {
            init(savedInstanceState.getString("jsonState"));
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public synchronized JSONObject getStep(String name) {
        synchronized (mJSONObject) {
            try {
                return mJSONObject.getJSONObject(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent,
                           String openMrsEntity, String openMrsEntityId) throws JSONException {
        synchronized (mJSONObject) {
            JSONObject jsonObject = mJSONObject.getJSONObject(stepName);
            JSONArray fields = jsonObject.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject item = fields.getJSONObject(i);
                String keyAtIndex = item.getString("key");
                if (key.equals(keyAtIndex)) {
                    item.put("value", value);
                    item.put("openmrs_entity_parent", openMrsEntityParent);
                    item.put("openmrs_entity", openMrsEntity);
                    item.put("openmrs_entity_id", openMrsEntityId);
                    refreshSkipLogic();
                    refreshConstraints();
                    return;
                }
            }
        }
    }

    @Override
    public void writeValue(String stepName, String parentKey, String childObjectKey, String childKey,
                           String value, String openMrsEntityParent, String openMrsEntity,
                           String openMrsEntityId)
            throws JSONException {
        synchronized (mJSONObject) {
            JSONObject jsonObject = mJSONObject.getJSONObject(stepName);
            JSONArray fields = jsonObject.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject item = fields.getJSONObject(i);
                String keyAtIndex = item.getString("key");
                if (parentKey.equals(keyAtIndex)) {
                    JSONArray jsonArray = item.getJSONArray(childObjectKey);
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject innerItem = jsonArray.getJSONObject(j);
                        String anotherKeyAtIndex = innerItem.getString("key");
                        if (childKey.equals(anotherKeyAtIndex)) {
                            innerItem.put("value", value);
                            refreshSkipLogic();
                            refreshConstraints();
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String currentJsonState() {
        synchronized (mJSONObject) {
            return mJSONObject.toString();
        }
    }

    @Override
    public String getCount() {
        synchronized (mJSONObject) {
            return mJSONObject.optString("count");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("jsonState", mJSONObject.toString());
    }

    @Override
    public void onFormStart() {
        try {
            if (propertyManager == null) {
                propertyManager = new PropertyManager(this);
            }
            FormUtils.updateStartProperties(propertyManager, mJSONObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFormFinish() {
        try {
            if (propertyManager == null) {
                propertyManager = new PropertyManager(this);
            }
            FormUtils.updateEndProperties(propertyManager, mJSONObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearSkipLogicViews() {
        skipLogicViews = new ArrayList<>();
    }

    @Override
    public void clearConstrainedViews() {
        constrainedViews = new ArrayList<>();
    }

    @Override
    public void addSkipLogicView(View view) {
        skipLogicViews.add(view);
    }

    @Override
    public void addConstrainedView(View view) {
        constrainedViews.add(view);
    }

    @Override
    public void refreshSkipLogic() {
        initComparisons();
        for (View curView : skipLogicViews) {
            String relevanceTag = (String) curView.getTag(R.id.relevance);
            if (relevanceTag != null && relevanceTag.length() > 0) {
                try {
                    JSONObject relevance = new JSONObject(relevanceTag);
                    Iterator<String> keys = relevance.keys();
                    boolean ok = true;
                    while (keys.hasNext()) {
                        String curKey = keys.next();
                        String[] address = curKey.split(":");
                        if (address.length == 2) {
                            JSONObject curRelevance = relevance.getJSONObject(curKey);
                            String curValue = getValueFromAddress(address);
                            try {
                                boolean comparison = doComparison(curValue, curRelevance);
                                ok = ok && comparison;
                                if (!ok) break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (ok) {
                        curView.setEnabled(true);
                        curView.setVisibility(View.VISIBLE);
                    } else {
                        curView.setEnabled(false);
                        curView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method checks if all views being watched for constraints enforce those constraints
     * This library currently only supports constraints on views that store the value in {@link MaterialEditText}
     * ie TreeViews, DatePickers, and EditTexts
     */
    @Override
    public void refreshConstraints() {
        initComparisons();
        for (View curView : constrainedViews) {
            String constraintTag = (String) curView.getTag(R.id.constraints);
            if (constraintTag != null && constraintTag.length() > 0) {
                try {
                    JSONArray constraint = new JSONArray(constraintTag);
                    String errorMessage = null;
                    for (int i = 0; i < constraint.length(); i++) {
                        JSONObject curConstraint = constraint.getJSONObject(i);
                        String addressString = (String) curView.getTag(R.id.address);
                        String[] address = addressString.split(":");
                        if (address.length == 2) {
                            String value = getValueFromAddress(address);
                            errorMessage = enforceConstraint(value, curConstraint);
                            if (errorMessage != null) break;
                        }
                    }

                    if (errorMessage != null) {
                        if (curView instanceof MaterialEditText) {
                            ((MaterialEditText) curView).setText(null);
                            ((MaterialEditText) curView).setError(errorMessage);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getValueFromAddress(String[] address) throws Exception {
        String result = null;
        if(address != null && address.length == 2) {
            JSONArray fields = mJSONObject.getJSONObject(address[0]).getJSONArray("fields");
            for(int i = 0; i < fields.length(); i++) {
                if(fields.getJSONObject(i).getString("key").equals(address[1])) {
                    result = fields.getJSONObject(i).optString("value");
                }
            }
        }

        return result;
    }

    private void initComparisons() {
        if (comparisons == null) {
            functionRegex = "";
            comparisons = new HashMap<>();

            LessThanComparison lessThanComparison = new LessThanComparison();
            functionRegex += lessThanComparison.getFunctionName();
            comparisons.put(lessThanComparison.getFunctionName(), lessThanComparison);

            LessThanEqualToComparison lessThanEqualToComparison = new LessThanEqualToComparison();
            functionRegex += "|" + lessThanEqualToComparison.getFunctionName();
            comparisons.put(lessThanEqualToComparison.getFunctionName(), lessThanEqualToComparison);

            EqualToComparison equalToComparison = new EqualToComparison();
            functionRegex += "|" + equalToComparison.getFunctionName();
            comparisons.put(equalToComparison.getFunctionName(), equalToComparison);

            NotEqualToComparison notEqualToComparer = new NotEqualToComparison();
            functionRegex += "|" + notEqualToComparer.getFunctionName();
            comparisons.put(notEqualToComparer.getFunctionName(), notEqualToComparer);

            GreaterThanComparison greaterThanComparison = new GreaterThanComparison();
            functionRegex += "|" + greaterThanComparison.getFunctionName();
            comparisons.put(greaterThanComparison.getFunctionName(), greaterThanComparison);

            GreaterThanEqualToComparison greaterThanEqualToComparison = new GreaterThanEqualToComparison();
            functionRegex += "|" + greaterThanEqualToComparison.getFunctionName();
            comparisons.put(greaterThanEqualToComparison.getFunctionName(), greaterThanEqualToComparison);

            RegexComparison regexComparison = new RegexComparison();
            functionRegex += "|" + regexComparison.getFunctionName();
            comparisons.put(regexComparison.getFunctionName(), regexComparison);
        }
    }

    private boolean doComparison(String value, JSONObject comparison) throws Exception {
        String type = comparison.getString("type").toLowerCase();
        String ex = comparison.getString("ex");

        Pattern pattern = Pattern.compile("(" + functionRegex + ")\\((.*)\\)");
        Matcher matcher = pattern.matcher(ex);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            String b = matcher.group(2);
            return comparisons.get(functionName).compare(value, type, b);
        }

        return false;
    }

    /**
     * This method checks whether a constraint has been enforced and returns an error message if not
     * The error message should be displayable to the user
     *
     * @param value         The value to be checked
     * @param constraint    The constraint expression to use
     * @return  An error message if constraint has not been enfored or NULL if constraint enforced
     * @throws Exception
     */
    private String enforceConstraint(String value, JSONObject constraint) throws Exception {
        String type = constraint.getString("type").toLowerCase();
        String ex = constraint.getString("ex");
        String errorMessage = constraint.getString("err");
        Pattern pattern = Pattern.compile("(" + functionRegex + ")\\((.*)\\)");
        Matcher matcher = pattern.matcher(ex);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            String addressForBString = matcher.group(2);
            String[] addressForB = addressForBString.split(":");
            if(addressForB.length == 2) {
                String b = getValueFromAddress(addressForB);
                if(TextUtils.isEmpty(b)
                        || TextUtils.isEmpty(value)
                        || comparisons.get(functionName).compare(value, type, b)) {
                    return null;
                }
            }
        }

        return errorMessage;
    }
}