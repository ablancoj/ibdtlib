package cat.urv.ibdt.utils;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/* All information between vendor and participant is
 * formatted as JSON objects. Elements are encoded
 * in base64.
 */

public class PairingsManager {

	public static Pairing GeneratePairingParameters(int rBits, int qBits, String path) {
		TypeACurveGenerator parametersGenerator = new TypeACurveGenerator(rBits, qBits);
		PairingParameters pairingParameters = parametersGenerator.generate();
		try (FileWriter fw = new FileWriter(new File(path))) {
			fw.write(pairingParameters.toString());
		} catch (IOException e) {
			System.out.println("File could not be created successfully.");
			return null;
		}
		return PairingFactory.getPairing(pairingParameters);
	}
	
	public static Pairing LoadPairingFromFile(String path) {
		return PairingFactory.getPairing(path);
	}
}