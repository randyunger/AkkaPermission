AkkaPermission
==============

Summary
--------
Request permission from a centralized Akka actor to perform any action. Allows lock free de-duplication of invocations for distributed systems.

This is very much a work-in-progress. More of a proof-of concept than anything else.

Problem
---------
The idea came from a problem we ran into at work. Our load balancer occasionally sends duplicate requests to two different Application servers. This usually isn't a problem except when running specific processes like payment processing. In that case, both app servers forward the payment request to our payment processer service, which notices the duplicate transactions an cancels one. Sometimes the user's first request is cancelled, and sometimes the second request is cancelled. This leads to unpredictable behavior from the user's perspective. The transaction may be processed by the processor and the user may receive their merchandise, but they may have been shown a screen indicating that the transaction was cancelled.

Solution
---------
The solution implemented here is to use an Akka actor system to coordinate the requests in a non-blocking way. The code that should only be executed once is wrapped in an Actor trait's receive() method and passes a message to a coordinating actor requesting permission to execute the code in question. If this is the only such request within a configurable span of time, the Permission granting actor grants the request and keeps a handle to the open request in the form of a Future. If a second request is received within the time span, the Permission actor returns that Future (which may or may not have completed). This solution would work well for our existing infrastructure because it allows us to move only the parts of our code that are sensitive to duplicate requests into the Actor model.

Todo
-----
The current implementation works but is not as simple as it should be. 

* make this more user friendly unwrapping the nested Futures at the appropriate place but I'm still deciding where that is. 
* simplify the API so that all that is needed for use is to extend a trait and implement an abstract method.
* include sample remote actor configuration (this is just a toy unless it is useful remotely)
