package cat.urv.ibdt.model;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CombPreComp {
	
	private Element preSigma1;
	private Element preSigma4;
	private Element[] preSet;
	private Element[] preLagPolyIds;
	private Policy gamma;
	
	public CombPreComp(Element preSigma1, Element preSigma4, Element[] preSet,
			Element[] preLagPolyIds, Policy gamma) {
		super();
		this.preSigma1 = preSigma1;
		this.preSigma4 = preSigma4;
		this.preSet = preSet;
		this.preLagPolyIds = preLagPolyIds;
		this.gamma = gamma;
	}

	public Element getPreSigma1() {
		return preSigma1;
	}

	public void setPreSigma1(Element preSigma1) {
		this.preSigma1 = preSigma1;
	}

	public Element getPreSigma4() {
		return preSigma4;
	}

	public void setPreSigma4(Element preSigma4) {
		this.preSigma4 = preSigma4;
	}

	public Element[] getPreSet() {
		return preSet;
	}

	public void setPreSet(Element[] preSet) {
		this.preSet = preSet;
	}

	public Element[] getPreLagPolyIds() {
		return preLagPolyIds;
	}

	public void setPreLagPolyIds(Element[] preLagPolyIds) {
		this.preLagPolyIds = preLagPolyIds;
	}

	public Policy getGamma() {
		return gamma;
	}

	public void setGamma(Policy gamma) {
		this.gamma = gamma;
	}
	
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int length;
		//Save preSigma1
		length = preSigma1.getLengthInBytes();
		dos.writeInt(length); dos.write(preSigma1.toBytes());
		//Save preSigma4
		length = preSigma4.getLengthInBytes();
		dos.writeInt(length); dos.write(preSigma4.toBytes());
		//Save preSet
		dos.writeInt(preSet.length);
		for (int i = 0; i < preSet.length; i++) {
			length = preSet[i].getLengthInBytes();
			dos.writeInt(length); dos.write(preSet[i].toBytes());
		}
		//Save preLagPolyIds
		dos.writeInt(preLagPolyIds.length);
		for (int i = 0; i < preLagPolyIds.length; i++) {
			length = preLagPolyIds[i].getLengthInBytes();
			dos.writeInt(length); dos.write(preLagPolyIds[i].toBytes());
		}
		//Save gamma
		length = gamma.toBytes().length;
		dos.writeInt(length); dos.write(gamma.toBytes());
		
		dos.close();
		return baos.toByteArray();
	}
	
	@SuppressWarnings("rawtypes")
	public static CombPreComp fromBytes(PublicParameters pms, byte[] content) throws IOException {
		Element preSigma1;
		Element preSigma4;
		Element[] preSet;
		Element[] preLagPolyIds;
		Policy gamma;
		
		Pairing pairing = pms.getPairing();
		Field G = pairing.getG1();
		Field Z = pairing.getZr();
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		byte[] buffer;
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		preSigma1 = G.newElement();
		preSigma1.setFromBytes(buffer);
		preSigma1 = preSigma1.getImmutable();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		preSigma4 = G.newElement();
		preSigma4.setFromBytes(buffer);
		preSigma4 = preSigma4.getImmutable();
		
		preSet = new Element[dis.readInt()];
		for (int i = 0; i < preSet.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			preSet[i] = Z.newElement();
			preSet[i].setFromBytes(buffer);
			preSet[i] = preSet[i].getImmutable();
		}
		
		preLagPolyIds = new Element[dis.readInt()];
		for (int i = 0; i < preLagPolyIds.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			preLagPolyIds[i] = Z.newElement();
			preLagPolyIds[i].setFromBytes(buffer);
			preLagPolyIds[i] = preLagPolyIds[i].getImmutable();
		}
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		gamma = Policy.fromBytes(buffer);
		
		dis.close();
		return new CombPreComp(preSigma1, preSigma4, preSet, preLagPolyIds, gamma);
	}
	
	
}
