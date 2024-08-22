package dev.ikm.reasoner.hybrid.temporal;

import java.util.ArrayList;
import java.util.List;

import dev.ikm.reasoner.hybrid.temporal.RtipConjunction;

public class RtipDisjunction {
	private List<RtipConjunction> rtipConjunctions = new ArrayList<RtipConjunction>();

	public RtipDisjunction() {
	}

	public List<RtipConjunction> getRtipConjunctions() {
		return this.rtipConjunctions;
	}

	public void setRtipConjunctions(List<RtipConjunction> rtipConjunctions) {
		this.rtipConjunctions = rtipConjunctions;
	}
	
	public void addRtipConjunction(RtipConjunction rtipConjunction) {
		this.rtipConjunctions.add(rtipConjunction);
	}

	public String toString() {
		return new String("I'm an RtipDisjunctionComponent!");
	}
}
