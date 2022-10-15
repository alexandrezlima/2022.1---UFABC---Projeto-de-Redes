package projetoredes;

import java.net.DatagramPacket;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Alexandre Zeferino Lima
 */
public class ThreadTemporizador extends Thread {
    private int ack;
    private int numSeq;
    private Sender sender;
    private int tipoTemporizador;
    private DatagramPacket sendPacket;
    
    public ThreadTemporizador(int ackEsperado, int numPacoteEnviado, Sender senderReference, int tipo, DatagramPacket packet) {
        ack = ackEsperado;
        numSeq = numPacoteEnviado;
        sender = senderReference;
        tipoTemporizador = tipo;
        sendPacket = packet;
    }
        
    public void run() {
        
        /* Esta classe, temporizador, é responsável por utilizar uma thread de forma que seja possível,
         * em paralelo, simular um atraso de envio ou uma atraso de forma a simular um timeout.
        */
        
        try {
            //O aguardo é de 15000ms, isto é, 15s. Caso necessário, altere este valor para aumentar ou diminuir o tempo de espera.
            Thread.sleep(15000);
            sender.setContadorAtivo(false);
            
            
            if (tipoTemporizador == 1) {
                //Simula o envio lento, isto é, após escolhida a opção de envio, aguarda-se o tempo acima para que o envio seja concretizado.
                sender.getSocket().send(sendPacket);
                sender.reenviaPacotes(ack);
            } else {
                //Simula-se a perda de pacote, o timeout, quando verifica-se que o destinatário não enviou um ACK resposta ao pacote "numSeq".
                int lastValidACK = sender.getLastValidACK();
                
                //Caso o ACK resposta não tenha sido recebido (i.e. ack atual é diferente do esperado), realiza-se um reenvio dos pacotes.
                if (lastValidACK <= ack) {
                    sender.reenviaPacotes(ack-1);
                }
            }            
        } catch (Exception ex) {
            Logger.getLogger(ThreadTemporizador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
