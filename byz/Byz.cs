using System;
using System.Collections;
using System.Collections.Generic;

namespace Space1 {
///////////////////////////////////////////////////////////////////////////////////////////
class EigTreePrj {

    private static EigTreePrj instance = new EigTreePrj();

    private EigTreePrj() {

    }

    static void Main(String[] args) {
        instance.doTask(args);           
    }

    public void doTask1(String[] args) {
        int round = 1;
        int N = 7;
        IList twoDIdList = generate2DIdListForMaliciousMessage(round, N, 3);
        print2DIdList(twoDIdList);
    }

    public void doTask(String[] args) {
        processParameters(args);

        foreach(Participant fromParticipant in Parameters.participants) {
            foreach(Participant toParticipant in Parameters.participants) {
                beginSend(fromParticipant, toParticipant);
            }
        }

        bottomUp();
//        printParameters();
        outputSendMessages();
        outputBottomUpMessages();
    }

    private void printParameters() {
        String str = Parameters.to2String();
        Console.WriteLine("------------------------------------");
        Console.WriteLine(str);
        Console.WriteLine("------------------------------------");
    }

    public void bottomUp() {
        Parameters.bottomUp();
    }

    private void beginSend(Participant fromParticipant, Participant toParticipant) {
        if(fromParticipant.isFaulty()) {
            // find out the malicious message to send to toParticipant
            MaliciousMessage messageToFind = null;
            foreach(MaliciousMessage maliciousMessage in fromParticipant.getMaliciousMessages()) {
                if(maliciousMessage.destParticipantId == toParticipant.getId()
                    && maliciousMessage.idList.Count == 1) {
                    messageToFind = maliciousMessage;
                    break;
                }
            }
            if(messageToFind == null) {
                throw new System.ArgumentException("cannot find maliciousMessage for participant with id = " + fromParticipant.getId());
            }

            toParticipant.acceptMessage(fromParticipant, messageToFind.value, messageToFind.idList);
        }
        else {
            IList idList = new ArrayList();
            idList.Add(fromParticipant.getId());
            toParticipant.acceptMessage(fromParticipant, fromParticipant.getInitValue(), idList);
        }
    }

    private void processParameters(String[] args) {
        String fileName = args[args.Length - 1];
        IList listContent = CommonUtil.getTextFileContent(fileName);

        for(int i = 0;i < listContent.Count;i++) {
            String lineContent = (String)listContent[i];
            String[] parts = lineContent.Split(new char[]{' '});
            if(i == 0) {
                // this is the first line
                if(parts.Length < 2) {
                    throw new System.ArgumentException("input file error at line: " + (i + 1));
                }
                Parameters.N = Int32.Parse(parts[0]);
                if(Parameters.N > listContent.Count - 1) {
                    throw new System.ArgumentException("the line number is not right with the N!");
                }
                Parameters.v0 = Int32.Parse(parts[1]);
                int maxF = Parameters.N/3;
                Parameters.roundTimes = maxF + 1;
            }
            else {
                // line about every participant
                if(parts.Length < 3) {
                    throw new System.ArgumentException("input file error at line: " + (i + 1));
                }
                // current participant's index is (i - 1)
                Participant participant = new Participant();
                participant.setId(Int32.Parse(parts[0]));
                participant.setInitValue(Int32.Parse(parts[1]));
                participant.setFaulty((parts[2].Equals("1")?true:false));
                if(participant.isFaulty()) {
                    Parameters.F++;
                    // continue to config the malicious messages
                    int maxF = Parameters.N/3;
                    int maxPartLength = 3 + Parameters.N * (maxF + 1);
                    if(parts.Length < maxPartLength) {
                        throw new System.ArgumentException("input file error at line: " + (i + 1) + ", there need " + maxPartLength + " parts in this line");
                    }
                    for(int round = 0;round < Parameters.roundTimes;round++) {
                        for(int j = 0;j < Parameters.N;j++) {
                            String part = parts[j + 3 + Parameters.N*round];
                            int innerPartLength = getInnerPartLength(round, Parameters.N);
                            if(part.Length != innerPartLength) {
                                throw new System.ArgumentException("input file error at line: " + (i+1) + ", inner part length should be: " + innerPartLength);
                            }
                            IList twoDIdList = generate2DIdListForMaliciousMessage(round, Parameters.N, participant.getId());
                            if(innerPartLength != twoDIdList.Count) {
                                throw new System.ArgumentException("innerPartLength != 2DIdList.Count");
                            }
                            for(int k = 0;k < part.Length;k++) {
                                String innerPart = part.Substring(k, 1);
                                MaliciousMessage maliciousMessage = new MaliciousMessage();
                                maliciousMessage.destParticipantId = j + 1;
                                maliciousMessage.value = Int32.Parse(innerPart);
                                maliciousMessage.idList = (IList)twoDIdList[k];
                                participant.addMaliciousMessage(maliciousMessage);
                            }
                        }
                    }
                }
                Parameters.participants.Add(participant);
            }
        }
        for(int i = 0;i < Parameters.participants.Count;i++) {
            Participant participant = (Participant)Parameters.participants[i];
            participant.setIndex(i);
        }

/*        // set the idlist of maliciousMessage according to indexList
        for(int i = 0;i < Parameters.participants.Count;i++) {
            Participant participant = (Participant)Parameters.participants[i];
            foreach(MaliciousMessage message in participant.getMaliciousMessages()) {
                IList idList = new ArrayList();
                foreach(int index in message.indexList) {
                    int id = ((Participant)Parameters.participants[index]).getId();
                    idList.Add(id);
                }
                message.idList = idList;
            }
        }
*/
    }

