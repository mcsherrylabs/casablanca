# casablanca
A HTTP microservices framework

[![Build Status](https://travis-ci.org/mcsherrylabs/casablanca.svg?branch=master)](https://travis-ci.org/mcsherrylabs/casablanca) 
(Compiled for 2.10 only at the moment, contact me @mcsherrylabs for a crosscompile)

There are, rightly, countless 'microservice' architectures. Indeed one of the great strengths of a microservice architecture is freedom from the constraints of a 'framework', just write a service that does a simple thing well in any language you want and have clients access it using the ubiquitous JSON over HTTP.
   
Yet, when a task is submitted to this type of system (e.g. 100 distributed, multi instance services) and it goes wrong how is the current status exposed to the task originator? When services are of disparate throughput rates (slow consumer/fast producer) how does the system handle that? What about retries? A service may be down for an extended period - how often and for how long should tasks retry before giving up? 
 
Traditionally a message queue is used but what if there was a simpler way? Tasks as state machines often need to spawn sub tasks and wait for the results of sub tasks before continuing, they may stall waiting for an event or timeout after a period and all of these activities must survive a hard reset and reboot into a consistent state. If some services are not idempotent the system needs to know if it has already started that task?
     
## casablanca features from a task submission point of view
      
A JSON task is submitted via a casablanca endpoint. A 'payload' field contains the JSON specific to the task. The submitter is willing to wait 1s for the task to complete. If the task completes inside the time the resulting JSON is returned. If it does not complete a GUID for that task is returned which the caller can use at a later time to look up the task status. 
  
The simple mythical task is broken into 2 subtasks, email customer service, debit the customers credit card. The card for some reason cannot be debited until the email is sent. But the email server is temporarily un contactable. Requests are still accepted and piled up in the background, the email retry strategy is retried until it either succeeds or the whole task is failed. This can be days and only depends on the size of your hard disk. If the client is waiting the optional 1 second for the task to timeout a natural backpressure is being applied to the incoming requests.
     
The credit card debit service has also been wrapped in a casablanca instance. The second subtask starts the debit task on the remote casablanca instance. While starting this task the casablanca instance dies. On reboot the task to debit the card attempts to execute again, but the remote casablanca service wrapping the credit card service knows it has already seen this request and indicates a duplicate. 
         
The credit card is eventually debited and the task finishes.
 
As a developer the only code that you need to write is the code to send the email and to specify the details given to the remote casablanca instance. That's it.
       
###This repository contains 4 projects

 - [sss-casablanca](../../tree/master/sss.casablanca "sss.casablanca README.md")
 - [template-app]( ../../tree/master/template-app "template-app README.md")
 - [demo-app]( ../../tree/master/demo-app "demo-app README.md")
 - [demo-ui]( ../../tree/master/demo-ui "demo-ui README.md")
 
####[sss-casablanca](../../tree/master/sss.casablanca "sss.casablanca README.md")

This is the core jar. It is depended on by the template app and and demo app.  
 
####[template-app](../../tree/master/template-app "template-app README.md")

The template app is the starting point for development, it contains a simple main App configured with 2 task handlers...  
  
```
  TaskHandlerFactoryFactory(
        RemoteTaskHandlerFactory,
        MailerTaskFactory)
```
     
Use ```sbt run``` to start up an instance which will handle Remote tasks and Mailer tasks. (Update mailer.conf with your gmail credentials at your leisure) The system should print out the tasks and statuses it's configured to handle and wait for tasks to arrive. Write and add more task handler factories... 

####[demo-app](../../tree/master/demo-app "demo-app README.md")

The demo app (demo.sh) runs 16 instances of a casablanca task handler all on separate ports....
 
####[demo-ui](../../tree/master/demo-ui "demo-ui README.md")
  
```sbt run``` and go to ```http://localhost:7070/example``` 
  
Each green square corresponds to an instance of the demo-app, when the demo app has started clicking on a square will start a Demo task on that instance, each demo task will randomly start a few remote tasks in the instances surrounding it, when each task finishes it notifies it's parent. As each node runs more tasks it gets a darker shade and as it finishes it gets lighter, click around like crazy to watch the tasks distribute, bounce around and eventually fade back to green      
