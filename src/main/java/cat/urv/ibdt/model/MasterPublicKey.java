package cat.urv.ibdt.model;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*
 * Aquesta classe encapsula els elements de la clau mestra pública,
 * definida com a MPK = ( e(g,g)^a, h_0, H ).
 * Conté les utilitats per a serialitzar, guardar i recuperar la clau. 
 */
@SuppressWarnings("rawtypes")
public class MasterPublicKey {

	private Element egga;
	private Element h0;
	private Element[] H;
	private Element[] D;

	public MasterPublicKey(Element egga, Element h0, Element[] H, Element[] D) {
		this.egga = egga;
		this.h0 = h0;
		this.H = H;
		this.D = D;
	}

	public Element getEgga() {
		return egga;
	}

	public Element getH0() {
		return h0;
	}

	public Element[] getH() {
		return H;
	}

	public Element[] getD() {
		return D;
	}
	
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int length;
		//Save e(g,g)^a
		length = egga.getLengthInBytes();
		dos.writeInt(length); dos.write(egga.toBytes());
		//save h0
		length = h0.getLengthInBytes();
		dos.writeInt(length); dos.write(h0.toBytes());
		//save H[]
		dos.writeInt(H.length);
		for (int i = 0; i < H.length; i++) {
			length = H[i].getLengthInBytes();
			dos.writeInt(length); dos.write(H[i].toBytes());
		}
		//save D[]
		dos.writeInt(D.length);
		for (int i = 0; i < D.length; i++) {
			length = D[i].getLengthInBytes();
			dos.writeInt(length); dos.write(D[i].toBytes());
		}
		dos.close();
		return baos.toByteArray();
	}
	
	public static MasterPublicKey fromBytes(PublicParameters pms, byte[] content) throws IOException {
		Element egga;
		Element h0;
		Element[] H;
		Element[] D;

		Pairing pairing = pms.getPairing();
		Field G = pairing.getG1();
		Field GT = pairing.getGT();
		Field Z = pairing.getZr();
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		byte[] buffer;

		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		egga = GT.newElement();
		egga.setFromBytes(buffer);
		egga = egga.getImmutable();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		h0 = G.newElement();
		h0.setFromBytes(buffer);
		h0 = h0.getImmutable();
		
		H = new Element[dis.readInt()];
		for (int i = 0; i < H.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			H[i] = G.newElement();
			H[i].setFromBytes(buffer);
			H[i] = H[i].getImmutable();
		}
		
		D = new Element[dis.readInt()];
		for (int i = 0; i < D.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			D[i] = Z.newElement();
			D[i].setFromBytes(buffer);
			D[i] = D[i].getImmutable();
		}
		
		dis.close();
		
		return new MasterPublicKey(egga, h0, H, D);
	}
}