    private void print2DIdList(IList twoDIdList) {
        foreach(IList idList in twoDIdList) {
            String str = CommonUtil.idList2String(idList);
            Console.WriteLine(str);
        }
    }

    private IList generate2DIdListForMaliciousMessage(int round, int N, int destId) {
        // generate the tree first
        IdTree idTree = new IdTree();
        idTree.id = destId;
        IList usedIds = new ArrayList();
        usedIds.Add(destId);
        IList subIdTrees = generateSubIdTreesFromParent(idTree, usedIds, round - 1, N);
        if(subIdTrees != null && subIdTrees.Count > 0) {
            idTree.subIdTrees = subIdTrees;
        }
        // assemble the idList
        IList twoDIdList = get2DIdListFromIdTree(idTree);
        return twoDIdList;
    }

    private IList generate2DIdListForMaliciousMessage(int round, int N) {
        // first generate the tree
        IList idTrees = new ArrayList();
        for(int i = 1;i <= N;i++) {
            IList usedIds = new ArrayList();
            IdTree idTree = new IdTree();
            idTree.id = i;
            usedIds.Add(idTree.id);
            if(round > 0) {
                IList subIdTrees = generateSubIdTreesFromParent(idTree, usedIds, round - 1, N);
                idTree.subIdTrees = subIdTrees;
            }
            idTrees.Add(idTree);
        }
        // begin to assemble the IdList
        IList twoDIdList = new ArrayList();
        foreach(IdTree idTree in idTrees) {
            IList twoDIdListOfTree = get2DIdListFromIdTree(idTree);
            CommonUtil.addAll(twoDIdList, twoDIdListOfTree);
        }
        return twoDIdList;
    }

    private IList get2DIdListFromIdTree(IdTree idTree) {
        IList twoDIdList = new ArrayList();
        if(idTree.subIdTrees != null && idTree.subIdTrees.Count > 0) {
            foreach(IdTree subIdTree in idTree.subIdTrees) {
                IList twoDSubIdList = get2DIdListFromIdTree(subIdTree);
                CommonUtil.addAll(twoDIdList, twoDSubIdList);
            }
            foreach(IList idList in twoDIdList) {
                idList.Add(idTree.id);
            }
        }
        else {
            IList idList = new ArrayList();
            idList.Add(idTree.id);
            twoDIdList.Add(idList);
        }
        return twoDIdList;
    }

    private IList generateSubIdTreesFromParent(IdTree parentIdTree, IList inputUsedIds, int round, int N) {
        if(round < 0) {
            return null;
        }

        IList idTrees = new ArrayList();
        for(int i = 1;i <= N;i++) {
            IList usedIds = new ArrayList();
            CommonUtil.addAll(usedIds, inputUsedIds);
            if(CommonUtil.isIn(i, usedIds)) {
                continue;
            }
            IdTree idTree = new IdTree();
            idTree.id = i;
            usedIds.Add(idTree.id);
            IList subIdTrees = generateSubIdTreesFromParent(idTree, usedIds, round - 1, N);
            if(subIdTrees != null && subIdTrees.Count > 0) {
                idTree.subIdTrees = subIdTrees;
            }
            idTrees.Add(idTree);
        }
        return idTrees;
    }

