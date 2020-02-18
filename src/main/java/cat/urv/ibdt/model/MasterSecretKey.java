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
 * Aquesta classe encapsula els elements de la clau mestra secreta,
 * definida com a MSK = ( g^a, Q[X] ).
 * Conté les utilitats per a serialitzar, guardar i recuperar la clau. 
 */
@SuppressWarnings("rawtypes")
public class MasterSecretKey {

	private Element gAlpha;
	private Polynomial Q;

	public MasterSecretKey(Element gAlpha, Polynomial Q) {
		this.gAlpha = gAlpha;
		this.Q = Q;
	}

	public Element getGAlpha() {
		return gAlpha;
	}

	public Polynomial getQ() {
		return Q;
	}

	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int length;
		//Save gAlpha
		length = gAlpha.getLengthInBytes();
		dos.writeInt(length); dos.write(gAlpha.toBytes());
		//Save Q
		Element[] coef = Q.getCoefficients();
		dos.writeInt(coef.length);
		for (int i = 0; i < coef.length; i++) {
			length = coef[i].getLengthInBytes();
			dos.writeInt(length); dos.write(coef[i].toBytes());
		}
		dos.close();
		return baos.toByteArray();
	}
	
	public static MasterSecretKey fromBytes(PublicParameters pms, byte[] content) throws IOException {
		Element gAlpha;
		Element[] Q;
		
		Pairing pairing = pms.getPairing();
		Field G = pairing.getG1();
		Field Z = pairing.getZr();
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		byte[] buffer;
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		gAlpha = G.newElement();
		gAlpha.setFromBytes(buffer);
		gAlpha = gAlpha.getImmutable();
		
		Q = new Element[dis.readInt()];
		for (int i = 0; i < Q.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			Q[i] = Z.newElement();
			Q[i].setFromBytes(buffer);
			Q[i] = Q[i].getImmutable();
		}
		dis.close();
		return new MasterSecretKey(gAlpha, new Polynomial(Q, Z));
	}
}
