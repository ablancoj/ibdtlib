package cat.urv.ibdt.model;

import cat.urv.ibdt.utils.PairingsManager;
import cat.urv.ibdt.utils.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/*
 * Aquesta classe encapsula els elements que seran els paràmetres públics,
 * definits com a PMS = ( Zp, n, lambda, G, Gt, g, H ).
 * L'anell Zp, els grups G i Gt i la funció de hash H es poden extreure
 * dels paràmetres de la corba elíptica, per tant, els paràmetres públics
 * queden reduits a PMS = ( parametres de la corba, g, n ).
 * Conté les utilitats per a serialitzar, guardar i recuperar els paràmetres. 
 */
@SuppressWarnings("rawtypes")
public class PublicParameters {
    
    public static final String KEY_PARAMETERS = "parameters";
    public static final String KEY_GENERATOR = "g";
    public static final String KEY_MAX_USERS = "n";

    private int n;
    private Element g;
    private String curveParametersFile;
    private Pairing pairing;
	
    public PublicParameters(String cpf, Element g, int n) {
        this.curveParametersFile = cpf;
        this.pairing = PairingsManager.LoadPairingFromFile(cpf);
        this.g = g;
        this.n = n;
    }

    public int getN() {
        return n;
    }

    public Element getGenerator() {
        return g;
    }

    public String getCurveParameters() {
        return curveParametersFile;
    }

    public Pairing getPairing() {
        return pairing;
    }
    
    public String toJSON() {
        JSONObject json = new JSONObject();
        json.put(KEY_PARAMETERS, this.curveParametersFile);
        json.put(KEY_GENERATOR, Utils.ToBase64(g));
        json.put(KEY_MAX_USERS, n);
        return json.toString();
    }
    
    public static PublicParameters FromJSON(String input) {
        JSONObject json = (JSONObject) JSONValue.parse(input);
        
        String cpf = (String) json.get(KEY_PARAMETERS);
        String sG = (String) json.get(KEY_GENERATOR);
        String sN = (String) json.get(KEY_MAX_USERS);
        
        Pairing p = PairingsManager.LoadPairingFromFile(cpf);
        Element g = Utils.FromBase64(sG, p.getG1());
        int n = Integer.parseInt(sN);
        
        return new PublicParameters(cpf, g, n);
    }
	
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//CurveParameters -> byte[]
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		curveParameters.writeExternal(oos);
		oos.close();
		byte[] curveParametersByteArray = baos.toByteArray();
		baos.close();
		
		baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int length;
		//Save curve parameters
		length = curveParametersByteArray.length;
		dos.writeInt(length); dos.write(curveParametersByteArray);
		//Save generator
		length = g.getLengthInBytes();
		dos.writeInt(length); dos.write(g.toBytes());
		//Save n
		dos.writeInt(n);
		dos.close();
		return baos.toByteArray();
	}
	
	public static PublicParameters fromBytes(byte[] content) throws Exception {
		DefaultCurveParameters curveParameters;
		Element g;
		int n;
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		byte[] buffer;
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
		curveParameters = new DefaultCurveParameters();
		curveParameters.readExternal(ois);
		ois.close();
		
		Pairing pairing = PairingFactory.getPairing(curveParameters);
		Field G = pairing.getG1();
		
		buffer = new byte[dis.readInt()];
		dis.read(buffer);
		g = G.newElement();
		g.setFromBytes(buffer);
		g = g.getImmutable();
		
		n = dis.readInt();
		
		return new PublicParameters(curveParameters, g, n);
	}
}