    private int getInnerPartLength(int round, int N) {
        int result = 1;
        int beginN = N;
        for(int i = 0;i < round;i++) {
            result = result * (beginN - 1);
            beginN --;
        }
        return result;
    }

    private void outputBottomUpMessages() {
        int round = Parameters.roundTimes;
        String message = "";
        foreach(Participant participant in Parameters.participants) {
            message += (round + 1) + " " + participant.getId() + " : ";
            for(int layer = round; layer > 0;layer --) {
                IList allSubEigTrees = getAllSubEigTreesWithLayer(participant, layer);
                foreach(SubEigTree subEigTree in allSubEigTrees) {
                    message += subEigTree.getNewValue();
                }
                if(allSubEigTrees != null && allSubEigTrees.Count > 0) {
                    message += " ";
                }
            }

            message += participant.getEigTree().getFinalValue();
            message += "\n";
        }
        Console.Write(message);
    }

    
    private IList getAllChildSubEigTreesWithLayer(SubEigTree subEigTree, int layer) {
        IList allSubEigTrees = new ArrayList();
        IList childSubEigTrees = subEigTree.getChildSubEigTrees();
        foreach(SubEigTree childSubEigTree in childSubEigTrees) {
            if(childSubEigTree.getIdList().Count == layer) {
                allSubEigTrees.Add(childSubEigTree);
                continue;
            }
            else {
                IList deepChildSubEigTrees = getAllChildSubEigTreesWithLayer(childSubEigTree, layer);
                CommonUtil.addAll(allSubEigTrees, deepChildSubEigTrees);
            }
        }

        return allSubEigTrees;
    }

    private IList getAllSubEigTreesWithLayer(Participant participant, int layer) {
        IList allSubEigTrees = new ArrayList();
        EigTree eigTree = participant.getEigTree();
        IList subEigTrees = eigTree.getSubEigTrees();
        foreach(SubEigTree subEigTree in subEigTrees) {
            if(subEigTree.getIdList().Count >= layer) {
                allSubEigTrees.Add(subEigTree);
                continue;
            }
            else {
                IList childSubEigTrees = getAllChildSubEigTreesWithLayer(subEigTree, layer);
                CommonUtil.addAll(allSubEigTrees, childSubEigTrees);
            }
        }

        // sort it
        allSubEigTrees = sortSubEigTrees(allSubEigTrees);
        return allSubEigTrees;
    }

    private IList sortSubEigTrees(IList subEigTrees) {
        List<SubEigTree> list = new List<SubEigTree>();
        foreach(SubEigTree subEigTree in subEigTrees) {
            list.Add(subEigTree);
        }

        list.Sort(delegate(SubEigTree subEigTree1, SubEigTree subEigTree2){
            IList idList1 = subEigTree1.getIdList();
            IList idList2 = subEigTree2.getIdList();

            int size = idList1.Count;
            for(int i = 0;i < size;i++) {
                int id1 = (int)idList1[i];
                int id2 = (int)idList2[i];
                if(id1 != id2) {
                    return id1.CompareTo(id2);
                }
                else if(i == size - 1) {
                    return 0;
                }
                else {
                    continue;
                }
            }
            return 0;
        });

        IList sortedList = new ArrayList();
        foreach(SubEigTree subEigTree in list) {
            sortedList.Add(subEigTree);
        }

        return sortedList;
    }

    private void outputSendMessages() {
        String message = "";
        for(int round = 1;round <= Parameters.roundTimes;round ++) {
            IList messageLogsWithRound = getAllMessagesWithRound(round, Parameters.messageLogs);
            for(int senderIndex = 0;senderIndex < Parameters.N;senderIndex ++) {
                int senderId = Parameters.getParticipantIdfromIndex(senderIndex);
                message += round + " " + senderId + " >";
                IList messageLogsWithSenderIndex = getAllMessagesWithSenderIndex(senderIndex, messageLogsWithRound);
                for(int receiverId = 1; receiverId <= Parameters.N;receiverId ++) {
                    message += " ";
                    IList messageLogsWithReceiverId = getAllMessagesWithReceiverId(receiverId, messageLogsWithSenderIndex);
                    foreach(MessageLog messageLog in messageLogsWithReceiverId) {
                        message += messageLog.value;
                    }
                }
                message += "\n";
            }
        }
        Console.Write(message);
    }

