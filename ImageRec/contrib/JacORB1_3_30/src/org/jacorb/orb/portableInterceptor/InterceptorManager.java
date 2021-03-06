package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import java.util.*;
import org.jacorb.util.Debug;

/**
 * This class "manages" the portable interceptors registered
 * with the ORB, and controls the PICurrent.
 *
 * @author Nicolas Noffke
 * @version $Id: InterceptorManager.java,v 1.1 2003-04-03 16:54:16 wbeebee Exp $
 */

public class InterceptorManager  
{
    private Interceptor[] client_req_interceptors = null;
    private Interceptor[] server_req_interceptors = null;
    private Interceptor[] ior_interceptors = null;

    private Hashtable currents = null;
    private org.omg.CORBA.ORB orb = null;
    private int current_slots = 0;

    public static final PICurrentImpl EMPTY_CURRENT = new PICurrentImpl(null, 0);

    public InterceptorManager(Vector client_interceptors,
                              Vector server_interceptors,
                              Vector ior_intercept,
                              int slot_count,
                              org.omg.CORBA.ORB orb) 
    {
        
        Debug.output( Debug.INTERCEPTOR | Debug.INFORMATION, 
                      "InterceptorManager started with " + 
                      server_interceptors.size() +" SIs, " 
                      + client_interceptors.size() + " CIs and " +
                      ior_intercept.size() + " IORIs");

        //build sorted arrays of interceptors
        client_req_interceptors = new ClientRequestInterceptor
            [client_interceptors.size()];

        //selection sort
        for (int j = 0; j < client_req_interceptors.length; j++){
            String min = ((ClientRequestInterceptor) client_interceptors.
                          elementAt(0)).name();
            int min_index = 0;

            for(int _i = 1; _i < client_interceptors.size(); _i++)
                if (min.compareTo(((ClientRequestInterceptor) client_interceptors.
                                   elementAt(_i)).name()) > 0){
                    min = ((ClientRequestInterceptor) client_interceptors.
                           elementAt(_i)).name();
                    min_index = _i;
                }

            client_req_interceptors[j] = (ClientRequestInterceptor) 
                client_interceptors.elementAt(min_index);
            client_interceptors.removeElementAt(min_index);
        }

        server_req_interceptors = new ServerRequestInterceptor
            [server_interceptors.size()];
        //selection sort
        for (int j = 0; j < server_req_interceptors.length; j++){
            String min = ((ServerRequestInterceptor) server_interceptors.
                          elementAt(0)).name();
            int min_index = 0;

            for(int _i = 1; _i < server_interceptors.size(); _i++)
                if (min.compareTo(((ServerRequestInterceptor) server_interceptors.
                                   elementAt(_i)).name()) > 0){
                    min = ((ServerRequestInterceptor) server_interceptors.
                           elementAt(_i)).name();
                    min_index = _i;
                }

            server_req_interceptors[j] = (ServerRequestInterceptor) 
                server_interceptors.elementAt(min_index);

            server_interceptors.removeElementAt(min_index);   
        }


        ior_interceptors = new IORInterceptor[ior_intercept.size()];
        //selection sort
        for (int j = 0; j < ior_interceptors.length; j++){
            String min = ((IORInterceptor) ior_intercept.elementAt(0)).name();
            int min_index = 0;

            for(int _i = 1; _i < ior_intercept.size(); _i++)
                if (min.compareTo(((IORInterceptor) ior_intercept.
                                   elementAt(_i)).name()) > 0){
                    min = ((IORInterceptor) ior_intercept.elementAt(_i)).name();
                    min_index = _i;
                }
	   
            ior_interceptors[j] = (IORInterceptor) ior_intercept.
                elementAt(min_index);

            ior_intercept.removeElementAt(min_index);
        }
      
        this.orb = orb;
        current_slots = slot_count;

        currents = new Hashtable();
    }

    /**
     * This method returns a thread specific PICurrent. 
     */
    public Current getCurrent()
    {
        Current ts_current = (Current) currents.get(Thread.currentThread());
    
        if (ts_current == null){
            //create new client current
            //server currents have been created and set separately
            Debug.output( Debug.INTERCEPTOR | Debug.INFORMATION, 
                          "InterceptorManager.getCurrent() creates new empty PI current");
            ts_current = getEmptyCurrent();
            currents.put(Thread.currentThread(),
                         ts_current);
        }

        return ts_current;
    }

    /**
     * Sets the thread scope current, i.e. a server side current
     * associated with the calling  thread.
     */
    public void setTSCurrent(Current current){
        currents.put(Thread.currentThread(), 
                     new PICurrentImpl((PICurrentImpl)current));
    }

    /**
     * Removes the thread scope current, that is associated with the
     * calling thread.
     */
    public void removeTSCurrent(){
        currents.remove(Thread.currentThread());
    }

    /**
     * Returns an empty current where no slot has been set.
     */
    public Current getEmptyCurrent(){
        return new PICurrentImpl(orb, current_slots);
    }

    /**
     * Returns an iterator object that contains the ClientRequestInterceptors
     * of this manager.
     */
    public ClientInterceptorIterator getClientIterator()
    {
        return new ClientInterceptorIterator(client_req_interceptors);
    }

    /**
     * Returns an iterator object that contains the ServerRequestInterceptors
     * of this manager.
     */
    public ServerInterceptorIterator getServerIterator()
    {
        return new ServerInterceptorIterator(server_req_interceptors);
    }

    /**
     * Returns an iterator object that contains the IORInterceptors
     * of this manager.
     */
    public IORInterceptorIterator getIORIterator()
    {
        return new IORInterceptorIterator(ior_interceptors);
    }

  
    /**
     * Test, if the manager has ClientRequestInterceptors
     */
    public boolean hasClientRequestInterceptors()
    {
        return client_req_interceptors.length > 0;
    }

    /**
     * Test, if the manager has ServerRequestInterceptors
     */
    public boolean hasServerRequestInterceptors()
    {
        return server_req_interceptors.length > 0;
    }

    /**
     * Test, if the manager has IORInterceptors
     */
    public boolean hasIORInterceptors()
    {
        return ior_interceptors.length > 0;
    }
} // InterceptorManager






