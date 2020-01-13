package aadl2upaal.aadl;

public class HCommunication {
	private APort p;
	private int direction = APort.in;
	private AVar var;

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	private int seq = 0;

	public APort getP() {
		return p;
	}

	public void setP(APort p) {
		this.p = p;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public AVar getVar() {
		return var;
	}

	public void setVar(AVar var) {
		this.var = var;
	}

}
