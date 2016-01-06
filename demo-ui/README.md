# demo-ui

This is a very simple demo ui used in conjunction with the demo-app.

```sbt run``` will run the demo ui, go to ```localhost:7070/example``` and you will see a matrix of green squares representing the 16
instances started by demo app. Clicking on any square starts a demo task on that instance which in turn starts a demo task on adjoining 
instances, each task started darkens the shade the square, each task finished lightens it, no tasks means the square is green. 

![demo-ui](https://cloud.githubusercontent.com/assets/6160346/12144817/30a863c8-b482-11e5-9dd3-1cffbf2d0cbe.png)

