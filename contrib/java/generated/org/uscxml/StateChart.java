package org.uscxml;

import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/** Base class for generated StateCharts */

public abstract class StateChart {
	
	public enum InterpreterState {
		  USCXML_FINISHED,
		  USCXML_UNDEF,
		  USCXML_IDLE,
		  USCXML_INITIALIZED,
		  USCXML_INSTANTIATED,
		  USCXML_MICROSTEPPED,
		  USCXML_MACROSTEPPED,
		  USCXML_CANCELLED
	}
	
	public enum StateType {
		USCXML_STATE_ATOMIC,
		USCXML_STATE_PARALLEL,
		USCXML_STATE_COMPOUND,
		USCXML_STATE_FINAL,
		USCXML_STATE_HISTORY_DEEP,
		USCXML_STATE_HISTORY_SHALLOW,
		USCXML_STATE_INITIAL,
		USCXML_STATE_HAS_HISTORY
	}

	public enum TransitionType {
		USCXML_TRANS_SPONTANEOUS,
		USCXML_TRANS_TARGETLESS,
		USCXML_TRANS_INTERNAL,
		USCXML_TRANS_HISTORY,
		USCXML_TRANS_INITIAL
	}

	public abstract class State {
		String name;
		int parent;
		BitSet children;
		BitSet completion;
		BitSet ancestors;
		Data data;
		StateType type;
		
		public abstract void onEntry() throws InterpreterException;
		public abstract void onExit() throws InterpreterException;
		public abstract void invoke(Invoke invoker, boolean invoke) throws InterpreterException;
	}
	
	public abstract class Transition {
		int source;
		BitSet target;
		String event;
		String condition;
		TransitionType type;
		BitSet conflicts;
		BitSet exitSet;

		public abstract boolean isEnabled();
		public abstract boolean isMatched();
		public abstract void onTransition();
	}

	public class Data {
		String id; 
		String src; 
		String expr; 
		String content; 
	}

	public class Assign {
		String location; 
		String expr; 
		String content; 
	}

	public class Foreach {
		String array; 
		String item; 
		String index; 
	}

	public class Param {
		String name; 
		String expr; 
		String location; 
	}

	public class DoneData {
		int source; 
		String content; 
		String contentExpr; 
		List<Param> params;
	}
	
	public abstract class Invoke {
		StateChart machine;
		String type;
		String typeExpr;
		String src;
		String srcExpr;
		String id;
		String idlocation;
		String sourceName;
		String namelist;
		boolean autoForward;
		String content;
		String contentExpr;
		List<Param> params;
		
		public abstract void finalize();
	}

	public class Send {
		String event;
		String eventExpr;
		String target;
		String targetExpr;
		String type;
		String typeExpr;
		String id;
		String idlocation;
		String delay;
		String delayExpr;
		String namelist;
		String content;
		String contentExpr;
		List<Param> params;
	}
	
	public List<Transition> transitions;
	public List<State> states;
	
	public Deque<Object> externalQueue;
	public Deque<Object> internalQueue;	
	
	protected InterpreterState state = InterpreterState.USCXML_UNDEF;
	protected Object event;
	
	protected BitSet flags;
	protected BitSet config;
	protected BitSet history;
	protected BitSet invocations;
	protected BitSet initializedData;
	
	protected Map<String, Integer> stateNamesToIndex;
	
	public InterpreterState step() throws org.uscxml.InterpreterException {
		return step(0);
	}

	public InterpreterState step(long blockMs) throws org.uscxml.InterpreterException {
		/** Here you would implement microstep(T) as in the book chapter */
		//check for  spontaneous transition, if yes set event to 
//		state = InterpreterState.USCXML_UNDEF;
		
		// if no more spontaneous event, deque from internal queue
		if(!internalQueue.isEmpty()){
			this.dequeueInternal();
			//TODO process
		}else if(!externalQueue.isEmpty()){
			this.dequeueExternal();
			//TODO process external event
		}else{
			//TODO Block and wait till external event
			
		}
		
		
		
		/** Just to silence the compiler warning */
		if (true) throw new InterpreterException("", "");
		return state;
	}

	public void cancel() {
		state = InterpreterState.USCXML_CANCELLED;
	}

	public void reset() {
		history.clear();
		config.clear();
		flags.clear();
		// @TODO: uninvoke any invokers
		invocations.clear();
	}

	public InterpreterState getState() { return state; }
	
	public boolean isInState(String stateId) {
		if (!stateNamesToIndex.containsKey(stateId))
			return false;
		return config.get((int) stateNamesToIndex.get(stateId));
	}

	public void receive(Object event) {
		externalQueue.addLast(event);
	}
	
	protected Object dequeueInternal() {
		try {
			return internalQueue.removeLast();
		} catch(Exception e) {
			return null;
		}
	}

	protected Object dequeueExternal() {
		try {
			return externalQueue.removeLast();
		} catch(Exception e) {
			return null;
		}
	}

	public abstract boolean isTrue(String expression);
	public abstract void raiseDoneEvent(State state, DoneData doneData);

	public abstract void execContentLog(String label, String expression);
	public abstract void execContentRaise(String event);
	public abstract void execContentSend(Send send);
	public abstract void execContentForeachInit(Foreach foreach);
	public abstract void execContentForeachNext(Foreach foreach);
	public abstract void execContentForeachDone(Foreach foreach);
	public abstract void execContentAssign(Assign assign);
	public abstract void execContentInit(Data data);
	public abstract void execContentCancel(String sendId, String sendIdExpr);
	public abstract void execContentScript(String src, String content);
	public abstract void execContentFinalize(Invoke invoker, Object event);
		
}