# Java面试题总结

## 基础部分

#### Java基本类型的长度

|  类型   |   长度（字节）   | bit  |           取值范围            | 默认值 |
| :-----: | :--------------: | :--: | :---------------------------: | :----: |
|  byte   |        1         | 8位  |           -128~127            |   0    |
|  short  |        2         | 16位 |         -2^15~2^15-1          |   0    |
|   int   |        4         | 32位 |         -2^31~2^31-1          |   0    |
|  long   |        8         | 64位 |         -2^63~2^63-1          |   0    |
|  float  |        4         | 32位 |  3.402823e+38～1.401298e-45   |  0.0   |
| double  |        8         | 64位 | 1.797693e+308～4.9000000e-324 |  0.0   |
|  char   |        2         | 16位 |                               | \u000  |
| boolean | 官方没有明确长度 |      |         true / false          | false  |

> [^注]: e+38 表示乘以10的38次方，而e-45 表示乘以10的负45次方，包含正负



## Java集合

#### ArrayList详解

###### 1、线程安全性

​		ArrayList是线程不安全的，但是可以使用工具类Collections中的synchronizedList()方法将该List转化为一个线程安全的List，底层其实使用sychronized关键字实现的。

###### 2、底层实现原理

​		底层使用数组实现的，从源码中可以看到实现的细节，如下代码：

```java
public class ArrayList<E> extends AbstractList<E> implements List<E>, 		         RandomAccess, Cloneable, java.io.Serializable{
    //其实现了List，RandomAccess，Cloneable，Serializable接口
    //继承了AbstractList类（这个很关键，subList会讲到）
   
    //默认的初始化容量
    private static final int DEFAULT_CAPACITY = 10;
    //空数组实例
    private static final Object[] EMPTY_ELEMENTDATA = {};
    /**
     * Shared empty array instance used for default sized empty instances. We
     * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
     * first element is added.
     提供一个默认的空数组实例
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     */
    transient Object[] elementData;

    //即表中元素个数
    private int size;
    
    //构造方法，如果传入初始化容量，则不适用默认的初始化容量
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }
    //无参构造器，使用默认的一个空数组
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
    //可以将其他实现了Collection接口的集合中的元素直接转化为一个ArrayList
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
}
```

数组实现导致的问题就是扩容，可看如下扩容代码：

```java
private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        // 原有容量加上原有容量的一半，oldCapacity >> 1 表示向右位移一位，相当于除以二
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
        // 此处的newCapacity实质上是
        // (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
    	// 直接拷贝原有数组的元素
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
```

接下来是我面试中问到的一个问题就是——ArrayList中的subList方法是产生一个新的ArrayList对象，还是直接使用原来的ArrayList，取其中需要截取的数据。现在我们分析源码：

```java
//返回的是一个List接口
public List<E> subList(int fromIndex, int toIndex) {
    //检查参数是否合法
        subListRangeCheck(fromIndex, toIndex, size);
    //看SubList类的实现细节，SubList是ArrayList的一个内部类，因为ArrayList实现了     
    //AbstractList，所以直接可将当前对象传给SubList类的构造器，有人觉得这里有
    //new SubList(),会生成新的ArrayList对象，往下看。
        return new SubList(this, 0, fromIndex, toIndex);
    }
//该类也继承了AbstractList类
private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;
//传入ArrayList，这里看不出是否生成了新的ArrayList，但是生成了新的SubList对象是肯定的
//我们看下面的get方法就可以知道是否有新的ArrayList对象生成。
        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }
}
//从get方法可以看出，他并没有生成新的ArrayList对象，只是一个视图而已
public E get(int index) {
    //检查index的合法性
            rangeCheck(index);
    //检查是否位fast-fail
            checkForComodification();
    //返回原有ArrayList中，带有偏移量的index的元素，并没有生成新的ArrayList对象
            return ArrayList.this.elementData(offset + index);
    }

```

###### LinkedList与ArrayList的区别：

1. 底层实现不同，LinkedList基于链表（双向链表），ArrayList基于数组
2. 增加、删除元素效率不同，数组需要改变位置，所以效率没有链表高

#### HashMap详解

1. HashMap是线程不安全的。

2. HashMap允许键值为空

