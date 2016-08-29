package org.ei.opensrp.vaccinator.application.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.cursoradapter.CursorCommonObjectSort;
import org.ei.opensrp.vaccinator.R;
import org.ei.opensrp.repository.db.CESQLiteHelper;
import org.ei.opensrp.repository.db.Client;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.controller.FormController;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.ei.opensrp.view.template.SmartRegisterClientsProvider;
import org.joda.time.DateTime;
import org.joda.time.Years;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.VaccinatorUtils;
import util.barcode.Barcode;
import util.barcode.BarcodeIntentIntegrator;
import util.barcode.BarcodeIntentResult;

import static org.ei.opensrp.util.Utils.getValue;

public abstract class SmartClientRegisterFragment extends SecuredNativeSmartRegisterFragment {

    public CESQLiteHelper getClientEventDb() {
        return ceDb;
    }

    private CESQLiteHelper ceDb;

    public SmartClientRegisterFragment(FormController formController) {
        super(formController);
    }

    @Override
    protected abstract SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() ;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ceDb = new CESQLiteHelper(activity);
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{
                };
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
                        new CursorCommonObjectSort(getResources().getString(R.string.woman_alphabetical_sort), "first_name"),
                        new DateSort("Age", "dob"),
                        new StatusSort("Due Status"),
                        new CursorCommonObjectSort(getResources().getString(R.string.id_sort), "program_client_id")
                };
            }

            @Override
            public String searchHint() {
                return Context.getInstance().getStringResource(R.string.search_hint);
            }
        };
    }//end of method

    @Override
    protected abstract SmartRegisterClientsProvider clientsProvider() ;

    @Override
    protected abstract void onInitialization() ;

    @Override
    protected void startRegistration() {
        BarcodeIntentIntegrator integ = new BarcodeIntentIntegrator(this);
        integ.addExtra(Barcode.SCAN_MODE, Barcode.QR_MODE);
        integ.initiateScan();
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        //todo getDefaultOptionsProvider();
        //todo updateSearchView();

        mView.findViewById(org.ei.opensrp.R.id.filter_selection).setVisibility(View.GONE);

        mView.findViewById(org.ei.opensrp.R.id.service_mode_selection).setVisibility(View.GONE);

        mView.findViewById(org.ei.opensrp.R.id.btn_report_month).setVisibility(View.GONE);

        mView.findViewById(org.ei.opensrp.R.id.village).setVisibility(View.GONE);

        ImageView imv = ((ImageView)mView.findViewById(org.ei.opensrp.R.id.register_client));
        imv.setImageResource(R.mipmap.qr_code);
        // create a matrix for the manipulation
        imv.setAdjustViewBounds(true);
        imv.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    protected abstract String getRegistrationForm(HashMap<String, String> overridemap);

    protected abstract String getOAFollowupForm(Client client, HashMap<String, String> overridemap);

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("", "REQUEST COODE " + requestCode);
        Log.i("", "Result Coode " + resultCode);
        if(requestCode == BarcodeIntentIntegrator.REQUEST_CODE) {
            BarcodeIntentResult res = BarcodeIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(StringUtils.isNotBlank(res.getContents())) {
                onQRCodeSucessfullyScanned(res.getContents());
            }
            else Log.i("", "NO RESULT FOR QR CODE");
        }
    }

    private void startEnrollmentForm(HashMap<String, String> overrides){
        overrides.putAll(VaccinatorUtils.providerDetails());
        startForm(getRegistrationForm(overrides), "", overrides);
    }

    private void startOffSiteFollowupForm(Client client, HashMap<String, String> overrides){
        overrides.putAll(VaccinatorUtils.providerDetails());
        startForm(getOAFollowupForm(client, overrides), client.getBaseEntityId(), overrides);
    }

    private void onQRCodeSucessfullyScanned(String qrCode) {
      /* #TODO:after reading the code , app first search for that id in database if he it is there ,
      that client appears  on register only . if it doesnt then it shows two options
       */
        SmartRegisterClients fc = getFilteredClients(qrCode);
        CommonPersonObject member = householdMember(qrCode);
        if(fc.size() > 0) {
            //do nothing. let user select from filtered data
        }
        else {
            Client c = null;
            try{
                c = ceDb.getClient(qrCode);
            }
            catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            if(c != null && getDefaultOptionsProvider().nameInShortFormForTitle().toLowerCase().contains("woman")
                    && c.getBirthdate() != null && Years.yearsBetween(c.getBirthdate(), DateTime.now()).getYears() < 8){
                showMessageDialog("Scanned ID already exists and is not a woman.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                return;
            }

            if(c != null && getDefaultOptionsProvider().nameInShortFormForTitle().toLowerCase().contains("child")
                    && c.getBirthdate() != null && Years.yearsBetween(c.getBirthdate(), DateTime.now()).getYears() >= 8){
                showMessageDialog("Scanned ID already exists and is not a child.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                return;
            }

            HashMap<String,String> map = new HashMap<>();
            map.put("existing_program_client_id", qrCode);
            map.put("program_client_id", qrCode);

            if(member != null){
                map.put("first_name", getValue(member.getColumnmaps(), "first_name", true));
                map.put("last_name", getValue(member.getColumnmaps(), "last_name", true));
                map.put("birth_date", getValue(member.getColumnmaps(), "member_birthdate", true));
                map.put("gender", getValue(member.getColumnmaps(), "gender", true));
            }

            Map<String, String> m = customFieldOverrides();
            if(m != null){
                map.putAll(m);
            }

            if (c != null){
                startOffSiteFollowupForm(c, map);
            }
            else {
                startEnrollmentForm(map);
            }
        }
        getSearchView().setText(qrCode);
    }

    protected abstract Map<String, String> customFieldOverrides();

    private SmartRegisterClients getFilteredClients(String filterString) {
        setCurrentSearchFilter(new BasicSearchOption(filterString, BasicSearchOption.Type.getByRegisterName(getDefaultOptionsProvider().nameInShortFormForTitle())));
        onFilterManual(filterString);
        return getClientsAdapter().currentPageList();
    }

    public CommonPersonObject householdMember(String qrCode){
        String memberExistQuery = "select * from pkindividual where existing_program_client_id = " + qrCode;

        List<CommonPersonObject> memberData = context.allCommonsRepositoryobjects("pkindividual").customQueryForCompleteRow(memberExistQuery, new String[]{}, "pkindividual");
        CommonPersonObject householdMember;
        if (memberData.size() == 0) {
            return null;
        } else {
            householdMember = memberData.get(0);
            /*String householdId = memberData.get(0).getDetails().get("household_id");
            String householdQuery = "select * from pkhousehold where household_id = " + householdId;
            //CommonPersonObject householdData = context.allCommonsRepositoryobjects("pkhousehold").customQueryForCompleteRow(householdQuery, new String[]{}, "pkhousehold").get(0);
            //todo: create new search option
            setCurrentSearchFilter(new HouseholdSearchOption(householdId));
            onFilterManual(householdId);*/
        }

        return householdMember;
    }
}