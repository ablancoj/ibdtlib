package cat.urv.ibdt.protocol;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.FileNotFoundException;

import cat.urv.ibdt.model.MasterPublicKey;
import cat.urv.ibdt.model.Policy;
import cat.urv.ibdt.model.PublicParameters;
import cat.urv.ibdt.model.Signature;
import utils.Utils;

public class Station {

	private PublicParameters pms;
	private MasterPublicKey mpk;

	/*
	 * IBDT.Verify
	 */
	public Station(PublicParameters pms, MasterPublicKey mpk) {
		this.pms = pms;
		this.mpk = mpk;
	}

	@SuppressWarnings("rawtypes")
	public boolean Verify(String Msg, Signature signature, Policy gamma)
			throws FileNotFoundException {
		Pairing p = pms.getPairing();
		Field G = p.getG1();
		Field Zp = p.getZr();

		Element M = Utils.Hash(Msg, gamma, G);
		Element[] Y = Utils.getY(gamma.getS(), mpk.getD(), Zp, pms.getN(),
				gamma.getT());

		Element factor1 = p.pairing(signature.getSigma1(), pms.getGenerator())
				.getImmutable();

		Element aux = Utils.precomp1(mpk.getH0(), mpk.getH(), Y);

		Element factor2 = p.pairing(signature.getSigma2(), aux).invert()
				.getImmutable();
		Element factor3 = p.pairing(signature.getSigma3(), M).invert()
				.getImmutable();
		Element factor4 = p.pairing(signature.getSigma4(), mpk.getH0())
				.invert().getImmutable();
		
		Element factors = factor1.mul(factor2).mul(factor3).mul(factor4).getImmutable();
		return mpk.getEgga().isEqual(factors);
	}

}
