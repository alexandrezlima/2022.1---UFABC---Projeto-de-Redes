# 2022.1 Projeto de Redes
 2022.1. Projeto para disciplica de Redes de Computadores. Consiste em uma aplicação para envio e recebimentos de pacotes, realizado por meio da linguagem Java com auxílio da biblioteca gson.


#FORMATO DE MENSAGEM TRANSFERIDA
	A mensagem transferida contém algumas informações relevantes ao funcionamento do programa, como número de sequência, ACK, flag de ACK (que informa se o pacote é um ACK de resposta), o tipo de envio da mensagem (lento, perda, duplicado, normal ou fora de ordem) e, por fim, a própria mensagem. Essa mensagem é enviada utilizando o protocolo GBN (Go Back N).

#TRATAMENTO DE MENSAGENS LENTAS
	Para as mensagens lentas, primeiramente inicia-se um temporizador com tempo de 15s. Ao final deste tempo, a mensagem é enviada.
	Caso o temporizador já esteja ativo, o pacote encontra-se na fila de envio para um próximo reenvio de pacotes com o fim do temporizador (onde verifica-se se o último ACK recebido corresponde ao pacote com atraso).

#TRATAMENTO DE MENSAGENS PERDIDAS
	Para o tratamento de mensagens perdidas, o programa não envia a mensagem de forma a simular a perda. Sendo assim, inicia-se um temporizador com tempo de 15s. Como o arquivo não foi enviado, haverá um timeout ao final dos 15s e o programa fará um reenvio do pacote.
	No caso em que o temporizador já está ativo, a mensagem simplesmente não é enviada e, ao final do temporizador, há uma checagem do último ACK recebido. Sendo ele diferente da mensagem não enviada, ocorre o reenvio dos pacotes.

#TRATAMENTO DE MENSAGENS FORA DE ORDEM
	As mensagens fora de ordem não são enviadas imediatamente após escolhida a opção de envio. Elas, a princípio, não são enviadas. Assim, com a próxima mensagem, o receiver emitirá um ACK cumulativo, fazendo com que o sender “reenvie” o pacote que agora é considerado fora de ordem.

#TRATAMENTO DE MENSAGENS DUPLICADAS
	As mensagens duplicadas ocorrem quando o usuário escolhe a opção 3 de envio após escrever uma mensagem. O pacote criado é simplesmente enviado duas vezes. O receiver faz uma verificação do número de sequência do último pacote recebido e, caso o atual pacote recebido tenha número de sequência menor ou igual ao número da verificação, trata-se de um pacote duplicado. Este pacote é descartado pelo receiver.

#FUNCIONAMENTO E CONSUMO DO BUFFER
	Os buffers foram implementados como listas de strings. Ao criar uma instância de mensagem, a mesma é armazenada no buffer de mensagens enviadas do sender e também no buffer de recebidos do receiver.
	Ocorre o mesmo com o ACKs cumulativos recebidos pelo sender, que os guarda em um buffer próprio, bem como são guardados nos buffers de mensagens emitidas pelo receiver, no receiver.

#LINKS UTILIZADOS COMO REFERÊNCIA PARA CONSTRUÇÃO DO PROJETO

    • Link sugerido pelo professor ao final do documento do EP, sobre o uso do formato JSON, em que transforma-se um objeto java (no caso deste projeto, a classe “Mensagem”) em uma string (e vice-versa): http://tutorials.jenkov.com/java-json/gson.html
      
    • Breve leitura sobre gson: https://github.com/google/gson
      
    • Aula sobre a implementação de threads, sockets, TCP e UDP em java: http://tutorials.jenkov.com/java-json/gson.html
