package cn.magicgone;

import java.util.*;

public class DistributedCacheImpl implements DistributedCache, DistributedCacheScore {

    private final static int NODE_NUM = 10;
    private final static int VIRTUAL_NODE_NUM = 200;
    private final static String NODE_NAME_PREFIX = "node";
    private final static String DELIMITER = "_";

    private int nodeNum;
    private int virtualNodeNum;
    private final Map<String, Node> nodes;
    private final List<VirtualNode> virtualNodes;


    public DistributedCacheImpl(){
        this.nodeNum = NODE_NUM;
        this.virtualNodeNum = VIRTUAL_NODE_NUM;
        this.nodes = new HashMap<>(nodeNum);
        this.virtualNodes = new ArrayList<>(nodeNum * virtualNodeNum);
        initNodes(nodeNum,virtualNodeNum);
    }

    public DistributedCacheImpl(int nodeNum,int virtualNodeNum){
        this.nodeNum = nodeNum;
        this.virtualNodeNum = virtualNodeNum;
        this.nodes = new HashMap<>(nodeNum);
        this.virtualNodes = new ArrayList<>(nodeNum * virtualNodeNum);
        initNodes(nodeNum,virtualNodeNum);
    }

    /**
     * 初始化nodes
     */
    private void initNodes(int nodeNum, int virtualNodeNum){
        for(int i=0; i < nodeNum; i++){
           String nodeName = NODE_NAME_PREFIX + DELIMITER + i;
           Node node = new Node(nodeName,virtualNodeNum);
           this.nodes.put(nodeName, node);
           this.virtualNodes.addAll(node.getVirtualNodes());
        }
        // virtualNodes升序
        virtualNodes.sort(Comparator.comparingInt(VirtualNode::hashCode));
    }

    @Override
    public int[] getScoreDataset() {
        int[] dataset = new int[nodes.size()];
        return nodes.values().stream().mapToInt(Node::getStorageCount).toArray();
    }

    @Override
    public void put(Iterable<String> iterable) {
        iterable.forEach(key -> {
            // 查询对应的virtualNode
            SearchTargetVirtualNode searchTargetVirtualNode = new NormalSearchTargetVirtualNode();
            VirtualNode virtualNode = searchTargetVirtualNode.search(virtualNodes,key);
            Node node = virtualNode.getNode();
            node.put(key,1);
        });
    }
}

class Node{

    private final static String DELIMITER = "_";

    private final String name;
    private final Map<String,Object> storage;
    private final List<VirtualNode> virtualNodes;

    public Node(String name, int virtualNodeNum){
        this.name = name;
        this.storage = new HashMap<>();
        this.virtualNodes = new ArrayList<>(virtualNodeNum);
        initVirtualNodes(virtualNodeNum);
    }

    private void initVirtualNodes(int virtualNodeNum){
        for (int i = 0; i < virtualNodeNum; i++) {
            String nodeName = this.name;
            String virtualNodeName = nodeName + DELIMITER + i;
            VirtualNode virtualNode = new VirtualNode(virtualNodeName, this);
            this.virtualNodes.add(virtualNode);
        }
    }

    public String getName() {
        return name;
    }

    public int getStorageCount(){
        return storage.size();
    }

    public List<VirtualNode> getVirtualNodes() {
        return virtualNodes;
    }

    public void put(String key,Object value){
        this.storage.put(key, value);
    }
}

class VirtualNode{
    private final String name;
    private final Node node;
    private final int hashCode;

    public VirtualNode(String name,Node node){
        this.name = name;
        this.node = node;
        this.hashCode = Objects.hashCode(UUID.randomUUID());
    }

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hashCode);
    }
}

interface SearchTargetVirtualNode{
    VirtualNode search(List<VirtualNode> virtualNodes, String key);
}

class NormalSearchTargetVirtualNode implements SearchTargetVirtualNode{

    @Override
    public VirtualNode search(List<VirtualNode> virtualNodes, String key) {
        int keyHashCode = Objects.hashCode(key);
        for(VirtualNode virtualNode : virtualNodes){
            if(keyHashCode <= virtualNode.hashCode()){
                return virtualNode;
            }
        }
        // 如果比最大的虚拟节点还要大，则返回第一个虚拟节点
        return virtualNodes.get(0);
    }
}
