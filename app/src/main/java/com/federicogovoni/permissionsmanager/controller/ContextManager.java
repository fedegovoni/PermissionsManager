package com.federicogovoni.permissionsmanager.controller;

import android.content.Context;
import android.preference.PreferenceManager;

import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionsmanager.model.LocationContext;
import com.federicogovoni.permissionsmanager.model.Permission;
import com.federicogovoni.permissionsmanager.utils.TmpContextKeeper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Federico on 10/03/2017.
 */

public class ContextManager {

    public static final String FILENAME = "Policies.txt";
    public static final String FILE_ID = "PolicyId.txt";

    private List<CurrentContext> currentContexts = null;
    private static ContextManager contextManager = null;
    private Context context;
    private File fileCurrentContext;
    private File fileId;
    private int lastUsedId = -1;

    private ContextManager(Context context) {
        this.context = context;
        currentContexts = new ArrayList<>();
        try {
            fileCurrentContext = new File(context.getFilesDir(), FILENAME);
            if(!fileCurrentContext.exists())
                fileCurrentContext.createNewFile();
            FileInputStream fis = new FileInputStream(fileCurrentContext);
            ObjectInputStream ois = new ObjectInputStream(fis);
            CurrentContext p;

            while((p = (CurrentContext) ois.readObject()) != null) {
                int size = ois.readInt();
                p.setApplicationPermissions(new HashMap<Permission, String>());
                for(int i = 0; i < size; i++) {
                    String name = (String) ois.readObject();
                    String packageName = (String) ois.readObject();

                    String action = (String) ois.readObject();
                    Permission perm = new Permission(name, packageName, context.getPackageManager());
                    p.getApplicationPermissions().put(perm, action);
                }
                //start alarms
                p.getTimeContext().check(context, p);
                currentContexts.add(p);
            }

            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ContextManager getInstance() {
        return contextManager;
    }

    public static ContextManager getInstance(Context context) {
        if(contextManager == null)
            contextManager = new ContextManager(context);
        return contextManager;
    }


    public static void init(Context context) {
        contextManager = new ContextManager(context);
    }

    public void addContext(CurrentContext p) {
        for(CurrentContext s : currentContexts)
            if(s.getId() == p.getId()) {
                modifyContext(currentContexts.indexOf(s), p); //store fatta nella modify
                return;
            }
        currentContexts.add(p);
        store();
    }

    public List<CurrentContext> getContexts(){
        final List<CurrentContext> p = currentContexts;
        return p;
    }

    public CurrentContext getById(int id) {
        for(CurrentContext p : currentContexts)
            if(p.getId() == id)
                return p;

        if(TmpContextKeeper.getInstance().getCurrentContext() != null && TmpContextKeeper.getInstance().getCurrentContext().getId() == id)
            return TmpContextKeeper.getInstance().getCurrentContext();

        return null;
    }

    private void modifyContext(int index, CurrentContext modified) {
        currentContexts.get(index).setEnabled(false, context);
        currentContexts.set(index, modified);
        store();
    }

    public void removeContext(int position) {
        currentContexts.get(position).setEnabled(false, context);
        currentContexts.get(position).getTimeContext().cancelAlarms(context);
        currentContexts.remove(position);
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(CurrentContext.RUNNING_STATE).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(CurrentContext.ENABLED).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(CurrentContext.IN_LOCATION).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(CurrentContext.IN_TIME).apply();
        store();
    }

    public int getNewId() {
        if (lastUsedId == -1) {
            try {
                fileId = new File(context.getFilesDir(), FILE_ID);
                if (!fileId.exists())
                    fileId.createNewFile();
                FileReader fis = new FileReader(fileId);
                BufferedReader bf = new BufferedReader(fis);

                String read =bf.readLine();
                lastUsedId = Integer.parseInt(read);

                bf.close();
            } catch (IOException | NumberFormatException | NullPointerException e) {
                lastUsedId = -1;
            }
        }
        lastUsedId++;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileId));
            bw.write(lastUsedId+"");
            bw.flush();
            bw.close();
        }catch(IOException e) {
        }
        return lastUsedId;
    }

    public void store() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileCurrentContext));
            for(CurrentContext tmp : currentContexts) {
                Map<Permission, String> map = tmp.getApplicationPermissions();
                tmp.setApplicationPermissions(null);
                oos.writeObject(tmp); oos.flush();
                oos.writeInt(map.size()); oos.flush();
                for(Permission p : map.keySet()) {
                    oos.writeObject(p.getName()); oos.flush();
                    oos.writeObject(p.getPackageName()); oos.flush();
                    oos.writeObject(map.get(p)); oos.flush();
                }
                tmp.setApplicationPermissions(map);
            }
            oos.flush();
            oos.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void convertMeasuresTo(String value) {
        for(CurrentContext p : currentContexts) {
            if(p.getLocationContext() != null) {
                if(value.equals(LocationContext.KM))
                    p.getLocationContext().setRadius((int) Math.round(p.getLocationContext().getRadius() * 1.60934));
                else
                    p.getLocationContext().setRadius((int) Math.round(p.getLocationContext().getRadius() / 1.60934));
            }
        }
        store();
    }
}
