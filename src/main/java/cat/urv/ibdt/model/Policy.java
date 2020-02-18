package cat.urv.ibdt.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/*
 * Aquesta classe encapsula els elements de la política de signatura Gamma,
 * definida com a Gamma = ( t, S ).
 */
public class Policy implements Serializable {

	private static final long serialVersionUID = -538679194904576887L;
	Integer t;
	Integer[] S;

	public Policy(Integer t, Integer[] S) {
		this.t = t;
		this.S = S;
	}

	public Integer getT() {
		return t;
	}

	public void setT(Integer t) {
		this.t = t;
	}

	public Integer[] getS() {
		return S;
	}

	public void setS(Integer[] s) {
		S = s;
	}
	
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		dos.writeInt(t);
		for (int i = 0; i < t; i++) {
			dos.writeInt(S[i]);
		}
		
		dos.close();
		return baos.toByteArray();
	}
	
	public static Policy fromBytes(byte[] content) throws IOException {
		Integer t;
		Integer[] S;
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		
		t = dis.readInt();
		S = new Integer[t];
		for (int i = 0; i < t; i++) {
			S[i] = dis.readInt();
		}
		
		dis.close();
		return new Policy(t, S);
	}
}
