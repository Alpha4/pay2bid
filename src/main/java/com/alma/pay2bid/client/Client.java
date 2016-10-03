package com.alma.pay2bid.client;

import com.alma.pay2bid.bean.AuctionBean;
import com.alma.pay2bid.server.IServer;
import com.alma.pay2bid.client.observer.IBidSoldObserver;
import com.alma.pay2bid.client.observer.INewAuctionObserver;
import com.alma.pay2bid.client.observer.INewPriceObserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Thomas Minier
 * @date 27/09/16
 */
public class Client extends UnicastRemoteObject implements IClient {

    private class TimerManager extends TimerTask {
        @Override
        public void run() {
            try {
                server.timeElapsed(Client.this);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Client.class.getCanonicalName());
    //TODO: move all constants/configs in Config class
    private static final long TIME_TO_RAISE_BID = 30000;
    private IServer server;
    //TODO: use ExecutorService instead of Timer ?
    private Timer timer;
    private AuctionBean currentAuction;
    private String name;
    private ClientState state;

    // collections of observers used to connect the client to the GUI
    private Collection<IBidSoldObserver> bidSoldObservers = new ArrayList<IBidSoldObserver>();
    private Collection<INewAuctionObserver> newAuctionObservers = new ArrayList<INewAuctionObserver>();
    private Collection<INewPriceObserver> newPriceObservers = new ArrayList<INewPriceObserver>();

    public Client(IServer server, String name) throws RemoteException {
        this.server = server;
        this.name = name;
        state = ClientState.WAITING;
    }

    /**
     * @param auction
     * @throws RemoteException
     */
    @Override
    public void newAuction(AuctionBean auction) throws RemoteException {
        /*if(auction == null) {
            throw new Exception();
        }*/

        LOGGER.info("New auction received from the server");

        currentAuction = auction;

        timer = new Timer();
        timer.schedule(new TimerManager(), TIME_TO_RAISE_BID);

        state = ClientState.WAITING;

        // notify the observers of the new auction
        for (INewAuctionObserver observer : newAuctionObservers) {
            observer.update(auction);
        }
    }

    /**
     * @param auction
     * @throws RemoteException
     */
    @Deprecated
    public void submit(AuctionBean auction) throws RemoteException {
        /*if (auction == null) {
            throw new Exception();
        }*/

        LOGGER.info("New auction submitted to the server");

        server.placeAuction(auction);
    }

    /**
     * @param buyer
     * @throws RemoteException
     */
    @Override
    public void bidSold(IClient buyer) throws RemoteException {
        /*if(currentAuction == null) {
            throw new Exception();
        }*/

        LOGGER.info((buyer == null ? "nobody" : buyer.getName()) + " won " + currentAuction.getName());

        currentAuction = null;

        timer.cancel();
        timer = null;

        state = ClientState.ENDING;

        // notify the observers of the new bid
        for (IBidSoldObserver observer : bidSoldObservers) {
            observer.update(buyer);
        }
    }

    /**
     * @param price
     * @throws RemoteException
     */
    @Override
    public void newPrice(int price) throws RemoteException {
        /*if(currentAuction == null) {
            throw new Exception();
        }*/

        LOGGER.info("New price received for the current auction");

        currentAuction.setPrice(price);

        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        timer.schedule(new TimerManager(), TIME_TO_RAISE_BID);

        state = ClientState.WAITING;

        // notify the observers of the new price for the current auction
        for (INewPriceObserver observer : newPriceObservers) {
            observer.update(price);
        }
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void addBidSoldObserver(IBidSoldObserver observer) throws RemoteException {
        bidSoldObservers.add(observer);
    }

    @Override
    public void addNewAuctionObserver(INewAuctionObserver observer) throws RemoteException {
        newAuctionObservers.add(observer);
    }

    @Override
    public void addNewPriceObserver(INewPriceObserver observer) throws RemoteException {
        newPriceObservers.add(observer);
    }

    @Override
    public void removeBidSoldObserver(IBidSoldObserver observer) throws RemoteException {
        bidSoldObservers.remove(observer);
    }

    @Override
    public void removeNewAuctionObserver(INewAuctionObserver observer) throws RemoteException {
        newAuctionObservers.remove(observer);
    }

    @Override
    public void removeNewPriceObserver(INewPriceObserver observer) throws RemoteException {
        newPriceObservers.remove(observer);
    }
}