// InterThreadPA.java, created Mon Jan 31 20:52:46 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Date;

import harpoon.Util.Util;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.Temp.Temp;

import harpoon.IR.Quads.NEW;


/**
 * <code>InterThreadPA</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: InterThreadPA.java,v 1.1.2.11 2000-03-02 04:45:30 salcianu Exp $
 */
abstract class InterThreadPA {
    
    private static final boolean DEBUG = true;
    private static final boolean DEBUG2 = true;
    private static final boolean TIMING = true;

    public static ParIntGraph resolve_threads(ParIntGraph pig,
					      PointerAnalysis pa){

	long begin_time = System.currentTimeMillis();

	pig = (ParIntGraph) pig.clone();

	HashSet analyzed_threads = new HashSet();

	while(true){
	    PANode nt = pick_an_unanalyzed_thread(pig,analyzed_threads);
	    if(nt==null) break;

	    if(DEBUG)
		System.out.println(nt + " was chosen");

	    HMethod[] ops = get_run_methods(nt,pig,pa);
	    analyzed_threads.add(nt);
	    if((ops == null) || (ops.length==0) ||
	       !analyzable_run_methods(ops,pa)) continue;	    

	    ParIntGraph old_pig = pig;
	    pig = interaction_nt(pig,nt,ops,pa);

	    if(!pig.equals(old_pig))
		analyzed_threads.clear();
	}
	
	if(TIMING){
	    long total_time = System.currentTimeMillis() - begin_time;
	    System.out.println("Inter-thread analysis done in " + 
			       total_time + "ms");
	}

	return pig;
    }


    // See if the run method(s) that could be the body of a thread are 
    // analyzable with regard to <code>pa</code> (i.e. <code>pa</code>
    // can satisfy queries about these methods.)
    private static boolean analyzable_run_methods(HMethod[] ops,
						  PointerAnalysis pa){
	for(int i = 0 ; i < ops.length ; i++)
	    if((ops[i] == null) || (pa.getExtParIntGraph(ops[i]) == null))
		return false;
	return true;			    
    }


    // Finds an active thread node (i.e. tau(nt) != 0) whose interactions
    // with the Starter haven't been analyzed yet.
    private static PANode pick_an_unanalyzed_thread(ParIntGraph pig,
						    Set analyzed_threads){
	Enumeration enum = pig.tau.activeThreads();
	while(enum.hasMoreElements()){
	    PANode nt = (PANode) enum.nextElement();
	    if((nt.type == PANode.INSIDE) && !analyzed_threads.contains(nt))
		return nt;
	}
	return null;
    }

    // Returns a vector containing all the run() methods that could be
    // the body of the threads abstracted by nt.
    private static HMethod[] get_run_methods(PANode nt, ParIntGraph pig,
					     PointerAnalysis pa){

	NEW q = (NEW) pa.getNodeRepository().node2Code(nt);
	HClass hclass = q.hclass();

	HMethod[] hms = hclass.getMethods();
	HMethod hm = null;

	for(int i = 0 ; i < hms.length ; i++)
	    if(hms[i].getName().equals("run") &&
	       (hms[i].getParameterTypes().length == 0)){
		hm = hms[i];
		break;
	    }

	if(hm == null) return null;

	if(DEBUG)
	    System.out.println("Run method: " + hm);

	return new HMethod[]{hm};
    }


    // Computes the interactions with all the threads launched by the node
    // nt. If tau(nt)==1 (at most one such thread could exist), this method
    // computes the interaction of the Starter with only one instance of
    // an nt-thread. If tau(nt)==2 (there could be more than one thread,
    // anything between 0 and infinity); a fixed-point algorithm is necessary
    // in this case.
    private static ParIntGraph interaction_nt(ParIntGraph pig, PANode nt,
					HMethod[] ops, PointerAnalysis pa){
	boolean only_once = (pig.tau.getValue(nt)==1);

	if(DEBUG)
	    System.out.println("interaction_nt:" + 
			       nt + (only_once?" only once":" many times"));

	// save the old outside edge set
	PAEdgeSet old_O = pig.G.O;
	// consider only the outside edges that have been read in //
	// with an "nt" thread.
	pig.G.O = construct_new_O(pig,nt);

	adjust_escape_and_tau(pig,nt);

	if(only_once)
	    // a single interaction takes place
	    pig = interact_once(pig,nt,ops,pa);
	else{
	    // a fixed-point algorithm is necessary in this case
	    while(true){
		ParIntGraph previous_pig = pig;
		pig = interact_once(pig,nt,ops,pa);
		if(pig.equals(previous_pig)) break;
	    }
	}

	// add the old, unconsidered outside edges
	pig.G.O.union(old_O);

	// TODO: is this correct? (here)
	pig.G.propagate();
	pig.removeEmptyLoads();

	return pig;
    }


