package model;

import java.util.Arrays;

/**
 * Each transition may have a set of actions to be performed. 
 * 
 **/
public class  Transition {
    private String source;
    private String target;
    private String [] actions;
	
    /**
     * Returns the source state of a transition.
     * @return the id of the source state
     * */
    public String getSource() {
	return source;
    }
    /**
     * Returns the target state of a transition.
     * @return the id of the target state
     * */
    public String getTarget() {
	return target;
    }
    /**
     * Returns the set of actions in a transition.
     * @return a set of actions.
     * */
    public String[] getActions() {
	return actions;
    }
	
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(this.source+"-");
	sb.append(Arrays.toString(this.actions)+"-");
	sb.append(this.target);
	return sb.toString();
    }
	
}
