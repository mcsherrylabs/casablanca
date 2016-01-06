# demo-app

The demo app consists of 16 instances of casablanca running in parallel, the demo-ui is then used to start tasks on each instance 
which will in turn start tasks on further instances bouncing around until they all die. 

The best way to run the demo app is to run ```sbt dist```

Then take the distribution and unzip it, go to the ```bin``` folder and call ```demo```

This should start all the instances. Then go to the demo-ui and start it... 

*Note this repo also contains some unused code and some Jmeter plans.*
