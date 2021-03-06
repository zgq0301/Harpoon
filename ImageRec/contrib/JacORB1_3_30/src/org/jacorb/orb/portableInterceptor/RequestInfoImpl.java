package org.jacorb.orb.portableInterceptor;

import org.omg.IOP.*;
import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*;
import org.omg.Dynamic.Parameter;
import java.util.*;

/**
 * This is the abstract base class of the two
 * Info classes, namely ClientRequestInfo and 
 * ServerRequestInfo. <br>
 * See PI Spec p. 5-41ff
 *
 * @author Nicolas Noffke
 * @version $Id: RequestInfoImpl.java,v 1.1 2003-04-03 16:54:16 wbeebee Exp $
 */

public abstract class RequestInfoImpl 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements RequestInfo
{

    public int request_id;
    public String operation = null;

    public Parameter[] arguments = null;
    public TypeCode[] exceptions = null;
    public Any result = null;
    public boolean response_expected;
    public org.omg.CORBA.Object forward_reference = null;
    public short reply_status;
    public org.omg.PortableInterceptor.Current current = null;
  
    protected Hashtable request_ctx = null;
    protected Hashtable reply_ctx = null;

    public short caller_op = -1;
    
    public RequestInfoImpl() {
        request_ctx = new Hashtable();
        reply_ctx = new Hashtable();
    }

    /**
     * Make the existing request ServiceContexts available to
     * the interceptors. Only one ServiceContext per id
     * is allowed.
     */
    public void setRequestServiceContexts(ServiceContext[] ctx){
        for (int _i = 0; _i < ctx.length; _i++)
            request_ctx.put(new Integer(ctx[_i].context_id), ctx[_i]);
    }

    /**
     * Make the existing reply ServiceContexts available to
     * the interceptors. Only one ServiceContext per id
     * is allowed.
     */
    public void setReplyServiceContexts(ServiceContext[] ctx){
        for (int _i = 0; _i < ctx.length; _i++)
            reply_ctx.put(new Integer(ctx[_i].context_id), ctx[_i]);
    }

    // implementation of org.omg.PortableInterceptor.RequestInfoOperations interface
    public Parameter[] arguments() {
        return null;
    }
  
    public String[] contexts() {
        throw new NO_RESOURCES("JacORB does not support operation contexts", 
                               1, CompletionStatus.COMPLETED_MAYBE);
    }
  
    public TypeCode[] exceptions() {
        return null;
    }
  
    public org.omg.CORBA.Object forward_reference() {
        return null;
    }
  
    public ServiceContext get_reply_service_context(int id) {
        Integer _id = new Integer(id);
        if (! reply_ctx.containsKey(_id))
            throw new BAD_PARAM("No ServiceContext with id " + id, 23, 
                                CompletionStatus.COMPLETED_MAYBE);
        else
            return (ServiceContext) reply_ctx.get(_id);
    }
  
    public ServiceContext get_request_service_context(int id) {    
        Integer _id = new Integer(id);
        if (! request_ctx.containsKey(_id))
            throw new BAD_PARAM("No ServiceContext with id " + id, 23, 
                                CompletionStatus.COMPLETED_MAYBE);
        else
            return (ServiceContext) request_ctx.get(_id);
    }
  
    public Any get_slot(int id) throws InvalidSlot {
        return current.get_slot(id);
    }
  
    public String operation() {
        return operation;
    }
  
    public String[] operation_context() {
        throw new NO_RESOURCES("JacORB does not support operation contexts", 1, 
                               CompletionStatus.COMPLETED_MAYBE);
    }
  
    public short reply_status() {
        return reply_status;
    }
  
    public int request_id() {
        return request_id;
    }
  
    public boolean response_expected() {
        return response_expected;
    }
  
    public Any result() {
        if (result == null)
            throw new NO_RESOURCES("Stream-based skeletons/stubs do not support this op", 
                                   1, CompletionStatus.COMPLETED_MAYBE);
        else
            return result;
    }
  
    public short sync_scope() {
        return org.omg.Messaging.SYNC_WITH_TRANSPORT.value;
    }
} // RequestInfoImpl






