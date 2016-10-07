package com.alma.pay2bid.gui;

import com.alma.pay2bid.bean.AuctionBean;
import com.alma.pay2bid.client.Client;
import com.alma.pay2bid.client.observer.INewAuctionObserver;
import com.alma.pay2bid.gui.listeners.ButtonClickListener;
import com.alma.pay2bid.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by Folkvir(Grall Arnaud)) on 28/09/16.
 */
public class ClientGui{
    private Client _client;


    /**
     * MAIN FRAME
     */
    private JFrame mainFrame;
    private JMenuBar menuBar;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel mainPanel;
    private JPanel auctionPanel;
    private JScrollPane scrollPane;


    /**
     * NEW AUCTION FRAME
     */
    private JFrame auctionFrame;

    /**
     * DATA
     */
    private HashMap<String,AuctionGui> auctionList;


    /**
     * Constructor
     * @param client
     */
    public ClientGui(Client client) throws RemoteException, InterruptedException {
        _client = client;

        /**
         * TODO A FUNCTION INTO THE CLIENT
         */
        client.getServer().register(_client);

        client.addNewAuctionObserver(new INewAuctionObserver() {
            @Override
            public void updateNewAuction(AuctionBean auction) {
                System.out.println("It's updated !");
                addAuctionPanel(auction);
            }
        });
        auctionList = new HashMap<String,AuctionGui>();
        createGui();
    }


    /**
     * CREATE THE ENTIRE GUI
     */
    private void createGui(){
        mainFrame = new JFrame("Pay2Bid - Auction");
        Dimension d = new Dimension(500,500);
        mainFrame.setSize(500,500);
        mainFrame.setMaximumSize(d);
        mainFrame.setLayout(new BorderLayout());


        menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        JMenuItem newAuction = new JMenuItem("New Auction");
        newAuction.setActionCommand("newAuction");
        newAuction.addActionListener(new MenuNewAuctionActionListener());
        menu.add(newAuction);
        menuBar.add(menu);

        headerLabel = new JLabel("",JLabel.CENTER );
        statusLabel = new JLabel("",JLabel.CENTER);
        statusLabel.setBackground(Color.red);

        statusLabel.setSize(400,0);

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        //controlPanel = new JPanel();
        //controlPanel.setLayout(new FlowLayout());
        auctionPanel = new JPanel();
        auctionPanel.setLayout(new BoxLayout(auctionPanel, BoxLayout.Y_AXIS));
        mainPanel.add(auctionPanel);
        mainPanel.setMaximumSize(d);
        scrollPane = new JScrollPane(mainPanel);


        headerLabel.setText("Current Auction");

        mainFrame.setJMenuBar(menuBar);

        mainFrame.add(headerLabel,BorderLayout.PAGE_START);
        mainFrame.add(scrollPane,BorderLayout.CENTER);
        statusLabel.setBackground(Color.red);
        mainFrame.add(statusLabel,BorderLayout.PAGE_END);
    }

    /**
     * LOAD THE CLIENT GUI
     */
    private void prepareView(){
        mainFrame.setVisible(true);
    }

    /**
     * ADD A NEW AUCTION TO THE MAIN FRAME
     * @param a
     */
    public void addAuctionPanel(AuctionBean a){
        if(auctionList.get(a.getName()) == null) {
            System.out.println("Add new auction to auctionPanel");

            AuctionGui auction = new AuctionGui(a);

            JButton raiseBidbutton = new JButton("Raise the bid");
            raiseBidbutton.setActionCommand("raiseBid");
            auction.auctionPanel.add(raiseBidbutton, 4);

            auctionList.put(a.getName(), auction);
            auctionPanel.add(auctionList.get(a.getName()).auctionPanel);
            raiseBidbutton.addActionListener(new ButtonClickListener(this, auctionList.get(a.getName()), _client, _client.getServer()));

            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }



    /**
     * SET A NEW PRICE FOR THE PROVIDED AUCTION
     */
    private void setAuctionPrice(AuctionBean a){
        System.out.println("auctionPrice set !");
        //UPDATE AUCTION IN OUR LIST
        auctionList.get(a.getName()).setProperties(a);

        //RELOAD THE MAIN PANEL
        auctionList.get(a.getName()).auctionPanel.revalidate();
        auctionList.get(a.getName()).auctionPanel.repaint();

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * CREATE A NEW AUCTION FRAME IN ORDER TO SEND IT TO THE SERVER
     */
    private void newAuctionView(){

        auctionFrame = new JFrame("Add a new auction");
        auctionFrame.setLayout(new BorderLayout());
        auctionFrame.setSize(new Dimension(500,200));
        auctionFrame.setResizable(false);

        AuctionBean a = new AuctionBean(0,"","");
        AuctionGui auction = new AuctionGui(a);

        auctionFrame.add(auction.newAuctionPanel, BorderLayout.CENTER);

        JButton auctionSend = new JButton("SEND NEW AUCTION");
        auctionSend.setActionCommand("newAuction");
        auctionSend.addActionListener(new AuctionGuiListener(auction,this));
        auction.newAuctionPanel.add(auctionSend);

        auctionFrame.add(auction.statusAuction,BorderLayout.PAGE_END);

        auctionFrame.setVisible(true);
    }

    public void sendAuction(AuctionGui auction) throws RemoteException {
        auctionFrame.setVisible(false);
        auctionFrame = null;

        System.out.println("New auction send to the server : [...]");
        System.out.println("Name : "+auction.name.getText());
        System.out.println("Price : "+auction.price.getText());
        System.out.println("Description : "+auction.description.getText());
        AuctionBean a = new AuctionBean(Integer.parseInt(auction.price.getText()),auction.name.getText(),auction.description.getText());
        //SEND HERE TO TE SERVER ....

        _client.submit(a);


        statusLabel.setText("New auction sent...");
    }


    /**
     * TEST ACTION LISTENER FOR NEW AUCTION
     */
    private class MenuNewAuctionActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if("newAuction".equals(command))  {
                if(auctionFrame == null){
                    newAuctionView();
                }


                /*statusLabel.setText("New Auction sent.");

                Auction a = new Auction(10,"Noix de coco x10", "");

                //TEST DE LA FONCTION SETAUCTIONPRICE
                addAuctionPanel(a);
                a.setPrice(50);
                System.out.println(a.getPrice());
                setAuctionPrice(a);*/
            }
        }
    }

    /**
     * MAIN FUNCTION TO RUN THE CLIENT
     * @param args
     */
    public static void main(String[] args) throws RemoteException, InterruptedException {
        Server server = new Server();
        Client client = new Client(server,"Arnaud");
        Client client2 = new Client(server,"Arnaud");

        //AuctionBean a = new AuctionBean(10,"Noix de coco x10", "");

        ClientGui c = new ClientGui(client);
        //ClientGui c2 = new ClientGui(client2);

        c.prepareView();
        //c2.prepareView();

        //c.addAuctionPanel(a);

    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }
}
