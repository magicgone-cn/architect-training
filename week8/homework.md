伪代码
```java
Map<String,Node> nodeMap = new HashMap<>();
Node node1 = linkedList1.firstNode;
Node node2 = linkeeList2.firstNode;
while(true){
    if(node1 != null){
        if(nodeMap.hasKey(node1.key)){
            return node1;
        }
        nodeMap.put(node1.key,node1);
        node1 = node1.nextNode;
    }

    if(node2 != null){
        if(nodeMap.hasKey(node2.key)){
            return node2;
        }
        nodeMap.put(node2.key,node2);
        node2 = node2.nextNode;
    }
    
    return null;
}
```

时间复杂度为O(m+n)
