package org.kelly.code.model;

public abstract class Channel {
    /***
     * Participant use this method to simulate the sending message process
     * the message might be blocked, delayed, or discarded in different simulation
     */
    abstract public void sendMessage(Participant sender, Message message, Participant receiver);

    private static Channel reliableChannelInstance = null;
    public static Channel getReliableChannelInstance() {
        if(reliableChannelInstance == null) {
            try{
                reliableChannelInstance = (Channel)Class.forName("org.kelly.code.model.ReliableChannel").newInstance();
            }
            catch(ClassNotFoundException cnfex) {
                cnfex.printStackTrace();
            }
            catch(InstantiationException iex) {
                iex.printStackTrace();
            }
            catch(IllegalAccessException iaex) {
                iaex.printStackTrace();
            }
        }

        return reliableChannelInstance;
    }
}
