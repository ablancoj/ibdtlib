package cat.urv.ibdt.protocol;

import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import cat.urv.ibdt.model.MasterPublicKey;
import cat.urv.ibdt.model.MasterSecretKey;
import utils.Polynomial;
import cat.urv.ibdt.model.PublicParameters;
import cat.urv.ibdt.model.SecretKey;

public class CertAg {

	private String curveParametersFile;
	private int n;
	private int N;
	private PublicParameters pms;
	private MasterSecretKey msk;
	private MasterPublicKey mpk;
	private ElementPowPreProcessing gPow;

	/*
	 * IBDT.Setup
	 * IBDT.KeyGen
	 */
	public CertAg(String curveParametersFile, int n) {
		this.curveParametersFile = curveParametersFile;
		this.n = n;
		this.N = n + 1;
	}

	@SuppressWarnings("rawtypes")
	public void Setup() throws FileNotFoundException {
		
		CurveParameters cp = PairingFactory.getInstance().loadCurveParameters(curveParametersFile);
		Pairing p = PairingFactory.getPairing(cp);
		
		Field G = p.getG1();
		Field Z = p.getZr();

		Element g = G.newRandomElement().getImmutable();
		gPow = g.pow();

		Element alpha = Z.newRandomElement().getImmutable();
		Element alphaZero = Z.newRandomElement().getImmutable();
		Element[] alphaVector = new Element[N];
		for (int i = 0; i < N; i++) {
			alphaVector[i] = Z.newRandomElement().getImmutable();
		}
		Element mainTest = p.pairing(g, g).powZn(alpha).getImmutable();
		Element hZero = gPow.powZn(alphaZero).getImmutable();
		Element[] hVector = new Element[N];
		for (int i = 0; i < N; i++) {
			hVector[i] = gPow.powZn(alphaVector[i]).getImmutable();
		}
		Element[] qCoefficients = new Element[n - 1];
		qCoefficients[0] = Z.newElement().set(alpha).getImmutable();
		for (int i = 1; i < n - 1; i++) {
			qCoefficients[i] = Z.newRandomElement().getImmutable();
		}
		Polynomial q = new Polynomial(qCoefficients, Z);

		Element[] D = new Element[n - 1];
		BigInteger pHalves = Z.getOrder().divide(BigInteger.valueOf(2));
		BigInteger pMinusOne = Z.getOrder().subtract(BigInteger.ONE);
		for (int i = 0; i < D.length; i++) {
			Element tmp;
			BigInteger iTmp;
			do {
				tmp = Z.newRandomElement().getImmutable();
				iTmp = tmp.toBigInteger();
			} while (iTmp.compareTo(pHalves) < 0
					|| iTmp.compareTo(pMinusOne) > 0);
			D[i] = tmp.getImmutable();
		}

		pms = new PublicParameters(cp, g, n);
		msk = new MasterSecretKey(G.newElement().set(gPow.powZn(alpha))
				.getImmutable(), q);
		mpk = new MasterPublicKey(mainTest, hZero, hVector, D);

	}

	@SuppressWarnings("rawtypes")
	public SecretKey Keygen(int id) throws IOException {
		
		if (pms == null) {
			return null;
		}
		
		Pairing p = pms.getPairing();
		
		Field Z = p.getZr();
		Field G = p.getG1();
		if (gPow == null) {
			Element g = G.newElement().set(pms.getGenerator()).getImmutable();
			gPow = g.pow();
		}
		Element g = G.newElement().set(pms.getGenerator()).getImmutable();
		Element hZero = G.newElement().set(mpk.getH0()).getImmutable();
		Polynomial q = msk.getQ();
		Element[] hVector = mpk.getH();
		Element[] dVector = mpk.getD();

		Element r = Z.newRandomElement().getImmutable();
		Element eUserId = Z.newElement().set(id).getImmutable();
		Element D1 = g.powZn(q.evaluate(eUserId)).getImmutable();
		D1 = D1.mul(hZero.powZn(r)).getImmutable();
		Element D2 = g.powZn(r).getImmutable();
		Element[] K = new Element[N - 1];
		for (int j = 0; j < (N - 1); j++) {
			Element eJ = Z.newElement().set(j + 1).getImmutable();
			K[j] = (hVector[0].invert().powZn(eUserId.powZn(eJ))
					.mul(hVector[j + 1])).powZn(r).getImmutable();
		}

		Element[][] D = new Element[n - 1][2];
		for (int j = 0; j < D.length; j++) {
			Element rj = Z.newRandomElement().getImmutable();
			D[j][0] = g.powZn(q.evaluate(dVector[j])).mul(hZero.powZn(rj))
					.getImmutable();
			D[j][1] = g.powZn(rj).getImmutable();
		}
		return new SecretKey(D1, D2, K, D);
	}

	public PublicParameters getPublicParameters() {
		return pms;
	}

	public MasterPublicKey getMasterPublicKey() {
		return mpk;
	}

	public MasterSecretKey getMasterSecretKey() {
		return msk;
	}

	public void setPms(PublicParameters pms) {
		this.pms = pms;
	}

	public void setMsk(MasterSecretKey msk) {
		this.msk = msk;
	}

	public void setMpk(MasterPublicKey mpk) {
		this.mpk = mpk;
	}
}
