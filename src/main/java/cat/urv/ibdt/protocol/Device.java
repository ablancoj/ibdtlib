package cat.urv.ibdt.protocol;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.IOException;

import cat.urv.ibdt.model.CombPreComp;
import cat.urv.ibdt.model.MasterPublicKey;
import cat.urv.ibdt.model.PartialSignature;
import cat.urv.ibdt.model.Policy;
import cat.urv.ibdt.model.PublicParameters;
import cat.urv.ibdt.model.SecretKey;
import cat.urv.ibdt.model.SignPreComp;
import cat.urv.ibdt.model.Signature;
import utils.Utils;

public class Device {

	
	private PublicParameters pms;
	private MasterPublicKey mpk;
	private int n;
	private int N;

	private int id;
	private SecretKey sk;

	/*
	 * IBDT.Sign
	 * IBDT.Comb
	 */
	public Device(int id, SecretKey sk, PublicParameters pms,
			MasterPublicKey mpk) {
		this.id = id;
		this.sk = sk;
		this.pms = pms;
		this.mpk = mpk;
		this.n = pms.getN();
		this.N = n + 1;
	}

	@SuppressWarnings("rawtypes")
	public PartialSignature Sign(String msg, Policy gamma) throws IOException {

		Pairing pairing = pms.getPairing();
		Field Zp = pairing.getZr();
		Field G = pairing.getG1();

		Element M = Utils.Hash(msg, gamma, G);
		Element[] Y = Utils.getY(gamma.getS(), mpk.getD(), Zp, pms.getN(),
				gamma.getT());
		// Calculate D'
		Element Dprima = G.newElement().set(sk.getD1()).getImmutable();
		for (int i = 0; i < (N - 1); i++) {
			Dprima = Dprima.mul(sk.getK()[i].powZn(Y[i + 1])).getImmutable();
		}
		Element z = Zp.newRandomElement().getImmutable();
		Element w = Zp.newRandomElement().getImmutable();

		// sigma1 = D1 * (h0*mul(hi^yi))^w_id * M^z_id
		Element sigma1 = G.newElement().set(Dprima).getImmutable();
		// Element sigma1 = sk.getD1().getImmutable();
		sigma1 = sigma1
				.mul(Utils.precomp1(mpk.getH0(), mpk.getH(), Y).powZn(w))
				.getImmutable();
		sigma1 = sigma1.mul(M.powZn(z)).getImmutable();

		Element sigma2 = (pms.getGenerator().powZn(w)).mul(sk.getD2())
				.getImmutable();
		Element sigma3 = (pms.getGenerator().powZn(z)).getImmutable();

		PartialSignature sigma = new PartialSignature(sigma1, sigma2, sigma3);
		return sigma;
	}

	public SignPreComp SignPreprocessing(Policy gamma) {
		ElementPowPreProcessing gPow = pms.getGenerator().pow();
		Pairing p = pms.getPairing();
		//calc Y
		Element[] coefY = Utils.getY(gamma.getS(), mpk.getD(), p.getZr(), pms.getN(), gamma.getT());
		//calc D'1
		Element Dprima = p.getG1().newElement().set(sk.getD1()).getImmutable();
		for (int i = 0; i < (N - 1); i++) {
			Dprima = Dprima.mul(sk.getK()[i].powZn(coefY[i + 1])).getImmutable();
		}
		//calc ph
		ElementPowPreProcessing ph = Utils.precomp1(mpk.getH0(), mpk.getH(), coefY).pow();
		
		return new SignPreComp(gPow, coefY, Dprima, ph, gamma);
	}

	@SuppressWarnings("rawtypes")
	public PartialSignature FastSign(String msg, SignPreComp preComp) {
		
		Pairing pairing = pms.getPairing();
		Field Zp = pairing.getZr();
		Field G = pairing.getG1();
		Element M = Utils.Hash(msg, preComp.getGamma(), G);
		
		Element z = Zp.newRandomElement().getImmutable();
		Element w = Zp.newRandomElement().getImmutable();

		// sigma1 = D1 * (h0*mul(hi^yi))^w_id * M^z_id
		Element sigma1 = G.newElement().set(preComp.getDprima()).getImmutable();
		// Element sigma1 = sk.getD1().getImmutable();
		sigma1 = sigma1.mul(preComp.getPh().powZn(w)).getImmutable();
		sigma1 = sigma1.mul(M.powZn(z)).getImmutable();

		Element sigma2 = (preComp.getgPow().powZn(w)).mul(sk.getD2())
				.getImmutable();
		Element sigma3 = (preComp.getgPow().powZn(z)).getImmutable();

		PartialSignature sigma = new PartialSignature(sigma1, sigma2, sigma3);
		return sigma;
	}

