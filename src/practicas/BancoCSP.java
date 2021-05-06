package cc.banco;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.Any2OneChannel;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.ProcessManager;


public class BancoCSP implements Banco, CSProcess {

  public BancoCSP() {

	  new ProcessManager(this).start();
  }

  /**
   * Un cajero pide que se ingrese una determinado valor v a una
   * cuenta c. Si la cuenta no existe, se crea.
   * @param c número de cuenta
   * @param v valor a ingresar
   */
  public void ingresar(String c, int v) {
  }

  /**
   * Un ordenante pide que se transfiera un determinado valor v desde
   * una cuenta o a otra cuenta d.
   * @param o número de cuenta origen
   * @param d número de cuenta destino
   * @param v valor a transferir
   * @throws IllegalArgumentException si o y d son las mismas cuentas
   *
   */
  public void transferir(String o, String d, int v) {
  }

  /**
   * Un consultor pide el saldo disponible de una cuenta c.
   * @param c número de la cuenta
   * @return saldo disponible en la cuenta id
   * @throws IllegalArgumentException si la cuenta c no existe
   */
  public int disponible(String c) {
    return 0;
  }

  /**
   * Un avisador establece una alerta para la cuenta c. La operación
   * termina cuando el saldo de la cuenta c baja por debajo de m.
   * @param c número de la cuenta
   * @param m saldo mínimo
   * @throws IllegalArgumentException si la cuenta c no existe
   */
  public void alertar(String c, int m) {
  }

  /**
   * Codigo para el servidor.
   */
  public void run() {
  }
}

