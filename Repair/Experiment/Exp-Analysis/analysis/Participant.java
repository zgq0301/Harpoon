package analysis;

/*
  This class represents a participant in the experiment
*/


import java.util.*;
import java.io.*;


public class Participant implements Comparable{

    private String userName; // the participant's user (identifies the participant)
    private String name;  // the participant's name (might be missing in some cases)
    private String population; // "tool" or "notool"

    private int order; // order 1 = "1 3 2 4",  order 2 = "3 1 4 2"
    
    private double experience; // the participant's level of experience


    private ArrayList rounds;  // the rounds in which this participant participated (usually four)


    // constructor
    public Participant(String userName, String name, String population) {
	setUserName(userName);
	setName(name);
	setPopulation(population);

	rounds = new ArrayList();

	// create four rounds for each participant
	//rounds.add(new Round(

	File file = new File("Participants"+File.separatorChar+userName);
	if (!file.exists())
	    file.mkdir();
    }


    // get round with the specified program;  adds it if it doesn't exist
    public Round getRound(int program) {
	Round r = getRoundWithProgram(program);
	if (r != null)
	    return r;

	Round newR = new Round(getNumberOfRounds()+1, program, population);
	rounds.add(newR);
	return newR;
    }
    

    // getters
    public String getUserName() {
	return userName;
    }

    public String getName() {
	return name;
    }
    
    public String getPopulation() {
	return population;
    }

    public int getOrder() {
	return order;
    }

    public double getExperience() {
	return experience;
    }

    public int getNumberOfRounds() {
	return rounds.size();
    }

    public Round getRoundNumber(int number) {
	return (Round) rounds.get(number);
    }

    public Round getRoundWithProgram(int program) {
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.getProgram() == program)
		return r;
	}	

	return null; // not found
    }


    // if only_correct is true, then we ignore the incorrect solutions
    public long getTotalTime(boolean only_correct) {
	long t = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    t += r.getTotalTime(only_correct);
	}

	return t; // not found
    }


    // if only_correct is true, then we ignore the incorrect solutions
    public long getTotalTime(int program, boolean only_correct) {
	long t = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.getProgram() == program)
		t += r.getTotalTime(only_correct);
	}

	return t; 
    }


    public long getTotalTime(String population, int program, boolean only_correct) {
	long t = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if ((r.getPopulation().equals(population)) && ((r.getProgram() == program)))
		t += r.getTotalTime(only_correct);
	}

	return t; 
    }


    public long getLocalizedTime(String population, int program) {
	long t = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if ((r.getPopulation().equals(population)) && ((r.getProgram() == program)))
		if ( (r.correct()) && (r.getLocalized()>0) )
		    t += r.getLocalized();
	}

	return t; 
    }


    public long getLocalizedTime(int program) {
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.getProgram() == program)
		return r.getLocalized();
	}

	return 0;
    }


    public long getLocalizedTime() {
	long t = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if ( (r.correct()) && (r.getLocalized()>0) )
		t += r.getLocalized();
	}

	return t; 
    }


    // if only_correct is true, then we ignore the incorrect solutions
    public long getLocalized(int program, boolean only_correct) {
	long t = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.getProgram() == program)
		t += r.getLocalized(only_correct);
	}

	return t; 
    }


    public int numberRounds(String population, int program) {
	int n = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if ((r.getPopulation().equals(population)) && ((r.getProgram() == program)))
		n++;
	}

	//System.out.println(getUserName()+" "+getPopulation()+" "+n);
	return n; 
    }


    public int numberCorrectRounds(String population, int program) {
	int n = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if ((r.getPopulation().equals(population)) && ((r.getProgram() == program)) && (r.correct()))
		n++;
	}

	return n; 
    }


    public int numberCorrectAndLocalizedRounds(String population, int program) {
	int n = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if ((r.getPopulation().equals(population)) && ((r.getProgram() == program)) && (r.correct()) && (r.getLocalized()>0))
		n++;
	}

	return n; 
    }



    public int numberCorrectRounds() {
	int n = 0;
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.correct())
		n++;
	}

	return n; 
    }



    // setters
    public void setUserName(String userName) {
	this.userName = userName;
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setPopulation(String population) {
	this.population = population;
    }

    public void setOrder(int order) {
	this.order = order;
    }

    public void setExperience(double experience) {
	this.experience = experience;
    }


    public void fixSubmissions() {
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (!r.submitted()) {
		//System.out.println(getUserName()+" did not submit program "+r.getProgram());	    
		r.setEndTime(r.getBuild(r.getNumberOfBuilds()-1).getTime());
	    }
	}
    }


    /* check if there are any builds with invokations to "calltool"
       this.getPopulation() should be "notool"       
    */
    public void checkCalltoolsInNotool() {
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.numberBuildsWithCalltools() != 0)
		System.out.println(getUserName()+" in population notool USED the tool in program "+r.getProgram());	    	    
	}
    }
    

    /* check if there are any rounds with NO invokations to "calltool"
       this.getPopulation() should be "tool"
    */
    public void checkNoCalltoolsInTool() {
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.numberBuildsWithCalltools() == 0)
		System.out.println(getUserName()+" in population tool DID NOT USE the tool in program "+r.getProgram());
	}
    }

    public void changePopulation(int program, String population) {
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    if (r.getProgram() == program)
		r.setPopulation(population);
	}
    }

	
    public String toString() {
	return 
	    "User name: "+getUserName()+"\n"+
 	    "Name: "+getName()+"\n"+
	    "Population: "+getPopulation();
    }


    public void printRounds() {
	Iterator it = rounds.iterator();
	while (it.hasNext()) {
	    Round r = (Round) it.next();
	    System.out.println(r);
	}	
    }


    public int compareTo(Object o) {
	if (o.equals(this))
	    return 0;

	if (this.getExperience() < ((Participant) o).getExperience())
	    return 1;
	else 
	    if (this.getExperience() > ((Participant) o).getExperience())
		return -1;
	    else 
		return this.getUserName().compareTo(((Participant) o).getUserName());    
    }
}
