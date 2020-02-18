package cat.urv.ibdt.utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

/*
 * Classe que proporciona utilitats per a manipular polinomis 
 * de forma simbòlica, mitjançant els coeficients.
 */
@SuppressWarnings("rawtypes")
public class Polynomial {

	Element[] coefficients;
	Field z;

	public Polynomial(Element[] coefficients, Field z) {
		this.coefficients = coefficients;
		this.z = z;
	}

	public Element[] getCoefficients() {
		return coefficients;
	}

	public void setCoefficients(Element[] coefficients) {
		this.coefficients = coefficients;
	}

	public Field getZ() {
		return z;
	}

	public void setZ(Field z) {
		this.z = z;
	}

	public Element getCoefficient(int i) {
		return coefficients[i];
	}

	public Element evaluate(Element x) {
		Element evAt = x.getImmutable(); 
		Element p = z.newZeroElement().getImmutable(); 
		for (int i = coefficients.length-1; i >= 0; i--) { 
			p = coefficients[i].add((evAt.mulZn(p))).getImmutable(); 
		} 
		return p;
	}

	public Polynomial times(Polynomial b) {
		Element[] coef_a = this.coefficients;
		Element[] coef_b = b.coefficients;
		Element[] coef_c = new Element[coef_a.length + coef_b.length - 1];
		for (int i = 0; i < coef_c.length; i++) {
			coef_c[i] = z.newZeroElement().getImmutable();
		}

		for (int i = 0; i < coef_a.length; i++)
			for (int j = 0; j < coef_b.length; j++)
				coef_c[i + j] = (coef_c[i + j].add(coef_a[i].mulZn(coef_b[j])))
						.getImmutable();
		return new Polynomial(coef_c, this.z);
	}

}