    private IList getAllMessagesWithRound(int round, IList messageLogs) {
        IList messageLogsWithRound = new ArrayList();
        if(messageLogs != null) {
            foreach(MessageLog messageLog in messageLogs) {
                if(messageLog.round == round) {
                    messageLogsWithRound.Add(messageLog);
                }
            }
        }

        return messageLogsWithRound;
    }

    private IList getAllMessagesWithSenderIndex(int senderIndex, IList messageLogs) {
        IList messageLogsWithSenderIndex = new ArrayList();
        if(messageLogs != null) {
            foreach(MessageLog messageLog in messageLogs) {
                if(messageLog.senderIndex == senderIndex) {
                    messageLogsWithSenderIndex.Add(messageLog);
                }
            }
        }

        return messageLogsWithSenderIndex;
    }

    private IList getAllMessagesWithReceiverIndex(int receiverIndex, IList messageLogs) {
        IList messageLogsWithReceiverIndex = new ArrayList();
        if(messageLogs != null) {
            foreach(MessageLog messageLog in messageLogs) {
                if(messageLog.receiverIndex == receiverIndex) {
                    messageLogsWithReceiverIndex.Add(messageLog);
                }
            }
        }

        // sort it
        messageLogsWithReceiverIndex = sortMessageLogs(messageLogsWithReceiverIndex);

        return messageLogsWithReceiverIndex;
    }

    private IList getAllMessagesWithReceiverId(int receiverId, IList messageLogs) {
        IList messageLogsWithReceiverId = new ArrayList();
        if(messageLogs != null) {
            foreach(MessageLog messageLog in messageLogs) {
                if(messageLog.receiverId == receiverId) {
                    messageLogsWithReceiverId.Add(messageLog);
                }
            }
        }

        // sort it
        messageLogsWithReceiverId = sortMessageLogs(messageLogsWithReceiverId);

        return messageLogsWithReceiverId;
    }

    private IList sortMessageLogs(IList messageLogs) {
        List<MessageLog> list = new List<MessageLog>();
        foreach(MessageLog messageLog in messageLogs) {
            list.Add(messageLog);
        }

        list.Sort(delegate(MessageLog m1, MessageLog m2){
            if(m1.idList.Count != m2.idList.Count) {
                return m1.idList.Count.CompareTo(m2.idList.Count);
            }
            else {
                IList idList1 = m1.idList;
                IList idList2 = m2.idList;
                int size = idList1.Count;
                for(int i = 0;i < size;i++) {
                    int id1 = (int)idList1[i];
                    int id2 = (int)idList2[i];
                    if(id1 != id2) {
                        return id1.CompareTo(id2);
                    }
                    else if(i == size - 1) {
                        return 0;
                    }
                    else {
                        continue;
                    }
                }
                return 0;
            }
        });

        IList sortedList = new ArrayList();
        foreach(MessageLog messageLog in list) {
            sortedList.Add(messageLog);
        }

        return sortedList;
    } 
}

class IdTree {
    public int id;
    public IList subIdTrees = new ArrayList();
}

class MaliciousMessage {
    public IList indexList = new ArrayList();
    public IList idList = new ArrayList();
    public int destParticipantId = -1;
    public int value = -1;

    public String toString() {
        String str = "print MaliciousMessage:";
        str += "\nindexList = " + CommonUtil.idList2String(indexList);
        str += "\nidList = " + CommonUtil.idList2String(idList);
        str += "\ndestParticipantId = " + destParticipantId;
        str += "\nvalue = " + value;
        return str;
    }
}

class CommonUtil {
    public static IList subList(IList list, int fromIndex, int toIndex) {
        IList list2 = new ArrayList();
        for(int i = fromIndex; i < toIndex;i++) {
            list2.Add(list[i]);
        }

        return list2;
    }

    public static void addAll(IList list1, IList list2) {
        foreach(var item in list2) {
            list1.Add(item);
        }
    }

