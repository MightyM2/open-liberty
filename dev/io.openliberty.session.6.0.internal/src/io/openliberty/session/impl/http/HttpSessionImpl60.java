/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.session.impl.http;

import jakarta.servlet.ServletContext;
import java.util.logging.Level;

import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.session.AbstractHttpSessionFacade;
import com.ibm.ws.session.AbstractSessionData;
import com.ibm.ws.session.SessionContext;
import com.ibm.ws.session.utils.LoggingUtil;
import com.ibm.wsspi.session.ISession;

import io.openliberty.session.impl.HttpSessionFacade60;

/**
 * This class provides the adapted version of the ISession.
 * It simply wrappers the session and proxies any of its method calls to
 * the underlying ISession object.
 *
 * Since Servlet 6.0
 */
public class HttpSessionImpl60 extends AbstractSessionData {

    private static final String methodClassName = "HttpSessionImpl60";

    protected HttpSessionImpl60(ISession session, SessionContext sessCtx, ServletContext servCtx) {
        super(session, sessCtx, servCtx);

        if (TraceComponent.isAnyTracingEnabled() && LoggingUtil.SESSION_LOGGER_CORE.isLoggable(Level.FINER)) {
            LoggingUtil.SESSION_LOGGER_CORE.log(Level.FINE, methodClassName + " Constructor");
        }
    }

    @Override
    protected AbstractHttpSessionFacade returnFacade() {
        if (TraceComponent.isAnyTracingEnabled() && LoggingUtil.SESSION_LOGGER_CORE.isLoggable(Level.FINER)) {
            LoggingUtil.SESSION_LOGGER_CORE.log(Level.FINE, methodClassName + " returnFacade HttpSessionFacade60");
        }

        return new HttpSessionFacade60(this);
    }

    /**
     * Method toString
     * <p>
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("# HttpSessionImpl60 # \n { ").append("\n _iSession=").append(getISession()).append("\n } \n");
        return sb.toString();
    }
}
