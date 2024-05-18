import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack 
{
    private class Card
    {
        String value;
        String type;

        Card(String value, String type)
        {
            this.value = value;
            this.type = type;
        }
        public String toString()
        {
            return value + "-" + type; 
        }
        public int getValue()
        {
            if("AJQK".contains(value))
            {
                if(value.equals("A")) // A, J, Q, K
                {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value); // 2-10
        }
        public boolean isAce()
        {
            return value.equals("A");
        }
        public String getImagePath()
        {
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck; 
    Random random = new Random(); //Shuffle

    // Dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    //Player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    // Money system
    int playerMoney = 1000; // Starting money
    int currentBet;

    //Window
    int boardWidth = 600;
    int boardHeight = boardWidth;

    int cardWidth = 110;
    int cardHeight = 154;

    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() 
    {
        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            // Draw hidden card
            try
            {
                Image hiddencardImage = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if(!stayButton.isEnabled())
                {
                    hiddencardImage = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddencardImage, 20, 20, cardWidth, cardHeight, null);

                // Draw dealer's hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5) * i, 20 , cardWidth, cardHeight, null);
                }

                // Draw player's hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, 20 + (cardWidth + 5) * i, 320, cardWidth, cardHeight, null);
                }

                // Draw results
                if(!stayButton.isEnabled())
                {
                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();
                    System.out.println("STAY: ");
                    System.out.println(dealerSum);
                    System.out.println(playerSum);

                    String message = "";

                    if(playerSum > 21)
                    {
                        message += "You Lose!";
                        playerMoney -= currentBet;
                    }
                    else if(dealerSum > 21)
                    {
                        message += "You Win!";
                        playerMoney += currentBet;
                    }
                    else if(playerSum == dealerSum)
                    {
                        message += "Tie!";
                    }
                    else if(playerSum > dealerSum)
                    {
                        message += "You Win!";
                        playerMoney += currentBet;
                    }
                    else if(playerSum < dealerSum)
                    {
                        message += "You Lose!";
                        playerMoney -= currentBet;
                    }
                    g.setFont(new Font("Ariel", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 220, 250);
                }

                // Display player's money
                g.setFont(new Font("Ariel", Font.PLAIN, 20));
                g.setColor(Color.white);
                g.drawString("Money: $" + playerMoney, 20, 300);

            }
           catch (Exception e) {
                e.printStackTrace();
           }
           
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton resetButton = new JButton("Reset");
    JTextField betField = new JTextField(5);

    public BlackJack()
    {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        resetButton.setFocusable(false);
        buttonPanel.add(resetButton);
        buttonPanel.add(new JLabel("Bet: "));
        buttonPanel.add(betField);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                Card card = deck.remove(deck.size() - 1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                if(reducePlayerAce() > 21) // A + 2 + J --> 1 + 2 + 10
                {
                    hitButton.setEnabled(false);
                }
                playerHand.add(card);
                gamePanel.repaint(); // update game panel
            }
        });

        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    currentBet = Integer.parseInt(betField.getText());
                    if (currentBet > playerMoney || currentBet <= 0) {
                        JOptionPane.showMessageDialog(frame, "Invalid bet amount!");
                        return;
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid bet amount!");
                    return;
                }
        
                startGame();
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                gamePanel.repaint();
            }
        });        
        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size() - 1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
            }
        });
        gamePanel.repaint();
    }

    public void startGame()
    {
        //Deck
        buildDeck();
        shuffleDeck();

        //Dealer
        dealerHand = new ArrayList<>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size() - 1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        System.out.println("Dealer");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);

        //Player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) 
        {
            card = deck.remove(deck.size() - 1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        System.out.println("Player:");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);
    }

    public void buildDeck()
    {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6" , "7" , "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);    
                deck.add(card); 
            }
        }
        System.out.println("Build Deck:");
        System.out.println(deck);
    }

    public void shuffleDeck()
    {
        for (int i = 0; i < deck.size(); i++) 
        {
            int j = random.nextInt(deck.size());
            Card currentCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currentCard);
        }
        System.out.println("After Shuffle:");
        System.out.println(deck);
    }

    public int reducePlayerAce()
    {
        while(playerSum > 21 && playerAceCount > 0)
        {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAce()
    {
        while(dealerSum > 21 && dealerAceCount > 0)
        {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }
}