    public static String idList2String(IList idList) {
        if(idList == null || idList.Count == 0) {
            return "";
        }

        String str = "";
        for(int i = 0;i < idList.Count;i++) {
            int id = (int)idList[i];
            if(str == "") {
                str = id.ToString();
            }
            else {
                str += "." + id.ToString();
            }
        }

        return str;
    }

    public static IList getTextFileContent(String fileName) {
        String[] lines = System.IO.File.ReadAllLines(fileName);
        IList contentList = new ArrayList();
        for(int i = 0;i < lines.Length;i++) {
            String line = lines[i];
            if(line == null) {
                continue;
            }
            if(line.Length == 0) {
                continue;
            }
            if(line.Trim().Equals("")) {
                continue;
            }
            contentList.Add(line);
        }

        return contentList;
    }

    public static bool isIn(int intValue, IList intList) {
        bool isIn = false;
        foreach(int currentValue in intList) {
            if(intValue.Equals(currentValue)) {
                isIn = true;
                break;
            }
        }
        return isIn;
    }

}

class SubEigTree {
    private IList idList;
    private int value;
    private int newValue;

    public String to2String(String tab) {
        String str = "print SubEigTree:";
        str += "\n" + tab + "idList = " + idList2String();
        str += "\n" + tab + "value = " + value;
        str += "\n" + tab + "newValue = " + newValue;

        if(childSubEigTrees != null) {
            foreach(SubEigTree child in childSubEigTrees) {
                str += "\n" + tab + tab + child.to2String(tab + tab);
            }
        }
        return str;
    }

    public IList getIdList() {
        return idList;
    }

    public void setIdList(IList inputIdList) {
        idList = inputIdList;
    }

    public String idList2String() {
        return CommonUtil.idList2String(idList);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int inputValue) {
        value = inputValue;
    }

    public int getNewValue() {
        return newValue;
    }

    public void setNewValue(int inputNewValue) {
        newValue = inputNewValue;
    }

    private IList childSubEigTrees;

    public IList getChildSubEigTrees() {
        return childSubEigTrees;
    }

    public void setChildSubEigTrees(IList inputSubEigTrees) {
        childSubEigTrees = inputSubEigTrees;
    }

    public int bottomUpValue() {
        if(childSubEigTrees == null || childSubEigTrees.Count == 0){
            if(value == 2) {
                value = Parameters.v0;
            }
            else {
                newValue = value;
            }
        }
        else {
            int numberOfZero = 0;
            int numberOfOne = 0;
            foreach(SubEigTree childSubEigTree in childSubEigTrees) {
                int bottomUpValue = childSubEigTree.bottomUpValue();
                if(bottomUpValue == 0) {
                    numberOfZero ++;
                }
                else if(bottomUpValue == 1) {
                    numberOfOne ++;
                }
            }
            if(numberOfZero > numberOfOne) {
                newValue = 0;
            }
            else if(numberOfZero < numberOfOne) {
                newValue = 1;
            }
            else {
                newValue = Parameters.v0;
            }
        }
        return newValue;
    }
}

class Parameters {
    public static int N = 0;
    public static int F = 0;
    public static int v0 = 0;
    public static int roundTimes = 0;
    public static IList participants = new ArrayList();
    public static IList messageLogs = new ArrayList();

    public static void bottomUp() {
        foreach(Participant participant in participants) {
            participant.bottomUp();
        }
    }

    public static int getParticipantIdfromIndex(int index) {
        return ((Participant)participants[index]).getId();
    }

    public static int getParticipantIndexfromId(int id) {
        for(int index = 0;index < participants.Count;index++) {
            Participant participant = (Participant)participants[index];
            if(participant.getId() == id) {
                return index;
            }
        }
        throw new System.ArgumentException("cannot get participant index by id: " + id);
    }

    public static String to2String() {
        String str = "print Parameters:";
        str += "\nN = " + N;
        str += "\nF = " + F;
        str += "\nv0 = " + v0;
        str += "\nroundTimes = " + roundTimes;
        if(participants != null) {
            foreach(Participant participant in participants) {
                str += "\n" + participant.toString();
            }
        }
        if(messageLogs != null) {
            foreach(MessageLog messageLog in messageLogs) {
                str += "\n" + messageLog.toString();
            }
        }
        return str;
    }
}

class EigTree {
    private int finalValue;
    public int getFinalValue() {
        return finalValue;
    }

