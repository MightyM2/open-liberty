/*******************************************************************************
 * Copyright (c) 2017, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.workcontext.jca;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.MessageListener;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.ResourceWarning;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
//-
import jakarta.resource.spi.work.WorkManager;

//import com.ibm.ws.jca.internal.WorkManagerImpl;
/**
 * Example interaction.
 */
public class InteractionImpl implements Interaction {
    // LH 05-10 adding for workmanager
    // -private transient ResourceAdapterImpl adapter;
    private WorkManager wmInstance;

    private ConnectionImpl con;

    InteractionImpl(ConnectionImpl con) {
        this.con = con;
    }

    @Override
    public void clearWarnings() throws ResourceException {
    }

    @Override
    public void close() throws ResourceException {
        if (con == null)
            throw new ResourceException("already closed");
        con = null;
    }

    @Override
    public Record execute(InteractionSpec ispec, Record input) throws ResourceException {
        Record output = con.cf.createMappedRecord("output");

        // temp
        System.out.println(" -- 1.debug InteractionImp execute and run WorkManager ? --");
        wmInstance = con.cf.mcf.adapter.getWmInstance();

        WorkContextMsgWork theWork1 = new WorkContextMsgWork("JCA");
        wmInstance.scheduleWork(theWork1);

        //WorkContextMsgWork theWork1 = new WorkContextMsgWork("JCA");
        //adapter.bootstrapContext.getWorkManager().scheduleWork(theWork1);
        //adapter.bootstrapContext.getWorkManager().scheduleWork(new WorkContextMsgWork("JCA"));

        execute(ispec, input, output);
        return output;
    }

    @Override
    public boolean execute(InteractionSpec ispec, Record input, Record output) throws ResourceException {
        if (con == null)
            throw new ResourceException("connection is closed");

        Boolean readOnly = (Boolean) con.cri.get("readOnly");
        String tableName = (String) con.cri.get("tableName");
        ConcurrentLinkedQueue<Map<String, String>> table = ManagedConnectionFactoryImpl.tables.get(tableName);

        @SuppressWarnings("unchecked")
        Map<String, String> inputMap = (Map<String, String>) input;
        @SuppressWarnings("unchecked")
        List<String> outputMap = (List<String>) output;

        // temp
        System.out.println(" -- 2.debug InteractionImp execute and run WorkManager ? --");
        wmInstance = con.cf.mcf.adapter.getWmInstance();
        WorkContextMsgWork theWork1 = new WorkContextMsgWork("JCA");
        wmInstance.scheduleWork(theWork1);

        String function = ((InteractionSpecImpl) ispec).getFunctionName();
        if ("ADD".equalsIgnoreCase(function)) {
            if (readOnly)
                throw new NotSupportedException("functionName=ADD for read only connection");
            table.add(new TreeMap<String, String>(inputMap));
            for (String key : inputMap.keySet()) {
                outputMap.add(key + "=" + inputMap.get(key));
            }
            //LH 05-10-23 define the work instance and do work
            //work = new FATWorkAndContext("java:comp/env/eis/ds1ref", "update TestTransactionContextTBL set col2='IV' where col1=4", transactionContext);
            //ActivationSpecImpl work = new ActivationSpecImpl();
            //FVTComplexWorkImpl work = new FVTComplexWorkImpl(deliveryId, message, this);

            // -> adapter.bootstrapContext.getWorkManager().scheduleWork(work );
            System.out.println(" -- Try to message to start work -- ");

            //adapter.bootstrapContext.getWorkManager().scheduleWork(new WorkContextMsgWork()); // Can't be null
            //adapter.bootstrapContext.getWorkManager().dowork(work, WorkManager.INDEFINITE, null, this);

            onMessage(function, output);

            //adapter.bootstrapContext.getWorkManager().scheduleWork(new WorkContextMsgWork());
            return true;
        } else if ("FIND".equalsIgnoreCase(function)) {
            for (Map<String, String> map : table) {
                boolean match = true;
                for (Map.Entry<?, ?> entry : inputMap.entrySet())
                    match &= entry.getValue().equals(map.get(entry.getKey()));
                if (match) {
                    for (String key : map.keySet()) {
                        outputMap.add(key + "=" + map.get(key));
                    }
                    return true;
                }
            }
            return false;
        } else if ("REMOVE".equalsIgnoreCase(function)) {
            if (readOnly)
                throw new NotSupportedException("functionName=REMOVE for read only connection");
            for (Iterator<Map<String, String>> it = table.iterator(); it.hasNext();) {
                Map<String, String> map = it.next();
                boolean match = true;
                for (Map.Entry<?, ?> entry : inputMap.entrySet())
                    match &= entry.getValue().equals(map.get(entry.getKey()));
                if (match) {
                    it.remove();
                    for (String key : map.keySet()) {
                        outputMap.add(key + "=" + map.get(key));
                    }
                    onMessage(function, output);
                    return true;
                }
            }
            return false;
        } else
            throw new NotSupportedException("InteractionSpec: functionName = " + function);
    }

    @Override
    public Connection getConnection() {
        return con;
    }

    @Override
    public ResourceWarning getWarnings() throws ResourceException {
        return null;
    }

    private void onMessage(String functionName, Record record) throws ResourceException {
        for (Entry<ActivationSpecImpl, MessageEndpointFactory> entry : con.cf.mcf.adapter.endpointFactories.entrySet())
            if (functionName.equalsIgnoreCase(entry.getKey().getFunctionName())) {
                MessageEndpoint endpoint = entry.getValue().createEndpoint(null);
                try {
                    ((MessageListener) endpoint).onMessage(record);
                } finally {
                    endpoint.release();
                }
            }
    }
}
