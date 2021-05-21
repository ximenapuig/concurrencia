package cc.banco;
import es.upm.babel.cclib.Monitor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class BancoMonitor implements Banco {
	private HashMap<String, Cuenta> bc;
	private Monitor mutex;
	private HashMap<String, Queue<Transferencia>> transferMap;

	public BancoMonitor() { 
		this.bc= new HashMap<String, Cuenta>();
		this.transferMap= new HashMap<String, Queue<Transferencia>>();
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
	private class Transferencia {
		public String cuentaOrigen;
		public String cuentaDestino;
		public Integer cantidad; 
		public Monitor.Cond condTransferencia;

		public Transferencia(String acc1, String acc2, int cantidad) {
			cuentaOrigen = acc1;
			cuentaDestino = acc2;
			this.cantidad = cantidad;
			condTransferencia = mutex.newCond();
		}
	}

	private class Cuenta {
		Integer saldo;
		//	Monitor thisMontor;
		public Cuenta(int v) {
			this.saldo = v;
		}
	}	

	/**
	 * Un cajero pide que se ingrese una determinado valor v a una
	 * cuenta c. Si la cuenta no existe, se crea.
	 * @param c n√∫mero de cuenta
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
		desbloqTransf(c);
	}

	/**
	 * Un ordenante pide que se transfiera un determinado valor v desde
	 * una cuenta o a otra cuenta d.
	 * @param o n√∫mero de cuenta origen
	 * @param d n√∫mero de cuenta destino
	 * @param v valor a transferir
	 * @throws IllegalArgumentException si o y d son las mismas cuentas
	 *
	 */
	public void transferir(String o, String d, int v) {
		if(o.equals(d)) {
			throw new IllegalArgumentException("Cannot transfer funds to same account");
		}
		//mutex.enter();
		Queue<Transferencia> curQ = transferMap.get(o);
		if (curQ == null) {
			curQ = new LinkedList<Transferencia>();
			transferMap.put(o,curQ);
		}
		curQ.add(new Transferencia(o,d,v));

		// Desbloqueo de transferencia y salida de secciÛn crÌtica
		desbloqTransf(d);
		desbloqAlerta();
	}
	/**
	 * Un consultor pide el saldo disponible de una cuenta c.
	 * @param c n√∫mero de la cuenta
	 * @return saldo disponible en la cuenta id
	 * @throws IllegalArgumentException si la cuenta c no existe
	 */
	public int disponible(String c) { 
		mutex.enter();
		Cuenta cuenta = bc.get(c);
		if(cuenta == null) {
			throw new IllegalArgumentException("Cuenta " + c + "no existe");
		}
		mutex.leave();
		return cuenta.saldo;
	}

	/**
	 * Un avisador establece una alerta para la cuenta c. La operaci√≥n
	 * termina cuando el saldo de la cuenta c baja por debajo de m.
	 * @param c n√∫mero de la cuenta
	 * @param m saldo m√≠nimo
	 * @throws IllegalArgumentException si la cuenta c no existe
	 */
	public void alertar(String c, int m) { 
		Cuenta cuenta = bc.get(c);
		if(cuenta == null) {
			throw new IllegalArgumentException("Cuenta " + c + "no existe");
		}
		Integer saldo = cuenta.saldo;
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

	private void desbloqTransf(String cuenta) {
		boolean signal = false;
		Queue<Transferencia> transQ = transferMap.get(cuenta);
		// Comprobar si hay transferencias en cola / si existe la cola
		if(transQ != null) {
			for(int i=0; i<transQ.size() && !signal; i++) {
				Transferencia trans = transQ.peek();
				Cuenta origen = bc.get(cuenta);
				Cuenta destino = bc.get(trans.cuentaDestino);
				// ComprobaciÛn de cPre para el primer elemento de cola
				if (origen != null && origen.saldo >= trans.cantidad) {
					if (destino )
					// RealizaciÛn de la transferencia
					mutex.enter();
					origen.saldo-=trans.cantidad;
					destino.saldo+=trans.cantidad;
					mutex.leave();

					// SeÒalizar y desencolar
					transQ.poll();
					signal = true;
					trans.condTransferencia.signal();
				}
				// Si no se cumple cPre, no hacer nada
			}
		}
	}
}
