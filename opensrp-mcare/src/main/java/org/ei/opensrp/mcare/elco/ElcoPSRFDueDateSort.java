package org.ei.opensrp.mcare.elco;


import android.util.Log;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.Alert;
import org.ei.opensrp.mcare.R;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.SortOption;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Raihan Ahmed on 3/22/15.
 */
public class ElcoPSRFDueDateSort implements SortOption {




    public ElcoPSRFDueDateSort() {

    }

    @Override
    public String name() {
        return Context.getInstance().applicationContext().getResources().getString(R.string.due_status);
    }

    @Override
    public SmartRegisterClients sort(SmartRegisterClients allClients) {
        Collections.sort(allClients, commoncomparator);
        return allClients;
    }

    Comparator<SmartRegisterClient> commoncomparator = new Comparator<SmartRegisterClient>() {
        @Override
        public int compare(SmartRegisterClient oneClient, SmartRegisterClient anotherClient2) {
            CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) oneClient;

            List<Alert> alertlist_for_client = Context.getInstance().alertService().findByEntityIdAndAlertNames(commonPersonObjectClient.entityId(), "ELCO PSRF");
            String alertforclient1 = "not synced";
            for(int i = 0;i<alertlist_for_client.size();i++){
                alertforclient1 = alertlist_for_client.get(i).status().value();
            }
            if((commonPersonObjectClient.getDetails().get("FWPSRSTS")!=null) && !(((commonPersonObjectClient.getDetails().get("psrf_schedule_logic")!=null?commonPersonObjectClient.getDetails().get("psrf_schedule_logic"):"").equalsIgnoreCase("1")) || commonPersonObjectClient.getDetails().get("FWPSRSTS").equalsIgnoreCase("2"))){
                alertforclient1 = "pregnant";
            }


            CommonPersonObjectClient commonPersonObjectClient2 = (CommonPersonObjectClient) anotherClient2;
            List<Alert> alertlist_for_client2 = Context.getInstance().alertService().findByEntityIdAndAlertNames(commonPersonObjectClient2.entityId(), "ELCO PSRF");
            String alertforclient2 = "not synced";
            for(int i = 0;i<alertlist_for_client2.size();i++){
                alertforclient2 = alertlist_for_client2.get(i).status().value();
            }

            if((commonPersonObjectClient2.getDetails().get("FWPSRSTS")!=null) && !(((commonPersonObjectClient2.getDetails().get("psrf_schedule_logic")!=null?commonPersonObjectClient2.getDetails().get("psrf_schedule_logic"):"").equalsIgnoreCase("1")) || commonPersonObjectClient2.getDetails().get("FWPSRSTS").equalsIgnoreCase("2"))){
                alertforclient2 = "pregnant";
            }

            Log.v("alertclient1", alertforclient1);
            Log.v("alertclient2", alertforclient2);
            if(alertforclient1.equalsIgnoreCase(alertforclient2)){
                return (new Integer(commonPersonObjectClient.getDetails().get("JiVitAHHID")!=null?commonPersonObjectClient.getDetails().get("JiVitAHHID"):"0").compareTo(new Integer(commonPersonObjectClient2.getDetails().get("JiVitAHHID")!=null?commonPersonObjectClient2.getDetails().get("JiVitAHHID"):"0")));

            }

            if(alertforclient1.equalsIgnoreCase("urgent") && alertforclient2.equalsIgnoreCase("normal") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("urgent") && alertforclient2.equalsIgnoreCase("upcoming") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("urgent") && alertforclient2.equalsIgnoreCase("expired") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("urgent") && alertforclient2.equalsIgnoreCase("not synced") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("urgent") && alertforclient2.equalsIgnoreCase("complete") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("urgent") && alertforclient2.equalsIgnoreCase("pregnant") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("upcoming") && alertforclient2.equalsIgnoreCase("not synced") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("upcoming") && alertforclient2.equalsIgnoreCase("normal") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("upcoming") && alertforclient2.equalsIgnoreCase("expired") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("upcoming") && alertforclient2.equalsIgnoreCase("pregnant") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("upcoming") && alertforclient2.equalsIgnoreCase("complete") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("normal") && alertforclient2.equalsIgnoreCase("expired") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("normal") && alertforclient2.equalsIgnoreCase("not synced") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("normal") && alertforclient2.equalsIgnoreCase("complete") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("normal") && alertforclient2.equalsIgnoreCase("pregnant") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("expired") && alertforclient2.equalsIgnoreCase("not synced") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("expired") && alertforclient2.equalsIgnoreCase("complete") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("expired") && alertforclient2.equalsIgnoreCase("pregnant") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("not synced") && alertforclient2.equalsIgnoreCase("complete") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("not synced") && alertforclient2.equalsIgnoreCase("pregnant") ){
                return -1;
            }
            if(alertforclient1.equalsIgnoreCase("complete") && alertforclient2.equalsIgnoreCase("pregnant") ){
                return -1;
            }else{
                return 1;
            }
        }

    };
}
