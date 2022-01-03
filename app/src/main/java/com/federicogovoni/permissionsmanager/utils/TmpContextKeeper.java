package com.federicogovoni.permissionsmanager.utils;

import com.federicogovoni.permissionsmanager.model.CurrentContext;

/**
 * Created by Federico on 14/03/2017.
 */

public class TmpContextKeeper {
    private static TmpContextKeeper tmpPolicyKeeper;
    private CurrentContext currentContext;
    private int phase = -1;

    public static final int GRANT_PHASE = 0;
    public static final int REVOKE_PHASE = 1;

    private TmpContextKeeper() {
    }

    public static TmpContextKeeper getInstance() {
        if(tmpPolicyKeeper == null)
            tmpPolicyKeeper = new TmpContextKeeper();
        return tmpPolicyKeeper;
    }

    public CurrentContext getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(CurrentContext currentContext) {
        this.currentContext = currentContext;
    }

    public int getPhase() { return  phase; }
    public void setPhase(int value) { phase = value; }
}
