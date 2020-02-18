package cat.urv.ibdt.model;

import cat.urv.ibdt.model.Policy;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SignPreComp {

	private ElementPowPreProcessing gPow;
	private Element[] coefY;
	private Element Dprima;
	private ElementPowPreProcessing ph;
	private Policy gamma;
	
	public SignPreComp(ElementPowPreProcessing gPow, Element[] coefY,
			Element dprima, ElementPowPreProcessing ph, Policy gamma) {
		super();
		this.gPow = gPow;
		this.coefY = coefY;
		Dprima = dprima;
		this.ph = ph;
		this.gamma = gamma;
	}
	
	public ElementPowPreProcessing getgPow() {
		return gPow;
	}

	public void setgPow(ElementPowPreProcessing gPow) {
		this.gPow = gPow;
	}

	public Element[] getCoefY() {
		return coefY;
	}

	public void setCoefY(Element[] coefY) {
		this.coefY = coefY;
	}

	public Element getDprima() {
		return Dprima;
	}

	public void setDprima(Element dprima) {
		Dprima = dprima;
	}

	public ElementPowPreProcessing getPh() {
		return ph;
	}

	public void setPh(ElementPowPreProcessing ph) {
		this.ph = ph;
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
		//Save gPow
		length = gPow.toBytes().length;
		dos.writeInt(length); dos.write(gPow.toBytes());
		//Save coefY
		dos.writeInt(coefY.length);
		for (int i = 0; i < coefY.length; i++) {
			length = coefY[i].getLengthInBytes();
			dos.writeInt(length); dos.write(coefY[i].toBytes());
		}
		//Save Dprima
		length = Dprima.getLengthInBytes();
		dos.writeInt(length); dos.write(Dprima.toBytes());
		//Save ph
		length = ph.toBytes().length;
		dos.writeInt(length); dos.write(ph.toBytes());
		//Save gamma
		length = gamma.toBytes().length;
		dos.writeInt(length); dos.write(gamma.toBytes());
		
		dos.close();
		return baos.toByteArray();
	}
	
	@SuppressWarnings("rawtypes")
	public static SignPreComp fromBytes(PublicParameters pms, byte[] content) throws IOException {
		ElementPowPreProcessing gPow;
		Element[] coefY;
		Element Dprima;
		ElementPowPreProcessing ph;
		Policy gamma;
		
		Pairing pairing = pms.getPairing();
		Field G = pairing.getG1();
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		byte[] buffer;
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		gPow = G.pow(buffer);
		
		coefY = new Element[dis.readInt()];
		for (int i = 0; i < coefY.length; i++) {
			buffer = new byte[dis.readInt()];
			dis.read(buffer);
			coefY[i] = G.newElement();
			coefY[i].setFromBytes(buffer);
			coefY[i] = coefY[i].getImmutable();
		}
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		Dprima = G.newElement();
		Dprima.setFromBytes(buffer);
		Dprima = Dprima.getImmutable();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		ph = G.pow(buffer);
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		gamma = Policy.fromBytes(buffer);
		
		return new SignPreComp(gPow, coefY, Dprima, ph, gamma);
	}
	
}