    private IList subEigTrees = new ArrayList();

    public IList getSubEigTrees() {
        return subEigTrees;
    }

    public void setSubEigTrees(IList inputSubEigTrees) {
        subEigTrees = inputSubEigTrees;
    }

    public void bottomUp() {
        finalValue = bottomUpValue();
    }

    private int bottomUpValue() {
        int numberOfZero = 0;
        int numberOfOne = 0;
        foreach(SubEigTree subEigTree in subEigTrees) {
            int bottomUpValue = subEigTree.bottomUpValue();
            if(bottomUpValue == 0) {
                numberOfZero++;
            }
            else if(bottomUpValue == 1) {
                numberOfOne++;
            }
        }
        if(numberOfZero < numberOfOne) {
            finalValue = 1;
        }
        else if(numberOfZero > numberOfOne) {
            finalValue = 0;
        }
        else {
            finalValue = Parameters.v0;
        }
        return finalValue;
    }

    public String toString() {
        String str = "print EigTree:";
        str += "\nfinalValue = " + finalValue;
        if(subEigTrees != null) {
            foreach(SubEigTree subEigTree in subEigTrees) {
                str += "\n\t" + subEigTree.to2String("\t");
            }
        }
        return str;
    }
}

class Participant {
    private int index = -1;
    private int id = -1;
    private int initValue = -1;
    private bool faulty = false;

    public String toString() {
        String str = "print Participant:";
        str += "\nindex = " + index;
        str += "\nid = " + id;
        str += "\ninitValue = " + initValue;
        str += "\nfaulty = " + faulty;
        if(maliciousMessages != null) {
            foreach(MaliciousMessage message in maliciousMessages) {
                str += "\n" + message.toString();
            }
        }
        str += "\n" + eigTree.toString();
        return str;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int inputIndex) {
        index = inputIndex;
    }

    public int getId() {
        return id;
    }

    public void setId(int inputId) {
        id = inputId;
    }

    public int getInitValue() {
        return initValue;
    }

    public void setInitValue(int inputInitValue) {
        initValue = inputInitValue;
    }

    public bool isFaulty() {
        return faulty;
    }

    public void setFaulty(bool inputFaulty) {
        faulty = inputFaulty;
    }

    private IList maliciousMessages = new ArrayList();

    public IList getMaliciousMessages() {
        return maliciousMessages;
    }

    public void setMaliciousMessages(IList inputMessages) {
        maliciousMessages = inputMessages;
    }

    public void addMaliciousMessage(MaliciousMessage maliciousMessage) {
        if(maliciousMessages == null) {
            maliciousMessages = new ArrayList();
        }
        maliciousMessages.Add(maliciousMessage);
    }

    private EigTree eigTree = new EigTree();

    public EigTree getEigTree() {
        return eigTree;
    }

    public void bottomUp() {
        eigTree.bottomUp();
    }

    public void acceptMessage(Participant sender, int value, IList idList) {
        MessageLog messageLog = new MessageLog();
        messageLog.senderIndex = sender.getIndex();
        messageLog.senderId = sender.getId();
        messageLog.receiverIndex = this.getIndex();
        messageLog.receiverId = this.getId();
        messageLog.round = idList.Count;
        messageLog.idList = idList;
        messageLog.value = value;
        Parameters.messageLogs.Add(messageLog);

        IList newIdList = new ArrayList();
        CommonUtil.addAll(newIdList, idList);
        newIdList.Add(id);

        SubEigTree subEigTreeToFind = findOrGenerateSubEigTree(eigTree, idList);
        subEigTreeToFind.setValue(value);

//        if(sender.getId() == this.getId()) {
//            return;
//        }

        if(CommonUtil.isIn(id, idList)) {
            return;
        }

        if(idList.Count <= (Parameters.roundTimes - 1)) {
            // need to continue sending to others
            for(int i = 0;i < Parameters.participants.Count;i++) {
                Participant participant = (Participant)Parameters.participants[i];
                if(this.isFaulty()) {
                    // find the malicious message to send
                    MaliciousMessage maliciousMessage = null;
                    foreach(MaliciousMessage message in maliciousMessages) {
                        if(message.destParticipantId == participant.getId()
                            && CommonUtil.idList2String(newIdList).Equals(CommonUtil.idList2String(message.idList))) {
                            maliciousMessage = message;
                            break;
                        }
                    }
                    if(maliciousMessage == null) {
                        throw new System.ArgumentException("cannot find the malicious message for participant with id = " + id + ", destPar.id = " + participant.getId() + ", idList = " + CommonUtil.idList2String(newIdList));
                    }
                    participant.acceptMessage(this, maliciousMessage.value, newIdList);
                }
                else{
                    participant.acceptMessage(this, value, newIdList);
                }

            }
        }
    }

