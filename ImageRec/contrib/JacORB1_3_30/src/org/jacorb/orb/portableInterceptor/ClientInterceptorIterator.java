package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.UserException;

import org.jacorb.orb.SystemExceptionHelper;
import org.jacorb.util.Debug;
/**
 * This class is an iterator over an array
 * of ClientRequestInterceptors.
 *
 * @author Nicolas Noffke
 * @version  $Id: ClientInterceptorIterator.java,v 1.1 2003-04-03 16:54:16 wbeebee Exp $
 */

public class ClientInterceptorIterator 
    extends RequestInterceptorIterator
{

    public static final short SEND_REQUEST = 0;
    public static final short SEND_POLL = 1;
    public static final short RECEIVE_REPLY = 2;
    public static final short RECEIVE_EXCEPTION = 3;
    public static final short RECEIVE_OTHER = 4;

    private ClientRequestInfoImpl info = null;

    public ClientInterceptorIterator(Interceptor[] interceptors)
    {
	super(interceptors);
    }

    public void iterate(ClientRequestInfoImpl info, short op)
	throws UserException
    {
	this.info = info;
	this.op = op;
	
	// ok, op <= SEND_POLL is more efficient but 
	// less understandable
	setDirection((op == SEND_REQUEST) || (op == SEND_POLL));

	iterate();

	//propagate last exception upwards
	if (interceptor_ex != null)
	    if (interceptor_ex instanceof ForwardRequest)
		throw (ForwardRequest) interceptor_ex;
	    else
		throw (org.omg.CORBA.SystemException) interceptor_ex;
    }

    /**
     * Iterates over the enumeration, i.e. calls "op" on
     * nextElement() until !hasMoreElements().
     */
    protected void invoke(Interceptor interceptor)
	throws UserException
    {
	info.caller_op = op;

	try
        {
	    Debug.output( Debug.DEBUG1 | Debug.INTERCEPTOR, 
                          "Invoking CI " + interceptor.name());
	    
            switch (op) 
            {
	    case SEND_REQUEST :
		((ClientRequestInterceptor) interceptor).send_request(info);
		break;
	    case SEND_POLL :
		((ClientRequestInterceptor) interceptor).send_poll(info);
		break;
	    case RECEIVE_REPLY :
		((ClientRequestInterceptor) interceptor).receive_reply(info);
		break;
	    case RECEIVE_EXCEPTION :
		((ClientRequestInterceptor) interceptor).receive_exception(info);
		break;
	    case RECEIVE_OTHER :
		((ClientRequestInterceptor) interceptor).receive_other(info);
	    }
	}catch (ForwardRequest _fwd)
        {
	    Debug.output(Debug.INFORMATION | Debug.INTERCEPTOR, _fwd);

	    reverseDirection();
	    op = RECEIVE_OTHER;
	
	    if (_fwd.permanent)
		info.reply_status = LOCATION_FORWARD_PERMANENT.value;
	    else
		info.reply_status = LOCATION_FORWARD.value;

	    info.forward_reference = _fwd.forward;
	    interceptor_ex = _fwd;

	}catch (org.omg.CORBA.SystemException _sysex)
        {
	    Debug.output(Debug.INFORMATION | Debug.INTERCEPTOR, 
                         _sysex);

	    reverseDirection();
	    op = RECEIVE_EXCEPTION;
	    interceptor_ex = _sysex;

	    SystemExceptionHelper.insert(info.received_exception, _sysex);
	
	    try
            {
		info.received_exception_id = SystemExceptionHelper.type(_sysex).id();
	    }catch(org.omg.CORBA.TypeCodePackage.BadKind _bk)
            {
		Debug.output(Debug.INFORMATION | Debug.INTERCEPTOR, _bk);
	    }
	}catch (Throwable th)
        {
	    Debug.output(Debug.IMPORTANT | Debug.INTERCEPTOR, 
                         "ClientInterceptorIterator: Caught a " + th);
	}
      
	info.caller_op = op;
    }
} // ClientInterceptorIterator






