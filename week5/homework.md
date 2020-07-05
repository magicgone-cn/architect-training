# 分布式缓存之一致性哈希实现

## 缓存与应用一体

缓存通常是一组KV结构的高速存储，在重复查询的情况下，可以显著提高系统的性能。

对于小系统，通常缓存和应用服务器放在一起就可以了，但是在大型系统中，往往缓存需要存储大量的数据，如果在每个应用都开辟大量的内存无疑是一种浪费；并且应用服务器多了之后，每个应用都访问各自的缓存，利用率显著下降，对后续系统的减压效果不好。

## 单独的缓存服务

缓存作为单独的应用服务，所有的应用都通过缓存服务来读写缓存，提高了缓存的利用率。

但同时也存在一个问题，当缓存空间越来越大，所需要的内存也越来越多。

一方面，内存越大，管理成本越高，可能一次GC照成的中断会使服务不可用。

另一方面，单体应用的内存始终是有限的，不可能无限制扩展。

另外，缓存在快，也是需要时间的，当并发大了之后，单体缓存也可能存在性能瓶颈。

## 集群缓存

使用多个相同的缓存服务作为集群，对外提供服务。

读的性能的得到了缓解，但是写的性能，由于需要做服务间的数据同步，反而照成性能下降。

并且也无法解决内存不够用的问题。

## 分布式缓存

通过哈希算法将key均匀的分散到多个节点，每个节点只存储部分信息，查询的时候，也通过哈希算法，从指定的节点读取数据，每个节点都只承担部分工作。

解决了单体的内存上限问题，查询速度也提高了。

但是当增加缓存服务节点的时候，又出现了新的问题，因为节点的选取是通过key取哈希，然后对节点个数取模，增加节点后，就会导致选取节点的计算结果完全改变，大量的查询，都无法在缓存中命中，对后续系统压力骤增。

## 一致性哈希

传统哈希取模的方法在节点数改变后，导致大量key无法命中缓存。

一致性哈希希望在节点数改变后，还是能使大量的key继续命中缓存，但允许少量的key无法命中。

传统哈希通过key的精准匹配，找到目标节点，一致性哈希通过构造一个0至2的32次方减1的哈希环，每个节点取其哈希值，放置在环上。key匹配的时候，顺时针找到距离key的哈希值最近的一个节点，即为key存放的节点。增加节点时，只影响了部分key的存储位置，其余的key还是可以照常访问。

我们希望这些节点平均分布在这个环上，当增加节点后，所有的节点还是平均分布在这个环上，才能保证每个节点存储的数量比较均衡。但是当节点数本身比较少的时候，很难保证节点的分布均匀，因此增加虚拟节点的概念。

每一个真实节点，都对应大量的虚拟机节点（150-200个），真实节点不再放置在环上，改为将虚拟节点放置在环上，由于虚拟节点数量比较多，通过对虚拟节点取哈希值，就能够较为均匀的分布在环上了。

存取时，通过key取哈希，然后再到环上顺时针查找最近的虚拟节点，然后找到虚拟节点对应的真实节点，就可以存取数据了。

## 简单实现

```java
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
            // 节约内存，测试时value为固定值
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
```

## 测试结果

100万的key,10个真实节点，每个真实节点对应200个虚拟节点

```java
package cn.magicgone;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

@Slf4j
public class DistributedCacheTest {

    private final int KEY_NUM = (int) Math.pow(10,6); // key数量
    private final int NODE_NUM = 10; // 节点数
    private final int VIRTUAL_NODE_NUM = 200; // 虚拟节点数

    @Test
    public void score() {

        // 生成测试数据
        log.info("生成测试数据");
        KeyGenerator keyGenerator = new GuidKeyGenerator();
        List<String> keys = keyGenerator.get(KEY_NUM);
        log.info("测试数据完成，{}",keys.size());

        // 将key装载到Cache中
        log.info("装载cache");
        DistributedCache distributedCache = new DistributedCacheImpl(NODE_NUM,VIRTUAL_NODE_NUM);
        distributedCache.put(keys);
        log.info("装载完成");

        // 计算得分
        DistributedCacheScore distributedCacheScore = (DistributedCacheScore) distributedCache;
        log.info("结果集: {}",distributedCacheScore.getScoreDataset());
        log.info("标准差: {}",distributedCacheScore.score());
    }
}
```

最终分布情况还算均匀，可以看到每个真实节点存放的数据量在9万到11万之间。

```
[Log] 2020-07-05 23:08:40 224   生成测试数据 
[Log] 2020-07-05 23:08:41 757   测试数据完成，1000000 
[Log] 2020-07-05 23:08:41 757   装载cache 
[Log] 2020-07-05 23:08:48 112   装载完成 
[Log] 2020-07-05 23:08:48 117   结果集: [92822, 98179, 107292, 92384, 99617, 105647, 97004, 99955, 101441, 105659] 
[Log] 2020-07-05 23:08:48 117   标准差: 4910.504515831341 
```

