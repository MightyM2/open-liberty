/*******************************************************************************
 * Copyright (c) 2013,2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.example.jca.adapter;

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;
import jakarta.resource.spi.ConnectionManager;

import javax.naming.NamingException;
import javax.naming.Reference;

import com.ibm.example.jca.adapter.ConnectionSpecImpl.ConnectionRequestInfoImpl;

/**
 * Example connection factory.
 */
public class ConnectionFactoryImpl implements ConnectionFactory, RecordFactory {
    private static final long serialVersionUID = 847022212144243370L;

    private final ConnectionManager cm;
    final ManagedConnectionFactoryImpl mcf;
    private Reference ref;

    ConnectionFactoryImpl(ConnectionManager cm, ManagedConnectionFactoryImpl mcf) {
        this.cm = cm;
        this.mcf = mcf;
    }

    @Override
    public IndexedRecord createIndexedRecord(String name) throws ResourceException {
        throw new NotSupportedException();
    }

    @Override
    public MappedRecord createMappedRecord(String name) throws ResourceException {
        MappedRecord record = new MappedRecordImpl();
        record.setRecordName(name);
        return record;
    }

    @Override
    public Connection getConnection() throws ResourceException {
        return getConnection(new ConnectionSpecImpl());
    }

    @Override
    public Connection getConnection(ConnectionSpec conSpec) throws ResourceException {
        ConnectionRequestInfoImpl cri = ((ConnectionSpecImpl) conSpec).createConnectionRequestInfo();
        cri.put("tableName", mcf.getTableName());

        ConnectionImpl con = cm == null ? new ConnectionImpl(null, cri) : (ConnectionImpl) cm.allocateConnection(mcf, cri);
        con.cf = this;
        return con;
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        throw new NotSupportedException();
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return this;
    }

    @Override
    public Reference getReference() throws NamingException {
        return ref;
    }

    @Override
    public void setReference(Reference ref) {
        this.ref = ref;
    }
}
