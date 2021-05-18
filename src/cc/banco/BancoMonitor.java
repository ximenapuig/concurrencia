package cc.banco;
import es.upm.babel.cclib.Monitor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class BancoMonitor implements Banco {
	private HashMap<String, Cuenta> bc;
	private Monitor mutex;

	public BancoMonitor() { 
		this.bc= new HashMap<String, Cuenta>();
		this.mutex = new Monitor();
	}

	private Queue<AlertaSaldo> colaAlertas = new LinkedList<AlertaSaldo>();

	private class AlertaSaldo {
		public String cuenta;
		public Integer minimo; 
		public Monitor.Cond condAlerta;

		public AlertaSaldo(String acc, int min) {
			minimo = min;
			cuenta = acc;
			condAlerta = mutex.newCond();
		}
	}

	private class Cuenta {
		Integer saldo;
		Monitor.Cond c;
		//	Monitor thisMon;tor;
		public Cuenta(int v) {
			this.saldo = v;
			this.c = mutex.newCond();
		}

	}	




	/**
	 * Un cajero pide que se ingrese una determinado valor v a una
	 * cuenta c. Si la cuenta no existe, se crea.
	 * @param c número de cuenta
	 * @param v valor a ingresar
	 */
	public void ingresar(String c, int v) { 
		if (!bc.containsKey(c)) {
			Cuenta newAcc = new Cuenta(v);
			bc.put(c, newAcc);
		}
		else {
			Cuenta acc = bc.get(c);
			acc.saldo += v;
			//bc.replace(c,acc);
		}
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
		if(o.equals(d)) {
			throw new IllegalArgumentException("Cannot transfer funds to same account");
		}
		mutex.enter();
		Cuenta accD=bc.get(d);
		Cuenta accO=bc.get(o);
		if(accD == null || accO = null) {
			//bloqueo
		}
		if(accO.saldo>=v) {
			accD.saldo-=v;
			accO.saldo+=v;

			
		}
		desbloqAlerta();
		mutex.leave();
	}
		/**
		 * Un consultor pide el saldo disponible de una cuenta c.
		 * @param c número de la cuenta
		 * @return saldo disponible en la cuenta id
		 * @throws IllegalArgumentException si la cuenta c no existe
		 */
		public int disponible(String c) { 
			mutex.enter();
			Integer saldo = bc.get(c).saldo;
			if(saldo == null) {
				throw new IllegalArgumentException("Cuenta " + c + "no existe");
			}
			mutex.leave();
			return saldo;
		}

		/**
		 * Un avisador establece una alerta para la cuenta c. La operación
		 * termina cuando el saldo de la cuenta c baja por debajo de m.
		 * @param c número de la cuenta
		 * @param m saldo mínimo
		 * @throws IllegalArgumentException si la cuenta c no existe
		 */
		public void alertar(String c, int m) { 
			Integer saldo = bc.get(c).saldo;
			if(saldo == null) {
				throw new IllegalArgumentException("Cuenta " + c + "no existe");
			}
			mutex.enter();
			// Bloqueo
			if(saldo >= m) {
				AlertaSaldo al = new AlertaSaldo(c,m);
				colaAlertas.add(al);
				al.condAlerta.await();
			}
			desbloqAlerta();
			mutex.leave();
		}

		private void desbloqAlerta() {
			boolean signal = false;
			for(int i=0; i<colaAlertas.size() && !signal; i++) {
				AlertaSaldo al = colaAlertas.peek();
				Integer saldoAl = bc.get(al.cuenta).saldo;
				if(saldoAl < al.minimo) {
					// Se elimina la alerta de la cola
					al.condAlerta.signal();
					colaAlertas.poll();
					// Desbloqueado, salida del bucle
					signal = true;
				} else {
					// No se puede desbloquear, reencolar la primera alerta
					colaAlertas.poll();
					colaAlertas.add(al);
				}
			}
		}
	}