    // Constructs a new set of outside edges for pig, containing only those
    // outside edges that are read in parallel with an nt thread.
    // Returns the new set of edges.
    private static PAEdgeSet construct_new_O(ParIntGraph pig, PANode nt){
	PAEdgeSet new_O = new PAEdgeSet();

	Iterator it_loads = pig.ar.parallelLoads(nt);
	while(it_loads.hasNext()){
	    PALoad load = (PALoad) it_loads.next();
	    new_O.addEdge(load.n1,load.f,load.n2);
	}

	return new_O;
    }


    // Computes the interaction with a SINGLE instance of a thread launched
    // by the nt node. This incolves separately computing the interactions
    // with all the possible run() methods (the body of the thread) and
    // joining the results.
    private static ParIntGraph interact_once(ParIntGraph pig, PANode nt,
				      HMethod[] ops, PointerAnalysis pa){
	int nb_ops = ops.length;
	Util.assert(nb_ops > 0, "No run method for the thread" + nt);

	// special, optimized case: only one run method to analyze
	if(nb_ops == 1)
	    return interact_once_op(pig,nt,ops[0],pa);

	// general case: many possible run() method. The following code could
	// seem too complicate but everything has been done to reduce the
	// costly ParIntGraph.clone() operation to the minimum

	// compute the first term of the join operation:
	// the interaction with the first run() method
	ParIntGraph pig_after = 
	    interact_once_op(pig,nt,ops[0],pa);
	
	// join to it all the other terms (interactions with all the
	// other run() methods).
	for(int i = 1 ; i < nb_ops ; i++)
	    pig_after.join(interact_once_op(pig,nt,ops[i],pa)); 

	return pig_after;
    }


