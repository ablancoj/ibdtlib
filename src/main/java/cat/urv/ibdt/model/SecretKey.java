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
 * Aquesta classe encapsula els elements de la clau secreta,
 * definida com a SK_id = ( D_id,1, D_id,2, K_id,1...K_id,N-1 ).
 * Conté les utilitats per a serialitzar, guardar i recuperar la clau. 
 */
@SuppressWarnings("rawtypes")
public class SecretKey {

	private Element D1;
	private Element D2;
	private Element[] K;
	private Element[][] D;

	public SecretKey(Element D1, Element D2, Element[] K, Element[][] D) {
		this.D1 = D1;
		this.D2 = D2;
		this.K = K;
		this.D = D;
	}

	public Element getD1() {
		return D1;
	}

	public Element getD2() {
		return D2;
	}

	public Element[] getK() {
		return K;
	}

	public Element[][] getD() {
		return D;
	}
	
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int length;
		//Save D1
		length = D1.getLengthInBytes();
		dos.writeInt(length); dos.write(D1.toBytes());
		//Save D2
		length = D2.getLengthInBytes();
		dos.writeInt(length); dos.write(D2.toBytes());
		//Save K
		dos.writeInt(K.length);
		for (int i = 0; i < K.length; i++) {
			length = K[i].getLengthInBytes();
			dos.writeInt(length); dos.write(K[i].toBytes());
		}
		//Save D
		dos.writeInt(D.length);
		for (int i = 0; i < D.length; i++) {
			length = D[i][0].getLengthInBytes();
			dos.writeInt(length); dos.write(D[i][0].toBytes());
			length = D[i][1].getLengthInBytes();
			dos.writeInt(length); dos.write(D[i][1].toBytes());
		}
		
		dos.close();
		return baos.toByteArray();
	}
	
	public static SecretKey fromBytes(PublicParameters pms, byte[] content) throws IOException {
		Element D1;
		Element D2;
		Element[] K;
		Element[][] D;
		
		Pairing pairing = pms.getPairing();
		Field G = pairing.getG1();
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		byte[] buffer;
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		D1 = G.newElement();
		D1.setFromBytes(buffer);
		D1 = D1.getImmutable();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		D2 = G.newElement();
		D2.setFromBytes(buffer);
		D2 = D2.getImmutable();
		
		K = new Element[dis.readInt()];
		for (int i = 0; i < K.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			K[i] = G.newElement();
			K[i].setFromBytes(buffer);
			K[i] = K[i].getImmutable();
		}
		
		D = new Element[dis.readInt()][2];
		for (int i = 0; i < D.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			D[i][0] = G.newElement();
			D[i][0].setFromBytes(buffer);
			D[i][0] = D[i][0].getImmutable();
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			D[i][1] = G.newElement();
			D[i][1].setFromBytes(buffer);
			D[i][1] = D[i][1].getImmutable();
		}
		dis.close();
		return new SecretKey(D1, D2, K, D);
	}
}