    private bool isIn(int intValue, IList intList) {
        bool isIn = false;
        foreach(int currentValue in intList) {
            if(intValue.Equals(currentValue)) {
                isIn = true;
                break;
            }
        }
        return isIn;
    }

    private SubEigTree findOrGenerateSubEigTree(EigTree eigTree, IList idList) {
        IList subEigTrees = eigTree.getSubEigTrees();
        if(subEigTrees == null) {
            // if the list doesn't exist, generate it
            subEigTrees = new ArrayList();
            eigTree.setSubEigTrees(subEigTrees);
        }

        int index = 1;
        IList partIdList = CommonUtil.subList(idList, 0, index);
        String partIdListString = CommonUtil.idList2String(partIdList);
        SubEigTree subEigTreeToFind = null;
        foreach(SubEigTree subEigTree in subEigTrees) {
            String thisIdList = subEigTree.idList2String();
            if(thisIdList.Equals(partIdListString)) {
                subEigTreeToFind = subEigTree;
                break;
            }
        }
        if(subEigTreeToFind == null) {
            // if it doesn't exist, generate it
            subEigTreeToFind = new SubEigTree();
            subEigTreeToFind.setIdList(partIdList);
            subEigTrees.Add(subEigTreeToFind);
            eigTree.setSubEigTrees(subEigTrees);
        }

        if(index == idList.Count) {
            return subEigTreeToFind;
        }
        else {
            return findOrGenerateSubEigTree(subEigTreeToFind, index + 1, idList);
        }
    }

    private SubEigTree findOrGenerateSubEigTree(SubEigTree parentSubEigTree, int index, IList idList) {
        IList childSubEigTrees = parentSubEigTree.getChildSubEigTrees();
        if(childSubEigTrees == null) {
            // if the list doesn't exist, generate it
            childSubEigTrees = new ArrayList();
            parentSubEigTree.setChildSubEigTrees(childSubEigTrees);
        }

        IList partIdList = CommonUtil.subList(idList, 0, index);
        String partIdListString = CommonUtil.idList2String(partIdList);
        SubEigTree childSubEigTreeToFind = null;
        foreach(SubEigTree childSubEigTree in childSubEigTrees) {
            String childIdListString = childSubEigTree.idList2String();
            if(childIdListString.Equals(partIdListString)) {
                childSubEigTreeToFind = childSubEigTree;
                break;
            }
        }

        if(childSubEigTreeToFind == null) {
            // if it doesn't exist, generate it
            childSubEigTreeToFind = new SubEigTree();
            childSubEigTreeToFind.setIdList(partIdList);
            childSubEigTrees.Add(childSubEigTreeToFind);
            parentSubEigTree.setChildSubEigTrees(childSubEigTrees);
        }

        if(index == idList.Count) {
            // it is at the lowest level
            return childSubEigTreeToFind;
        }
        else {
            // continue to search one deep step
            return findOrGenerateSubEigTree(childSubEigTreeToFind, index + 1, idList);
        }
    }
}

class MessageLog {
    public int round;
    public int senderIndex;
    public int senderId;
    public int receiverIndex;
    public int receiverId;
    public IList idList;
    public int value;

    public String toString() {
        String str = "print MessageLog: ";
        str += "round = " + round;
        str += ", senderIndex = " + senderIndex;
        str += ", senderId = " + senderId;
        str += ", receiverIndex = " + receiverIndex;
        str += ", receiverId = " + receiverId;
        str += ", idListString = " + CommonUtil.idList2String(idList);
        str += ", value = " + value;

        return str;
    }
}

////////////////////////////////////////////////////////////////////////////////////
}