    // Computes the interaction between the Starter and a SINGLE thread having
    // the node nt as a receiver and op as the run() body function.
    private static ParIntGraph interact_once_op(ParIntGraph pig_starter,
			       PANode nt,HMethod op, PointerAnalysis pa){
	ParIntGraph pig[] = new ParIntGraph[2];
	pig[0] = pig_starter;
	pig[1] = pa.getExtParIntGraph(op);

	if(DEBUG2){
	    System.out.println("interact_once_op:");
	    System.out.println("  nt node: " + nt);
	    System.out.println("  run method: " + op);
	    System.out.println("PIG STARTER: " + pig[0]);
	    System.out.println("PIG STARTEE: " + pig[1]);
	}
	
	PANode[] params = pa.getParamNodes(op);
	
	Relation mu[] = compute_initial_mappings(pig,nt,params);

	if(DEBUG2){
	    System.out.println("INITIAL MAPPINGS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	concretize_loads(pig,mu);
	
	if(DEBUG2){
	    System.out.println("AFTER CONCRETIZE LOADS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	compute_final_mappings(pig,mu,nt);

	if(DEBUG2){
	    System.out.println("FINAL MAPPINGS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	ParIntGraph new_pig = build_new_pig(pig,mu,params[0],nt);

	if(DEBUG2){
	    System.out.println("NEW GRAPH:");
	    System.out.println(new_pig);
	}

	return new_pig;
    }

    // In Starter: e(n) = e(n) - {nt}, for all n and tau(nt)--
    private static void adjust_escape_and_tau(ParIntGraph pig, PANode nt){
	pig.G.e.removeNodeHoleFromAll(nt);
	//pig.tau.setToZero(nt);
	pig.tau.dec(nt);
    }

    /** Set the initial mappings: class nodes, parameter->thread node.
	Parameters:<br><ul>
	<li>pig[0] - the parallel interaction graph of the Starter;
	<li>pig[1] - the parallel interaction graph of the Startee.
	</ul><br>
	Returns:<br><ul>
	<li>mu[0] - the mapping of the nodes from the Starter <br>;
	<li>mu[1] - the mapping of the nodes from the Startee <br>.
	</ul>
    */    
    private static Relation[] compute_initial_mappings(ParIntGraph[] pig,
						       PANode nt,
						       PANode[] params){
	// Paranoic debug! Trust no one!
	Util.assert(params.length == 1, "Thread function with too many args");

	Relation mu0 = new Relation();
	map_static_nodes(pig[0],mu0);

	Relation mu1 = new Relation();
	mu1.add(params[0],nt);
	map_static_nodes(pig[1],mu1);

	return (new Relation[]{mu0,mu1});
    }


    /* Maps the static nodes that appear in pig to themselves. Only those
       static nodes that appear as sources of arcs need to be mapped; if
       necessary, the others wil be mapped by the rest of the algorithm.
       (the matching goes always "forward" on the edges, never "backward", so
       it's necessary to trigger it just in the sources of the edges. */
    private static void map_static_nodes(ParIntGraph pig,Relation mu){
	Enumeration enum = pig.G.O.allSourceNodes();
	while(enum.hasMoreElements()){
	    PANode node = (PANode) enum.nextElement();
	    if(node.type == PANode.STATIC)
		mu.add(node,node);
	}
	enum = pig.G.I.allSourceNodes();
	while(enum.hasMoreElements()){
	    PANode node = (PANode) enum.nextElement();
	    if(node.type == PANode.STATIC)
		mu.add(node,node);
	}
    }


    /** Computes the mappings by matching ouside edges from one graph
	against inside edges from the other one. */
    private static void concretize_loads(ParIntGraph[] pig, Relation[] mu){

	PAWorkList W[] = { new PAWorkList(), new PAWorkList() };

	Relation new_info[] = { (Relation)(mu[0].clone()),
				(Relation)(mu[1].clone()) };
	
	W[0].addAll(mu[0].keySet());
	W[1].addAll(mu[1].keySet());

	while(true){
	    int i,ib;
	    if(!W[0].isEmpty()) { i=0; ib=1; }
	    else 
		if(!W[1].isEmpty()) { i=1; ib=0; }
		else{
		    Matching.rule0(mu,W);
		    if(W[0].isEmpty() && W[1].isEmpty()) break;
		    else continue;
		}

	    PANode node = (PANode) W[i].remove();

	    // new mappings for node
	    Set new_mappings = new HashSet(new_info[i].getValuesSet(node));
	    new_info[i].removeAll(node);

	    // Matching.rule0(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule2(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule22(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule3(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule32(node,new_mappings,pig,W,mu,new_info,i,ib);
	}
    }


    // Compute the final mappings. Every node from the Starter and the Startee
    // will be put in the new graph (node-mu->node) except for the parameter
    // node of the Startee run() method; this one will be mapped to nt.
    private static void compute_final_mappings(final ParIntGraph[] pig,
					       final Relation[] mu,
					       final PANode nt){
	PANodeVisitor visitor_starter = new PANodeVisitor(){
		public void visit(PANode node){
		    mu[0].add(node,node);
		}
	    };

	pig[0].G.forAllNodes(visitor_starter);

	PANodeVisitor visitor_startee = new PANodeVisitor(){
		public void visit(PANode node){
		    int type = node.type();
		    if(type == PANode.PARAM)
			mu[1].add(node,nt);
		    else
			mu[1].add(node,node);
		}
	    };

	pig[1].G.forAllNodes(visitor_startee);
    }


    // Build the new graph using the graphs from the starter and the startee
    // and the mu mappings.
    private static ParIntGraph build_new_pig(ParIntGraph[] pig, Relation[] mu,
					     PANode nparam, PANode nt){

	ParIntGraph new_pig = new ParIntGraph();

	new_pig.insertAllButAr(pig[0],mu[0], true);
	new_pig.insertAllButAr(pig[1],mu[1], false, Collections.singleton(nparam));

	// compute the escape function for the new graph
	new_pig.G.propagate();

	bring_starter_actions(pig[0], new_pig, mu[0],
			      pig[1].tau.activeThreadSet(), nt);

	bring_startee_actions(pig[1], new_pig, mu[1],
			      pig[0].tau.activeThreadSet(), nt);

	return new_pig;
    }


    // Add the actions from the starter into the new graph.
    private static void bring_starter_actions(final ParIntGraph pig_starter,
					      final ParIntGraph new_pig,
					      final Relation mu_starter,
					      final Set startee_active_threads,
					      final PANode nt){

	mu_starter.add(ActionRepository.THIS_THREAD,
		       ActionRepository.THIS_THREAD);

	ActionVisitor act_visitor_starter = new ActionVisitor(){
		public void visit_ld(PALoad load){
		    if(!mu_starter.contains(load.n2,load.n2)) return;
		    new_pig.ar.add_ld(mu_starter.getValuesSet(load.n1),
				      load.f,
				      load.n2,
				      mu_starter.getValuesSet(load.nt),
				      Collections.EMPTY_SET);
		}
		public void visit_sync(PANode n, PANode nt1){
		    new_pig.ar.add_sync(mu_starter.getValuesSet(n),
					mu_starter.getValuesSet(nt1),
					Collections.EMPTY_SET);
		}
	    };

	pig_starter.ar.forAllActions(act_visitor_starter);

	ParActionVisitor par_act_visitor_starter = new ParActionVisitor(){

		public void visit_par_ld(PALoad load, PANode nt2){

		    Set parallel_threads = mu_starter.getValuesSet(nt2);
		    if(nt2 == nt)
			parallel_threads.addAll(startee_active_threads);

		    if(!mu_starter.contains(load.n2,load.n2)) return;
		    new_pig.ar.add_ld(mu_starter.getValuesSet(load.n1),
				      load.f,
				      load.n2,
				      mu_starter.getValuesSet(load.nt),
				      parallel_threads);
		}

		public void visit_par_sync(PANode n, PANode nt1, PANode nt2){

		    Set parallel_threads = mu_starter.getValuesSet(nt2);
		    if(nt2 == nt)
			parallel_threads.addAll(startee_active_threads);

		    new_pig.ar.add_sync(mu_starter.getValuesSet(n),
					mu_starter.getValuesSet(nt1),
					parallel_threads);
		}
	    };

	pig_starter.ar.forAllParActions(par_act_visitor_starter);
    }


    // Add the actions from the startee into the new graph.
    private static void bring_startee_actions(final ParIntGraph pig_startee,
					      final ParIntGraph new_pig,
					      final Relation mu_startee,
					      final Set starter_active_threads,
					      final PANode nt){

	mu_startee.add(ActionRepository.THIS_THREAD,nt);

	ActionVisitor act_visitor_startee = new ActionVisitor(){
		public void visit_ld(PALoad load){
		    if(!mu_startee.contains(load.n2,load.n2)) return;
		    new_pig.ar.add_ld(mu_startee.getValuesSet(load.n1),
				      load.f,
				      load.n2,
				      mu_startee.getValuesSet(load.nt),
				      starter_active_threads);
		}
		public void visit_sync(PANode n, PANode nt1){
		    new_pig.ar.add_sync(mu_startee.getValuesSet(n),
					mu_startee.getValuesSet(nt1),
					starter_active_threads);
		}
	    };

	pig_startee.ar.forAllActions(act_visitor_startee);

	ParActionVisitor par_act_visitor_startee = new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		    if(!mu_startee.contains(load.n2,load.n2)) return;
		    new_pig.ar.add_ld(mu_startee.getValuesSet(load.n1),
				      load.f,
				      load.n2,
				      mu_startee.getValuesSet(load.nt),
				      mu_startee.getValuesSet(nt2));
		}
		public void visit_par_sync(PANode n, PANode nt1, PANode nt2){
		    new_pig.ar.add_sync(mu_startee.getValuesSet(n),
					mu_startee.getValuesSet(nt1),
					mu_startee.getValuesSet(nt2));
		}
	    };

	pig_startee.ar.forAllParActions(par_act_visitor_startee);

    }

}
