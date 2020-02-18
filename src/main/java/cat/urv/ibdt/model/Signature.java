package cat.urv.ibdt.model;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Signature {

	private Element sigma1;
	private Element sigma2;
	private Element sigma3;
	private Element sigma4;

	public Signature(Element s1, Element s2, Element s3, Element s4) {
		this.sigma1 = s1;
		this.sigma2 = s2;
		this.sigma3 = s3;
		this.sigma4 = s4;
	}

	public Element getSigma1() {
		return sigma1;
	}

	public Element getSigma2() {
		return sigma2;
	}

	public Element getSigma3() {
		return sigma3;
	}

	public Element getSigma4() {
		return sigma4;
	}
	
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int length;
		//Save sigma1
		length = sigma1.getLengthInBytes();
		dos.writeInt(length); dos.write(sigma1.toBytes());
		//Save sigma2
		length = sigma2.getLengthInBytes();
		dos.writeInt(length); dos.write(sigma2.toBytes());
		//Save sigma3
		length = sigma3.getLengthInBytes();
		dos.writeInt(length); dos.write(sigma3.toBytes());
		//Save sigma4
		length = sigma4.getLengthInBytes();
		dos.writeInt(length); dos.write(sigma4.toBytes());
		
		dos.close();
		return baos.toByteArray();
	}
	
	@SuppressWarnings("rawtypes")
	public static Signature fromBytes(PublicParameters pms, byte[] content) throws IOException {
		Element sigma1;
		Element sigma2;
		Element sigma3;
		Element sigma4;
		
		Pairing pairing = pms.getPairing();
		Field G = pairing.getG1();
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		byte[] buffer;
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		sigma1 = G.newElement();
		sigma1.setFromBytes(buffer);
		sigma1 = sigma1.getImmutable();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		sigma2 = G.newElement();
		sigma2.setFromBytes(buffer);
		sigma2 = sigma2.getImmutable();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		sigma3 = G.newElement();
		sigma3.setFromBytes(buffer);
		sigma3 = sigma3.getImmutable();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		sigma4 = G.newElement();
		sigma4.setFromBytes(buffer);
		sigma4 = sigma4.getImmutable();
		
		dis.close();
		
		return new Signature(sigma1, sigma2, sigma3, sigma4);
	}
}
