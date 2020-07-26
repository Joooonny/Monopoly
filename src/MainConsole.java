import sample.Box;
import sample.Monopoly;
import sample.Player;

import java.util.*;

public class MainConsole {
    public static void main(String[] args) throws InterruptedException {
        Scanner input = new Scanner(System.in);
        Monopoly m = new Monopoly();

        /* SET GIOCATORI, RICORDA TRY CATCH */
        int playerNum = 0;

            do {
                System.out.println("Quanti giocatori partecipano? (2-6)");
                playerNum = input.nextInt();
                if (playerNum <= 1 || playerNum > 6) {
                    System.out.println("Errore!");
                }
            }while(playerNum <= 1 || playerNum > 6);

        Player[] players = new Player[playerNum];

        /* SET PEDINE, RICORDA TRY CATCH */
        ArrayList<Player.Pawn> availablePawns = Player.getPawnArray();
        for (int k = 0; k<playerNum; k++) {
            int choice;
            do {
                System.out.println("Giocatore "+(k+1)+":\nScegli la pedina tra queste:");
                for (int j = 0; j < availablePawns.size(); j++) {
                    System.out.println((j+1)+" - "+ availablePawns.get(j));
                }
                choice = input.nextInt();
                if (choice < 1 || choice > availablePawns.size()) {
                    System.out.println("Errore!");
                }
            }while(choice < 1 || choice > availablePawns.size());
            players[k]= new Player(availablePawns.get(choice-1));
            System.out.println("Il giocatore "+(k+1)+" ha scelto "+availablePawns.get(choice-1)+"!\n");
            availablePawns.remove(choice-1);


        }

        //MESCOLAMENTO GIOCATORI f
        players = m.shuffle(players);

        do {
            for (Player player : players) {
                if(player.isLoser()){
                    continue;
                }
                short doubleDice = 0;
                do {
                    char choiceYesOrNot;
                    System.out.println("E' il turno di " + player.getPawn() + ":\nLa tua posizione attuale è " + player.getPosition() + "\n");
                    Thread.sleep(1000);
                    int dice1 = 24;
                    int dice2 = 6;
                    System.out.println("Tiri il dado!");
                    Thread.sleep(2000);
                    System.out.println("Sono usciti " + dice1 + " e " + dice2);
                    //FARE LA ROBA SE E' DOPPIO
                    if (dice1 == dice2) {
                        System.out.println("Hai fatto doppio!");
                        doubleDice++;
                    }
                    if (doubleDice == 3){
                        System.out.println("Hai fatto 3 doppi, vai in prigione");
                        player.setPrisoner(true);
                        break;
                    }
                    System.out.println(player.toString());
                    System.out.println(m.stringBoard(player));
                    if (!player.isPrisoner()) {
                        System.out.println("Ti muovi di " + (dice1 + dice2) + " caselle!\n");
                        player.movement((dice1 + dice2));
                        Box.Type type = m.field[player.getPosition()].getType();
                        System.out.println("Sei su " + m.field[player.getPosition()].getName());
                        switch (type) {
                            case PROPERTY:
                            case SOCIETY:
                            case STATION:
                                Thread.sleep(1000);
                                //se la proprietà è tua
                                if (player.checkProprieties(m.field[player.getPosition()].getName())) {
                                    System.out.println("La proprietà è tua");
                                }
                                //se la proprietà è libera
                                else if (m.isPropertyFree(players, m.field[player.getPosition()].getName())) {
                                    System.out.println("Questa proprietà è libera e costa " + m.field[player.getPosition()].getPrice());
                                    //controlla se hai i soldi, se non ce li hai imposta direttamente di andare all'asta
                                    if (player.getBill() < m.field[player.getPosition()].getPrice()) {
                                        System.out.println("Non hai i soldi per comprala");
                                        choiceYesOrNot = 'n';
                                    }
                                    //altrimenti ti chiede di inserire la tua scelta
                                    else {

                                        do {
                                            System.out.println("Vuoi comprarla? (s/n)");
                                            choiceYesOrNot = input.next().charAt(0);
                                            if (choiceYesOrNot != 's' && choiceYesOrNot != 'n') {
                                                System.out.println("Errore!");
                                            }
                                        } while (choiceYesOrNot != 's' && choiceYesOrNot != 'n');

                                    }
                                    switch (choiceYesOrNot) {
                                        case 's':
                                            player.payment(m.field[player.getPosition()].getPrice());
                                            player.addProperty(m.field[player.getPosition()].getName());
                                            if (type.equals(Box.Type.PROPERTY)) {
                                                m.setBuildable(player.getProperties());
                                            }
                                            break;
                                        case 'n':
                                            //arraylist di gente esclusa dall'asta
                                            ArrayList<Player> auctionPlayers = new ArrayList<>(Arrays.asList(players));
                                            auctionPlayers.remove(player);
                                            boolean auctionExit = false;
                                            Player.Pawn lastAuctionPlayer = null;

                                            //prezzo iniziale
                                            int price = m.field[player.getPosition()].getPrice();

                                            do {
                                                for (Player auctionPlayer : auctionPlayers) {
                                                    int outOfAuctionPlayerNum = 0;
                                                    for (Player nonePlayer : auctionPlayers) {
                                                        if (nonePlayer.isOutOfAuction()) {
                                                            outOfAuctionPlayerNum++;
                                                        }
                                                    }

                                                    if (auctionPlayer.getPawn() != Player.Pawn.NONE) {

                                                        int raise;
                                                        if ((auctionPlayers.size() - 1 == outOfAuctionPlayerNum) && auctionPlayer.getPawn() == lastAuctionPlayer) {
                                                            auctionPlayer.payment(price);
                                                            auctionPlayer.addProperty(m.field[player.getPosition()].getName());
                                                            System.out.println(auctionPlayer.getPawn() + " si è aggiudicato " + m.field[player.getPosition()].getName() + " per " + price + " euro");
                                                            auctionExit = true;
                                                            break;
                                                        }
                                                        //controllo se puoi puntare già di base o no
                                                        if (!auctionPlayer.payment(m.field[player.getPosition()].getPrice())) {
                                                            auctionPlayer.payment(-m.field[player.getPosition()].getPrice());
                                                            System.out.println(auctionPlayer.getPawn() + " non può pagare!");
                                                            auctionPlayer.setOutOfAuction(true);
                                                            continue;
                                                        }
                                                        auctionPlayer.payment(-m.field[player.getPosition()].getPrice());
                                                        do {
                                                            System.out.println("\n" + auctionPlayer.toString());
                                                            //solo nel primo turno
                                                            System.out.println("Ultima puntata:");
                                                            if (lastAuctionPlayer != null) {
                                                                System.out.println("Giocatore: " + lastAuctionPlayer);
                                                            }
                                                            System.out.println("Prezzo attuale: " + price + "\nrilancia (oppure scrivi 0 per lasciare)");
                                                            raise = input.nextInt();
                                                            if ((raise != 0 && raise <= price) || raise > auctionPlayer.getBill())
                                                                System.out.println("errore");
                                                        } while ((raise != 0 && raise <= price) || raise > auctionPlayer.getBill());

                                                        if ((auctionPlayers.size() - 1 == outOfAuctionPlayerNum) && raise == 0) {
                                                            System.out.println("\n" + m.field[player.getPosition()].getName() + " rimane libera!\n");
                                                            auctionExit = true;
                                                            break;
                                                        }
                                                        if (raise == 0) {
                                                            auctionPlayer.setOutOfAuction(true);
                                                        } else {
                                                            price = raise;
                                                            lastAuctionPlayer = auctionPlayer.getPawn();
                                                        }
                                                    }
                                                }
                                            } while (!auctionExit);
                                            for (Player nonePlayer : auctionPlayers) {
                                                if (nonePlayer.isOutOfAuction()) {
                                                    nonePlayer.setOutOfAuction(false);
                                                }
                                            }
                                            break;
                                    }
                                } else {
                                    if (!m.field[player.getPosition()].isMortgaged()){
                                        if (type.equals(Box.Type.PROPERTY)) {
                                            System.out.println("Devi pagare " + m.field[player.getPosition()].getPropertyTax() + " euri");
                                            if (!player.payment(m.field[player.getPosition()].getPropertyTax())) {
                                                m.setTaxFund(m.getTaxFund() + player.getBill());
                                                System.out.println("ma non puoi pagare, " +
                                                        "lasci tutti i tuoi beni nel fondocassa");
                                            }
                                        } else if (type.equals(Box.Type.STATION)) {
                                            System.out.println("Devi pagare " + m.getStationTax(player.getProperties()) + " euri");
                                            if (!player.payment(m.getStationTax(player.getProperties()))) {
                                                m.setTaxFund(m.getTaxFund() + player.getBill());
                                                System.out.println("ma non puoi pagare, " +
                                                        "lasci tutti i tuoi beni nel fondocassa");
                                            }
                                        } else if (type.equals(Box.Type.SOCIETY)) {
                                            System.out.println("Devi pagare " + m.getStationTax(player.getProperties()) + " euri");
                                            if (!player.payment(m.getSocietyTax(player.getProperties(), dice1+dice2))) {
                                                m.setTaxFund(m.getTaxFund() + player.getBill());
                                                System.out.println("ma non puoi pagare, " +
                                                        "lasci tutti i tuoi beni nel fondocassa");
                                            }
                                        }
                                    }
                                }
                                break;
                            case GO:
                                System.out.println(m.passGo(player));
                                break;
                            case GO_TO_PRISON:
                                System.out.println(m.goToPrison(player));
                                break;
                            case PARKING:
                                player.payment(-m.getTaxFund());
                                System.out.println("Sei passato dal parcheggio, ritira " + m.getTaxFund() + " euro");
                                m.setTaxFund(0);
                                break;
                            case CHANCE:
                                System.out.println(m.chance(player));
                                break;
                            case TAX:
                                if (m.field[player.getPosition()].getName().equals("Tassa patrimoniale")) {
                                    System.out.println("Paghi 200 euro");
                                    if (!player.payment(200)){
                                        m.setTaxFund(m.getTaxFund() + player.getBill());
                                        System.out.println("ma non puoi pagare, " +
                                                "lasci tutti i tuoi beni nel fondocassa");
                                    }
                                    else {
                                        m.setTaxFund(m.getTaxFund() + 200);
                                    }
                                } else {
                                    System.out.println("Paghi 300 euro");
                                    if (!player.payment(300)){
                                        m.setTaxFund(m.getTaxFund() + player.getBill());
                                        System.out.println("ma non puoi pagare, " +
                                                "lasci tutti i tuoi beni nel fondocassa");
                                    }
                                    else {
                                        m.setTaxFund(m.getTaxFund() + 300);
                                    }
                                }
                                break;
                        }
                    }
                    //questo lo fa se è prigioniero
                    else if(player.getPrisonTurns() < 2){
                        player.setPrisonTurns((short) (player.getPrisonTurns()+1));
                        //uscita di prigione
                        if (dice1 == dice2) {
                            player.setPrisoner(false);
                            player.setPrisonTurns((short) 0);
                            System.out.println("Sono usciti due " + dice1 + ", sei uscito di prigione");
                        } else {
                            int prisonerChoice;
                            do {
                                System.out.println(player.toString());
                                System.out.println("Sei in prigione, cosa vuoi fare?\n1) Paga 125 euro per uscire\n2) Rimani in prigione");
                                if (player.isCanEscapeFromPrison()){
                                    System.out.println("3) Usa il cartellino per uscire\n");
                                }
                                prisonerChoice = input.nextInt();
                                if (prisonerChoice < 1 || prisonerChoice > 4) {
                                    System.out.println("Errore!");
                                }
                                if (prisonerChoice == 1 && player.getBill()< 125) {
                                    System.out.println("Non hai abbastanza soldi");
                                    prisonerChoice = 0; /* lo imposto a 0 così il ciclo si ripete */
                                }
                            } while (prisonerChoice < 1 || prisonerChoice > 3);
                            switch (prisonerChoice){
                                case 1:
                                    player.payment(125);
                                    player.setPrisoner(false);
                                    player.setPrisonTurns((short) 0);
                                    System.out.println("Sei uscito di prigione");
                                    break;
                                case 2:
                                    System.out.println("Rimani in prigione");
                                    break;
                                case 3:
                                    player.setCanEscapeFromPrison(false);
                                    player.setPrisoner(false);
                                    player.setPrisonTurns((short) 0);
                                    System.out.println("Sei uscito di prigione");
                                    break;
                            }
                        }
                    } else {
                        player.setPrisoner(false);
                        player.setPrisonTurns((short) 0);
                        System.out.println("Hai passato 3 turni in prigione, " +
                                "devi pagare la tassa di 125 euro per uscire\n");
                        if (!player.payment(125)){
                            m.setTaxFund(m.getTaxFund() + player.getBill());
                            System.out.println("ma non puoi pagare, " +
                                    "lasci tutti i tuoi beni nel fondocassa");
                        }
                    }
                }while (doubleDice != 0);
                int secondChoice;
                do {
                    System.out.println("Cosa vuoi fare?\n1) Costruisci\n2) Ipoteca una proprietà\n3) Ricompra una proprietà ipotecata\n4) Fine turno\n");
                    secondChoice = input.nextInt();
                    if (secondChoice <= 1 || secondChoice > 4) {
                        System.out.println("Errore!");
                    }
                    if(secondChoice == 1 && !(m.comboBuildableColors(player.getProperties()).size()>0)) {
                        System.out.println("Non hai proprietà edificabili");
                        secondChoice = 0; /* lo imposto a 0 così il ciclo si ripete */
                    }
                    else if (secondChoice == 2 && player.getProperties().size()==0) {
                        System.out.println("Non hai proprietà attive");
                        secondChoice = 0; /* lo imposto a 0 così il ciclo si ripete */
                    }
                    else if (secondChoice == 3 && m.getMortgagedProperties(player.getProperties()).size() == 0) {
                        System.out.println("Non hai proprietà ipotecate");
                        secondChoice = 0; /* lo imposto a 0 così il ciclo si ripete */
                    }

                }while(secondChoice < 1 || secondChoice > 4);

                switch (secondChoice) {
                    case 1:
                        System.out.println("Elenco zone ");
                        ArrayList<String> propertiesPerColor = new ArrayList<>();
                        for (Box.Color c: m.comboBuildableColors(player.getProperties())) {
                            System.out.println(c + "ZONE");
                        }
                        Box.Color[] colorsArray = m.comboBuildableColors(player.getProperties()).toArray(new Box.Color[0]);
                        /*for (String s: m.getNamesFromColor(colorechesceglitu)) {
                            System.out.println(s);
                        }*/
                        break;
                    case 2:
                        ArrayList<String> activeProperties = m.getActiveProperties(player.getProperties());
                        String activeListOutput = "";
                        for (int k = 0; k < activeProperties.size(); k++) {
                            activeListOutput += ((k+1)+" - "+activeProperties.get(k));
                        }

                        do {
                            System.out.println("Elenco proprietà ipotecabili:\n"+activeListOutput);
                            secondChoice = input.nextInt();
                            if (secondChoice < 1 || secondChoice > activeProperties.size()) {
                                System.out.println("Errore!");
                            }
                        }while(secondChoice < 1 || secondChoice > activeProperties.size());

                        player = m.setMortgageProperty(player, activeProperties.get(secondChoice-1));
                        break;
                    case 3:
                        ArrayList<String> mortgagedProperties = m.getMortgagedProperties(player.getProperties());
                        String mortgagedListOutput = "";
                        for (int k = 0; k < mortgagedProperties.size(); k++) {
                            mortgagedListOutput += ((k+1)+" - "+mortgagedProperties.get(k));
                        }
                        do {
                            System.out.println("Elenco proprietà ipotecate:\n"+mortgagedListOutput);
                            secondChoice = input.nextInt();
                            if (secondChoice < 1 || secondChoice > mortgagedProperties.size()) {
                                System.out.println("Errore!");
                            }
                        }while(secondChoice < 1 || secondChoice > mortgagedProperties.size());

                        player = m.setActiveProperty(player, mortgagedProperties.get(secondChoice-1));
                        break;
                    case 4:
                        break;
                }

            }
        }while (!m.getWin(players));
    }
}