3. HashMap底层是数组+链表+红黑树实现的（jdk1.8）

   HashMap源码分析如下：

   ```java
   //HashMap实现了Map，Cloneable，Serializable接口，继承了AbstractMap类，其实可以不用实现Map接口
   //因为AbstractMap类实现了Map接口
   public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>,Cloneable, Serializable {
       //默认初始话大小，必须是2的指数次幂（稍后解释）
       static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
       //最大容量
       static final int MAXIMUM_CAPACITY = 1 << 30;
       //默认的加载因子
       static final float DEFAULT_LOAD_FACTOR = 0.75f;
       /*
        *链表转化为红黑树的阈值，取8是经过计算概率得到的，通过泊松分布，发现当为8时，链表转为
        *红黑树的概率为0.00000006，源码中也指出一个概率的一个计算结果
        * 0:    0.60653066
        * 1:    0.30326533
        * 2:    0.07581633
        * 3:    0.01263606
        * 4:    0.00157952
        * 5:    0.00015795
        * 6:    0.00001316
        * 7:    0.00000094
        * 8:    0.00000006
        */
       static final int TREEIFY_THRESHOLD = 8;
       //当红黑数节点到6时，再次将红黑树转化为链表
       static final int UNTREEIFY_THRESHOLD = 6;
       //哈希表，实质上是一个Node数组
       transient Node<K,V>[] table;
       //计算哈希的方式是将算得的哈希值异或上哈希无符号右移16位，这样可以减少哈希碰撞
       static final int hash(Object key) {
           int h;
           return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
       }
       //此方法可以返回一个大于给定值并且最接近给定值的一个2的指数次幂的值
       static final int tableSizeFor(int cap) {
           int n = cap - 1;
           n |= n >>> 1;
           n |= n >>> 2;
           n |= n >>> 4;
           n |= n >>> 8;
           n |= n >>> 16;
           return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
       }
       //这个构造器可以看出，无论initialCapacity的值是多少，都会得到一个2的指数次幂
       public HashMap(int initialCapacity, float loadFactor) {
           if (initialCapacity < 0)
               throw new IllegalArgumentException("Illegal initial capacity: " +
                                                  initialCapacity);
           if (initialCapacity > MAXIMUM_CAPACITY)
               initialCapacity = MAXIMUM_CAPACITY;
           if (loadFactor <= 0 || Float.isNaN(loadFactor))
               throw new IllegalArgumentException("Illegal load factor: " +
                                                  loadFactor);
           this.loadFactor = loadFactor;
           //调用上面提到的方法
           this.threshold = tableSizeFor(initialCapacity);
       }
   }
   ```

   HashMap的put过程：

   ```java
   final V putVal(int hash, K key, V value, boolean onlyIfAbsent,boolean evict) {
           Node<K,V>[] tab; Node<K,V> p; int n, i;
       //如果当前的table为空或者长度为0，那么就要resize()
           if ((tab = table) == null || (n = tab.length) == 0)
               n = (tab = resize()).length;
       //由于容量一定是2的指数次幂，所以n-1的二进制表示一定全部是1，然后与hash做与运算，得到key所在的位置，如果该位置为null，那么直接在此处new一个新node，否则看下面
           if ((p = tab[i = (n - 1) & hash]) == null)
               tab[i] = newNode(hash, key, value, null);
           else {
               Node<K,V> e; K k;
               //如果得到key所在的位置不为null，那么就要看该node的hash值是否等于要put元素的hash值
               //如果相等，并且p.key == key或者key.equals(k)（所以为什么一般使用HashMap的时候，
               //key最好是重写equals方法的对象），那么表明put的key有重复，那么该位置
               //放上新put的value，而返回原来的value
               if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) e = p;
               //如果只是hash冲突，那么就判断该元素是不是一个红黑树节点，如果是，则直接插入
               else if (p instanceof TreeNode)
                   e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
               //如果不是红黑树节点，那么就按照一下方法执行
               else {
                   for (int binCount = 0; ; ++binCount) {
                       if ((e = p.next) == null) {
                           //找到尾节点然后插入，这里会有线程安全的问题，多线程的时候，这里会形成环
                           p.next = newNode(hash, key, value, null);
                           //大于或等于阈值8的时候，就要转化为红黑数，注意不是7,而是8
                           if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                               treeifyBin(tab, hash);
                           break;
                       }
                       if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                           break;
                       p = e;
                   }
               }
               //重复put相同key，则返回旧key对应的value
               if (e != null) { // existing mapping for key
                   V oldValue = e.value;
                   if (!onlyIfAbsent || oldValue == null)
                       e.value = value;
                   afterNodeAccess(e);
                   return oldValue;
               }
           }
           ++modCount;
           if (++size > threshold)
               resize();
           afterNodeInsertion(evict);
           return null;
       }
   
   //resize过程
   final Node<K,V>[] resize() {
           Node<K,V>[] oldTab = table;
           int oldCap = (oldTab == null) ? 0 : oldTab.length;
           int oldThr = threshold;
           int newCap, newThr = 0;
           if (oldCap > 0) {
               if (oldCap >= MAXIMUM_CAPACITY) {
                   threshold = Integer.MAX_VALUE;
                   return oldTab;
               }
               else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                        oldCap >= DEFAULT_INITIAL_CAPACITY)
                   newThr = oldThr << 1; // double threshold
           }
           else if (oldThr > 0) // initial capacity was placed in threshold
               newCap = oldThr;
           else {               // zero initial threshold signifies using defaults
               newCap = DEFAULT_INITIAL_CAPACITY;
               newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
           }
           if (newThr == 0) {
               float ft = (float)newCap * loadFactor;
               newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                         (int)ft : Integer.MAX_VALUE);
           }
           threshold = newThr;
           @SuppressWarnings({"rawtypes","unchecked"})
               Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
           table = newTab;
           if (oldTab != null) {
               for (int j = 0; j < oldCap; ++j) {
                   Node<K,V> e;
                   if ((e = oldTab[j]) != null) {
                       oldTab[j] = null;
                       if (e.next == null)
                           newTab[e.hash & (newCap - 1)] = e;
                       else if (e instanceof TreeNode)
                           ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                       else { // preserve order
                           Node<K,V> loHead = null, loTail = null;
                           Node<K,V> hiHead = null, hiTail = null;
                           Node<K,V> next;
                           do {
                               next = e.next;
                               if ((e.hash & oldCap) == 0) {
                                   if (loTail == null)
                                       loHead = e;
                                   else
                                       loTail.next = e;
                                   loTail = e;
                               }
                               else {
                                   if (hiTail == null)
                                       hiHead = e;
                                   else
                                       hiTail.next = e;
                                   hiTail = e;
                               }
                           } while ((e = next) != null);
                           if (loTail != null) {
                               loTail.next = null;
                               newTab[j] = loHead;
                           }
                           if (hiTail != null) {
                               hiTail.next = null;
                               newTab[j + oldCap] = hiHead;
                           }
                       }
                   }
               }
           }
           return newTab;
       }
   ```

   