	@SuppressWarnings("rawtypes")
	public Signature Comb(PartialSignature[] partialSignatures, Policy gamma)
			throws IOException {
		Pairing p = pms.getPairing();

		Field G = p.getG1();
		Field Zp = p.getZr();

		// Set S
		Integer[] ids = gamma.getS();
		Element[] s1 = new Element[ids.length];
		for (int i = 0; i < ids.length; i++) {
			s1[i] = Zp.newElement().set(ids[i]).getImmutable();
		}

		// Set D_n-s
		Element[] s2 = new Element[pms.getN() - gamma.getT()];
		for (int i = 0; i < s2.length; i++) {
			s2[i] = mpk.getD()[i].getImmutable();
		}

		// S U D_n-s
		Element[] set = Utils.union(s1, s2);

		Element sigma1 = G.newOneElement().getImmutable();
		Element sigma2 = G.newOneElement().getImmutable();
		Element sigma3 = G.newOneElement().getImmutable();
		Element sigma4 = G.newOneElement().getImmutable();

		for (int i = 0; i < gamma.getT(); i++) {
			Element id = Zp.newElement().set(ids[i]).getImmutable();
			Element partialSigma1 = partialSignatures[i].getSigma1();
			Element partialSigma2 = partialSignatures[i].getSigma2();
			Element partialSigma3 = partialSignatures[i].getSigma3();
			Element lagrangePoly = Utils.lagrangePolynomial(id, set, Zp);

			sigma1 = sigma1.mul(partialSigma1.powZn(lagrangePoly))
					.getImmutable();
			sigma2 = sigma2.mul(partialSigma2.powZn(lagrangePoly))
					.getImmutable();
			sigma3 = sigma3.mul(partialSigma3.powZn(lagrangePoly))
					.getImmutable();
		}

		for (int i = 0; i < pms.getN() - gamma.getT(); i++) {
			Element d1 = sk.getD()[i][0].getImmutable();
			Element d2 = sk.getD()[i][1].getImmutable();
			Element lagrangePoly = Utils.lagrangePolynomial(mpk.getD()[i], set,
					Zp);
			sigma1 = sigma1.mul(d1.powZn(lagrangePoly)).getImmutable();
			sigma4 = sigma4.mul(d2.powZn(lagrangePoly)).getImmutable();
		}

		Signature sigma = new Signature(sigma1, sigma2, sigma3, sigma4);
		return sigma;
	}
	
	@SuppressWarnings("rawtypes")
	public CombPreComp CombPreprocessing(Policy gamma) {
		Pairing p = pms.getPairing();
		//Prepare comb
		// Set S
		Field G = p.getG1();
		Field Zp = p.getZr();
		Integer[] ids = gamma.getS();
		Element[] s1 = new Element[ids.length];
		for (int i = 0; i < ids.length; i++) {
			s1[i] = p.getZr().newElement().set(ids[i]).getImmutable();
		}

		// Set D_n-s
		Element[] s2 = new Element[pms.getN() - gamma.getT()];
		for (int i = 0; i < s2.length; i++) {
			s2[i] = mpk.getD()[i].getImmutable();
		}
		
		// S U D_n-s
		Element[] preSet = Utils.union(s1, s2);
		Element[] preLagPolyIds = new Element[gamma.getT()];
		Element preSigma1 = G.newOneElement().getImmutable();
		Element preSigma4 = G.newOneElement().getImmutable();
		
		for (int i = 0; i < gamma.getT(); i++) {
			Element id = Zp.newElement().set(ids[i]).getImmutable();
			preLagPolyIds[i] = Utils.lagrangePolynomial(id, preSet, Zp);
		}
		for (int i = 0; i < pms.getN() - gamma.getT(); i++) {
			Element d1 = sk.getD()[i][0].getImmutable();
			Element d2 = sk.getD()[i][1].getImmutable();
			Element lagrangePoly = Utils.lagrangePolynomial(mpk.getD()[i], preSet, Zp);
			preSigma1 = preSigma1.mul(d1.powZn(lagrangePoly)).getImmutable();
			preSigma4 = preSigma4.mul(d2.powZn(lagrangePoly)).getImmutable();
		}
		
		return new CombPreComp(preSigma1, preSigma4, preSet, preLagPolyIds, gamma);
	}
	
	@SuppressWarnings("rawtypes")
	public Signature FastComb(PartialSignature[] partialSignatures, CombPreComp preComp) {
		Pairing p = pms.getPairing();

		Field G = p.getG1();

		Element sigma1 = preComp.getPreSigma1().getImmutable();
		Element sigma2 = G.newOneElement().getImmutable();
		Element sigma3 = G.newOneElement().getImmutable();

		for (int i = 0; i < preComp.getGamma().getT(); i++) {
			Element partialSigma1 = partialSignatures[i].getSigma1();
			Element partialSigma2 = partialSignatures[i].getSigma2();
			Element partialSigma3 = partialSignatures[i].getSigma3();

			sigma1 = sigma1.mul(partialSigma1.powZn(preComp.getPreLagPolyIds()[i]))
					.getImmutable();
			sigma2 = sigma2.mul(partialSigma2.powZn(preComp.getPreLagPolyIds()[i]))
					.getImmutable();
			sigma3 = sigma3.mul(partialSigma3.powZn(preComp.getPreLagPolyIds()[i]))
					.getImmutable();
		}

		Signature sigma = new Signature(sigma1, sigma2, sigma3, preComp.getPreSigma4());
		return sigma;
	}

	public int getId() {
		return id;
	}

	public SecretKey getSk() {
		return sk;
	}

	public PublicParameters getPms() {
		return pms;
	}

	public MasterPublicKey getMpk() {
		return mpk;
	}
}
