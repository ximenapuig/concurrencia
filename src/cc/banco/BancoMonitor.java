package cc.banco;


public class BancoMonitor implements Banco {

  public BancoMonitor() { }

  /**
   * Un cajero pide que se ingrese una determinado valor v a una
   * cuenta c. Si la cuenta no existe, se crea.
   * @param c número de cuenta
   * @param v valor a ingresar
   */
  public void ingresar(String c, int v) { }

  /**
   * Un ordenante pide que se transfiera un determinado valor v desde
   * una cuenta o a otra cuenta d.
   * @param o número de cuenta origen
   * @param d número de cuenta destino
   * @param v valor a transferir
   * @throws IllegalArgumentException si o y d son las mismas cuentas
   *
   */
  public void transferir(String o, String d, int v) { }

  /**
   * Un consultor pide el saldo disponible de una cuenta c.
   * @param c número de la cuenta
   * @return saldo disponible en la cuenta id
   * @throws IllegalArgumentException si la cuenta c no existe
   */
  public int disponible(String c) { return 0; }

  /**
   * Un avisador establece una alerta para la cuenta c. La operación
   * termina cuando el saldo de la cuenta c baja por debajo de m.
   * @param c número de la cuenta
   * @param m saldo mínimo
   * @throws IllegalArgumentException si la cuenta c no existe
   */
  public void alertar(String c, int m) { }
}
