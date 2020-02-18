package cat.urv.ibdt.utils;

import cat.urv.ibdt.model.Policy;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Classe que implementa una sèrie de funcions de suport
 * utilitzades per diverses parts del protocol.
 */
@SuppressWarnings("rawtypes")
public class Utils {

	private Utils() {}

	// M = H(msg|gamma)
	public static Element Hash(String msg, Policy gamma, Field G) {
		String message = msg;
		message += gamma.getT();
		for (Integer i : gamma.getS()) {
			message += i.toString();
		}
                
		byte[] b = message.getBytes();
                return Hash(b, G);
	}

	// Obtain coefficients of Y[X] = mul[id in ids](X-id)*mul[d in D](X-d)
	public static Element[] getY(Integer[] ids, Element[] D, Field Zp, int n, int t) {
		Polynomial Ps = new Polynomial(new Element[] { Zp.newOneElement().getImmutable() }, Zp);
		for (int i = 0; i < t; i++) {
			Polynomial aux = new Polynomial(new Element[] {
					Zp.newElement().set(ids[i]).negate().getImmutable(),
					Zp.newOneElement().getImmutable() }, Zp);
			Ps = Ps.times(aux);
		}

		for (int i = 0; i < (n - t); i++) {
			Polynomial aux = new Polynomial(new Element[] {
					Zp.newElement().set(D[i].negate()).getImmutable(),
					Zp.newOneElement().getImmutable() }, Zp);
			Ps = Ps.times(aux);
		}
		Element[] Y = Ps.getCoefficients();
		return Y;
	}

	// delta^s_id(0)
	public static Element lagrangePolynomial(Element id, Element[] set, Field Zp) {
		Element delta = Zp.newOneElement().getImmutable();
		for (int i = 0; i < set.length; i++) {
			if (!id.isEqual(set[i])) {
				Element aux = ((set[i].getImmutable()).negate()).getImmutable();
				aux = aux.div(id.sub(set[i])).getImmutable();
				delta = delta.mul(aux).getImmutable();
			}
		}
		return delta;
	}

	// (h_0 * prod(hi^yi))
	public static Element precomp1(Element h0, Element[] H, Element[] Y) {
		Element aux = h0.getImmutable();
		for (int i = 0; i < H.length; i++) {
			aux = aux.mul(H[i].powZn(Y[i])).getImmutable();
		}
		return aux;
	}

	// S = S1 U S2
	public static Element[] union(Element[] s1, Element[] s2) {
		Element[] union = new Element[s1.length + s2.length];
                System.arraycopy(s1, 0, union, 0, s1.length);
                System.arraycopy(s2, 0, union, s1.length, s2.length);
		return union;
	}
        
        public static Element Hash(byte[] input, Field F) {
            byte[] h = Hash(input);
            Element r = F.newElement().setFromHash(h, 0, h.length);
            return r;
        }
        
        public static byte[] Hash(byte[] input) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
                return md.digest(input);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        
        public static String ToBase64(Element e) {
            return Base64.encodeBytes(e.toBytes());
        }
        
        public static Element FromBase64(String input, Field F) {
            Element r = null;
            try {
                r = F.newElementFromBytes(Base64.decode(input)).getImmutable();
            } catch (IOException e) {
		Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
            }
            return r;
        }
}