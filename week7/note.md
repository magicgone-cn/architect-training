# 进程与线程

一个web服务通常是由一个进程来支撑的，那么当多个请求同时到达时是怎样处理的呢？

进程会针对每一个请求，分配相应的线程来处理，多个请求也就意味着多线程了。

请求进来，由线程接待，线程等待请求包完整接收后，将请求内容封装成HttpRequest，然后交由具体的servlet处理。

servlet继续执行，或执行自身的逻辑，或读取数据库，或调用远程请求，一旦遇到比较耗时的非本地计算类操作，线程就会进入阻塞状态，直到这些操作完成，线程才重新唤醒，继续执行。如果出现一些慢请求，比如慢sql，那么对应的线程就会进入阻塞，如果这些请求数量多了，那么阻塞的线程就会增多。一个系统能够支撑的线程数是有限的，当所有的线程都进入阻塞状态，那么新的请求就无法得到响应，对于外界来说，系统也就宕机了。

因为少量的慢请求，就导致整个系统不可用，显然不是一个好设计。有没有办法做到，这些慢请求，不影响其余的正常请求呢？线程的隔离可能是个好办法。姑且将线程池分成两部分，一部分专门给慢请求使用，一部分给其余正常的请求使用。当慢请求过多的时候，线程池不够用了，直接返回“系统繁忙”就好，不再继续占用过多的线程，其余的线程留给响应时间短的正常请求使用，保证整个系统不会因为少量的慢请求就陷入崩溃。

在微服务架构下，服务间隔离，天然也就隔离了慢请求和正常请求，某些服务及时挂了，也不印象其他服务的使用，再加上服务熔断、服务降级等策略，使整个系统的可用性得到了提升。那么单体架构是不是也可以引用这套方案呢？做不到服务单独部署，但通过线程池的隔离，一定程度上也限制了某些慢服务的破坏性。

对于慢服务的界定，可以人为设置红名单，也可以监控响应结果，实时的根据请求url，动态的认定哪些url是慢请求，从而分配到慢请求的专属线程池中，进而做熔断和降级。


